/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redline.jenkins.gatling;

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
public class GatlingStepExecution extends AbstractSynchronousNonBlockingStepExecution<Void> {

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
    private transient GatlingStep step;

    @Override
    protected Void run() throws Exception {
        listener.getLogger().println("Running Gatling Build Step.("+step.getName()+")");
        GatlingBuilder builder = new GatlingBuilder(
                step.getName(), 
                step.getDesc(), 
                step.getStoreOutput(),
                step.getMasterFile(),
                step.getExtraFiles(),
                step.getVersion(),
                step.getOpts(), 
                step.getServers(),
                step.getThresholds()
        );
        builder.perform(build, ws, launcher, listener);
        return null;
    }
    
}
