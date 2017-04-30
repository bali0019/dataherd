## DATAHERD

# Description

A generic tool/scripts suite runner to manage the execution of deployment scripts written for Spring boot to manage change sets against a database, HDFS or other systems as a means to automatically run release change scripts as an automatic application startup event.

 ```
 Example:
    - Running mongo aggregation pipeline on an existing collection of documents to transform it to a new desired collection that is 
      compatible with a newer version of the app.
 ```
 
 The scripts suite runner plugs into an existing spring boot project by registering a listener for 
 ```org.springframework.boot.context.event.ApplicationReadyEvent```. 
By doing that, it allows the automated capability to run scripts on app startup.

This tool stands up its own TaskExecutor/ThreadPool on app startup to allow running of independent scripts in a parallel manner.
Once the execution of scripts' completes, it shutdowns its internal TaskExecutor, returning resources back to the app.
 

Furthermore, it uses app's DB to catalog scripts results.

It also adds the ability to execute multiple releases' scripts that were added for new release tags since the last release tag.

# How to import and use with an existing Spring Boot project

It can be imported as a maven dependency into an existing Spring Boot project with the following coordinates:

```
    <dependency>
    	<groupId>org.jas.plugins</groupId>
    	<artifactId>dataherd</artifactId>
    	<version>1.0-SNAPSHOT</version>
    </dependency

```

First, include ```@EnableDataHerd``` annotation on Spring Boot application class:

```
@EnableDataHerd(
scriptsBasePackage = "us.deloitteinnovation.utils", 
isBlocking = true)
@SpringBootApplication
@EnableAutoConfiguration
public class AppWithScriptsBoot {

    public static void main(String[] args) {
        SpringApplication.run(AppWithScriptsBoot.class, args);
    }

}

```

```@EnableDataHerd``` has useful properties attached to it: <br>
```scriptsBasePackage``` - The base package of the project that becomes a root for the datamigration tool to scan for data migration scripts in a project's classpath. <br>
```isBlocking``` -  A flag to indicate whether the execution of scripts should be done in a blocking manner.
                    Basically, registers a health check with spring boot for scripts executor and Will return an unhealthy status. 
                    If true, it will prevent app from starting up untill all scripts are processed.<br>
                    

Now, implement a release script conforming to the following abstract behavior:

```
@ChangeSet(
        order = 2,
        releaseTag = 1.2,
        description = "Scripts for adding CST capability to existing clients",
        isBlocking = true,
        target = {Systems.HDFS},
        source = {Systems.MONGO)
public class AplineItemsCollectionScript extends FeatureScript {

    private static final Logger LOGGER = Logger.getLogger(AplineItemsCollectionScript.class);

    @Override
    public ScriptExecutionResult execute(ReleaseContext releaseContext) {
        LOGGER.info("Update of ApLineItems Collections complete.");
        return new ScriptExecutionResult(ScriptExecutionStatusEnum.SUCCESS, this.getClass());
    }
}
```

```order``` - The order for this script to be execute in the batch execution of release scripts. <br>
```isBlocking``` - The flag for the tool to indicate whether this script is independent of other scripts and therefore can be executed in parallel. <br>
```releaseTag``` - The release tag for the script. <br>
```description``` - The brief description for the script. <br>
```target``` - An intended target system for the script where a script is supposed to be executed. Right now, this tool supports: Mongo, HDFS, etc. <br>
```source``` - A source system for the script, if any <br>

```
public enum TargetSystems {
    MONGO,
    HDFS
}

```

Once beamway is included as a dependency in a Spring Boot project, it is enabled by default.
To disable it, set the following property as an environmental variable, runtime argument or in a standard Spring property file as an application.yaml

```--datamigration = disabled```



# Illustration with an example

Execute the following Spring Boot class inside ```test-project-with-scripts``` will execute all release scripts for the included test project.

```
@EnableDataHerd(scriptsBasePackage = "us.deloitteinnovation.utils", isBlocking = true, initialReleaseTag = 1.2)
@SpringBootApplication
@EnableAutoConfiguration
public class AppWithScriptsBoot {

    public static void main(String[] args) {
        SpringApplication.run(AppWithScriptsBoot.class, args);
    }

}
```
Execution results: 
As seen below in the logs, the tool executed total of 7 scripts 

