package grails.util

import org.apache.commons.logging.LogFactory
import java.lang.reflect.Method

/** Superclass skeleton for fuctional tests.
  * Subclasses can override suite() method.
  */
class WebTest {

    public final static WEBTEST_DIR = 'webtest/tests'

    def ant = new AntBuilder()    // may be initialized lazily for memory opt. in subclasses

    def log = LogFactory.getLog(this.class)

    /** @deprecated not used any longer (only there for backward compatibility with older test bases)      */
    def configMap

  /**
   * @deprecated no longer needed since 0.6
   */
    void webtest(String name, Closure yield) {
        ant.webtest(name: name) {
            steps {
                yield.delegate = ant
                yield()
            }
        }
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

    /** Main entry point to run a single test.      */
    void runTests(args) {
        prepare()
        suite() // template method call
        finish()
    }

    Map initProps() {
        String portOverride = System.getProperty('server.port')
        if (portOverride) {
            ant.property(name: 'wt.config.port', value: portOverride)
            println "Overriding server port to ${portOverride}"
        }

        // obey local properties file if available
        File propFile = new File('webtest/conf/webtest.properties')
        if (propFile.exists()) {
            ant.property(file: propFile)
            println propFile.absolutePath + " added."
        } else {
            println propFile.absolutePath + " not found: running without."
        }

        // Load the application properties.
        ant.property(file: 'application.properties')

        def props = ant.project.properties

        // find dir names that change with installation root and plugin version
        def pluginHome = new File("./plugins").listFiles().find { it.name.startsWith("webtest")}
        props.webtestHome = new File("webtest/home").absolutePath
        props.projectName = new File('.').absolutePath.tokenize('./\\')[-1]
        if (!props.webtest_basepath) props.webtest_basepath = props.'app.name'
        println 'Testing ' + props.'app.name'
        return props
    }

    // prepare the ant taskdef, classpath and filesystem for reporting
    void prepare() {
        def props = initProps()
        // map local (old) "webtest_*" props to new "wt.config.*" props for backward compatibility
        props.findAll {it.key.startsWith('webtest_')}.each {key, value ->
            ant.project.setUserProperty('wt.config.' + key - 'webtest_', value)
        }

        def webtestXmlFile = new File("${props.webtestHome}/webtest.xml")
        ant.'import'(file: webtestXmlFile.absolutePath)   // sets properties into current ant project
        ant.project.executeTarget 'wt.before.testInWork'
        if (System.getProperty('wt.headless')) {
            println 'Running WebTest in headless mode...'
        }
        registerSteps()
    }

    private void registerSteps() {
        def scanner = ant.fileScanner {
            fileset(dir: 'webtest/tests', includes: '**/*Step.groovy')
        }
        for (file in scanner) {
            Class stepClass = getClass().classLoader.parseClass(file)
            def m = (stepClass.name =~ /(.*\.)?(.*?)Step$/)
            String stepName = m[0][2]
            stepName = stepName[0].toLowerCase() + stepName[1..-1]
            ant.project.addTaskDefinition(stepName, stepClass);
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

    def methodMissing(String name, args) {
        ant.invokeMethod(name, args)
    }

}
