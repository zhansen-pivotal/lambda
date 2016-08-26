## Build Your Own
 * If you would like to build your own version of this using Pivotal Cloudfoundry, the following steps may be taken. 

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
