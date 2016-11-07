package com.redline.jenkins;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.AbortException;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jenkins.model.Jenkins;

import org.apache.commons.lang.StringUtils;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.BuildListener;
import hudson.model.Item;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import hudson.util.Secret;
import hudson.util.ListBoxModel;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

/**
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link RedlinePublisher} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #apiKey})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform} method will be invoked.
 */
public class RedlinePublisher extends Notifier implements SimpleBuildStep{

    private String apiKey;

    private String templateId = "";

    private int errorFailedThreshold = 0;

    private int errorUnstableThreshold = 0;

    private int responseTimeFailedThreshold = 0;

    private int responseTimeUnstableThreshold = 0;

    private PrintStream logger;

    @DataBoundConstructor
    public RedlinePublisher(
            String apiKey,
            String templateId,
            int errorFailedThreshold,
            int errorUnstableThreshold,
            int responseTimeFailedThreshold,
            int responseTimeUnstableThreshold) {
        this.apiKey = apiKey;
        this.errorFailedThreshold = errorFailedThreshold;
        this.errorUnstableThreshold = errorUnstableThreshold;
        this.responseTimeFailedThreshold = responseTimeFailedThreshold;
        this.responseTimeUnstableThreshold = responseTimeUnstableThreshold;
        this.templateId = templateId;
    }

    @Override
    public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        
        logger = listener.getLogger();
        
        Result result; // Result.SUCCESS;
        String session;
        if ((result = validateParameters(logger)) != Result.SUCCESS) {
            return;
        }

        String apiKeyId = StringUtils.defaultIfEmpty(getApiKey(), getDescriptor().getApiKey());
        String apiKey = null;
        for (RedlineCredential c : CredentialsProvider
                .lookupCredentials(RedlineCredential.class, build.getParent(), ACL.SYSTEM)) {
            if (StringUtils.equals(apiKeyId, c.getId())) {
                apiKey = c.getApiKey().getPlainText();
                break;
            }
        }

        RedlineApi redlineApi = new RedlineApi(apiKey);
        RedlineTest testInfo = redlineApi.runTemplate(getTemplateId());
        if (testInfo == null) {
            logInfo("Invalid test information");
            throw new AbortException("Invalid test information");
        }

        logInfo(" Waiting for test results - Sleeping for 5 seconds ");
        Thread.sleep(5000);

        while (true) {

            RedlineTest testStatus = redlineApi.getTestStatus(testInfo);

            if (testStatus == null) {
                logInfo("API return invalid test information (" + testInfo.getTestId() + ") (" + testInfo.getReferenceId() + ") ");
                throw new AbortException("API return invalid test information (" + testInfo.getTestId() + ") (" + testInfo.getReferenceId() + ") ");
            }

            logInfo("Test is (" + testStatus.getStatus() + ") (" + testStatus.getStatusMessage() + ")");
            if (testStatus.isRunning() || testStatus.isBuilding()) {

                Thread.sleep(5000);

                // TODO : Timeout?  tests have built in timeouts. Tests can be long running too.
                // 			: offer wait time option?
            } else if (testStatus.isCancelled()) {

                logInfo("Test was cancelled (" + testStatus.getStatusMessage() + ")");
                throw new AbortException("Test was cancelled (" + testStatus.getStatusMessage() + ")");

            } else if (testStatus.isCompleted()) {

                logInfo("Success (" + testStatus.getSuccessRate() + ") stable(" + errorUnstableThreshold + ") fail(" + errorFailedThreshold + ") ");
                logInfo("Response (" + (testStatus.getAvergageResponseTime() * 1000) + ") stable(" + responseTimeUnstableThreshold + ") fail(" + responseTimeFailedThreshold + ") ");

                if (errorFailedThreshold > 0 && testStatus.getSuccessRate() < errorFailedThreshold) {
                    result = Result.FAILURE;
                    logInfo("Test ended with " + Result.FAILURE + " on error percentage threshold");
                } else if (errorUnstableThreshold > 0 && testStatus.getSuccessRate() < errorUnstableThreshold) {
                    logInfo("Test ended with " + Result.UNSTABLE + " on error percentage threshold");
                    result = Result.UNSTABLE;
                }

                if (responseTimeFailedThreshold > 0 && (testStatus.getAvergageResponseTime() * 1000) > responseTimeFailedThreshold) {
                    result = Result.FAILURE;
                    logInfo("Test ended with " + Result.FAILURE + " on response time threshold");
                } else if (responseTimeUnstableThreshold > 0 && (testStatus.getAvergageResponseTime() * 1000) > responseTimeUnstableThreshold) {
                    result = Result.UNSTABLE;
                    logInfo("Test ended with " + Result.UNSTABLE + " on response time threshold");
                }

                break;

            } else {

                logInfo("Unkown State, Failed Build");

            }
        }

