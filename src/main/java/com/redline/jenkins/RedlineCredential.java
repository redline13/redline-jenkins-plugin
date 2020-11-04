package com.redline.jenkins;

import com.cloudbees.plugins.credentials.Credentials;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.util.Secret;

/**
 * Exposes the concept of application credentials within Jenkins. 
 */
public interface RedlineCredential extends Credentials {

    /**
     * Get description string for credentials screen.
     * @return String
     */
    public String getDescription();

    /**
     * The API Key bot masked for display.
     * @return String  AAAA...AAAA
     */
    public String getId();

    /**
     * The stored API Key.
     * @return String API Key
     */
    public Secret getApiKey();

}