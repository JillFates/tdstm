import org.codehaus.groovy.grails.plugins.GrailsPluginUtils

// Unpack the webtest distribution into "webtest/home", after removing
// any existing WebTest installation.

includeTargets << grailsScript("_PluginDependencies")

target(install: 'downloads webtest') {
    depends(parseArguments)

    def oldWebtestLocation = new File("${basedir}/webtest")
    def oldTestLocation = new File(oldWebtestLocation, "tests")
    if (oldWebtestLocation.exists()) {        
        println '''
############## ############## ############## ##############

There are manual steps required when upgrading from 1.1.x to 1.2.x. Please read the release notes at http://refactor.com.au/blog/grails-webtest-plugin-120-released

############## ############## ############## ##############
'''
        def move = true
        if (isInteractive) {
            move = confirmInput('The location used to store webtests has changed from webtest/tests to test/webtest. Would like the plugin to move your tests and configuration?', 'move.confirm')
        }
        if (move) {
            Ant.copy(toDir: new File("${basedir}/test/webtest").absolutePath, verbose: true, overwrite: false,
                    failOnError: false) {
                fileset(dir: oldTestLocation.absolutePath, includes: '**/*')
                fileset(dir: oldWebtestLocation.absolutePath, excludes: oldTestLocation.name + '/**/*')
            }
            Ant.delete(dir: oldWebtestLocation.absolutePath, failOnError: false, verbose: true)
        } else {
            println '#### YOU MUST MANUALLY MOVE YOUR TESTS TO THE TEST/WEBTEST FOLDER ####'
        }
    }

    def pluginHome = webtestPluginDir.path
    Ant.copy(todir: "${basedir}", overwrite: false) {
        fileset(dir: "${pluginHome}/src/templates", includes: "test/webtest/**/*")
    }

    Ant.delete(failOnError: false, verbose: true) {
        fileset(dir: grailsSettings.grailsWorkDir.absolutePath + '/scriptCache', includes: "*Webtest*")
    }
}

install()
