package com.redline.jenkins;


import hudson.model.Action;
import hudson.model.Run;

/**
 * This gives access to the RedLine13 Generated Report in the output section of
 * the build.
 *
 * @author rfriedman
 */
public class RedlineBuildAction implements Action {

    private final Run<?, ?> build;
    private final RedlineTest test;

    /**
     * Track which 'Build' and which 'Test' we display action for.
     *
     * @param build Build object
     * @param test Test Object
     */
    public RedlineBuildAction(Run<?, ?> build, RedlineTest test) {
        this.build = build;
        this.test = test;
    }

    public Run<?, ?> getRun() {
        return build;
    }

    public RedlineTest getTest() {
        return this.test;
    }

    public int getTestId() {

        if (this.test == null) {
            return 0;
        }

        return this.test.getTestId();
    }

    public String getReferenceId() {

        if (this.test == null) {
            return "";
        }

        return this.test.getReferenceId();
    }

    @Override
    public String getIconFileName() {
        return "/plugin/redline-jenkins-plugin/images/icon.png";
    }

    @Override
    public String getDisplayName() {
        return "Redline13 Report";
    }

    @Override
    public String getUrlName() {
        return "redline";
    }

    public String getDomain() {

        RedlineApi api = new RedlineApi(null);
        return api.baseUri;

    }

}
