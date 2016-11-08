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
public class Thresholds extends AbstractDescribableImpl<Thresholds> {

    public final int errorFailedThreshold;
    public final int errorUnstableThreshold;
    public final int responseTimeFailedThreshold;
    public final int responseTimeUnstableThreshold;

    /**
     * Gather all the inputs required to define cloud settings data to launch
     * tests.
     *
     * @param errorFailedThreshold Error Threshold for error $
     * @param errorUnstableThreshold Unstable Threshold for error $
     * @param responseTimeFailedThreshold Error threshold for time in ms
     * @param responseTimeUnstableThreshold Unstable threshold for time in ms
     */
    @DataBoundConstructor
    public Thresholds(
            int errorFailedThreshold,
            int errorUnstableThreshold,
            int responseTimeFailedThreshold,
            int responseTimeUnstableThreshold
    ) {
        this.errorFailedThreshold = errorFailedThreshold;
        this.errorUnstableThreshold = errorUnstableThreshold;
        this.responseTimeFailedThreshold = responseTimeFailedThreshold;
        this.responseTimeUnstableThreshold = responseTimeUnstableThreshold;
    }

    public int getErrorFailedThreshold() {
        return errorFailedThreshold;
    }
    
    public boolean checkErrorFailed(int value){
        return  this.errorFailedThreshold > 0 && value < this.errorFailedThreshold;
    }

    public int getErrorUnstableThreshold() {
        return errorUnstableThreshold;
    }
    
    public boolean checkErrorUnstable(int value){
        return  this.errorUnstableThreshold > 0 && value < this.errorUnstableThreshold;
    }

    public int getResponseTimeFailedThreshold() {
        return responseTimeFailedThreshold;
    }
    
    public boolean checkResponseTimeFailed(int value){
        return this.responseTimeFailedThreshold > 0 && value > this.responseTimeFailedThreshold;
    }

    public int getResponseTimeUnstableThreshold() {
        return responseTimeUnstableThreshold;
    }
    
    public boolean checkResponseTimeUnstable(int value){
        return this.responseTimeUnstableThreshold > 0 && value > this.responseTimeUnstableThreshold;
    }
    
    @Extension
    public static class DescriptorImpl extends Descriptor<Thresholds> {

        @Override
        public String getDisplayName(){
            return "";
        }
        
    }

}
