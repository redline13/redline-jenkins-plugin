package com.redline.jenkins;

import net.sf.json.JSONObject;
import net.sf.json.JSONArray;

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
	
	/**
	 * Extract from JSON Object
	 */	
	public RedlineTest(JSONObject json) {
		
		test = json.getJSONObject( "test" );
		
		if ( json.has( "settings" ) ) { 
			settings = json.getJSONObject( "settings" );	
		}
		
		if ( json.has( "specs" ) ) { 
			specs = json.getJSONArray( "specs" );	
		}
		
		if ( json.has( "servers" ) ) { 
			servers = json.getJSONArray( "servers" );	
		}
		
		if ( json.has( "status" ) ) { 
			status = json.getJSONObject( "status" );	
		}
		
		if ( json.has( "results" ) ) { 
			results = json.getJSONObject( "results" );	 
		}

		if ( json.has( "cost" ) ) { 
			results = json.getJSONObject( "cost" );	 
		}
		
	}
	
	/** Is functions for the current state */
	public boolean isCancelled() { 
		return getStatus().equalsIgnoreCase( RedlineTest.CANCELLED );
	}
	public boolean isCompleted() { 
		return getStatus().equalsIgnoreCase( RedlineTest.COMPLETED );
	}
	public boolean isRunning() { 
		return getStatus().equalsIgnoreCase( RedlineTest.RUNNING );
	}
	public boolean isBuilding() { 
		return getStatus().equalsIgnoreCase( RedlineTest.BUILDING );
	}
	
	/** Get functions for common data */
	public String getStatusMessage() { 
		return status.getString( "message" );		
	}
	
	public String getStatus() { 
		return status.getString( "code" );
	}
	
	public int getTestId() { 
		return test.getInt("load_test_id");
	}
	
	public String getReferenceId() {
		return test.getString("rand"); 
	}
	
	public double getSuccessRate() { 
		return test.getDouble( "success_rate" );
	}
	
	public double getAvergageResponseTime() { 
		return test.getDouble( "avg_resp_time" );
	}
	
	/**
	 * Easy Print.
	 */
	public String toString() {
		return String.format("#<TestData testId: %d, status: %s, duration: %d>", 
			getTestId(), getStatus(), 3 );
	}

}