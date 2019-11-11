package com.redline.jenkins.jmeter;

import com.redline.jenkins.ExtraFile;
import com.redline.jenkins.RedlineBuilder;
import com.redline.jenkins.Servers;
import com.redline.jenkins.Thresholds;
import com.redline.jenkins.Plugin;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import java.util.HashMap;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * The RedLine13 Build Task for running a new load test where the resources come
 * from the project.
 *
 * @author Richar dFriedman rich@redline13.com
 */
public class JMeterBuilder extends RedlineBuilder {

    private final String jmeterVersion;
    private final String opts;
    private final String jvmArgs;

    public JMeterBuilder( String desc, Boolean storeOutput ){
      this.desc = desc;
      this.storeOutput = storeOutput;

      this.name = null;
      this.masterFile = null;
      this.extraFiles = null;
      this.jmeterVersion = null;
      this.opts = null;
      this.jvmArgs = null;
      this.servers = null;
      this.thresholds = null;
      this.testType = "jmeter-test";
      this.plugins = null;
    }

    @DataBoundConstructor
    public JMeterBuilder(
            String name,
            String desc,
            Boolean storeOutput,
            String masterFile,
            ExtraFile[] extraFiles,
            String jmeterVersion,
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
        this.jmeterVersion = jmeterVersion;
        this.opts = opts;
        this.jvmArgs = jvmArgs;
        this.servers = servers;
        this.thresholds = thresholds;
        this.testType = "jmeter-test";
        this.plugins = plugins;
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


    @Override
    public HashMap<String, String> buildTestProperties() {
        HashMap<String, String> map = new HashMap<>();
        map.put("version", jmeterVersion);
        map.put("opts", opts);
        map.put("jvm_args", jvmArgs);
        return map;
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> type) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "RedLine13 JMeter";
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
