package com.redline.jenkins;

import com.cloudbees.plugins.credentials.CredentialsDescriptor;
import com.cloudbees.plugins.credentials.CredentialsScope;

import hudson.Extension;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import net.sf.json.JSONException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import java.io.IOException;


public class RedlineCredentialImpl extends RedlineCredentialAbstract {

	/** Serialization */
	private static final long serialVersionUID = 1L;
	
	/** ApiKey */
	private final Secret apiKey;
	
	/** Description */
  private final String description;

  @DataBoundConstructor
  public RedlineCredentialImpl( String apiKey, String description ) {
	  this.apiKey = Secret.fromString(apiKey);
	  this.description = description;
  }

  public String getDescription() {
	  return description;
  }

	public Secret getApiKey() {
	  return apiKey;
	}

  @Extension
  public static class DescriptorImpl extends CredentialsDescriptor {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName() {
        return Messages.RedlineCredential_DisplayName();
    }

    @Override
    public ListBoxModel doFillScopeItems() {
      ListBoxModel m = new ListBoxModel();
      m.add(CredentialsScope.GLOBAL.getDisplayName(), CredentialsScope.GLOBAL.toString());
      return m;
    }

		/** 
		 * Used by global jelly to authenticate user 
		 */
    public FormValidation doTestConnection( @QueryParameter("apiKey") final String apiKey ) 
    		throws MessagingException, IOException, JSONException, ServletException {
    	return checkApiKey(apiKey);
    }
        
		/** 
		 * Used by global jelly to authenticate user, but in the case of an update. 
		 */
    public FormValidation doTestExistedConnection(@QueryParameter("apiKey") final Secret apiKey) 
    		throws MessagingException, IOException, JSONException, ServletException {
      return checkApiKey(apiKey.getPlainText());
    }

    private FormValidation checkApiKey( final String apiKey ) 
    		throws JSONException, IOException, ServletException {

    	RedlineApi api = new RedlineApi( apiKey );
      if (api.isValidApiKey()) {
          return FormValidation.ok("Your API Key is good.");
      } else {
          return FormValidation.errorWithMarkup("API Key is not valid.");
      }
      
    }

  }
    
}