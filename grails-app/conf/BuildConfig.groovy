/*
 * Since 1.1, Grails no longer stores plugins inside your PROJECT_HOME/plugins  directory by default.
 */
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.plugins.dir="./plugins"

grails.project.dependency.resolution = {
	// inherit Grails' default dependencies
	inherits("global") {
	}
	
	log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'

	repositories {
		mavenRepo("http://tmdev.tdsops.com/grails-maven")
		grailsPlugins()
		grailsHome()
		grailsCentral()
	}
	
	dependencies {
		// specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
	}
  
	plugins {
		runtime ':database-migration:1.0'
		runtime ':shiro:1.1.4'
		// compile ':mongodb:1.0.0.RC4'
	}
}
