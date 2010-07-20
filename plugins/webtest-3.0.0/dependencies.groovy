grails.project.dependency.resolution = {
  inherits "global" // inherit Grails' default dependencies
  log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'

  repositories {
    grailsHome()
    mavenCentral()
  }
  
  dependencies {
    test('com.canoo.webtest:webtest:3.0') {
      excludes 'xalan' // IVY-1006 - use xalan 2.7.0 to avoid (see below)
      excludes 'xml-apis' // GROOVY-3356
    }

    test('xalan:xalan:2.7.0') {
      excludes 'xml-apis' // GROOVY-3356
    }
  }
}