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
public class Plugin extends AbstractDescribableImpl<Plugin> {

    public String plugin = null;

    /**
     * Gather all the inputs required to define cloud settings data to launch
     * tests.
     *
     * @param plugin Plugin to include in build
     */
    @DataBoundConstructor
    public Plugin(String plugin) {
        this.plugin = plugin;
    }

    public String getPlugin() {
        return this.plugin;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<Plugin>{

        @Override
        public String getDisplayName() {
            return "";
        }
    }

}
