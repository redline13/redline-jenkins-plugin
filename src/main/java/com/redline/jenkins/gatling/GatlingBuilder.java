package com.redline.jenkins.gatling;

import com.redline.jenkins.ExtraFile;
import com.redline.jenkins.RedlineBuilder;
import com.redline.jenkins.Servers;
import com.redline.jenkins.Thresholds;
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
public class GatlingBuilder extends RedlineBuilder {

    private final String gatlingVersion;
    private final String opts;

    @DataBoundConstructor
    public GatlingBuilder(
            String name,
            String desc,
            Boolean storeOutput,
            String masterFile,
            ExtraFile[] extraFiles,
            String gatlingVersion,
            String opts,
            Servers[] servers,
            Thresholds thresholds
    ) {
        this.name = name;
        this.desc = desc;
        this.storeOutput = storeOutput;
        this.masterFile = masterFile;
        this.extraFiles = extraFiles;
        this.gatlingVersion = gatlingVersion;
        this.opts = opts;
        this.servers = servers;
        this.thresholds = thresholds;
        this.testType = "gatling-test";
    }

    public String getGatlingVersion() {
        return gatlingVersion;
    }

    public String getOpts() {
        return opts;
    }

    @Override
    public HashMap<String, String> buildTestProperties() {
        HashMap<String, String> map = new HashMap<>();
        map.put("version", gatlingVersion);
        map.put("opts", opts);
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
            return "RedLine13 Gatling";
        }

        public ListBoxModel doFillGatlingVersionItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("2.2.0", "2.2.0");
            items.add("2.1.6", "2.1.6");
            return items;
        }
    }

}
