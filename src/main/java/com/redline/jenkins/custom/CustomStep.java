/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redline.jenkins.steps;

import com.redline.jenkins.ExtraFile;
import com.redline.jenkins.Servers;
import hudson.Extension;
import hudson.util.ListBoxModel;
import javax.annotation.Nonnull;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 *
 * @author rfriedman
 */
public class RedlineJMeterStep extends AbstractStepImpl{

    private final String name;
    private final String desc;
    private final Boolean storeOutput;
    private final String jmeterFile;
    private final ExtraFile[] extraFiles;
    private final String jmeterVersion;
    private final String opts;
    private final String jvmArgs;
    private final Servers[] servers;
    
    @DataBoundConstructor
    public RedlineJMeterStep(
            String name,
            String desc,
            Boolean storeOutput,
            String jmeterFile,
            ExtraFile[] extraFiles,
            String jmeterVersion,
            String opts,
            String jvmArgs,
            Servers[] servers
    ) {
        this.name = name;
        this.desc = desc;
        this.storeOutput = storeOutput;
        this.jmeterFile = jmeterFile;
        this.extraFiles = extraFiles;
        this.jmeterVersion = jmeterVersion;
        this.opts = opts;
        this.jvmArgs = jvmArgs;
        this.servers = servers;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public Boolean getStoreOutput() {
        return storeOutput;
    }

    public String getJmeterFile() {
        return jmeterFile;
    }

    public ExtraFile[] getExtraFiles() {
        return extraFiles;
    }
    
    public String getJmeterVersion() {
        return jmeterVersion;
    }

    public String getOpts() {
        return opts;
    }

    public String getJvmArgs() {
        return jvmArgs;
    }

    public Servers[] getServers() {
        if (servers == null) {
            return new Servers[0];
        }
        return this.servers;
    }
    
    @Extension
    public static class DescriptorImpl extends AbstractStepDescriptorImpl{

        public DescriptorImpl(){
            super(RedlineJMeterStepExecution.class);
        }
        
        @Override
        public String getFunctionName() {
            return "RedlineJMeter";
        }
        
        @Nonnull
        @Override
        public String getDisplayName(){
            return "Run a JMeter load test on RedLine13.com";
        }

        public ListBoxModel doFillJmeterVersionItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("3.0", "3.0");
            items.add("2.13", "2.13");
            return items;
        }        
    }
}
