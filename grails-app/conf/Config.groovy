import grails.util.Environment
import net.transitionmanager.security.Permission

/**
 * WARNING! CERTAIN SECTIONS OF THIS FILE SHOULD NOT BE MODIFIED:
 * 	- log4j
 * 	- mail settings
 * 	-
 *
 * Instead do those changes in the configuration file "tdstm-config.groovy.template" of the application
 */

// This will add a CRLF so that follow logging in dev mode is legible and not overwriting other log statements
println()

// copy binding variables into properties in the config for visibility in external scripts; as of 2.5.4 the
// vars are: appName, appVersion, basedir, baseFile, baseName, grailsHome,
//           grailsSettings, grailsVersion, groovyVersion, springVersion, userHome
getBinding().variables.each { name, value -> setProperty name, value }
String appName = this.appName ?: null

grails.config.locations = []

// Change the bindData default so that it doesn't automatically convert blanks to null when storing in the database
// See http://docs.grails.org/latest/guide/theWebLayer.html#dataBinding
grails.databinding.convertEmptyStringsToNull = false

List candidates = []
String configManagerFile = null
String configFileFromJavaOpts = System.getProperty("${appName}.config.location")

if (configFileFromJavaOpts){
	candidates << configFileFromJavaOpts
	try{ //get the possible Manager config File
		File f = new File(configFileFromJavaOpts)
		configManagerFile = "${f.getParent()}/licman-config.groovy"
	}catch(e){
		//configuration not found fails silently??
	}
}

//check if the manager config file was injected on the JavaOPTS
configManagerFile = System.getProperty("licManConf") ?: configManagerFile
if(configManagerFile){
	candidates << configManagerFile
}

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
	if (Environment.current.name == 'test') {
		println "ERROR The application configuration file was not found in the following locations: ${candidates}"
	} else {
		throw new IllegalArgumentException("The application configuration file was not found in the following locations: ${candidates}")
	}
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

			//mail.port = com.icegreen.greenmail.util.ServerSetupTest.SMTP.port
			/*
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
			*/
		}

		//used for testing email
		grails.mail.port = com.icegreen.greenmail.util.ServerSetupTest.SMTP.port
		grails.plugin.greenmail.ports.smtp = 2025
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

		grails.serverURL = 'http://localhost:8080/tdstm'

		//used for testing email
		grails.mail.port = com.icegreen.greenmail.util.ServerSetupTest.SMTP.port
		grails.plugin.greenmail.ports.smtp = 2025
	}
	production {
		greenmail.disabled = true
		grails {
			logging.jul.usebridge = false
			// TODO serverURL = 'http://www.changeme.com'

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
	}
}

