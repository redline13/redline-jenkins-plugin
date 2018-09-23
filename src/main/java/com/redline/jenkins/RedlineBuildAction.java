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
    private final int testId;
    private final String referenceId;

    /**
     * Track which 'Build' and which 'Test' we display action for.
     *
     * @param build Build object
     * @param test Test Object
     */
    public RedlineBuildAction(Run<?, ?> build, RedlineTest test) {
        this.build = build;
        if ( test != null ){
          this.testId = test.getTestId();
          this.referenceId = test.getReferenceId();
        } else {
          this.testId = 0;
          this.referenceId = null;
        }
    }

    public Run<?, ?> getRun() {
        return build;
    }

    public int getTestId() {
      return this.testId;
    }

    public String getReferenceId() {
      return this.referenceId;
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
