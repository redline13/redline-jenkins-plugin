/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redline.jenkins.scenario;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.redline.jenkins.RedlineApi;
import com.redline.jenkins.RedlineCredential;
import com.redline.jenkins.Thresholds;
import hudson.Extension;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 *
 * @author rfriedman
 */
public class ScenarioStep extends AbstractStepImpl {

    public String templateId;
    public Thresholds thresholds;
    
    @DataBoundConstructor
    public ScenarioStep(
            String templateId,
            Thresholds thresholds 
    ) {
        this.templateId = templateId;
        this.thresholds = thresholds;
    }

    public String getTemplateId() {
        return templateId;
    }
    
    public Thresholds getThresholds(){
        return this.thresholds;
    }

    @Extension
    public static class DescriptorImpl extends AbstractStepDescriptorImpl{

        public DescriptorImpl(){
            super(ScenarioStepExecution.class);
        }
        
        @Override
        public String getFunctionName() {
            return "redlineScenario";
        }
        
        @Nonnull
        @Override
        public String getDisplayName(){
            return "Run existing templates on RedLine13.com";
        }

        /**
         * Display list of API Keys, using Jelly
         *
         * @return List of templates
         */
        public ListBoxModel doFillTemplateIdItems() {
            String apiKey = getApiKey();

            ListBoxModel items = new ListBoxModel();
            if (apiKey == null) {
                items.add("No API Key", "-1");
            } else {

                RedlineApi api = new RedlineApi(apiKey);

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
                    items.add("Failed to find tests", "-1");
                }
            }

            return items;
        }

        public String getApiKey() {
            List<RedlineCredential> credentials = CredentialsProvider.lookupCredentials(RedlineCredential.class, Jenkins.getInstance(), ACL.SYSTEM);
            if (!credentials.isEmpty()) {
                return credentials.get(0).getApiKey().getPlainText();
            }
            // API key is not valid any more
            return null;
        }
    }
}