/********************************************************
** log4J Logging Configuration  (Basic configuration al INFO LEVEL)
**
** Any custom logging configuration should be done by copying this whole definition into a local tdstm-config.groovy
** configuration file in order to override this closure. When running locally, the logs will reside in the target directory
** and for Tomcat they will reside in the CATALINA_HOME/logs directory.
*********************************************************/
/* // THIS IS FOUND IN THE "tdstm-config.groovy.template" of the application
log4j = {
	// Set level for all application artifacts
	info	'grails.app'

	// enable *debug* to track security issues
	info	'grails.plugin.springsecurity',
			'org.springframework.security'

	//   'controllers.AuthController'
	//   'org.hibernate.SQL'
	warn	'org.codehaus.groovy.grails.web.servlet',			// controllers
			'org.codehaus.groovy.grails.web.pages',				// GSP
			'org.codehaus.groovy.grails.web.sitemesh',			// layouts
			'org.codehaus.groovy.grails.web.mapping.filter',	// URL mapping
			'org.codehaus.groovy.grails.web.mapping',			// URL mapping
			'org.codehaus.groovy.grails.commons',				// core / classloading
			'org.codehaus.groovy.grails.plugins',       		// plugins
			'org.codehaus.groovy.grails.orm.hibernate',			// hibernate integration
			'org.codehaus.groovy.grails',      					// Most of all grails code base
			'org.apache.jasper',
			'org.grails',
			'grails.app.services.org.grails.plugin.resource',
			'org.codehaus.groovy.grails.domain.GrailsDomainClassCleaner',
			'grails.app.taglib.org.grails.plugin.resource',
			'grails.app.resourceMappers.org.grails.plugin.resource',
			'grails.spring.BeanBuilder',
			'org.hibernate',
			'org.quartz',
			'grails.plugins.quartz.QuartzGrailsPlugin',
			'org.apache.catalina',
			'org.apache.coyote',
			'org.apache.naming',
			'net.sf.ehcache',
			'net.sf.ehcache.hibernate',
			'org.springframework',
			'grails.plugin.databasemigration.GrailsChangeLogParser'

	error	'org.hibernate.hql.internal.ast.HqlSqlWalker',
			'grails.plugin.hibernate4',
			'org.apache.tomcat',
			'liquibase',
			'net.bull.javamelody'

	// ** Enable Hibernate SQL logging with param values *********
	// trace 'org.hibernate.type'
	// debug 'org.hibernate.SQL'

	appenders {
		String logAppName = appName ?: 'tdstm'    // If not defined (for local config)
		String commonPattern = '%d{ISO8601} [%t] %-5p %c %x - %m%n'
		String auditPattern = '%d{ISO8601} - %m%n'
		String catalinaBase = System.getProperty('catalina.base')
		String logDirectory = 'target'

		if (catalinaBase) {
			logDirectory = "${catalinaBase}/logs"
		}

		// Use this if we want to modify the default appender called 'stdout'.
		console name:'stdout', layout:pattern(conversionPattern: '[%t] %-5p %c{2} %x - %m%n')

		// Application log file
		file (
			name:'applicationLog',
			file:"${logDirectory}/${logAppName}.log",
			layout:pattern(conversionPattern: commonPattern)
		)

		// Audit log file
		file (
			name:'auditLog',
			file:"$logDirectory/${logAppName}-audit.log",
			layout:pattern(conversionPattern: auditPattern)
		)

		// Disable the Stacktrace
		'null' name:'stacktrace'
	}

	root {
		info 'stdout', 'applicationLog'
	}

	// Send debug logging to application log (and console since additivity is true)
	debug  applicationLog: 'grails.app', additivity: true

	// Setup Audit Logging messages to go to their own log file in addition to the application log
	info  auditLog: 'net.transitionmanager.service.AuditService', additivity: true
}
*/

//Maintenance file path
tdsops.maintModeFile = "/tmp/tdstm-maint.txt"

//Build number file path
tdsops.buildFile = "/build.txt"

// Audit configuration, valid options are: access and activity (default is access)
// access: logging will include login, logout and security violations
// activity:  will also include all user interactions with the application.
//tdstm.security.auditLogging = "access"

tdstm {

}