```
2017-03-05 22:27:40.194  INFO 23551 --- [           main] s.b.c.e.t.TomcatEmbeddedServletContainer : Tomcat started on port(s): 8080 (http)
2017-03-05 22:27:40.234  INFO 23551 --- [           main] u.d.u.e.impl.ScriptsBatchExecuteImpl     : Scanning data migration scripts in the classpath
2017-03-05 22:27:40.258  INFO 23551 --- [           main] org.mongodb.driver.cluster               : No server chosen by ReadPreferenceServerSelector{readPreference=primary} from cluster description ClusterDescription{type=UNKNOWN, connectionMode=SINGLE, serverDescriptions=[ServerDescription{address=10.120.43.130:27017, type=UNKNOWN, state=CONNECTING}]}. Waiting for 30000 ms before timing out
2017-03-05 22:27:40.262  INFO 23551 --- [20.43.130:27017] org.mongodb.driver.cluster               : Monitor thread successfully connected to server with description ServerDescription{address=10.120.43.130:27017, type=STANDALONE, state=CONNECTED, ok=true, version=ServerVersion{versionList=[3, 4, 0]}, minWireVersion=0, maxWireVersion=5, maxDocumentSize=16777216, roundTripTimeNanos=91065717}
2017-03-05 22:27:40.832  INFO 23551 --- [           main] org.mongodb.driver.connection            : Opened connection [connectionId{localValue:2, serverValue:6720}] to 10.120.43.130:27017
2017-03-05 22:27:40.932  INFO 23551 --- [           main] .d.u.s.DawbServiceAccountAclUpdateScript : Successfully Executed ACL entried for existing artifacts to share with a new role type
2017-03-05 22:27:40.932  INFO 23551 --- [           main] u.d.u.e.impl.ScriptsBatchExecuteImpl     : Executed data migration script DawbServiceAccountAclUpdateScript with order 1
2017-03-05 22:27:40.933  INFO 23551 --- [           main] u.d.u.s.AplineItemsCollectionScript      : Update of ApLineItems Collections complete.
2017-03-05 22:27:40.933  INFO 23551 --- [           main] u.d.u.e.impl.ScriptsBatchExecuteImpl     : Executed data migration script AplineItemsCollectionScript with order 2
2017-03-05 22:27:40.935  INFO 23551 --- [           main] u.d.u.e.impl.ScriptsBatchExecuteImpl     : Started Execution of non-blocking data migration script Script12 with order 3
2017-03-05 22:27:40.935  INFO 23551 --- [           main] u.d.u.e.impl.ScriptsBatchExecuteImpl     : Started Execution of non-blocking data migration script Script13 with order 4
2017-03-05 22:27:40.936  INFO 23551 --- [           main] u.d.u.e.impl.ScriptsBatchExecuteImpl     : Started Execution of non-blocking data migration script Script14 with order 5
2017-03-05 22:27:40.936  INFO 23551 --- [           main] u.d.u.e.impl.ScriptsBatchExecuteImpl     : Started Execution of non-blocking data migration script Script15 with order 6
2017-03-05 22:27:40.936  INFO 23551 --- [           main] dateCSTCollectionToAdddefaultGstMappings : Updated CST collecion to provision for new clients
2017-03-05 22:27:40.936  INFO 23551 --- [           main] dateCSTCollectionToAdddefaultGstMappings : Updated CST collecion to provision for new clients
2017-03-05 22:27:40.936  INFO 23551 --- [           main] u.d.u.e.impl.ScriptsBatchExecuteImpl     : Executed data migration script UpdateCSTCollectionToAdddefaultGstMappings with order 7
2017-03-05 22:27:50.945  INFO 23551 --- [ablesExecutor-1] u.d.utils.scripts.Script12               : Ran Mongo Aggregation pipeline to generate a new collection compatible for a new release.
2017-03-05 22:27:50.945  INFO 23551 --- [ablesExecutor-4] u.d.utils.scripts.Script15               : Transformed existing datasets to add newer columns
2017-03-05 22:27:50.945  INFO 23551 --- [ablesExecutor-2] u.d.utils.scripts.Script13               : Completed modification of all documents inside CST collection
2017-03-05 22:27:50.945  INFO 23551 --- [ablesExecutor-1] u.d.utils.scripts.Script12               : Ran script: Script12
2017-03-05 22:27:50.950  INFO 23551 --- [           main] u.d.u.e.impl.ScriptsBatchExecuteImpl     : Executed non-blocking data migration script class Script12 with order 3
2017-03-05 22:27:50.951  INFO 23551 --- [           main] u.d.u.e.impl.ScriptsBatchExecuteImpl     : Executed non-blocking data migration script class Script13 with order 4
2017-03-05 22:27:50.951  INFO 23551 --- [           main] u.d.u.e.impl.ScriptsBatchExecuteImpl     : Executed non-blocking data migration script class Script14 with order 5
2017-03-05 22:27:50.951  INFO 23551 --- [           main] u.d.u.e.impl.ScriptsBatchExecuteImpl     : Executed non-blocking data migration script class Script15 with order 6
2017-03-05 22:27:50.951  INFO 23551 --- [           main] o.s.s.concurrent.ThreadPoolTaskExecutor  : Shutting down ExecutorService 'releaseScriptsRunnablesExecutor'
2017-03-05 22:27:50.953  INFO 23551 --- [           main] u.d.u.e.impl.ScriptsBatchExecuteImpl     : Total of 4 data migration scripts ran for release tag 1.2 
2017-03-05 22:27:50.953  INFO 23551 --- [           main] u.d.u.e.impl.ScriptsBatchExecuteImpl     : Total of 3 data migration scripts ran for release tag 1.3 

```

