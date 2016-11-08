/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redline.jenkins.gatling;

import com.redline.jenkins.ExtraFile;
import com.redline.jenkins.Servers;
import com.redline.jenkins.Thresholds;
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
public class GatlingStep extends AbstractStepImpl {

    public String name;
    public String desc;
    public Boolean storeOutput;
    public String gatlingFile;
    public ExtraFile[] extraFiles;
    public Servers[] servers;
    private final String gatlingVersion;
    private final String opts;
    public Thresholds thresholds;
    
    @DataBoundConstructor
    public GatlingStep(
            String name,
            String desc,
            Boolean storeOutput,
            String gatlingFile,
            ExtraFile[] extraFiles,
            String gatlingVersion,
            String opts,
            Servers[] servers,
            Thresholds thresholds
    ) {
        this.name = name;
        this.desc = desc;
        this.storeOutput = storeOutput;
        this.gatlingFile = gatlingFile;
        this.extraFiles = extraFiles;
        this.servers = servers;
        this.gatlingVersion = gatlingVersion;
        this.opts = opts;
        this.thresholds = thresholds;
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

    public String getGatlingFile() {
        return gatlingFile;
    }

    public ExtraFile[] getExtraFiles() {
        return extraFiles;
    }

    public Servers[] getServers() {
        if (servers == null) {
            return new Servers[0];
        }
        return this.servers;
    }
    
    public String getGatlingVersion() {
        return gatlingVersion;
    }

    public String getOpts() {
        return opts;
    }

    public Thresholds getThresholds(){
        return this.thresholds;
    }

    @Extension
    public static class DescriptorImpl extends AbstractStepDescriptorImpl{

        public DescriptorImpl(){
            super(GatlingStepExecution.class);
        }
        
        @Override
        public String getFunctionName() {
            return "redlineGatling";
        }
        
        @Nonnull
        @Override
        public String getDisplayName(){
            return "Run a Gatling load test on RedLine13.com";
        }

        public ListBoxModel doFillGatlingVersionItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("3.0", "3.0");
            items.add("2.13", "2.13");
            return items;
        }        
    }
}
