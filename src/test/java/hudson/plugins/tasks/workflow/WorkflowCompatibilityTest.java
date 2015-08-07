package hudson.plugins.tasks.workflow;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.FilePath;

import hudson.model.Result;

import hudson.plugins.tasks.TasksResultAction;

import static org.junit.Assert.*;

/**
 * Test workflow compatibility.
 */
public class WorkflowCompatibilityTest {

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    private static final String TASKS_FILE = "/hudson/plugins/tasks/parser/file-with-tasks.txt";

    /**
     * Run a workflow job using {@link TasksPublisher} and check for success.
     */
    @Test
    public void tasksPublisherWorkflowStep() throws Exception {
        WorkflowJob job = j.jenkins.createProject(WorkflowJob.class, "wf");
        FilePath workspace = j.jenkins.getWorkspaceFor(job);
        FilePath report = workspace.child("target").child("tasks.txt");
        report.copyFrom(WorkflowCompatibilityTest.class.getResourceAsStream(TASKS_FILE));
        job.setDefinition(new CpsFlowDefinition(
        "node {" +
        "  step([$class: 'TasksPublisher', pattern: '**/tasks.txt', high: 'FIXME', normal: 'TODO'])" +
        "}"));

        // Build must be success, since there are no thresholds set
        j.assertBuildStatusSuccess(job.scheduleBuild2(0));
        TasksResultAction result = job.getLastBuild().getAction(TasksResultAction.class);

        // The result has to report exactly 2 annotations coming from two tasks markers in the original file
        assertTrue(result.getResult().getAnnotations().size() == 2);
    }

    /**
     * Run a workflow job using {@link TasksPublisher} with a failing threshols of 0, so the given example file
     * "/hudson/plugins/tasks/parser/tasks-case-test.txt" will make the build to fail.
     */
    @Test
    public void tasksPublisherWorkflowStepSetLimits() throws Exception {
        WorkflowJob job = j.jenkins.createProject(WorkflowJob.class, "wf2");
        FilePath workspace = j.jenkins.getWorkspaceFor(job);
        FilePath report = workspace.child("target").child("tasks.txt");
        report.copyFrom(WorkflowCompatibilityTest.class.getResourceAsStream(TASKS_FILE));
        job.setDefinition(new CpsFlowDefinition(
        "node {" +
        "  step([$class: 'TasksPublisher', pattern: '**/tasks.txt', high: 'FIXME', normal: 'TODO', failedTotalAll: '0', usePreviousBuildAsReference: false])" +
        "}"));

        // The build must fail, since there is a failing threshold set (failedTotalAll: '0') 
        j.assertBuildStatus(Result.FAILURE, job.scheduleBuild2(0).get());
        TasksResultAction result = job.getLastBuild().getAction(TasksResultAction.class);

        // The result has to report exactly 2 annotations coming from two tasks markers in the original file
        assertTrue(result.getResult().getAnnotations().size() == 2);
    }

    /**
     * Run a workflow job using {@link TasksPublisher} with a unstable threshols of 0, so the given example file
     * "/hudson/plugins/tasks/parser/tasks-case-test.txt" will make the build to fail.
     */
    @Test
    public void tasksPublisherWorkflowStepFailure() throws Exception {
        WorkflowJob job = j.jenkins.createProject(WorkflowJob.class, "wf3");
        FilePath workspace = j.jenkins.getWorkspaceFor(job);
        FilePath report = workspace.child("target").child("tasks.txt");
        report.copyFrom(WorkflowCompatibilityTest.class.getResourceAsStream(TASKS_FILE));
        job.setDefinition(new CpsFlowDefinition(
        "node {" +
        "  step([$class: 'TasksPublisher', pattern: '**/tasks.txt', high: 'FIXME', normal: 'TODO', unstableTotalAll: '0', usePreviousBuildAsReference: false])" +
        "}"));

        // The build must report unstability, since there is an unstable threshold set (unstableTotalAll: '0') 
        j.assertBuildStatus(Result.UNSTABLE, job.scheduleBuild2(0).get());
        TasksResultAction result = job.getLastBuild().getAction(TasksResultAction.class);

        // The result has to report exactly 2 annotations coming from two tasks markers in the original file
        assertTrue(result.getResult().getAnnotations().size() == 2);
    }
}
