/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redline.jenkins;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 *
 * @author rfriedman
 */
public class ExtraFile extends AbstractDescribableImpl<ExtraFile> {

    public String extraFile = null;

    /**
     * Gather all the inputs required to define cloud settings data to launch
     * tests.
     *
     * @param extraFile Extra File to include
     */
    @DataBoundConstructor
    public ExtraFile(String extraFile) {
        this.extraFile = extraFile;
    }

    public String getExtraFile() {
        return extraFile;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ExtraFile>{
        
        @Override
        public String getDisplayName() {
            return "";
        }
    }
    
}