Now, run it again:


```
2017-03-05 22:28:56.764  INFO 23564 --- [           main] u.d.u.e.impl.ScriptsBatchExecuteImpl     : Scanning data migration scripts in the classpath
2017-03-05 22:28:58.025  INFO 23564 --- [           main] o.s.d.m.c.m.event.LoggingEventListener   : onAfterConvert: { "_id" : { "$oid" : "58bcd7371e5f7a5bffd633ba"} , "_class" : "ReleaseScriptsInfo" , "releaseTag" : 1.3 , "scriptsRan" : [ { "key" : "Script14" , "value" : { "_class" : "ScriptStatusInfo" , "scriptExecutionStatusEnum" : "SUCCESS"} , "_class" : "MapEntryImpl"} , { "key" : "Script13" , "value" : { "_class" : "ScriptStatusInfo" , "scriptExecutionStatusEnum" : "SUCCESS"} , "_class" : "MapEntryImpl"} , { "key" : "Script12" , "value" : { "_class" : "ScriptStatusInfo" , "scriptExecutionStatusEnum" : "SUCCESS"} , "_class" : "MapEntryImpl"}] , "scriptsExecutedOn" : { "$date" : "2017-03-06T03:27:51.078Z"} , "scriptExecutionOverallStatus" : "SUCCESS"}, ReleaseScriptsInfo@366d8b97
2017-03-05 22:28:58.028  INFO 23564 --- [           main] u.d.u.e.impl.ScriptsBatchExecuteImpl     : Last data migration's release tag found: 1.3
2017-03-05 22:28:58.028  INFO 23564 --- [           main] u.d.u.e.impl.ScriptsBatchExecuteImpl     : Skipping previous data migration's release scripts ran: 1.3
2017-03-05 22:28:58.028  INFO 23564 --- [           main] u.d.u.e.impl.ScriptsBatchExecuteImpl     : Last data migration's release tag found: 1.3
2017-03-05 22:28:58.028  INFO 23564 --- [           main] u.d.u.e.impl.ScriptsBatchExecuteImpl     : Skipping previous data migration's release scripts ran: 1.3
2017-03-05 22:28:58.028  INFO 23564 --- [           main] u.d.u.e.impl.ScriptsBatchExecuteImpl     : Last data migration's release tag found: 1.3
2017-03-05 22:28:58.028  INFO 23564 --- [           main] u.d.u.e.impl.ScriptsBatchExecuteImpl     : Skipping previous data migration's release scripts ran: 1.3
2017-03-05 22:28:58.028  INFO 23564 --- [           main] u.d.u.e.impl.ScriptsBatchExecuteImpl     : Last data migration's release tag found: 1.3
2017-03-05 22:28:58.028  INFO 23564 --- [           main] u.d.u.e.impl.ScriptsBatchExecuteImpl     : Skipping previous data migration's release scripts ran: 1.3
2017-03-05 22:28:58.028  INFO 23564 --- [           main] u.d.u.e.impl.ScriptsBatchExecuteImpl     : Last data migration's release tag found: 1.3
2017-03-05 22:28:58.028  INFO 23564 --- [           main] u.d.u.e.impl.ScriptsBatchExecuteImpl     : Skipping previous data migration's release scripts ran: 1.3
2017-03-05 22:28:58.028  INFO 23564 --- [           main] u.d.u.e.impl.ScriptsBatchExecuteImpl     : Last data migration's release tag found: 1.3
2017-03-05 22:28:58.028  INFO 23564 --- [           main] u.d.u.e.impl.ScriptsBatchExecuteImpl     : Skipping previous data migration's release scripts ran: 1.3
2017-03-05 22:28:58.028  INFO 23564 --- [           main] u.d.u.e.impl.ScriptsBatchExecuteImpl     : Last data migration's release tag found: 1.3
2017-03-05 22:28:58.028  INFO 23564 --- [           main] u.d.u.e.impl.ScriptsBatchExecuteImpl     : Skipping previous data migration's release scripts ran: 1.3
2017-03-05 22:28:58.140  INFO 23564 --- [           main] o.s.d.m.c.m.event.LoggingEventListener   : onAfterLoad: { "_id" : { "$oid" : "58bcd7371e5f7a5bffd633ba"} , "_class" : "ReleaseScriptsInfo" , "releaseTag" : 1.3 , "scriptsRan" : [ { "key" : "Script14" , "value" : { "_class" : "ScriptStatusInfo" , "scriptExecutionStatusEnum" : "SUCCESS"} , "_class" : "MapEntryImpl"} , { "key" : "Script13" , "value" : { "_class" : "ScriptStatusInfo" , "scriptExecutionStatusEnum" : "SUCCESS"} , "_class" : "MapEntryImpl"} , { "key" : "Script12" , "value" : { "_class" : "ScriptStatusInfo" , "scriptExecutionStatusEnum" : "SUCCESS"} , "_class" : "MapEntryImpl"}] , "scriptsExecutedOn" : { "$date" : "2017-03-06T03:27:51.078Z"} , "scriptExecutionOverallStatus" : "SUCCESS"}
2017-03-05 22:28:58.143  INFO 23564 --- [           main] o.s.d.m.c.m.event.LoggingEventListener   : onAfterConvert: { "_id" : { "$oid" : "58bcd7371e5f7a5bffd633ba"} , "_class" : "ReleaseScriptsInfo" , "releaseTag" : 1.3 , "scriptsRan" : [ { "key" : "Script14" , "value" : { "_class" : "ScriptStatusInfo" , "scriptExecutionStatusEnum" : "SUCCESS"} , "_class" : "MapEntryImpl"} , { "key" : "Script13" , "value" : { "_class" : "ScriptStatusInfo" , "scriptExecutionStatusEnum" : "SUCCESS"} , "_class" : "MapEntryImpl"} , { "key" : "Script12" , "value" : { "_class" : "ScriptStatusInfo" , "scriptExecutionStatusEnum" : "SUCCESS"} , "_class" : "MapEntryImpl"}] , "scriptsExecutedOn" : { "$date" : "2017-03-06T03:27:51.078Z"} , "scriptExecutionOverallStatus" : "SUCCESS"}, ReleaseScriptsInfo@d613308
2017-03-05 22:28:58.144  INFO 23564 --- [           main] u.d.u.e.impl.ScriptsBatchExecuteImpl     : Last data migration scripts ran for release tag: 1.3
2017-03-05 22:28:58.144  INFO 23564 --- [           main] u.d.u.e.impl.ScriptsBatchExecuteImpl     : Found No data migration scripts to run since release tag: 1.3
2017-03-05 22:28:58.144  INFO 23564 --- [           main] u.d.u.e.impl.ScriptsBatchExecuteImpl     : Found No data migration scripts to run

```

