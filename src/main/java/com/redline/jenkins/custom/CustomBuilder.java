package com.redline.jenkins.custom;

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
public class CustomBuilder extends RedlineBuilder {

    public final String language;
    
    @DataBoundConstructor
    public CustomBuilder(
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
        this.servers = servers;
        this.language = language;
        this.thresholds = thresholds;
        this.testType = "custom-test";
    }

    public String getLanguage() {
        return language;
    }

    @Override
    public HashMap<String, String> buildTestProperties() {
        HashMap<String, String> map = new HashMap<>();
        map.put("lang", language);
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
            return "RedLine13 Custom";
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
