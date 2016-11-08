/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redline.jenkins.steps;

import com.redline.jenkins.RedlineJMeterBuilder;
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
public class RedlineJMeterStepExecution extends AbstractSynchronousNonBlockingStepExecution<Void> {

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
    private transient RedlineJMeterStep step;

    @Override
    protected Void run() throws Exception {
        listener.getLogger().println("Running JMeter Build Step.("+step.getName()+")");
        RedlineJMeterBuilder builder = new RedlineJMeterBuilder(
                step.getName(), 
                step.getDesc(), 
                step.getStoreOutput(),
                step.getJmeterFile(),
                step.getExtraFiles(),
                step.getJmeterVersion(),
                step.getOpts(), 
                step.getJvmArgs(),
                step.getServers());
        builder.perform(build, ws, launcher, listener);
        return null;
    }
    
}
