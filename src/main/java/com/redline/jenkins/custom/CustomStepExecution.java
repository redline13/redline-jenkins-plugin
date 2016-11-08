/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redline.jenkins.custom;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import javax.inject.Inject;

/**
 *
 * @author rfriedman
 */
public class CustomStepExecution extends AbstractSynchronousNonBlockingStepExecution<Void> {

    @StepContextParameter
    private transient TaskListener listener;

    @StepContextParameter
    private transient FilePath ws;

    @StepContextParameter
    private transient Run build;

    @StepContextParameter
    private transient Launcher launcher;

    @StepContextParameter
    private transient EnvVars env;
    
    @Inject
    private transient CustomStep step;

    @Override
    protected Void run() throws Exception {
        listener.getLogger().println("Running Custom Build Step.("+step.getName()+")");
        CustomBuilder builder = new CustomBuilder(
                step.getName(), 
                step.getDesc(), 
                step.getStoreOutput(),
                step.getMasterFile(),
                step.getExtraFiles(),
                step.getLanguage(), 
                step.getServers(),
                step.getThresholds()
        );
        builder.perform(build, ws, launcher, listener);
        return null;
    }
    
}
