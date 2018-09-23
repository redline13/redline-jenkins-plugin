/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redline.jenkins;

import com.redline.jenkins.jmeter.JMeterBuilder;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import static junit.framework.TestCase.assertTrue;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.Rule;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;

/**
 *
 * @author rfriedman
 */
public class RedlineJMeterBuilderTest {

    @ClassRule
    public static BuildWatcher buildWatcher = new BuildWatcher();

    @Rule
    public JenkinsRule r = new JenkinsRule();


    public RedlineJMeterBuilderTest() {
    }

    @Test
    public void checkNameOutput() throws  Exception {

        // Given
        String name = "How Now!";
        JMeterBuilder builder = new JMeterBuilder( name, Boolean.TRUE );
        FreeStyleProject p = r.createFreeStyleProject();
        p.getBuildersList().add(builder);

        // When
        FreeStyleBuild fsb = p.scheduleBuild2(0).get();

        // Then
        r.assertLogContains("FreeStyle JMeter Load Test ("+name+").", fsb);
    }

    @Test
    public void checkNameOnPipeline() throws Exception{

        // Given
        String name = "I am in a pipe!";
        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "p");
        CpsFlowDefinition c = new CpsFlowDefinition("node {step([$class: 'RedlineJMeterBuilder', name: '"+name+"'])}", true);
        p.setDefinition(c);

        // When
        WorkflowRun build = p.scheduleBuild2(0).get();

        // Then
        r.assertLogContains("Pipeline JMeter Loxad Test ("+name+").", build);
    }

}
