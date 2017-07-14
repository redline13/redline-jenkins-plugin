/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redline.jenkins;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.tasks.Builder;
import java.io.IOException;
import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.HashMap;
import jenkins.tasks.SimpleBuildStep;

/**
 *
 * @author rfriedman
 */
public class RedlineBuilder extends Builder implements SimpleBuildStep{

    public String templateId;
    public String testType;
    public String name;
    public String desc;
    public Boolean storeOutput;
    public String masterFile;
    public ExtraFile[] extraFiles;
    public Servers[] servers;
    public Thresholds thresholds;
    public Plugin[] plugins;

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public Boolean getStoreOutput() {
        return storeOutput;
    }

    public String getMasterFile() {
        return masterFile;
    }

    public ExtraFile[] getExtraFiles() {
        return extraFiles;
    }

    public Servers[] getServers() {
        if (servers == null) {
            return new Servers[0];
        }
        return this.servers;
    }

    public Plugin[] getPlugins() {
      if(plugins == null){
        return new Plugin[0];
      }
      return this.plugins;
    }

    public String getTemplateId(){
        return templateId;
    }

    public Thresholds getThresholds(){
        return thresholds;
    }

    private FilePath[] buildExtraFiles(FilePath workspace) throws AbortException, IOException, InterruptedException{
        FilePath[] extraFilePaths = null;
        if (this.extraFiles != null && this.extraFiles.length > 0) {
            extraFilePaths = new FilePath[this.extraFiles.length];
            for ( int i = 0; i < this.extraFiles.length; i++ ) {
                String filename = this.extraFiles[i].getExtraFile();
                if ( filename.trim().length() == 0 ){
                    continue;
                }
                FilePath efp = new FilePath(workspace, filename);
                if (!efp.exists()) {
                    throw new AbortException("Failed to find Extra File : " + filename );
                }
                if (!efp.isDirectory()){
                    extraFilePaths[i]=efp;
                }
            }
        }
        return extraFilePaths;
    }

    private FilePath buildMasterFile(FilePath workspace) throws AbortException, IOException, InterruptedException{
        System.out.println( "Master File for you " + this.masterFile );
        FilePath master = null;
        if ( this.masterFile != null && this.masterFile.trim().length() > 0 ){
            master = new FilePath(workspace, this.masterFile);
            if (!master.exists()) {
                throw new AbortException("Failed to find JMeter File : " + master.getName());
            }
        }
        return master;
    }

    private String buildApiKey(Run<?,?> build) throws AbortException{
        String keyToUse = null;
        for (RedlineCredential c : CredentialsProvider
                .lookupCredentials(RedlineCredential.class, build.getParent(), ACL.SYSTEM)) {
            keyToUse = c.getApiKey().getPlainText();
            break;
        }
        if (keyToUse == null) {
            throw new AbortException("No API Key available");
        }
        return keyToUse;
    }

    /**
     * Return test properties in a format that can be used generically.
     * @return HashMap with the name-value pairs for this load test type.
     */
    public HashMap<String,String> buildTestProperties(){
        return null;
    }

