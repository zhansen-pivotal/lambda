# Demographic Data Manufacturing Demo
  * This demo is a sample of a Data Manufacturing Pipeline that takes in a variety of different data with different formats and different destinations. The key here is to showcase reusable code that will allow for multiple processing features on data in motion. 
  * In order to achieve this, Spring Cloud Stream microservices deployed to Cloud Foundry via Spring Cloud Dataflow was used. 
  * In a Data Manufacturing Pipeline, it is feasible that a variety of data will pass through this process. It is not, however, feasible to assume that all data should go to the same destination. A paradigm shift from an A to B load to a Data Microservice is needed. Namely that data purpose, data relevance, and its value to business should dictate its location. A Data Microservice platform, not unlike a standard Microservice platform, maps data to product in a way that will handle it best for the business. This also means that data product is irrelevant in the overall vision of the platform and can be changed will very little iteration. 
  * What follows is an example of such a platform.
  
       ![Screenshot] (Screen Shot 2016-08-26 at 1.42.04 PM.png)

    * In this **Example**, the process:
       * **File** (Source) --> 
         * **URLs** which then gets bound by RabbitMq message bus and sent down the pipeline.
       * **Http-Get** (Processor) -->
         * Custom Code. Gets the data via a rest api and sends it to the output channel of the message bus to be consumed by currator.
       * **Currator-Processor** (Processor) -->
         * Custom Code. Enriches json data based on stream definition input params. This can differ based on destination (sink)
       * **Formatter**
         * Custom Code. Simple Formatter that provides readable data for HDFS.
       * **Gemfire** or **HDFS** (Sink) -->X
         * **Gemfire** (Sink)
           * Current data is cached in Gemfire to be consumed by the Demographic Data Browser UI
         * **HDFS** (Sink)
           * Historical and Current data is stored in HDFS to be consumed by Pivotal HDB for BI/Analytic use cases. 
       
    
  * A working demo for viewing is available and deployed via Pivotal Web Services and Amazon Web Services. 
    * For this demo, the following architecture was used: 
    
      ![Screenshot](Screen Shot 2016-08-26 at 2.28.00 PM.png)

    * The Data Browser UI is available at:
       
       http://ec2-54-218-91-184.us-west-2.compute.amazonaws.com:8080/app.html
      
      * The process in which data is landed follows the current data pipeline.
      
      ![Screenshot](Screen Shot 2016-08-26 at 11.30.28 AM.png)
      
      * Within this stream, the major player is the curration processor. This allows for data to be enriched on its journey to Gemfire. For the demo a security group, date, source info, and destination info are added. Another use of this filter/transform layer of pipelining is to build a data subset to be cached in GemFire. GemFire should not cache large historical data that would normally be destined for Hadoop or Greenplum type system.
      
      * It is then served as a Spring Boot app that queries Gemfire for current Demographic data. The data points are aggregated on the google map api and can be zoomed in by clicking in the area you want to browse. This fast access layer showcases the power of GemFire.
  * For historical data, a small sample has been stored using the historical pipeline: 
  
      ![Screenshot] (Screen Shot 2016-08-26 at 2.33.48 PM.png)
  
      * To view this data in the working demo example, **Download** the single-node Greenplum VM from Pivotal Network. 
 
               https://network.pivotal.io/products/pivotal-gpdb#/releases/2146/file_groups/465 
       * Once downloaded, follow install instructions and start psql client. 
      
              ```$ psql```

         * We now need to create and configure an External Table to query from s3. 
          * This is made possible by creating a PROTOCOL and Function and then pointing the table to the url of the s3 bucket.
          
          ```
          $ sudo mkdir -p /home/gpadmin/s3
          
          $ vi /home/gpadmin/s3/s3.conf
          ```
          * Add the s3 Configuration file
          ```
          [default]
          secret = "your aws secret"
          accessid = "your aws accessid"
          connections = 1
          chunksize = 67108864
          ```
          
          ```
          psql> CREATE OR REPLACE FUNCTION read_from_s3() RETURNS integer AS 
             '$libdir/gps3ext.so', 
             's3_import'
          LANGUAGE C STABLE;
          
          psql>CREATE PROTOCOL s3 (readfunc = read_from_s3);
          ```
          
          * Then we give a table definition:
          
          ```
          psql> CREATE EXTERNAL TABLE demographics_ext (
            year text,
            securityGroup text,
            destination text,
            fipsCode text,
            source text,  
            id text,
            date text,
            lon float,
            lat float, 
            incomeBetween100to200 float, 
            incomeLessThan25 float,
            incomeBetween25to50 float,
            incomeBetween50to100 float,
            medianIncome float) 
          LOCATION ('s3://s3-us-west-2.amazonaws.com/scdf-bucket-test/1 config=/home/gpadmin/s3/s3.conf') 
          
          FORMAT 'TEXT' (DELIMITER ',') LOG ERRORS INTO demographics_err SEGMENT REJECT LIMIT 100;
          ```
          
          * Now we can query for data. 
          
          ```psql> select count(*) from demographics_ext; ```
          
          ```psql> select * from demographics_ext limit 10```
          
          