grails {
	plugin {
		springsecurity {
			// Refer to spring security REST Plugin configuration:
			// http://alvarosanchez.github.io/grails-spring-security-rest/1.5.4/docs/guide/single.html#tokenValidation
			filterChain.chainMap = [
					'/api/projects/heartbeat':'anonymousAuthenticationFilter,restTokenValidationFilter,restExceptionTranslationFilter,filterInvocationInterceptor',
					'/api/**': 'JOINED_FILTERS,-anonymousAuthenticationFilter,-exceptionTranslationFilter,-authenticationProcessingFilter,-securityContextPersistenceFilter,-rememberMeAuthenticationFilter',  // Stateless chain
					'/**': 'JOINED_FILTERS,-restTokenValidationFilter,-restExceptionTranslationFilter' // Traditional chain
			]

			// Refer to information on these settings please refer to:
			//    https://grails-plugins.github.io/grails-spring-security-core/v2/guide/urlProperties.html
			adh {
				// errorPage = '/errorHandler/error'
				errorPage = null
				useForward = false
			}
			apf {
				filterProcessesUrl = '/auth/signIn'
				usernameParameter = 'username'
				passwordParameter = 'password'
				postOnly = true
			}
			auth {
				loginFormUrl = '/auth/login'
				useForward = false
				ajaxLoginFormUrl = loginFormUrl // Overrides default redirect Login URL for AJAX requests.
			}
			dao {
				hideUserNotFoundExceptions = false
			}

			failureHandler.defaultFailureUrl = '/auth/login'

			// See https://grails-plugins.github.io/grails-spring-security-core/v2/guide/requestMappings.html for details
			// Note that when the two property values are reversed that Forbidden is thrown for unknown controllers while
			// logged in and NotFound when unauthenticated however it also throws a stack trace for ever 404. Therefore it
			// was choosen to have that inconsistence to avoid unnecessary noise in the application log files.
			rejectIfNoRule = true
			fii.rejectPublicInvocations = false
			//rejectIfNoRule = false
			//fii.rejectPublicInvocations = true

			controllerAnnotations.staticRules = [
					'/ws/**'              : 'isAuthenticated()',
					'/'                   : 'permitAll',
					'/index'              : 'permitAll',
					'/index.gsp'          : 'permitAll',
					'/assets/**'          : 'permitAll',        // Don't believe it is used
					'/auth/**'            : 'permitAll',        // Authentication Controller
					'/**/js/**'           : 'permitAll',        // Javascript
					'/**/css/**'          : 'permitAll',
					'/**/images/**'       : 'permitAll',
					'/i/**'               : 'permitAll',
					'/**/icons/**'        : 'permitAll',
					'/**/favicon.ico'     : 'permitAll',
					'/app-js/**'          : 'permitAll', // Angular1.6 - resources
					'/i18n/**'            : 'permitAll', // Angular - Translate
					'/tds/web-app/**'     : 'permitAll', // Angular2* - resources
					'/module/**'          : 'permitAll', // Angular2  - router access
					'/test/**'            : 'permitAll', // Angular - Test
					'/dist/**'            : 'permitAll',
					'/monitoring'         : "hasPermission(request, '${Permission.AdminUtilitiesAccess}')",
					'/greenmail/**'       : 'permitAll',
					'/components/**'      : 'permitAll',
					'/templates/**'       : 'permitAll',
					'/console/**'         : "hasPermission(request, '${Permission.AdminUtilitiesAccess}')",
					'/plugins/console*/**': "hasPermission(request, '${Permission.AdminUtilitiesAccess}')",
					'/jasper/**'          : 'permitAll',
					'/oauth/access_token' : 'permitAll'
			]

			ldap.active = false

			// http://alvarosanchez.github.io/grails-spring-security-rest/1.5.4/docs/guide/single.html#tokenValidation
			rest {
				token {
					validation {
						enableAnonymousAccess = true
					}
				}
			}

		}
	}
}

// Graph Properties
graph {
//	graphviz {
//		//dotCmd = '/usr/bin/dot'
//		dotCmd = '/usr/local/bin/dot'
//		graphType = 'svg'
//	}
//	deleteDotFile = false
	tmpDir = '/tmp/'
//	targetDir = '/var/www/tdstm/images/tmp/'
//	targetURI = '/../images/tmp/'
}

xssSanitizer.enabled = true

// JPM 5/2018 : TM-10317 - Tried using both formats but the 2nd would not work correctly
// grails.databinding.dateFormats = ['yyyyMMdd', 'yyyy-MM-dd']
grails.databinding.dateFormats = ["yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", 'yyyy-MM-dd']

org.codehaus.groovy.grails.validation.ConstrainedProperty.registerNewConstraint(net.transitionmanager.domain.constraint.OfSameProjectConstraint.NAME, net.transitionmanager.domain.constraint.OfSameProjectConstraint.class)
