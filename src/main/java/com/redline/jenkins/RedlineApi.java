package com.redline.jenkins;

import hudson.FilePath;
import hudson.ProxyConfiguration;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Proxy;
import java.net.URI;
import java.util.Map;
import java.util.HashMap;
import jenkins.model.Jenkins;

import net.sf.json.JSONException;
import net.sf.json.JSONSerializer;
import net.sf.json.JSONObject;
import net.sf.json.JSONArray;
import net.sf.json.JSON;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class RedlineApi {

    /**
     * Base URI To Redline
     */
    String baseApiUri = "https://www.redline13.com/api/";
    public String baseUri = "https://www.redline13.com/";

    /**
     * Helper for Debug, TODO kill
     */
    PrintStream logger = new PrintStream(System.out);

    /**
     * API Key for this test or Request
     */
    String apiKey;

    /**
     * Construct with API Key
     *
     * @param apiKey API Key for this test or Request
     */
    public RedlineApi(String apiKey) {

        this.apiKey = apiKey;

    }

    /**
     * Check the API Key by validating against the system status check.
     *
     * @return is the API Key valid.
     */
    public boolean isValidApiKey() {

        if (apiKey == null || apiKey.trim().isEmpty()) {
            return false;
        }

        Result result = doRequest(new HttpGet(), "status");
        return !result.isFail();

    }

    /**
     * Call api to get list of Jenkins accessible tests.
     *
     * @return List of templates available to run from RL
     */
    public Map<String, String> getTemplateList() {

        Result result = doRequest(new HttpGet(), "templates");
        if (result.isFail()) {
            return null;
        }

        JSONArray list;
        try {
            list = (JSONArray) JSONSerializer.toJSON(result.body);
        } catch (RuntimeException ex) {
            logger.println("Got Exception: " + ex);
            return null;
        }

        if (list == null) {
            return null;
        }

        Map<String, String> tests = new HashMap<>();
        for (Object test : list) {
            JSONObject t = (JSONObject) test;
            tests.put(t.getString("test_api_key"), t.getString("load_test_name"));
        }

        return tests;
    }

    /**
     * Get test information for a specific test.
     *
     * @param templateId id of template to retrieve
     * @return The TEMPLATE with full details
     */
    public RedlineTest getTemplate(String templateId) {

        Result result = doRequest(new HttpGet(), "template/" + templateId);
        if (result.isFail()) {
            return null;
        }

        try {
            JSONObject json = (JSONObject) JSONSerializer.toJSON(result.body);
            return new RedlineTest(json);

        } catch (RuntimeException ex) {

            logger.format("Got exception: %s", ex);
            return null;

        }
    }

    /**
     * Run any test type through the API.
     *
     * @param testType One of the different test types supported.
     * @param testName Name of test.
     * @param testDescription Description for test
     * @param storeOutput Boolean if output on test should be stored
     * @param testProperties Test properties, passed directly through
     * @param file filename for the master file jmeter, gatling, custom
     * @param extras if extra files are to be attached to custom tests
     * @param servers The meta describing type of servers to use
     * @param plugins List plugins needed by name
     * @return Test object
     * @throws java.io.IOException pass through
     * @throws java.lang.InterruptedException pass through
     */
    public RedlineTest runTest(
            String testType,
            String testName,
            String testDescription,
            boolean storeOutput,
            FilePath file,
            FilePath[] extras,
            HashMap<String, String> testProperties,
            Servers servers[],
            Plugin plugins[] ) throws IOException, InterruptedException {

        // Build up HTTP Post object using multipart builder.
        HttpPost request = new HttpPost(baseApiUri);
        MultipartEntityBuilder meb = MultipartEntityBuilder.create();

        // Add the basic fields.
        meb.addTextBody("name", testName);
        meb.addTextBody("desc", testDescription);
        meb.addTextBody("storeOutput", storeOutput ? "T" : "F");

        // The File is added as a stream
        if (file != null) {
            meb.addBinaryBody("file", file.read(), ContentType.APPLICATION_OCTET_STREAM, file.getName());
        }

        // Add in support for EXTRAS
        if (extras != null && extras.length > 0 ) {
            for (FilePath fp : extras) {
                if ( fp != null ){
                    meb.addBinaryBody("extras[]", fp.read(), ContentType.APPLICATION_OCTET_STREAM, fp.getName());
                }
            }
        }

        // Send over test specific properties for JMeter, Gatling, Custom, ...
        for (Map.Entry<String, String> entry : testProperties.entrySet()) {
            String value = entry.getValue();
            if (value != null && !value.isEmpty()) {
                meb.addTextBody(entry.getKey(), value);
            }
        }

        // Add in support for PLUGINS, TODO add in properties for plugins
        if (plugins != null && plugins.length > 0 ) {
          for (int i = 0; i < plugins.length; i++) {
            meb.addTextBody("plugin[" + i + "]", plugins[i].getPlugin());
          }
        }

        // total users or servers depdending on test type.
        int totalUsers = 0;
        // Add Cloud Server settings by iterating over each server object
        for (int i = 0; i < servers.length; i++) {
            totalUsers += (servers[i].getNumberServers()*servers[i].getUsersPerServer());

            String server = "servers[" + i + "]";
            meb.addTextBody(server + "[num]", Integer.toString(servers[i].getNumberServers()));
            meb.addTextBody(server + "[location]", servers[i].getLocation());
            meb.addTextBody(server + "[size]", servers[i].getSize());
            if (servers[i].getUseSpot() == true) {
                meb.addTextBody(server + "[onDemand]", "F");
                meb.addTextBody(server + "[maxPrice]", Double.toString(servers[i].getMaxPrice()));
            } else {
                meb.addTextBody(server + "[onDemand]", "T");
            }
            meb.addTextBody(server + "[volumeSize]", Integer.toString(servers[i].getVolumeSize()));
            if (servers[i].getSubnetId() != null) {
                meb.addTextBody(server + "[subnetId]", servers[i].getSubnetId());
            }
            if (servers[i].getSecurityGroupIds() != null) {
                meb.addTextBody(server + "[securityGroupIds]", servers[i].getSecurityGroupIds());
            }
            meb.addTextBody(server + "[associatePublicIpAddress]", servers[i].getAssociatePublicIpAddress() ? "T" : "F");
            meb.addTextBody(server + "[usersPerServer]", Integer.toString(servers[i].getUsersPerServer()) );
        }
        // Total # Servers or Users depending on test type.
        meb.addTextBody("numUsers", Integer.toString(totalUsers));
        request.setEntity(meb.build());

        // Do the Request
        Result result = doRequest(request, "LoadTest/" + testType);
        if (result.isFail()) {
            logger.println("Error code was " + result.code);
            return null;
        }

        // Read in JSON and get data into a test object.
        JSONObject json = (JSONObject) JSONSerializer.toJSON(result.body);
        return new RedlineTest(json);
    }

    /**
     * Run a test scenario
     *
     * @param templateId id of template to run
     * @return The RUNNING test object
     */
    public RedlineTest runTemplate(String templateId) {

        Result result = doRequest(new HttpPost(), "template/" + templateId);
        if (result.isFail()) {
            return null;
        }

        //TODO: check on exception
        JSONObject json = (JSONObject) JSONSerializer.toJSON(result.body);
        return new RedlineTest(json);

    }

    /**
     * Get test information for a specific test.
     *
     * @param test Test object
     * @return the redlinetest object
     */
    public RedlineTest getTestStatus(RedlineTest test) {

        int testId = test.getTestId();
        String reference = test.getReferenceId();

        Result result = doRequest(new HttpGet(), "teststatus/" + testId + "/" + reference);
        if (result.isFail()) {
            logger.println("Failed teststatus/" + testId + "/" + reference);
            return null;
        }

        try {

            JSONObject json = (JSONObject) JSONSerializer.toJSON(result.body);
            return new RedlineTest(json);

        } catch (RuntimeException ex) {

            logger.format("Exception JSONSerializer: %s", ex);
            return null;

        }
    }

    /**
     * Gets as much information about the test as it can This can include stats
     * details as well if available.
     *
     * @param test test object
     * @return redlinetest object
     */
    public RedlineTest getTestSummary(RedlineTest test) {

        int testId = test.getTestId();
        String reference = test.getReferenceId();

        Result result = doRequest(new HttpGet(), "testsummary/" + testId + "/" + reference);
        if (result.isFail()) {
            return null;
        }

        try {

            JSONObject json = (JSONObject) JSONSerializer.toJSON(result.body);
            return new RedlineTest(json);

        } catch (RuntimeException ex) {

            logger.format("Exception JSONSerializer: %s", ex);
            return null;

        }
    }

    public RedlineTest getTestFiles(RedlineTest test){
        int testId = test.getTestId();

        Result result = doRequest(new HttpGet(), "StatsDownloadUrls?loadTestId=" + testId);
        if (result.isFail()) {
            return null;
        }

        try {

            JSONObject json = (JSONObject) JSONSerializer.toJSON(result.body);
            return new RedlineTest(json);

        } catch (RuntimeException ex) {

            logger.format("Exception JSONSerializer: %s", ex);
            return null;

        }
    }

    /**
     * The request is built and executed for path. A Result object is used to
     * wrap the http response.
     *
     * @return Response
     */
    private Result doRequest(HttpRequestBase request, String path) {

        if (path != null) {
            try {
                request.setURI(new URI(baseApiUri + path));
            } catch (java.net.URISyntaxException ex) {
                throw new RuntimeException("Incorrect URI format: %s", ex);
            }
        }

        request.addHeader("X-Redline-Source", "Jenkins");
        request.addHeader("X-Redline-Auth", apiKey);

        CloseableHttpClient client = null;

        ProxyConfiguration proxyConfig = Jenkins.getInstance().proxy;
        if( proxyConfig != null ){
            Proxy proxy = proxyConfig.createProxy("www.redline13.com");
            if ( proxy != null && proxy.type() == Proxy.Type.HTTP ){

                if ( proxyConfig.getUserName() != null ){
                    CredentialsProvider credsProvider = new BasicCredentialsProvider();
                    credsProvider.setCredentials(
                        new AuthScope(proxyConfig.name, proxyConfig.port ),
                        new UsernamePasswordCredentials(proxyConfig.getUserName(), proxyConfig.getPassword()));
                    client = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
                }

                HttpHost httpProxy = new HttpHost(proxyConfig.name, proxyConfig.port, "http");
                RequestConfig config = RequestConfig.custom()
                    .setProxy(httpProxy)
                    .build();
                request.setConfig(config);
                logger.println( "--- PROXY DEBUG >> " + proxy.toString() );
            }
        }

        if ( client == null ) {
            client = HttpClients.createDefault();
        }

        CloseableHttpResponse response;
        Result result;
        try {
            response = client.execute(request);
            result = new Result(response);
            response.close();

        } catch (IOException ex) {

            logger.format("Error during remote call to API. Exception received: %s", ex);
            ex.printStackTrace(logger);

            return new Result("Network error during remote call to API");
        } finally {
            try {
                client.close();
            } catch (IOException ex) {
                // Do nothing.
            }
        }

        // Useful for reporting issues and debugging.
//        logger.println( "--- Result Body ---");
//        logger.println(result.body);
//        logger.println("---------");

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
                body = EntityUtils.toString(response.getEntity());
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
         * Extract error from JSON message {"message":"error","errors":["wrong
         * api key(xxx)"]}
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
