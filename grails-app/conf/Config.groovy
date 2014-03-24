
// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts
grails.config.locations = []
// grails.config.locations = [ "classpath:${appName}-config.groovy" ]
// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]
// Load properties file that is passed in as an Java Startup argument
def appConfigLocation = System.properties["${appName}.config.location"]
if (appConfigLocation) {
	File f = new File(appConfigLocation)
	if ( f.exists() ) {
		grails.config.locations << "file:${appConfigLocation}"
	} else {
		// For whatever reason log.error if bombing here...
		println "Application properties file System.properties['${appName}.config.location'] defined as [${appConfigLocation}] is missing"
	}
}

grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.types = [ html: ['text/html','application/xhtml+xml'],
                      xml: ['text/xml', 'application/xml'],
                      text: 'text-plain',
                      js: 'text/javascript',
                      rss: 'application/rss+xml',
                      atom: 'application/atom+xml',
                      css: 'text/css',
                      csv: 'text/csv',
                      all: '*/*',
                      json: ['application/json','text/json'],
                      form: 'application/x-www-form-urlencoded',
                      multipartForm: 'multipart/form-data'
                    ]
// The default codec used to encode data with ${}
grails.views.default.codec="none" // none, html, base64
grails.views.gsp.encoding="UTF-8"
grails.converters.encoding="UTF-8"

// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true

// set pagination default (default was 10)
grails.pagination.default="20"
grails.pagination.max="20"

//
// Database Migrations Plugin Settings
// 
grails.plugin.databasemigration.changelogLocation = 'grails-app/migrations'
grails.plugin.databasemigration.changelogFileName='changelog.groovy'
grails.plugin.databasemigration.updateOnStart = true
grails.plugin.databasemigration.updateOnStartFileNames = ['changelog.groovy']
grails.plugin.databasemigration.dbDocController.enabled = true

//
// For Shiro Security Plugin
//
// jsecurity.legacy.filter.enabled = true
security.shiro.annotationdriven.enabled = true
// fix the strategy in Config.groovy to point to (http://groovy-grails.blogspot.com/search?q=shiro)
// security.shiro.authentication.strategy = new org.apache.shiro.authc.pam.AtLeastOneSuccessfulStrategy()
security.shiro.authentication.strategy = new org.apache.shiro.authc.pam.FirstSuccessfulStrategy()

//
// SendMail Configuration
//
grails {
	mail {
		host = "smtp.gmail.com"
		port = 465
		username = ""
		password = ""
		props = ["mail.smtp.auth":"true",
					"mail.smtp.socketFactory.port":"465",
					"mail.smtp.socketFactory.class":"javax.net.ssl.SSLSocketFactory",
					"mail.smtp.socketFactory.fallback":"false"
				]
	}
}
grails.mail.default.from = "TDS Transition Manager <tds.transition.manager@gmail.com>"

//
// log4J Logging Configuration
//
// Any custom logging configuration should be done by copying this whole definition into a local tdstm-config.groovy 
// configuration file in order to override this closure. When running locally, the logs will reside in the target directory
// and for Tomcat they will reside in the CATALINA_HOME/logs directory.  
// 
log4j = {
	// Configure classes to log at the various logging levels (defaulting to error)
	error 'org.codehaus.groovy.grails.web.servlet',  //  controllers
		  'org.codehaus.groovy.grails.web.pages', //  GSP
		  'org.codehaus.groovy.grails.web.sitemesh', //  layouts
		  'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
		  'org.codehaus.groovy.grails.web.mapping', // URL mapping
		  'org.codehaus.groovy.grails.commons', // core / classloading
		  'org.codehaus.groovy.grails.plugins', // plugins
		  'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
		  'org.springframework',
		  'net.sf.ehcache.hibernate'
	// trace 'org.hibernate'
	// debug 'org.hibernate'
	// info 'org.codehaus.groovy.grails.web.mapping' // URL mapping
	// off 'org.hibernate'
 
	appenders {
	   def appName = appName ?: "tdstm"	// If not defined (for local config)
	   def commonPattern = "%d{[EEE, dd-MMM-yyyy @ HH:mm:ss.SSS]} [%t] %-5p %c %x - %m%n"
	   def logDirectory = 'target'
	   if (System.properties.getProperty('catalina.base')) {
		   logDirectory = "${System.properties.getProperty('catalina.base')}/logs"
	   }
 
	   // Use this if we want to modify the default appender called 'stdout'.
	   console name:'stdout', layout:pattern(conversionPattern: '[%t] %-5p %c{2} %x - %m%n')
 
	   // Application log file
	   rollingFile name:'applicationLog',
			   file:"${logDirectory}/${appName}.log",
			   maxFileSize:'500MB',
			   maxBackupIndex:7
			   layout:pattern(conversionPattern: commonPattern)
 
	   // Stacktrace log file
	   // Use the 'null' line only, if we want to prevent creation of a stacktrace.log file.
	   // 'null' name:'stacktrace'
	   rollingFile name:'stacktraceLog',
			   file:"$logDirectory/${appName}-stacktrace.log",
			   maxFileSize:'500MB',
			   maxBackupIndex:7
			   layout:pattern(conversionPattern: commonPattern)
	}
	
	// Set the logging level for the various log files:
	info 'stdout', 'applicationLog'
	
	additivity.grails=false
	additivity.StackTrace=false
 }

//Maintenance file path
tdsops.maintModeFile = "/tmp/tdstm-maint.txt"
