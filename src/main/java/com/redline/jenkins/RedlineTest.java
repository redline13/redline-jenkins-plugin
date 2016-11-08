package com.redline.jenkins;

import net.sf.json.JSONObject;
import net.sf.json.JSONArray;

/**
 * Represents the data returned for a completed or running RedLine13 load test.
 * @author Richard Friedman
 */
public class RedlineTest {

    public static final String CANCELLED = "CANCELLED";
    public static final String COMPLETED = "COMPLETED";
    public static final String RUNNING = "RUNNING";
    public static final String BUILDING = "BUILDING";

    public JSONObject test;
    public JSONObject settings;
    public JSONArray specs;
    public JSONArray servers;
    public JSONObject status;
    public JSONObject results;
    public JSONObject cost;
    public JSONArray errors;
    public JSONObject json;

    /**
     * Extract from JSON Object
     *
     * @param json JSON data for test data.
     */
    public RedlineTest(JSONObject json) {
        
        // Save original
        this.json = json;
        
        test = json.getJSONObject("test");

        if (json.has("settings")) {
            settings = json.getJSONObject("settings");
        }

        if (json.has("specs")) {
            specs = json.getJSONArray("specs");
        }

        if (json.has("servers")) {
            servers = json.getJSONArray("servers");
        }

        if (json.has("status")) {
            status = json.getJSONObject("status");
        }

        if (json.has("results")) {
            results = json.getJSONObject("results");
        }

        if (json.has("cost")) {
            cost = json.getJSONObject("cost");
        }
        
        if (json.has("errors")){
            errors = json.getJSONArray("errors");
        }

    }

    /**
     * Is functions for the current state
     *
     * @return is Canceled
     */
    public boolean isCancelled() {
        return getStatus().equalsIgnoreCase(RedlineTest.CANCELLED);
    }

    public boolean isCompleted() {
        return getStatus().equalsIgnoreCase(RedlineTest.COMPLETED);
    }

    public boolean isRunning() {
        return getStatus().equalsIgnoreCase(RedlineTest.RUNNING);
    }

    public boolean isBuilding() {
        return getStatus().equalsIgnoreCase(RedlineTest.BUILDING);
    }

    /**
     * Get functions for common data
     *
     * @return status message
     */
    public String getStatusMessage() {
        return status.getString("message");
    }

    public String getStatus() {
        return status.getString("code");
    }

    public int getTestId() {
        return test.getInt("load_test_id");
    }

    public String getReferenceId() {
        return test.getString("rand");
    }

    public double getSuccessRate() {
        return test.getDouble("success_rate");
    }

    public double getAvergageResponseTime() {
        return test.getDouble("avg_resp_time");
    }

    public boolean hasError(){
       return ( errors != null );
    }
    
    public String getError(){
        return errors.getString(0);
    }
    
    public String getMerged(){
        if ( json.has("merged") ){
            return json.getString("merged");
        }
        return null;
    }
    
    public Double getCost(){
        Double value = 0.0;
        if ( cost != null && cost.has("data") ){
            value = cost.getDouble("data") + cost.getDouble("hours") + cost.getDouble("servers");
            value /= 100;
        }
        return value;
    }
 
    /**
     * Easy Print.
     *
     * @return string of test data
     */
    @Override
    public String toString() {
        return String.format("#<TestData testId: %d, status: %s, duration: %d>",
                getTestId(), getStatus(), 3);
    }

}
