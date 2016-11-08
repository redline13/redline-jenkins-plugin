/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redline.jenkins.custom;

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
public class CustomStep extends AbstractStepImpl {

    public String name;
    public String desc;
    public Boolean storeOutput;
    public String masterFile;
    public ExtraFile[] extraFiles;
    public Servers[] servers;
    public String language;
    public Thresholds thresholds;
    
    @DataBoundConstructor
    public CustomStep(
            String name,
            String desc,
            Boolean storeOutput,
            String masterFile,
            ExtraFile[] extraFiles,
            String language,
            Servers[] servers,
            Thresholds thresholds
    ) {
        this.name = name;
        this.desc = desc;
        this.storeOutput = storeOutput;
        this.masterFile = masterFile;
        this.extraFiles = extraFiles;
        this.language = language;
        this.servers = servers;
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

    public String getMasterFile() {
        return masterFile;
    }

    public ExtraFile[] getExtraFiles() {
        return extraFiles;
    }

    public String getLanguage() {
        return language;
    }
    
    public Servers[] getServers() {
        if (servers == null) {
            return new Servers[0];
        }
        return this.servers;
    }

    public Thresholds getThresholds() {
        return thresholds;
    }
    
    @Extension
    public static class DescriptorImpl extends AbstractStepDescriptorImpl{

        public DescriptorImpl(){
            super(CustomStepExecution.class);
        }
        
        @Override
        public String getFunctionName() {
            return "redlineCustom";
        }
        
        @Nonnull
        @Override
        public String getDisplayName(){
            return "Run a Custom load test on RedLine13.com";
        }

        public ListBoxModel doFillLanguageItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("php", "php");
            items.add("nodejs", "nodejs");
            items.add("python", "python");
            return items;
        }        
    }
}
