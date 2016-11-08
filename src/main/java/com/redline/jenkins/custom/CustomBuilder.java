package com.redline.jenkins;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * The RedLine13 Build Task for running a new load test where the resources come
 * from the project.
 *
 * @author Richar dFriedman rich@redline13.com
 */
public class RedlineJMeterBuilder extends Builder implements SimpleBuildStep {

    private final String name;
    private final String desc;
    private final Boolean storeOutput;
    private final String jmeterFile;
    private final ExtraFile[] extraFiles;
    private final String jmeterVersion;
    private final String opts;
    private final String jvmArgs;
    private final Servers[] servers;

    @DataBoundConstructor
    public RedlineJMeterBuilder(
            String name,
            String desc,
            Boolean storeOutput,
            String jmeterFile,
            ExtraFile[] extraFiles,
            String jmeterVersion,
            String opts,
            String jvmArgs,
            Servers[] servers
    ) {
        this.name = name;
        this.desc = desc;
        this.storeOutput = storeOutput;
        this.jmeterFile = jmeterFile;
        this.extraFiles = extraFiles;
        this.jmeterVersion = jmeterVersion;
        this.opts = opts;
        this.jvmArgs = jvmArgs;
        this.servers = servers;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public Boolean getStoreOutput() {
        return storeOutput;
    }

    public String getJmeterFile() {
        return jmeterFile;
    }

    public ExtraFile[] getExtraFiles() {
        return extraFiles;
    }

    public String getJmeterVersion() {
        return jmeterVersion;
    }

    public String getOpts() {
        return opts;
    }

    public String getJvmArgs() {
        return jvmArgs;
    }

    public Servers[] getServers() {
        if (servers == null) {
            return new Servers[0];
        }
        return this.servers;
    }

    @Override
    public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        PrintStream logger = listener.getLogger();
        if (build instanceof AbstractBuild) {
            logger.println("FreeStyle JMeter Load Test (" + this.name + ")." + workspace.getBaseName());
        } else {
            logger.println("Pipeline JMeter Load Test (" + this.name + ")." + workspace.getBaseName());
        }

        FilePath fp = new FilePath(workspace, this.jmeterFile);
        if (!fp.exists()) {
            throw new AbortException("Failed to find JMeter File : " + this.jmeterFile);
        }

        String apiKey = null;
        for (RedlineCredential c : CredentialsProvider
                .lookupCredentials(RedlineCredential.class, build.getParent(), ACL.SYSTEM)) {
            apiKey = c.getApiKey().getPlainText();
            break;
        }
        if (apiKey == null) {
            throw new AbortException("No API Key available");
        }

        // Send over test type specific properties as HashMap
        HashMap<String, String> map = new HashMap<>();
        map.put("version", jmeterVersion);
        map.put("opts", opts);
        map.put("jvm_args", jvmArgs);

        // Send over extra files.
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

        RedlineApi redlineApi = new RedlineApi(apiKey);
        RedlineTest testInfo = redlineApi.runTest("jmeter-test", this.name, this.desc, this.storeOutput, fp, extraFilePaths, map, this.servers);
        if (testInfo == null) {
            throw new AbortException("Test Information is NULL");
        } else if (testInfo.hasError()) {
            throw new AbortException("Failed to execute remote test, " + testInfo.getError());
        }

        logger.println(" Waiting for test results - Sleeping for 5 seconds ");
        Thread.sleep(5000);

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
        build.getActions().add(action);

        // Download merged file if available.
        if (storeOutput == true) {
            while (true) {
                RedlineTest testFiles = redlineApi.getTestStatus(testInfo);
                String merged = testFiles.getMerged();
                if (merged != null) {
                    logger.println("Merged report is " + merged);
                    break;
                }
                logger.println("Waiting for merged test results");
                Thread.sleep(5000);
                break;
            }
        }
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> type) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.RedlineJMeterBuilder_DisplayName();
        }

        public ListBoxModel doFillJmeterVersionItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("3.0", "3.0");
            items.add("2.13", "2.13");
            return items;
        }
    }

}
