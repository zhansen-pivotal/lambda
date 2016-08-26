## Build Your Own
 * If you would like to build your own version of this using Pivotal Cloudfoundry, the following steps may be taken. 

## GemFire on AWS
 * Create a rhel 7 instance on AWS. 
 * Download and Install Gemfire from pivotal network. 
   * https://network.pivotal.io/products/pivotal-gemfire#/releases/1753/file_groups/429
   * ```sudo rpm -ivh <pivotal-gemfire.rpm>```
   
 * Clone the repository for demographic-demo. 
 * In the scripts directory, there will be a gem-locator.sh and startServers.sh 
   * vi the startServers.sh file and change to your AWS internal ip
   * Now run the provided gemfire start scripts
   
          ```$ ./gem-locator.sh <host-ip>```

          ```$ ./startServers.sh```

  * Now that Gemfire is started login to gfsh
     
      ```
      $ gfsh
      gfsh> connect --locator=<host>[10334]
      gfsh> create region --name=demo --type=PARTITION
      ```

## PCF Cli
 * To create a Data Manufacturing Pipeline in Pivotal Cloud Foundry requires the use of the cloud foundry command line tool.
  * This can be downloaded for your platform at https://github.com/cloudfoundry/cli/releases
    * Once downloaded follow the install documentation at https://docs.run.pivotal.io/cf-cli/install-go-cli.html
  * Log in to PCF using the PCF CLI.
 
    ```$ cf login```
  * Run the following command to target the API endpoint, org and space where you want to create the service:
 
    ```$ cf target -a <api-endpoint> -o <organization> -s <space name>```
    
## Deploy Dataflow Server Cloudfoundry
  * This demo requires the latest version of spring-cloud-dataflow-server-cloudfoundry and dataflow-shell
    * To Download execute command:
    
       ```$ git clone https://github.com/spring-cloud/spring-cloud-dataflow-server-cloudfoundry.git```
       
       ```$ wget http://repo.spring.io/release/org/springframework/cloud/spring-cloud-dataflow-shell/1.0.0.RELEASE/spring-cloud-dataflow-shell-1.0.0.RELEASE.jar```
    
    * To deploy to your Cloud Foundry follow the instructions at http://docs.spring.io/spring-cloud-dataflow-server-cloudfoundry/docs/current-SNAPSHOT/reference/htmlsingle/#_deploying_on_cloud_foundry

## Start Demo
 * Once the dataflow server is deployed, you may start the local dataflow shell.
 
     ``` $ java -jar <Path to Jar>/spring-cloud-dataflow-shell-1.0.1.BUILD-SNAPSHOT.jar```

 * Now point the dataflow shell at the Cloudfoundry instance of the server.
   
      ```dataflow:> dataflow config server http://<dataflow-server-instance>```

 * Some apps required for this demo are custom and must be registered as a Stream App before use. This can be done through a simple     ```app register`` command: 

``` 
  dataflow:>app import --uri http://bit.ly/stream-applications-rabbit-maven

  dataflow:>app register --name curration-processor --type processor --uri https://github.com/zhansen-pivotal/lambda/blob/master/pcf-demo/curration-processor/target/curration-processor-0.0.1-SNAPSHOT.jar?raw=true --force

  dataflow:>app register --name hdfs-formatter --type processor --uri https://github.com/zhansen-pivotal/lambda/blob/master/pcf-demo/hdfs-formatter/target/hdfs-formatter-0.0.1-SNAPSHOT.jar?raw=true --force

   dataflow:>app register --name http-get --type processor --uri https://github.com/zhansen-pivotal/lambda/blob/master/pcf-demo/http-extended-processor/target/http-extended-processor-0.0.1-SNAPSHOT.jar?raw=true --force
```

 * In this **Demo**, the process:
       * **Http** (Source) --> 
         * **URLs** which then gets bound by RabbitMq message bus and sent down the pipeline.
       * **Http-Get** (Processor) -->
         * Custom Code. Gets the data via a rest api and sends it to the output channel of the message bus to be consumed by currator.
       * **Currator-Processor** (Processor) -->
         * Custom Code. Enriches json data based on stream definition input params. This can differ based on destination (sink)
       * **Formatter**
         * Custom Code. Simple Formatter that provides readable data for S3.
       * **Gemfire** or **S3** (Sink) -->X
         * **Gemfire** (Sink)
           * Current data is cached in Gemfire to be consumed by the Demographic Data Browser UI
         * **S3** (Sink)
           * Historical and Current data is stored in s3 to be consumed by Pivotal Greenplum for BI/Analytic use cases. (**note:** should be aggregated before write to s3. Not in current itteration)


   * We can now start to deploy streams. To do so you can either use the dataflow-shell or the web-ui. 

      *  For easy-use Spring Cloud Dataflow Dashboard go to:
             
             ```http://<your-dataflow-server>/dashboard ```
             
      *  Go to the streams tab and then stream create. 
      *  We will need to enter our stream definition here. 
   * The streams we need for this demo are:
       
       ```
       demo-current=http | http-get | curration-processor --securityGroup="3" --destination="GemFire" --dataSetYear="2014" --dataSource="www.broadbandmap.gov/broadbandmap/demographic/2014/coordinates" --spring.cloud.stream.bindings.output.contentType='application/json' |s3-formatter: hdfs-formatter --spring.cloud.stream.bindings.output.contentType='application/octet-stream' > :s3-topic
demo-gemfire=:demo-current.curration-processor > gemfire --connect-type=server --region-name=demo --host-addresses=<host>:<port> --key-expression=payload.getField('id') --json=true
demo-2012=http | http-get | curration-processor --securityGroup="2" --destination="s3" --dataSetYear="2012" --dataSource="www.broadbandmap.gov/broadbandmap/demographic/2012/coordinates" | s3-formatter:hdfs-formatter --spring.cloud.stream.bindings.output.contentType='application/octet-stream' > :s3-topic
demo-2010=http | http-get | curration-processor --securityGroup="1" --destination="s3" --dataSetYear="2010" --dataSource="www.broadbandmap.gov/broadbandmap/demographic/2010/coordinates" | s3-formatter:hdfs-formatter --spring.cloud.stream.bindings.output.contentType='application/octet-stream' > :s3-topic
demo-merge=:s3-topic > s3 --bucket='<your-bucket>' --acl=PublicRead --cloud.aws.region.static='<your-region>' --cloud.aws.credentials.accessKey='<your-accesskey>' --cloud.aws.credentials.secretKey='<your-secret>' --key-expression=payload.hashCode()
       
       ```
       
       
       * Configuration parameters will need to be set for AWS credentials/bucket and Gemfire server host and port.
       * An example of what the Spring Dataflow Dashboad will look like is as follows:
       
          ![Screenshot] (../Screen Shot 2016-08-26 at 3.47.15 PM.png)
          
          * Now we can click create and deploy the streams.
          
 ## Start Data Movement
   * This data manufacturing pipeline uses http as its source. We can now post our URLs via a file using curl. 
   * To do this, go to the data directory and find the years 2014 (being current). We can now post these data files to the Cloudfoundry http instances of the stream branches we desire. 
   * For Example:
     * If we want to use 2014 and post current data to gemfire, we only need to post to the http app associated with it. (**note:** Should be scripted in future itteration)
     
      ```for l in `cat 2014demo-4ksample.txt`; do curl -XPOST -H "Content-Type:text/plain"  -d $l http://<your-app-route>; done;```
     
     * We can then continue the process for 2012 and 2010 datasets. 
     * 2014 will be written to GemFire and S3
     * 2012,2010 will be written to S3. 
            
                   **(see first README.md for S3 GPDB information)**
