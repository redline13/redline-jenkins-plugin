package com.redline.jenkins;

import com.cloudbees.plugins.credentials.Credentials;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.util.Secret;

/**
 * Exposes the concept of application credentials within Jenkins. 
 */
public interface RedlineCredential extends Credentials {

    public String getDescription();

    public String getId();
    
    @NonNull
    public Secret getApiKey();

}