    @Override
    public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener)
            throws InterruptedException, IOException {

        PrintStream logger = listener.getLogger();
        if (build instanceof AbstractBuild) {
            logger.println("FreeStyle Load Test (" + this.name + ")." + workspace.getBaseName());
        } else {
            logger.println("Pipeline Load Test (" + this.name + ")." + workspace.getBaseName());
        }

        // Get the API Key to use.
        String apiKey = this.buildApiKey(build);

        // Redline API Instance
        RedlineApi redlineApi = new RedlineApi(apiKey);
        RedlineTest testInfo;

        // IF we have templateId we are just running clone of template.
        if ( this.templateId != null ){
            testInfo = redlineApi.runTemplate( this.templateId );
        } else {
            // If test (gatling, custom, jmeter) has a master file include
            FilePath master = this.buildMasterFile(workspace);

            // Send over extra files.
            FilePath[] extraFilePaths = this.buildExtraFiles(workspace);

            // Send over test type specific properties as HashMap
            HashMap<String, String> map = this.buildTestProperties();

            // Support expanded values based on build parameters or environment settings
            if (build instanceof AbstractBuild) {
                EnvVars env = build.getEnvironment(listener);
                env.overrideAll(((AbstractBuild) build).getBuildVariables());
                this.name = env.expand(this.name);
                this.desc = env.expand(this.desc);
                if ( map != null ){
                    for (HashMap.Entry<String, String> entry : map.entrySet()) {
                        map.put( entry.getKey(), env.expand(entry.getValue()) );
                    }
                }
            }

            testInfo = redlineApi.runTest(this.testType, this.name, this.desc, this.storeOutput, master, extraFilePaths, map, this.servers, this.plugins );
        }

        // Check that a test was really started.
        if (testInfo == null) {
            throw new AbortException("Test Information is NULL");
        } else if (testInfo.hasError()) {
            throw new AbortException("Failed to execute remote test, " + testInfo.getError());
        }

        // Just polling for results.
        logger.println(" Waiting for test results - Sleeping for 15 seconds ");
        Thread.sleep(15000);

        // Sleep around waiting for test (TODO Async)
        while (true) {
            RedlineTest testStatus = redlineApi.getTestStatus(testInfo);

            if (testStatus == null) {
                throw new AbortException("API return invalid test information (" + testInfo.getTestId() + ") (" + testInfo.getReferenceId() + ") ");
            }

            logger.println("Test is (" + testStatus.getStatus() + ") (" + testStatus.getStatusMessage() + ")");
            if (testStatus.isRunning() || testStatus.isBuilding()) {
                Thread.sleep(5000);

                // TODO : Timeout?  tests have built in timeouts. Tests can be long running too.
                // 			: offer wait time option?
            } else if (testStatus.isCancelled()) {
                throw new AbortException("Test was cancelled (" + testStatus.getStatusMessage() + ")");
            } else if (testStatus.isCompleted()) {
                logger.println("Test marked completed");
                break;
            } else {
                throw new AbortException("Unkown State, Failed Build");
            }
        }

        Thread.sleep(5000);
        RedlineTest testResults = redlineApi.getTestSummary(testInfo);

        RedlineBuildAction action = new RedlineBuildAction(build, testResults);
        build.addAction(action);
        build.setResult(checkThresholds(testResults,logger));
        NumberFormat c = NumberFormat.getCurrencyInstance();
        logger.println( "This test cost " + c.format(testResults.getCost()));

        // Download merged file if available.
//        if (storeOutput == true) {
//            while (true) {
//                RedlineTest testFiles = redlineApi.getTestStatus(testInfo);
//                String merged = testFiles.getMerged();
//                if (merged != null) {
//                    logger.println("Merged report is " + merged);
//                    break;
//                }
//                logger.println("Waiting for merged test results");
//                Thread.sleep(5000);
//                break;
//            }
//        }
    }

    private Result checkThresholds(RedlineTest testStatus, PrintStream logger ){
        Result result = Result.SUCCESS;
        if ( this.thresholds != null ){
            logger.println( "Checking Thresholds." );
            logger.println("Success (" + testStatus.getSuccessRate() + ") " +
                    "stable(" + thresholds.getErrorUnstableThreshold() + ") " +
                    "fail(" + thresholds.getErrorFailedThreshold() + ") ");

            logger.println("Response (" + (testStatus.getAvergageResponseTime() * 1000) + ") " +
                    "stable(" + thresholds.getResponseTimeUnstableThreshold() + ") " +
                    "fail(" + thresholds.getResponseTimeFailedThreshold() + ") ");

            if ( thresholds.checkErrorFailed( (int) testStatus.getSuccessRate() ) ){
                logger.println( "checkErrorFailed" );
                return Result.FAILURE;
            } else if ( thresholds.checkErrorUnstable((int) testStatus.getSuccessRate() )){
                logger.println("checkErrorUnstable");
                result = Result.UNSTABLE;
            }

            if ( thresholds.checkResponseTimeFailed((int) testStatus.getAvergageResponseTime()*1000) ){
                logger.println("checkResponseTimeFailed");
                return Result.FAILURE;
            } else if ( thresholds.checkResponseTimeUnstable((int) testStatus.getAvergageResponseTime()*1000)){
                logger.println("checkResponseTimeUnstable");
                result = Result.UNSTABLE;
            }
        } else {
            logger.println( "No thresholds set." );
        }
        return result;
    }
}
