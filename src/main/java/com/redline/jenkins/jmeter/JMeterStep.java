/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redline.jenkins.jmeter;

import com.redline.jenkins.ExtraFile;
import com.redline.jenkins.Servers;
import com.redline.jenkins.Thresholds;
import com.redline.jenkins.Plugin;
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
public class JMeterStep extends AbstractStepImpl {

    public String name;
    public String desc;
    public Boolean storeOutput;
    public String masterFile;
    public ExtraFile[] extraFiles;
    public Servers[] servers;
    private final String version;
    private final String opts;
    private final String jvmArgs;
    public Thresholds thresholds;
    public Plugin[] plugins;

    @DataBoundConstructor
    public JMeterStep(
            String name,
            String desc,
            Boolean storeOutput,
            String masterFile,
            ExtraFile[] extraFiles,
            String version,
            String opts,
            String jvmArgs,
            Servers[] servers,
            Thresholds thresholds,
            Plugin[] plugins
    ) {
        this.name = name;
        this.desc = desc;
        this.storeOutput = storeOutput;
        this.masterFile = masterFile;
        this.extraFiles = extraFiles;
        this.servers = servers;
        this.version = version;
        this.opts = opts;
        this.jvmArgs = jvmArgs;
        this.thresholds = thresholds;
        this.plugins = plugins;
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

    public Servers[] getServers() {
        if (servers == null) {
            return new Servers[0];
        }
        return this.servers;
    }

    public String getVersion() {
        return version;
    }

    public String getOpts() {
        return opts;
    }

    public String getJvmArgs() {
        return jvmArgs;
    }

    public Thresholds getThresholds(){
        return this.thresholds;
    }

    public Plugin[] getPlugins(){
      return this.plugins;
    }

    @Extension
    public static class DescriptorImpl extends AbstractStepDescriptorImpl{

        public DescriptorImpl(){
            super(JMeterStepExecution.class);
        }

        @Override
        public String getFunctionName() {
            return "redlineJMeter";
        }

        @Nonnull
        @Override
        public String getDisplayName(){
            return "Run a JMeter load test on RedLine13.com";
        }

        public ListBoxModel doFillJmeterVersionItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("Nightly", "nightly");
            items.add("5.2", "5.2");
            items.add("5.0", "5.0");
            items.add("4.0", "4.0");
            items.add("3.1", "3.1");
            items.add("3.0", "3.0");
            items.add("2.13", "2.13");
            return items;
        }
    }
}
