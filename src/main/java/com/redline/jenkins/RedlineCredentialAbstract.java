package com.redline.jenkins;

import com.cloudbees.plugins.credentials.BaseCredentials;
import com.cloudbees.plugins.credentials.CredentialsScope;
import org.apache.commons.lang.StringUtils;

/**
 * Implements the RedlineCredential, exposing the implement of the fields.
 */
public abstract class RedlineCredentialAbstract extends BaseCredentials implements RedlineCredential {

    protected RedlineCredentialAbstract() {
        super(CredentialsScope.GLOBAL);
    }

    protected RedlineCredentialAbstract(CredentialsScope scope) {
        super(scope);
    }

    /**
     * Display for the API Key in the UI.
     */
    public String getId() {
        final String apiKey = getApiKey().getPlainText();
        return StringUtils.left(apiKey, 4) + "..." + StringUtils.right(apiKey, 4);
    }
}
