def webtestAnt = new AntBuilder()

eventAllTestsStart = {
    if (getBinding().variables.containsKey("functionalTests")) {
        functionalTests << "webtest"
    }
}

eventTestPhaseStart = {phase ->
    if (phase == 'functional') {
        event("StatusUpdate", ['Loading WebTest configuration'])

        def testClassLoader = Thread.currentThread().contextClassLoader

        File propFile = new File("${basedir}/test/webtest/conf/webtest.properties")
        if (propFile.exists()) {
            webtestAnt.property(file: propFile)
            event("StatusUpdate", [propFile.absolutePath + " added."])
        } else {
            event("StatusUpdate", [propFile.absolutePath + " not found: running without."])
        }
        // Load the application properties.
        webtestAnt.property(file: 'application.properties')
        webtestAnt.project.properties.findAll {it.key.startsWith('webtest_')}.each {key, value ->
            webtestAnt.project.setUserProperty('wt.config.' + key - 'webtest_', value)
        }

        def props = webtestAnt.project.properties
        if (props.'wt.config.basepath' == null) {
            props.'wt.config.basepath' = props.'app.name'
        }

        if (props.'wt.config.resultpath' == null) {
            props.'wt.config.resultpath' = (grailsSettings.testReportsDir?.absolutePath ?: (basedir + '/test/reports')) + '/webtest'
        }

        event("StatusUpdate", ['Testing ' + props.'app.name' + ' deployed as ' + props.'wt.config.basepath'])
                
        def webtestJarFile = grailsSettings.testDependencies.find { it.name ==~ /webtest-.+(?<!sources)\.jar/ }
        assert webtestJarFile : "couldn't find webtest jar"
        
        def webtestJarExplodedDir = new File(grailsSettings.projectWorkDir, "webtest-jar")
        if (!webtestJarExplodedDir.exists()) {
            assert webtestJarExplodedDir.mkdirs() : "couldn't make dir for webtest jar explosion!"

            def expandTo = webtestJarExplodedDir
            def jar = new java.util.jar.JarFile(webtestJarFile)
            for (jarEntry in jar.entries()) {
                def destination = new File(expandTo.path + File.separator + jarEntry.name)
                def parent = destination.parentFile
                if (!parent.exists()) {
                    parent.mkdirs()
                }
                if (jarEntry.directory) {
                    destination.mkdir()
                } else {
                    destination.createNewFile()
                    destination << jar.getInputStream(jarEntry)
                }
            }
        }

        def webTestHome = new File(webtestJarExplodedDir.path + File.separator + "com/canoo/webtest/resources")
        webtestAnt.project.setUserProperty('wt.config.home', webTestHome.absolutePath)
        
        def webTestHomeLib = new File(webTestHome, "lib")
        if (!webTestHomeLib.exists()) {
            assert webTestHomeLib.mkdirs()
            def webTestHomeLibWebTestJar = new File(webTestHomeLib, "webtest.jar")
            webTestHomeLibWebTestJar.createNewFile()
            webtestJarFile.withInputStream {
                webTestHomeLibWebTestJar << it
            }
        }
        
        def webtestXmlFile = new File(webTestHome, "webtest.xml")
        webtestAnt.'import'(file: webtestXmlFile.absolutePath)   // sets properties into current ant project

        if (!isInteractive || argsMap.headless) {
            event("StatusUpdate", ["Running in headless mode"])
            webtestAnt.project.setUserProperty('wt.headless', 'true')
        }

        String portOverride = System.getProperty('server.port')
        if (portOverride) {
            props.'wt.config.port' = portOverride
            event("StatusUpdate", ["Overriding server port in config to ${portOverride}"])
        }

        props.each {key, value ->
            webtestAnt.project.setUserProperty(key, value)
        }


        def webTestCaseClass = testClassLoader.loadClass('org.grails.plugins.webtest.WebTestCase')
        def listener = testClassLoader.loadClass('org.grails.plugins.webtest.WebTestPluginBuildListener').newInstance()
        webTestCaseClass.ant = webtestAnt

        webTestCaseClass.buildListener = listener
        webtestAnt.project.addBuildListener(listener)

    }

}

eventTestSuiteStart = {type ->
    if (type == "webtest") {
        event("StatusUpdate", ["Registering steps and initialising WebTest..."])
        def testClassLoader = Thread.currentThread().contextClassLoader
        def cl = new GroovyClassLoader(testClassLoader)
        registerSteps(webtestAnt, cl)

        webtestAnt.project.executeTarget 'wt.before.testInWork'

        //This allows us to override the normal one so groovy steps can access domain classes etc
        def gstep = new File("${webtestPluginDir}/src/runtime/org/grails/plugins/webtest/GroovyStep.groovy")
        webtestAnt.project.addTaskDefinition('groovy', cl.parseClass(gstep));

        def junitResult = new File(webtestAnt.project.properties.'wt.junitLikeReports.file' ?: 'dummyfile')
        if (junitResult.exists()) {
            junitResult.delete()
        }
    }
}

private void registerSteps(webtestAnt, ClassLoader cl) {
    def scanner = webtestAnt.fileScanner {
        fileset(dir: 'test/webtest', includes: '**/*Step.groovy')
    }
    if (scanner.hasFiles()) {
        event("StatusUpdate", ["Found custom steps"])
    }

    cl.addURL(new File(grailsSettings.testClassesDir, 'webtest').toURL());

    for (file in scanner) {
        try {
            Class stepClass = cl.parseClass(file)
            def m = (stepClass.name =~ /(.*\.)?(.*?)Step$/)
            String stepName = m[0][2]
            stepName = stepName[0].toLowerCase() + stepName[1..-1]
            webtestAnt.project.addTaskDefinition(stepName, stepClass);
            event("StatusUpdate", ["Registered custom step: ${stepName}"])
        } catch (Exception e) {
            event("StatusError", ["Failed to parse class: ${file}"])
            throw e
        }
    }
}

eventTestSuiteEnd = {type ->
    if (type == "webtest") {
        try {
            webtestAnt.project.executeTarget 'wt.after.testInWork'
        } catch (Exception e) {
            event("StatusError", ["WebTests failed: ${e}"])
        }
    }
}
