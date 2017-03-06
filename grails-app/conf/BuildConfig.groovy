grails.plugin.location.'eav-model' = './pluginscustom/eav-model-0.1'

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
		mavenRepo('http://tmdevold.tdsops.com/grails-maven')
		mavenLocal()
		grailsCentral()
		mavenCentral()
	}

	dependencies {
		compile 'com.google.guava:guava:16.0.1'
		compile 'net.sf.jasperreports:jasperreports:4.5.1'
		compile 'org.apache.poi:poi-ooxml:3.14'
		compile 'org.apache.poi:ooxml-schemas:1.1'
		compile 'org.apache.xmlbeans:xmlbeans:2.6.0'
		compile 'org.apache.poi:poi:3.14'

		compile 'org.glassfish.external:ant:3.0-b29'
		runtime 'mysql:mysql-connector-java:5.1.40'
		runtime 'org.apache.commons:commons-lang3:3.1'

		// test 'com.canoo.webtest:webtest:3.0'
		test 'org.grails:grails-datastore-test-support:1.0.2-grails-2.4'
	}

	plugins {
		build ':tomcat:8.0.33'

//		compile ':filterpane:2.5.0', {
//			excludes 'asset-pipeline'
//		}
		compile ':jasper:1.11.0'
		compile ':quartz:1.0.2'
		compile ':plugin-config:0.2.0'
		compile ':ref-code:0.3.0'
		compile ':scaffolding:2.1.2'
		compile ':spring-security-core:2.0.0'
		compile ':spring-security-ldap:2.0.1'
		// Added the acl for some testing JPM 12/2016
		compile ':spring-security-acl:2.0.0'


		runtime ':database-migration:1.4.0'
		runtime ':grails-melody:1.54.0'
		runtime ':hibernate4:4.3.10'
		runtime ':jmesa:2.0.4-SNAPSHOT-0.1'
		runtime ':jqgrid:3.8.0.1'
		runtime ':jquery-ui:1.8.15'
		runtime ':jquery:1.11.1'
		runtime ':mail:1.0.7'
		runtime ':resources:1.2.14' // TODO ':asset-pipeline:2.9.1'

		test ':functional-test:1.2.7'
	}
}