As expected, the release scripts that were previously run were excluded from execution.

# More Examples
   CS use case: https://stash.tools.deloitteinnovation.us/projects/CFSA/repos/core/browse/cfsa/cfsa-backend/cfsa-engine/src/main/java/us/deloitteinnovation/cfsa/release/release1dot2/scripts

# TODO


   - ```TODO:``` Add distributed coordination to scripts' executors.
   
   - ```TODO: Rollback feature (copy-execute) - ``` Before executing the scripts, clone a db for a full rollback in case of a failure.
        It would work by cloning a db snapshot before the execution of scripts' begins and then running those scripts against a cloned db.
         Once the execution of scripts' completes, it would point an app to the new db for the new release.
   
   - ```TODO: ``` Add a separate task executor for each target system to add more parallelism to scripts.
   

   - As of now, the script execution details are a blackbox to this data migration tool
     as scripts take care of connection details and what system to connect.
     <br>```TODO```: Pass target system connection details to the data  migration tool that can be then injected into scripts by this data migration tool via ReleaseContext,
           which makes sure all scripts are applied to the same system in a controlled manner.
     
   - As of now, this tool uses app's mongodb to save scripts' execution results.
     <br>```TODO:``` Add the capability to the data migration tool to take in db details as a bean to be used for cataloging scripts' execution results.