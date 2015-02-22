package com.redline.jenkins;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.util.Map;
import java.util.HashMap;

import net.sf.json.JSONException;
import net.sf.json.JSONSerializer;
import net.sf.json.JSONObject;
import net.sf.json.JSONArray;
import net.sf.json.JSON;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.apache.http.HttpResponse;

public class RedlineApi {

	/** Base URI To Redline */
	String baseApiUri = "https://www.redline13.com/api/";
	public String baseUri = "https://www.redline13.com/";

	/** Helper for Debug, TODO kill */
  PrintStream logger = new PrintStream(System.out);
  
  /** API Key for this test or Request */
  String apiKey;

	/**
	 * Construct with API Key
	 */
  public RedlineApi( String apiKey ) {
    
    this.apiKey = apiKey;

  }

	/**
	 * Check the API Key by validating against the system status check.
	 */
	public boolean isValidApiKey() {
	
		if ( apiKey == null || apiKey.trim().isEmpty() ) {
			return false;
		}
	
		Result result = doRequest( new HttpGet(), "status" );
		if ( result.isFail() ) { 
			return false;
		}
		
		return true;

	}

	/**
	 * Call api to get list of Jenkins accessible tests. 
	 */
  public Map<String, String> getTemplateList() {
		
		Result result = doRequest( new HttpGet(), "templates" ); 
		if ( result.isFail() ) { 
			return null;
		}

		JSONArray list = null;
		try {
			list = (JSONArray) JSONSerializer.toJSON( result.body );			
		} catch (RuntimeException ex) {
			logger.println("Got Exception: " + ex);
			return null;
		}
		
		if (list == null) {
			return null;
		}
		
		 Map<String, String> tests = new HashMap<String, String>();
		for (Object test : list) {
			JSONObject t = (JSONObject) test;
			tests.put( t.getString("test_api_key"), t.getString("load_test_desc"));
		}
		
		return tests;
  }

	/**
	 * Get test information for a specific test. 
	 * @return The TEMPLATE with full details
	 */
	public RedlineTest getTemplate( String templateId ) {
	
		Result result = doRequest( new HttpGet(), "template/" + templateId );
		if (result.isFail()) {
			return null;
		}
		
		try {
			JSONObject json = (JSONObject) JSONSerializer.toJSON( result.body );
			return new RedlineTest( json );
			
		} catch (RuntimeException ex) {
			
			logger.format("Got exception: %s", ex);
			return null;
		
		}
	}

	/**
	 * Run a test scenario
	 * @return The RUNNING test object
	 */
	public RedlineTest runTemplate( String templateId ) {
	
		Result result = doRequest( new HttpPost(), "template/" + templateId );		
		if (result.isFail()) {
			return null;
		}
		
		//TODO: check on exception
		JSONObject json = (JSONObject) JSONSerializer.toJSON( result.body );
		return new RedlineTest(json);
		
	}

	/**
	 * Get test information for a specific test. 
	 */
	public RedlineTest getTestStatus( RedlineTest test ) {
	
		int testId = test.getTestId();
		String reference = test.getReferenceId();
		
		Result result = doRequest( new HttpGet(), "teststatus/" + testId + "/" + reference );
		if (result.isFail()) {
			logger.println( "Failed teststatus/" + testId + "/" + reference );
			return null;
		}
		
		try {
		
			JSONObject json = (JSONObject) JSONSerializer.toJSON( result.body );
			return new RedlineTest( json );
			
		} catch (RuntimeException ex) {
			
			logger.format("Exception JSONSerializer: %s", ex);
			return null;
		
		}
	}

	/**
	 * Gets as much information about the test as it can
	 * This can include stats details as well if available. 
	 */
	public RedlineTest getTestSummary( RedlineTest test ) {

		int testId = test.getTestId();
		String reference = test.getReferenceId();

		Result result = doRequest( new HttpGet(), "testsummary/" + testId + "/" + reference );
		if (result.isFail()) {
			return null;
		}
		
		try {
		
			JSONObject json = (JSONObject) JSONSerializer.toJSON( result.body );
			return new RedlineTest( json );
			
		} catch (RuntimeException ex) {
			
			logger.format("Exception JSONSerializer: %s", ex);
			return null;
		
		}
	}

	/**
	 * The request is built and executed for path.
	 * A Result object is used to wrap the http response.
	 * @return Response
	 */
  private Result doRequest( HttpRequestBase request, String path) {
  
		URI fullUri = null;		
		try {
			fullUri = new URI( baseApiUri + path );
		} catch (java.net.URISyntaxException ex) {
			throw new RuntimeException("Incorrect URI format: %s", ex);
		}
		
		request.setURI(fullUri);
		request.addHeader("Content-Type", "application/json");
		request.addHeader("X-Redline-Source", "Jenkins" );
		request.addHeader("X-Redline-Auth", apiKey);
		
		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse response;
  	Result result = null;
		
		try {
			response = client.execute(request);
			result = new Result(response);
			
		} catch (IOException ex) {
  		
			logger.format("Error during remote call to API. Exception received: %s", ex);
			ex.printStackTrace( logger );

			return new Result("Network error during remote call to API");
		}
		
		return result;
  }

	/** 
	 * Class to wrap the Response Results from API
	 */
  static class Result {
  
	  public int code;
	  public String errorMessage;
	  public String body;

	  static final String badResponseError = "API Error.";
	  static final String formatError = "Invalid API Format.";

		public Result(String error) {
			code = -1;
			errorMessage = error;
		}

    public Result(HttpResponse response) {
	    code = response.getStatusLine().getStatusCode();
	    try {
        body = EntityUtils.toString( response.getEntity() );
	    } catch (IOException ex) {
        code = -1;
        errorMessage = badResponseError;
	    }
	    
	    if (code != 200) {
        errorMessage = getErrorFromJson(body);
	    }
    }

		/**
		 * Anything not 200 is fail.
		 */
	  public boolean isFail() {
      return 200 != code;
	  }

    /**
     * Extract error from JSON message 
     * {"message":"error","errors":["wrong api key(xxx)"]}
     */
    private String getErrorFromJson(String json) {
      // parse json
      JSON object;
      try {
        object = JSONSerializer.toJSON(json);
      } catch (JSONException ex) {
        return formatError;
      }
      if (!(object instanceof JSONObject)) {
        return formatError;
      }
      StringBuilder error = new StringBuilder(badResponseError);
      //TODO: check on error
      for (Object message : ((JSONObject) object).getJSONArray("errors")) {
        error.append(message.toString());
      }
      return error.toString();
    }
  }
}