/*
 * Copyright 2004-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Gant script that runs all the webtests against a Grails application
 *
 * @author Graeme Rocher
 * @author Dierk Koenig
 * @author Bernd Schiffer
 * @author Stefan Roock
 *
 * @since 0.4
 */

Ant.property(environment: "env")
grailsHome = Ant.antProject.properties."env.GRAILS_HOME"

pluginHome = new File("./plugins").listFiles().find { it.name.startsWith("webtest")}
includeTargets << new File("${grailsHome}/scripts/RunApp.groovy")

startServer = true

target(default: "Run's all (or one) of the Web tests against a Grails application") {
    depends(checkForTests, classpath, checkVersion, packagePlugins, packageApp, generateWebXml)
    event("StatusUpdate", ["Running WebTest"])

    def argsSplitted = args?.tokenize()
    startServer = !(argsSplitted?.remove('-nostart'))
    if (!startServer) {
        event("StatusUpdate", ["Using existing server"])
    }
    if (argsSplitted) {
        System.setProperty('webtest.class.run.pattern', argsSplitted[0])
        event("StatusUpdate", ["Class filter: ${argsSplitted[0]}"])
        if (argsSplitted.size() > 1) {
            System.setProperty('webtest.test.run.pattern', argsSplitted[1])
            event("StatusUpdate", ["Test filter: ${argsSplitted[1]}"])
        }

    }

    Ant.property(file: './webtest/conf/webtest.properties')

    def systemPropOverride = System.getProperty('server.port')
    def newPortProp = Ant.antProject.properties.'wt.config.port'
    def serverPort = systemPropOverride ? systemPropOverride : (newPortProp ? newPortProp : Ant.antProject.properties.'webtest_port')
    runApp.serverPort = serverPort?.toInteger()

    def failed = false
    try {
        failed = runWebTest()
        event("StatusFinal", ["WebTest complete"])
    } catch (Throwable t) {
        failed = true
        event("StatusError", ["${t.class.name}: $t.message"])
        event("StatusFinal", ["WebTest error occurred"])
        throw t
    }
    finally {
        if (startServer) {
            stopServer()
        }
        if (failed) {
            event("StatusFinal", ["Build failed!!"])
            exit(1)
        }
    }
}

/** @return true when failed, false if successful %-/             **/
target(runWebTest: "Main implementation that executes a Grails' Web tests") {
    if (startServer) {
        depends(runApp)
    }

    Ant.ant(antfile: "${pluginHome}/scripts/call-webtest.xml") {
        property(name: 'pluginHome', value: pluginHome)
        property(name: 'grailsHome', value: grailsHome)
    }

    // Load the result file and determine whether any of the tests failed.
    Ant.property(file: "${basedir}/webtest/conf/webtest.properties")

    // map local (old) "webtest_*" props to new "wt.config.*" props for backward compatibility
    Ant.project.properties.findAll {it.key.startsWith('webtest_')}.each {key, value ->
        Ant.project.setUserProperty('wt.config.' + key - 'webtest_', value)
    }

    File resultFile = new File("${Ant.project.properties.'wt.config.resultpath'}/${Ant.project.properties.'wt.config.resultfile'}")
    if (!resultFile.exists()) {
        event("StatusError", ["Result file (${resultFile}) not found!"])
        return true
    }
    def xml = new XmlSlurper().parse(resultFile)
    def failedTests = xml.folder.summary.topsteps.@failed.text().toList()*.toInteger().sum()
    event("StatusUpdate", ["${failedTests} tests have failed"])
    return failedTests != 0
}

target(checkForTests: "Checks that there are WebTests to run and fails if not") {
    def tests = resolveResources("file:${basedir}/webtest/tests/**/*")
    if (!tests) {
        Ant.fail("WARNING: This project does not contain any WebTests.")
    }
}

