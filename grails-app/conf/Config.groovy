import grails.util.Environment

tdstm.license = [
		password: "sampleKey1984",
		enabled : true
]

// This will add a CRLF so that follow logging in dev mode is legible and not overwriting other log statements
println ""

// copy binding variables into properties in the config for visibility in external scripts; as of 2.5.4 the
// vars are: appName, appVersion, basedir, baseFile, baseName, grailsHome,
//           grailsSettings, grailsVersion, groovyVersion, springVersion, userHome
getBinding().variables.each { name, value -> setProperty name, value }
String appName = this.appName ?: null

grails.config.locations = []

List candidates = []
String configFileFromJavaOpts = System.getProperty("${appName}.config.location")
if (configFileFromJavaOpts) candidates << configFileFromJavaOpts
candidates << "$userHome/.grails/${appName}-config.groovy"

boolean foundAppConfig=false
for (appConfigLocation in candidates) {
	File f
	try {
		f = new File(appConfigLocation)
	}
	catch (e) {
		throw new IllegalArgumentException("ERROR Invalid application configuration file path '$appConfigLocation'")
	}

	if (!f.exists()) continue

	try {
		// Test that there are no errors in the config syntax
		new ConfigSlurper(Environment.current.name).parse(f.toURI().toURL())
	}
	catch (e) {
		throw new IllegalArgumentException("ERROR There appears to be an error in the $appConfigLocation application configuration file: $e.message")
	}

	grails.config.locations << 'file:' + appConfigLocation
	println "INFO The application config was loaded from ${appConfigLocation}"

	foundAppConfig = true
}
if (!foundAppConfig) {
	throw new IllegalArgumentException("The application configuration file was not found in the following locations: ${candidates}")
}

grails {
	controllers.defaultScope = 'singleton'
	converters.encoding = 'UTF-8'
	enable.native2ascii = true
	exceptionresolver.params.exclude = ['password']
	hibernate {
		cache.queries = false
		osiv.readonly = false
		pass.readonly = false
	}
	json.legacy.builder = false

	mail.default.from = "TDS Transition Manager <tds.transition.manager@gmail.com>"
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

	mime {
		disable.accept.header.userAgents = ['Gecko', 'WebKit', 'Presto', 'Trident']
		file.extensions = true // enables the parsing of file extensions from URLs into the request format
		types = [
			all:            '*/*',
			atom:           'application/atom+xml',
			css:            'text/css',
			csv:            'text/csv',
			form:           'application/x-www-form-urlencoded',
			hal:           ['application/hal+json','application/hal+xml'],
			html:          ['text/html','application/xhtml+xml'],
			js:             'text/javascript',
			json:          ['application/json', 'text/json'],
			multipartForm:  'multipart/form-data',
			rss:            'application/rss+xml',
			text:           'text/plain',
			xls :           'application/vnd.ms-excel',
			xlsx:           'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
			xml:           ['text/xml', 'application/xml']
		]
	}
	pagination.default = "20"
	pagination.max = "20"

	plugin {
		databasemigration {
			changelogFileName='changelog.groovy'
			changelogLocation = 'grails-app/migrations'
			dbDocController.enabled = true
			updateOnStart = true
			updateOnStartFileNames = ['changelog.groovy']
		}
	}

	project.groupId = appName
	resources {
		adhoc {
			excludes = ['*', '**/WEB-INF/**','**/META-INF/**']
			includes = ['/components/**', '/css/**', '/fonts/**', '/i/**', '/icons/**', '/images/**',
			            '/js/**', '/app-js/**', '/i18n/**', '/test/**', '/reports/**', '/resource/**', '/static/**', '/swf/**', '/templates/**',
			            '/plugins/**', '/d3/**', '/dist/**']
			patterns = []
		}
		rewrite.css = false
	}
	scaffolding.templates.domainSuffix = 'Instance'
	spring.bean.packages = []
	stringchararrayaccessor.disabled = true // Eliminates warning error - see TM-3681
	views.default.codec = 'none'
	views {
		gsp {
			codecs {
				expression = 'html'
				scriptlet = 'html'
				staticparts = 'none'
				taglib = 'none'
			}
			encoding = 'UTF-8'
			gsp.sitemesh.preprocess = true
			htmlcodec = 'xml'
		}
	}
	web.disable.multipart=false
}

