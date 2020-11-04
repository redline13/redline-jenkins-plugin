/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redline.jenkins;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores the cloud key used for a load test.
 * If not used then will default first on account.
 * @author rfriedman
 */
public class CloudKey extends AbstractDescribableImpl<CloudKey> {

    public final String cloudKey;

    /**
     * Gather all the inputs required to define cloud settings data to launch
     * tests.
     * @param cloudKey initialize from a string
     */
    @DataBoundConstructor
    public CloudKey( String cloudKey ) {
        if( cloudKey != null && cloudKey.length() == 0 ){
            cloudKey = null;
        }
        this.cloudKey = cloudKey;
    }

    /**
     * @return CloudKey as string
     */
    public String getCloudKey() {
        return cloudKey;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<CloudKey> {

        @Override
        public String getDisplayName(){
            return "CloudKey";
        }

        /** Keep list of cloud keys returned */
        transient private Map<String,String> cloudKeys = null;
        transient private long lastCloudKeyTS = 0;

        public RedlineApi getRedlineClient(){
            List<RedlineCredential> credentials = CredentialsProvider.lookupCredentials(RedlineCredential.class, Jenkins.getInstance(), ACL.SYSTEM);
            if (!credentials.isEmpty()) {
                String apiKey = credentials.get(0).getApiKey().getPlainText();
                return new RedlineApi(apiKey);
            }
            // API key is not valid any more
            return null;
        }

        /**
         * @return Cloud Keys or null
         */
        public Map<String, String> getCloudKeys(){

            if ( cloudKeys == null || System.currentTimeMillis() > lastCloudKeyTS + 30000 ){
                RedlineApi api = getRedlineClient();
                try {
                    cloudKeys = api.getCloudKeys();
                } catch (Exception ex){
                    cloudKeys = new HashMap<>();
                }
                lastCloudKeyTS = System.currentTimeMillis();
            }
            return cloudKeys;
        }

        /**
         * Get the number of keys for the account.
         * @return key count.
         */
        public int getCloudKeyCount(){
            Map<String,String> keys = this.getCloudKeys();
            return keys.size();
        }

        /**
         * Display list of Cloud Keys, using Jelly
         *
         * @return List of Cloud Keys
         */
        public ListBoxModel doFillCloudKeyItems() {
            Map <String, String> keys = getCloudKeys();

            ListBoxModel items = new ListBoxModel();
            if (keys == null) {
                items.add("No Cloud Key", "-1");
            } else if (cloudKeys.isEmpty()) {
                items.add("Empty Cloud Keys", "-1");
            } else {
                for (Map.Entry<String, String> test : cloudKeys.entrySet()) {
                    items.add(test.getValue(), test.getKey());
                }
            }

            return items;
        }

    }

}
