grails.servlet.version = '3.0'
grails.project.work.dir = 'target'
grails.project.target.level = 1.7
grails.project.source.level = 1.7

grails.project.dependency.resolver = 'maven'
grails.project.dependency.resolution = {
	inherits('global') {
		excludes 'grails-docs' // gdocs are not used, and the dependency brings in an older iText jar in build scope
	}
	log 'warn'
	checksums true
	legacyResolve false

	repositories {
		inherits true
		mavenLocal()

		// TDS Private Repository
		mavenRepo 'https://tm-nexus.transitionmanager.net/repository/TransitionManager-Application-Libraries'

		grailsCentral()
		mavenCentral()
	}

	dependencies {
		compile 'com.google.guava:guava:19.0'
		compile 'net.sf.jasperreports:jasperreports:4.5.1'
		compile 'org.apache.poi:poi-ooxml:3.14'
		compile 'org.apache.poi:ooxml-schemas:1.1'
		compile 'org.apache.xmlbeans:xmlbeans:2.6.0'
		compile 'org.apache.poi:poi:3.14'
		compile 'xml-apis:xml-apis:1.4.01'

		// LIB JARS
		compile 'net.sourceforge.barbecue:barbecue:jar:1.5-beta1'
		compile 'org.codelibs:jcifs:1.3.18.3'
		compile 'net.nicholaswilliams.java.licensing:licensing-core:1.1.0'
		compile 'net.nicholaswilliams.java.licensing:licensing-licensor-base:1.1.0'

		// From the Private Repo TM-NEXUS
		compile 'com.barcodelib:barcode:1.0'

		compile 'org.glassfish.external:ant:3.0-b29'
		runtime 'mysql:mysql-connector-java:5.1.40'
		runtime 'org.apache.commons:commons-lang3:3.1'

		// HTTP Client used on integration agents e.g. HttpAgent, ServiceNow, VMware
		runtime 'org.apache.httpcomponents:httpclient:4.5.5'
		runtime 'commons-httpclient:commons-httpclient:3.1'

		// CSV Parser - https://github.com/xlson/groovycsv
		runtime 'com.xlson.groovycsv:groovycsv:1.2'

		/*
		 * GETL Groovy ETL - https://github.com/ascrus/getl/
		 * NOTE: we are using latest from the 1.2.x series since the latest library breaks our implementation
		 * TODO: If we are going to use the latest GETL version we need to fix our implementation (or the authors)
		 */
		runtime ('net.sourceforge.getl:getl:1.2.11') {
			excludes 'org.apache.hadoop:hadoop-hdfs'
			excludes 'org.apache.hadoop:hadoop-common'
		}

		test 'org.grails:grails-datastore-test-support:1.0.2-grails-2.4'
		test ('com.stehno.ersatz:ersatz:1.7.0') {
			excludes 'ch.qos.logback:logback-classic'
		}
	}

	plugins {
		build ':tomcat:8.0.33'

		compile ':jasper:1.11.0'
		compile ':quartz:1.0.2'
		compile ':scaffolding:2.1.2'
		compile ':spring-security-core:2.0.0'
		compile ':spring-security-ldap:2.0.1'
		compile ':spring-events:1.2'

		/*
		 * TODO: (oluna@tdsi.com) We have a compilation dependency for the Following plugin in the UnitTest,
		 *       it is safe to build the war file ignoring the test cases???
		 */
		compile ':greenmail:1.3.4'

		compile (':spring-security-rest:1.5.4') {
			// Remove old Guava files packed in spring-security-rest plugin
			excludes 'guava-io'
			/*
			 * removing the library that can cause a loop in the Log4J configuration
			 * @see  http://www.slf4j.org/legacy.html#log4j-over-slf4j
			 */
			excludes 'org.slf4j:log4j-over-slf4j'
		}

		compile ":rest-client-builder:2.1.0"

		compile "org.grails.plugins:cascade-validation:0.1.5"
		runtime "org.grails.plugins:asset-pipeline:2.14.1.1"

		runtime ':database-migration:1.4.0'
		runtime ':grails-melody:1.54.0'
		runtime ':hibernate4:4.3.10'
		runtime ':jqgrid:3.8.0.1'
		runtime ':jquery-ui:1.10.4'
		runtime ':jquery:1.11.1'
		runtime ':mail:1.0.7'
		runtime ':xss-sanitizer:0.4.0'

		test ':functional-test:1.2.7'

		// don't include plugins in production
		if ( Environment.current != Environment.PRODUCTION ) {
			compile ":console:1.5.12"
		}
	}


}

grails.war.resources = { stagingDir ->
	echo message: "StagingDir: $stagingDir"
	delete(verbose: true) {
		fileset(dir: stagingDir) {
			include name: '**/node_modules/**'
		}
	}
}
