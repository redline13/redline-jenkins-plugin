package com.redline.jenkins;

import java.util.Enumeration;
import java.net.*;

import hudson.model.Action;
import hudson.model.AbstractBuild;

public class RedlineBuildAction implements Action {

	private final AbstractBuild<?, ?> build;
	private RedlineTest test;

	public RedlineBuildAction(AbstractBuild<?, ?> build, RedlineTest test ) {
		this.build = build;
		this.test = test;
	}

	public AbstractBuild<?, ?> getOwner() {
		return build;
	}

	public RedlineTest getTest() {
		return this.test;
	}

	public int getTestId() {
		
		if ( this.test == null ) { 
			return 0;
		}
		
		return this.test.getTestId();
	}

	public String getReferenceId() {
		
		if ( this.test == null ) { 
			return "";
		}
		
		return this.test.getReferenceId();
	}

	public String getIconFileName() {
		return "/plugin/redline-jenkins-plugin/images/icon.png";
	}

	public String getDisplayName() {
		return "Redline13 Report";
	}

	public String getUrlName() {
		return "redline";
	}
	
	public String getDomain() {
  	
  	RedlineApi api = new RedlineApi( null );
  	return api.baseUri;
  	
	}

}