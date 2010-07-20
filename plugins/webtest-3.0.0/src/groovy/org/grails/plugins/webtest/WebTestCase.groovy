package org.grails.plugins.webtest

import junit.framework.TestCase
import org.apache.commons.logging.LogFactory
import grails.util.GrailsUtil
import org.codehaus.groovy.runtime.InvokerHelper
import java.lang.reflect.Method
import java.lang.reflect.InvocationTargetException
import org.junit.Rule

/**
 * JUnit TestCase to allow WebTest to run as part of test-app suite. 
 */
public class WebTestCase extends TestCase {

    static ant //set via eventTestPhaseStart
    static buildListener //set via eventTestPhaseStart
    boolean testInProgress = false

    @Rule public WebTestRule webtestRule = new WebTestRule() 

    def invokeMethod(String name, args) {
        try {
            return InvokerHelper.getMetaClass(this).invokeMethod(this.class, this, name, args, false, true)
        } catch (MissingMethodException e) {
            try {
                return ant.invokeMethod(name, args)
            } catch (Throwable t) {
                throwWrapped(name, t, "Unable to invoke missing method as ant task ${name}");
            }
        } catch (Throwable e) {
            GrailsUtil.deepSanitize(e);
            throwWrapped(name, e, 'Unable to invoke test method ' + name);
        }
    }

    private def throwWrapped(String methodName, Throwable e, String message) {
        GrailsUtil.deepSanitize(e);
        LogFactory.getLog(this.class).error(message, e)
        def execException = this.class.classLoader.loadClass('com.canoo.webtest.engine.StepExecutionException')
        def wrapped = execException.newInstance([message, e] as Object[])
        throw wrapped
    }
}