        Thread.sleep(5000);
        RedlineTest testResults = redlineApi.getTestSummary(testInfo);

        RedlineBuildAction action = new RedlineBuildAction(build, testResults);
        build.getActions().add(action);
        build.setResult(result);
    }

    private void logInfo(String str) {
        if (logger != null) {
            logger.println("Redline13: " + str);
        }
    }

    /**
     * Chance to validate the parameters in the UI
     */
    private Result validateParameters(PrintStream logger) {

        Result result = Result.SUCCESS;

        if (errorUnstableThreshold >= 0 && errorUnstableThreshold <= 100.0) {
            logInfo("Errors percentage greater than or equal to "
                    + errorUnstableThreshold + "% will be considered as "
                    + Result.UNSTABLE.toString().toLowerCase());
        } else {
            logInfo("ERROR! percentage should be between 0 to 100");
            result = Result.NOT_BUILT;
        }

        if (errorFailedThreshold >= 0 && errorFailedThreshold <= 100.0) {
            logInfo("Errors percentage greater than or equal to "
                    + errorFailedThreshold + "% will be considered as "
                    + Result.FAILURE.toString().toLowerCase());
        } else {
            logInfo("ERROR! percentage should be between 0 to 100");
            result = Result.NOT_BUILT;
        }

        if (responseTimeUnstableThreshold >= 0) {
            logInfo("Response time greater than or equal to "
                    + responseTimeUnstableThreshold + "millis will be considered as "
                    + Result.UNSTABLE.toString().toLowerCase());
        } else {
            logger.println("ERROR! percentage should be greater than or equal to 0");
            result = Result.NOT_BUILT;
        }

        if (responseTimeFailedThreshold >= 0) {
            logInfo("Response time greater than or equal to "
                    + responseTimeFailedThreshold + "millis will be considered as "
                    + Result.FAILURE.toString().toLowerCase());
        } else {
            logInfo("ERROR! percentage should be greater than or equal to 0");
            result = Result.NOT_BUILT;
        }

        return result;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    public String getApiKey() {
        return apiKey;
    }

    public int getResponseTimeFailedThreshold() {
        return responseTimeFailedThreshold;
    }

    public void setResponseTimeFailedThreshold(int responseTimeFailedThreshold) {
        this.responseTimeFailedThreshold = responseTimeFailedThreshold;
    }

    public int getResponseTimeUnstableThreshold() {
        return responseTimeUnstableThreshold;
    }

    public void setResponseTimeUnstableThreshold(int responseTimeUnstableThreshold) {
        this.responseTimeUnstableThreshold = responseTimeUnstableThreshold;
    }

    public int getErrorFailedThreshold() {
        return errorFailedThreshold;
    }

    @DataBoundSetter
    public void setErrorFailedThreshold(int errorFailedThreshold) {
        this.errorFailedThreshold = Math.max(0, Math.min(errorFailedThreshold, 100));
    }

    public int getErrorUnstableThreshold() {
        return errorUnstableThreshold;
    }

    @DataBoundSetter
    public void setErrorUnstableThreshold(int errorUnstableThreshold) {
        this.errorUnstableThreshold = Math.max(0, Math.min(errorUnstableThreshold, 100));
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    @Override
    public RedlinePublisherDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

    @Extension
    public static final RedlinePublisherDescriptor DESCRIPTOR = new RedlinePublisherDescriptor();

    public static final class DescriptorImpl
            extends RedlinePublisherDescriptor {
    }

    /**
     * Descriptor class for a BuildStep
     */
    public static class RedlinePublisherDescriptor extends BuildStepDescriptor<Publisher> {

        /**
         * The Api Key to use, user could have setup multiple
         */
        private String apiKey;

        public RedlinePublisherDescriptor() {
            super(RedlinePublisher.class);
            load();
        }

        /**
         * Display list of API Keys, using Jelly
         *
         * @param apiKey API Key to use to retrieve template items
         * @return List of templates
         * @throws hudson.util.FormValidation form errors.
         */
        public ListBoxModel doFillTemplateIdItems(@QueryParameter String apiKey) throws FormValidation {

            if (StringUtils.isBlank(apiKey)) {
                apiKey = getApiKey();
            }

            Secret apiKeyValue = null;
            Item item = Stapler.getCurrentRequest().findAncestorObject(Item.class);
            for (RedlineCredential c : CredentialsProvider.lookupCredentials(RedlineCredential.class, item, ACL.SYSTEM)) {
                if (StringUtils.equals(apiKey, c.getId())) {
                    apiKeyValue = c.getApiKey();
                    break;
                }
            }

            ListBoxModel items = new ListBoxModel();
            if (apiKeyValue == null) {
                items.add("No API Key", "-1");
            } else {

                RedlineApi api = new RedlineApi(apiKeyValue.getPlainText());

                try {

                    Map<String, String> testList = api.getTemplateList();
                    if (testList == null) {
                        items.add("Invalid API key ", "-1");
                    } else if (testList.isEmpty()) {
                        items.add("No tests", "-1");
                    } else {
                        for (Map.Entry<String, String> test : testList.entrySet()) {
                            items.add(test.getValue(), test.getKey());
                        }
                    }
                } catch (Exception e) {
                    throw FormValidation.error(e.getMessage(), e);
                }
            }

            return items;
        }

        /**
         * List all the API Keys
         * @return List box model for display
         */
        public ListBoxModel doFillApiKeyItems() {

            ListBoxModel items = new ListBoxModel();
            Set<String> apiKeys = new HashSet<String>();

            Item item = Stapler.getCurrentRequest().findAncestorObject(Item.class);
            if (item instanceof Job) {
                List<RedlineCredential> global = CredentialsProvider.lookupCredentials(RedlineCredential.class, Jenkins.getInstance(), ACL.SYSTEM);
                if (!global.isEmpty() && !StringUtils.isEmpty(getApiKey())) {
                    items.add("Default API Key", "");
                }
            }

            for (RedlineCredential c : CredentialsProvider.lookupCredentials(RedlineCredential.class, item, ACL.SYSTEM)) {
                String id = c.getId();
                if (!apiKeys.contains(id)) {
                    items.add(StringUtils.defaultIfEmpty(c.getDescription(), id), id);
                    apiKeys.add(id);
                }
            }

            return items;
        }

        /*
		 * Get the credentials for the same id selected
         */
        public List<RedlineCredential> getCredentials(Object scope) {

            List<RedlineCredential> result = new ArrayList<RedlineCredential>();
            Set<String> apiKeys = new HashSet<String>();

            Item item = scope instanceof Item ? (Item) scope : null;
            for (RedlineCredential c : CredentialsProvider.lookupCredentials(RedlineCredential.class, item, ACL.SYSTEM)) {
                String id = c.getId();
                if (!apiKeys.contains(id)) {
                    result.add(c);
                    apiKeys.add(id);
                }
            }

            return result;
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Redline13";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            apiKey = formData.optString("apiKey");
            save();
            return true;
        }

        public String getApiKey() {

            List<RedlineCredential> credentials = CredentialsProvider.lookupCredentials(RedlineCredential.class, Jenkins.getInstance(), ACL.SYSTEM);
            if (StringUtils.isBlank(apiKey) && !credentials.isEmpty()) {
                return credentials.get(0).getId();
            }

            if (credentials.size() == 1) {
                return credentials.get(0).getId();
            }

            for (RedlineCredential c : credentials) {
                if (StringUtils.equals(c.getId(), apiKey)) {
                    return apiKey;
                }
            }

            // API key is not valid any more
            return "";
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

    }

}
