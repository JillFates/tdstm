package org.grails.plugins.webtest

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.codehaus.groovy.control.CompilationFailedException;

// This has been borrowed from WebTest so that it uses the classLoader that contains the grails application and plugin classes
class GroovyInvoker {
    private static final Logger LOG = Logger.getLogger(GroovyInvoker.class);
    /**
     * Key used to save the binding as webtest properties and allow reuse across different Groovy steps of a webtest
     */
    private static final String KEY_GROOVY_BINDING = GroovyInvoker.class.getName() + "#binding";

    public void doExecute(final step, final String script) {
        final Binding variables = getBinding(step);

        // set standard variable (need to be set even if binding is reused as these 2 variables are bound to current step)
        variables.setVariable("step", step);
        final DummyPrinter out = new DummyPrinter(step, Level.INFO);
        variables.setVariable("out", out);
        final GroovyShell shell = new GroovyShell(getClass().getClassLoader(), variables);
        try {
            LOG.debug("Evaluating script: " + StringUtils.abbreviate(script, 20));
            shell.evaluate(script);
        }
        catch (final CompilationFailedException e) {
            LOG.error("CompilationFailedException", e);
            throw getClass().getClassLoader().loadClass('com.canoo.webtest.engine.StepExecutionException').newInstance(["Cannot compile groovy code: " + script, step, e] as Object[]);
        }
        catch (final AssertionError e) {
            LOG.info("AssertionError", e);
            throw getClass().getClassLoader().loadClass('com.canoo.webtest.engine.StepFailedException').newInstance(["Assertion failed within groovy code: " + script, step] as Object[]);
        }
        catch (final RuntimeException e) {
            LOG.error("RuntimeException", e);
            throw getClass().getClassLoader().loadClass('com.canoo.webtest.engine.StepExecutionException').newInstance(["Error invoking groovy: " + e.getMessage(), step, e] as Object[]);
        }
        finally {
            out.flush();
        }
    }

    /**
     * Gets the binding to use for step execution. Retrieve the binding for this webtest if one exists
     * or create a new one if this is the first groovy step of this webtest
     * @param step the current step
     * @return the binding to use for script execution
     */
    Binding getBinding(final step) {
        Binding binding = (Binding) step.getWebtestProperties().get(KEY_GROOVY_BINDING);
        if (binding == null) {
            LOG.info("No existing binding for this webtest, creating a new one");
            binding = new Binding();
            step.getWebtestProperties().put(KEY_GROOVY_BINDING, binding);
        }
        else {
            LOG.info("Reusing existing binding of this webtest.");
        }

        return binding;
    }

}