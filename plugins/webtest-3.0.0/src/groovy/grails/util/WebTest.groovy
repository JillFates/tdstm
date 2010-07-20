package grails.util

import org.apache.commons.logging.LogFactory
import java.lang.reflect.Method
import org.codehaus.groovy.grails.plugins.GrailsPluginUtils
import org.codehaus.groovy.runtime.StackTraceUtils
import org.codehaus.groovy.grails.commons.cfg.ConfigurationHelper
import org.grails.plugins.webtest.WebTestCase

/**
 * This class allows for running WebTests outside of the Grails test-app framework
 */
class WebTest extends WebTestCase {

    public final static WEBTEST_DIR = 'test/webtest'

    def log = LogFactory.getLog(this.class)

    /** @deprecated not used any longer (only there for backward compatibility with older test bases)                            */
    def configMap

    /**
     * @deprecated no longer needed since 0.6
     */
    void webtest(String name, Closure yield) {
        yield()
    }

    /**
     * Suite method which runs all methods starting with 'test'
     */
    void suite() {
        boolean foundTest = false

        this.class.methods.sort {m -> m.name}.each {Method method ->
            def methodName = method.name
            if (methodName.startsWith('test') && shouldRunTest(methodName, this.class.name)) {
                if (!foundTest) {
                    webTestMethodIfExists("classSetUp")
                    foundTest = true
                }
                webTestMethod(method, true)
            }
        }
        if (foundTest) {
            webTestMethodIfExists("classTearDown")
        }
    }

    /** Main entry point to run a single test.                            */
    void runTests(args) {
        if (!ant) {
            ant = new AntBuilder()
        }
        prepare()
        suite() // template method call
        finish()
    }

    Map initProps() {
        BuildSettings build = loadBuildSettings()

        // obey local properties file if available
        File propFile = new File("${build.getBaseDir().absolutePath}/test/webtest/conf/webtest.properties")
        if (propFile.exists()) {
            ant.property(file: propFile)
            println propFile.absolutePath + " added."
        } else {
            println propFile.absolutePath + " not found: running without."
        }

        // Load the application properties.
        ant.property(file: 'application.properties')

        def props = ant.project.properties
        props.projectName = new File('.').absolutePath.tokenize('./\\')[-1]


        ConfigurationHelper.loadConfigFromClasspath()

        // map local (old) "webtest_*" props to new "wt.config.*" props for backward compatibility
        props.findAll {it.key.startsWith('webtest_')}.each {key, value ->
            ant.project.setUserProperty('wt.config.' + key - 'webtest_', value)
        }

        if (props.'wt.config.basepath' == null) {
            props.'wt.config.basepath' = props.'app.name'
        }

        if (props.'wt.config.resultpath' == null) {
            props.'wt.config.resultpath' = (build.testReportsDir?.absolutePath ?: (build.baseDir + '/test/reports')) + '/webtest'
        }

        println 'Testing ' + props.'app.name' + ' deployed as ' + props.'wt.config.basepath'

        String portOverride = System.getProperty('server.port')
        if (portOverride) {
            props.'wt.config.port' = portOverride
            println "Overriding server port to ${portOverride}"
        }

        props.each {key, value ->
            ant.project.setUserProperty(key, value)
        }

        return props
    }

    private def loadBuildSettings() {
        String baseDir = System.getProperty("base.dir");
        BuildSettings build = new BuildSettings(null, new File(baseDir));
        build.loadConfig()
        def pluginDir = build.getProjectPluginsDir()
        if (!pluginDir.exists()) {
            pluginDir = new File(baseDir, 'plugins')
            if (!pluginDir.exists()) {
                throw new RuntimeException("Unable to find plugin dir. Please set via BuildSettings.groovy")
            } else {
                build.setProjectPluginsDir(pluginDir)
            }
        }
        BuildSettingsHolder.setSettings(build)
        return vuild
    }

// prepare the ant taskdef, classpath and filesystem for reporting

    void prepare() {
        def props = initProps()

        ant.project.executeTarget 'wt.before.testInWork'

        //This allows us to override the normal one so groovy steps can access domain classes etc
        def webtestPluginDir = GrailsPluginUtils.getPluginDirForName("webtest").path
        def gstep = new File("${webtestPluginDir}/src/runtime/org/grails/plugins/webtest/GroovyStep.groovy")
        def gcl = new GroovyClassLoader(getClass().classLoader)
        ant.project.addTaskDefinition('groovy', gcl.parseClass(gstep));

        if (System.getProperty('wt.headless')) {
            println 'Running WebTest in headless mode...'
        }
        registerSteps(gcl)
    }

    private void registerSteps(ClassLoader cl) {
        def scanner = ant.fileScanner {
            fileset(dir: 'test/webtest', includes: '**/*Step.groovy')
        }
        if (scanner.hasFiles()) {
            event("StatusUpdate", ["Found custom steps"])
        }
        for (file in scanner) {
            Class stepClass = cl.parseClass(file)
            def m = (stepClass.name =~ /(.*\.)?(.*?)Step$/)
            String stepName = m[0][2]
            stepName = stepName[0].toLowerCase() + stepName[1..-1]
            ant.project.addTaskDefinition(stepName, stepClass);
            event("StatusUpdate", ["Registered custom step: ${stepName}"])
        }
    }

    def finish() {
        ant.project.executeTarget 'wt.after.testInWork'
    }

    def ifMethod(String methodName, Closure closure) {
        try {
            Method method = this.class.getMethod(methodName)
            closure.call(method)
        } catch (NoSuchMethodException e) {
        }
    }

    def webTestMethodIfExists(String methodName) {
        ifMethod(methodName) { webTestMethod(it, false) }
    }

    def runMethodInsideGroupIfExists(String methodName) {
        ifMethod(methodName) {method -> group(description: methodName) { method.invoke(this, null) } }
    }

    def runMethodRawIfExists(String methodName) {
        ifMethod(methodName) {method -> method.invoke(this, null) }
    }

    def webTestMethod(method, setUpAndTearDown) {
        ant.webtest(name: this.class.name + "." + method.getName()) {
            try {
                if (setUpAndTearDown) { runMethodRawIfExists("setUp") }
                method.invoke(this, null)
                if (setUpAndTearDown) { runMethodInsideGroupIfExists("tearDown()") }
            } catch (Throwable e) {
                StackTraceUtils.deepSanitize(e);
                LogFactory.getLog(this.class).error('Unable to invoke test method ' + method.name, e)
                def wrapped = this.class.classLoader.loadClass('com.canoo.webtest.engine.StepExecutionException').newInstance(["Unable to invoke test method ${method.name}", e] as Object[])
                throw wrapped;
            }
        }
    }

    def shouldRunTest(testName, className) {
        def testPattern = System.getProperty('webtest.test.run.pattern')
        if (testPattern && !(testName =~ testPattern)) {
            log.debug("Running ${className}.$testName as it does not match the test pattern: $testPattern")
            return false;
        }

        def classPattern = System.getProperty('webtest.class.run.pattern')
        if (classPattern && !(className =~ classPattern)) {
            log.debug("Running ${className}.$testName as it does not match the class pattern: $classPattern")
            return false;
        }
        return true;

    }

}
