package org.grails.plugins.webtest

import org.apache.tools.ant.BuildListener
import org.apache.tools.ant.BuildEvent
import org.apache.tools.ant.Task


public class WebTestPluginBuildListener implements BuildListener {

    private passed = true
    private lastException = null

    public void buildFinished(BuildEvent buildEvent) {

    }

    public void buildStarted(BuildEvent buildEvent) {

    }

    public void messageLogged(BuildEvent buildEvent) {

    }

    public void targetFinished(BuildEvent buildEvent) {//To change body of implemented methods use File | Settings | File Templates.
    }

    public void targetStarted(BuildEvent buildEvent) {
    }

    public void taskFinished(final BuildEvent event) {
        def task = event.task
        def taskClass = getClass().classLoader.loadClass('com.canoo.webtest.ant.TestStepSequence')
        if (!(task instanceof Task))
            return;
        else if (task.class == taskClass) {
            def webtestTask = task.context.webtest
                passed = (event.exception == null)
                if (!passed) {
                    lastException = event.exception
                    println "Webtest failure: ${event.exception}"
                } else {
                    lastException = null
                }
        }
    }

    public void taskStarted(BuildEvent buildEvent) {

    }

    public notifyWebTestAdded(task) {
        def wrapper = task.getWrapper()
        def webtestName = task.getProject().replaceProperties(wrapper.attributeMap['name']);
        def webTestLabel = swing.label(text: "${webtestName}...",
                toolTipText: task.getLocation().toString() - ": ")

        mapWebTest2Label[task.wrapper] = webTestLabel
    }

    public boolean hasPassed() {
        passed
    }

    public void reset() {
        passed = false
    }
    
    def getLastException() {
        lastException
    }
}