environments {
	development {
		grails {
			logging.jul.usebridge = true
			serverURL = 'http://localhost:8080/tdstm'
		}
	}
	test {
		// used for testing
		tdstm {
			testing {
				foo {
					intVal = 123
					stringVal = 'abc'
					configVal {
						hasSomeProp = true
					}
				}
			}
		}
	}
	production {
		grails {
			logging.jul.usebridge = false
			// TODO serverURL = 'http://www.changeme.com'
		}
	}
}

// log4J Logging Configuration
//
// Any custom logging configuration should be done by copying this whole definition into a local tdstm-config.groovy
// configuration file in order to override this closure. When running locally, the logs will reside in the target directory
// and for Tomcat they will reside in the CATALINA_HOME/logs directory.
//

String commonPattern = "%d{[EEE, dd-MMM-yyyy @ HH:mm:ss.SSS]} [%t] %-5p %c %x - %m%n"
String auditPattern = "%d{[EEE, dd-MMM-yyyy @ HH:mm:ss.SSS]} - %m%n"
String catalinaBase = System.getProperty('catalina.base')
String logDirectory = catalinaBase ? catalinaBase + '/logs' : 'target'

log4j.main = {
	appenders {
		// Use this if we want to modify the default appender called 'stdout'.
		console name: 'stdout', layout: pattern(conversionPattern: '[%t] %-5p %c{2} %x - %m%n')

		// Application log file
		rollingFile name: 'applicationLog',
			file: "$logDirectory/${appName}.log",
			maxFileSize: '500MB',
			maxBackupIndex: 7,
			layout: pattern(conversionPattern: commonPattern)

		// Audit log file
		rollingFile name: 'auditLog',
			file: "$logDirectory/${appName}-audit.log",
			maxFileSize: '500MB',
			maxBackupIndex: 7,
			layout: pattern(conversionPattern: auditPattern)

		// Stacktrace log file
		// Use the 'null' line only, if we want to prevent creation of a stacktrace.log file.
		// 'null' name: 'stacktrace'
		rollingFile name: 'stacktraceLog',
			file: "$logDirectory/${appName}-stacktrace.log",
			maxFileSize: '500MB',
			maxBackupIndex: 7,
			layout: pattern(conversionPattern: commonPattern)
	}

	error 'org.codehaus.groovy.grails',
	      'org.hibernate',
	      'org.springframework',
	      'net.sf.ehcache.hibernate',
	      'grails.app.services.org.grails.plugin.resource',
	      'grails.app.taglib.org.grails.plugin.resource',
	      'grails.app.resourceMappers.org.grails.plugin.resource'

	root {
		info 'stdout', 'applicationLog'
		additivity: true
	}

	// Send debug logging to application log (and console since additivity is true)
	debug  applicationLog: 'grails.app', additivity: true

	// Setup Audit Logging messages to go to their own log file in addition to the application log
	info  auditLog: 'net.transitionmanager.service.AuditService', additivity: true

	//additivity.grails=false
	//additivity.StackTrace=false
}

//Maintenance file path
tdsops.maintModeFile = "/tmp/tdstm-maint.txt"

//Build number file path
tdsops.buildFile = "/build.txt"

// Audit configuration, valid options are: access and activity (default is access)
// access: logging will include login, logout and security violations
// activity:  will also include all user interactions with the application.
//tdstm.security.auditLogging = "access"


grails {
	plugin {
		springsecurity {
			adh {
				errorPage = '/auth/unauthorized'
				useForward = false
			}
			apf {
				filterProcessesUrl = '/auth/signIn'
				usernameParameter = 'username'
				passwordParameter = 'password'
			}
			auth.loginFormUrl = '/auth/login'
			dao.hideUserNotFoundExceptions = false
			failureHandler.defaultFailureUrl = '/auth/login'

			controllerAnnotations.staticRules = [
				'/'					:'permitAll',
				'/index'			:'permitAll',
				'/index.gsp'		:'permitAll',
				'/assets/**'		:'permitAll',
				'/**/js/**'			:'permitAll',
				'/**/css/**'		:'permitAll',
				'/**/images/**'		:'permitAll',
				'/i/**'				:'permitAll',
				'/**/icons/**'		:'permitAll',
				'/**/favicon.ico'	:'permitAll',
				'/app-js/**'		:'permitAll', // Angular - resources
				'/i18n/**'			:'permitAll', // Angular - Translate
				'/test/**'			:'permitAll', // Angular - Test
				'/monitoring'		:'hasPermission(request, "ViewAdminTools")',
				'/components/**'	:'permitAll',
				'/templates/**' 	:'permitAll',
			]
		}
	}
}
