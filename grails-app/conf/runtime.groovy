
import com.tdssrc.grails.TimeUtil
import grails.util.Environment
import net.transitionmanager.security.Permission

// copy binding variables into properties in the config for visibility in external scripts; as of 2.5.4 the
// vars are: appName, appVersion, basedir, baseFile, baseName, grailsHome,
//           grailsSettings, grailsVersion, groovyVersion, springVersion, userHome
getBinding().variables.each { name, value -> setProperty name, value }

String appName = this.appName

if ( ! appName ) {
	File userDir = new File( System.getProperty("user.dir") )
	appName = userDir.name ?: 'tdstm'
}

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

configFileFromJavaOpts = System.getProperty("tdstm.config.location")
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


if(userHome) {
	candidates << "$userHome/.grails/${appName}-config.groovy"
}

//adding a default canidate for the build servers
candidates << "./tdstm-config.groovy"

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
		continue //If no config is found a error will be thrown, but don't fail if one is missing.
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

server.servlet['context-path'] = '/tdstm'
spring {
	jmx['unique-names'] = true
	main['banner-mode'] = 'off'

	groovy {
		template['check-template-location'] = false
	}

	devtools {
		restart['additional-exclude:'] = [
			'*.gsp',
			'**/*.gsp',
			'*.gson',
			'**/*.gson',
			'logback.groovy',
			'*.properties'
		]
	}
}

management {
	endpoints.web.exposure.include= "*"
    endpoints['enabled-by-default'] = true
}

management {
    endpoints {
        jmx['unique-names'] = true
    }
}

grails {
	project.groupId = appName
	enable.native2ascii = true
	exceptionresolver.params.exclude = ['password']
	pagination.default = "20"
	pagination.max = "20"
	mail.default.from = "TDS TransitionManager <tds.transition.manager@gmail.com>"
	stringchararrayaccessor.disabled = true // Eliminates warning error - see TM-3681
	web.disable.multipart=false
	scaffolding.templates.domainSuffix = 'Instance'
	//spring.bean.packages = []

    mime {
		file.extensions = true // enables the parsing of file extensions from URLs into the request format
        disable {
            accept {
                header {
                    userAgents = [
                        'Gecko',
                        'WebKit',
                        'Presto',
                        'Trident'
                    ]
                }
            }
        }

		types {
			all = '*/*'
			atom = 'application/atom+xml'
			css = 'text/css'
			csv = 'text/csv'
			form = 'application/x-www-form-urlencoded'
			html = [
				'text/html',
				'application/xhtml+xml'
			]
			js = 'text/javascript'
			json = [
				'application/json',
				'text/json'
			]
			multipartForm = 'multipart/form-data'
			pdf = 'application/pdf'
			rss = 'application/rss+xml'
			text = 'text/plain'
			hal = [
				'application/hal+json',
				'application/hal+xml'
			]
			xls = 'application/vnd.ms-excel'
			xlsx = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
			xml = [
				'text/xml',
				'application/xml'
			]
		}
	}

	urlmapping {
		cache {
			maxsize = 1000
		}
	}

	controllers {
		defaultScope = 'singleton'

		upload {
			maxFileSize = 500 * 1024 * 1024
			maxRequestSize = 500 * 1024 * 1024
		}
	}

	converters {
		encoding = 'UTF-8'
	}

	views {
		'default' {
			codec = 'none'
		}

		gsp {
			encoding = 'UTF-8'
			htmlcodec = 'xml'
			gsp.sitemesh.preprocess = true
			codecs {
				expression = 'html'
				scriptlets = 'html'
				taglib = 'none'
				staticparts = 'none'
			}
		}
	}
}

environments {
	development {
		grails {
			logging.jul.usebridge = true
			serverURL = 'http://localhost:8080/tdstm'

			mail.port = com.icegreen.greenmail.util.ServerSetupTest.SMTP.port
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
		javamelody.disabled = true
		graph.tmpDir = File.createTempDir().getCanonicalPath()
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

hibernate {
	cache {
		queries = false
		use_second_level_cache = false
		use_query_cache = false
		region.factory_class = 'org.hibernate.cache.ehcache.EhCacheRegionFactory'
	}

	// format_sql = true
	singleSession = true
	// use_sql_comments = true
}

dataSource {
	dbCreate = 'none'
	dialect = 'com.tdsops.common.sql.CustomMySQLDialect'
	driverClassName = 'com.mysql.jdbc.Driver'
	jmxExport = true
	pooled = true
}

environments {
	development {
		dataSource {
			// url = "jdbc:mysql://localhost/tdstm?autoReconnect=true"
			username = "tdstmapp"
			password = "tdstmpswd"
			logSql = false

			// See http://grails.org/doc/latest/guide/conf.html#dataSource for documentation
			properties {
				defaultTransactionIsolation = java.sql.Connection.TRANSACTION_READ_COMMITTED
				initialSize = 15
				jdbcInterceptors = 'ConnectionState'
				jmxEnabled = true
				maxActive = 70
				maxAge = 10 * 60000
				maxIdle = 15
				maxWait = 10000
				minEvictableIdleTimeMillis = 1000 * 60 * 5 // Evictions set to 5 minutes of idle time
				minIdle = 5
				removeAbandoned = true
				removeAbandonedTimeout = 600
				testOnBorrow = true
				testOnReturn = false
				testWhileIdle = false
				timeBetweenEvictionRunsMillis = 1000 * 60 // Run evictions on idle connections every 60 seconds (default 5 seconds)
				validationInterval = 15000
				validationQuery = '/* ping */'
				validationQueryTimeout = 3
			}
		}
	}

	test {
		grails.plugin.springsecurity.rest.token.storage.jwt.secret = 'Some secret key to dev, hope this is long enough.'
		dataSource {
			url = "jdbc:mysql://localhost/tdstm?autoReconnect=true"
			username = "tdstmapp"
			password = "tdstmpswd"
			logSql = false
		}
	}

	production {
		dataSource {
			// url = "jdbc:mysql://127.0.0.1/tdstm"
			// username = ''
			// password = ''

			// See http://grails.org/doc/latest/guide/conf.html#dataSource for documentation
			properties {
				defaultTransactionIsolation = java.sql.Connection.TRANSACTION_READ_COMMITTED
				initialSize = 15
				jdbcInterceptors = 'ConnectionState'
				jmxEnabled = true
				maxActive = 70
				maxAge = 10 * 60000
				maxIdle = 15
				maxWait = 10000
				minEvictableIdleTimeMillis = (1000 * 60 * 5) // Evictions set to 5 minutes of idle time
				minIdle = 5
				removeAbandoned = true
				removeAbandonedTimeout = 600
				testOnBorrow = true
				testOnReturn = false
				testWhileIdle = false
				timeBetweenEvictionRunsMillis = (1000 * 60) // Run evictions on idle connections every 60 seconds (default 5 seconds)
				validationInterval = 15000
				validationQuery = '/* ping */'
				validationQueryTimeout = 3
			}
		}
	}
}

//Maintenance file path
tdsops.maintModeFile = "/tmp/tdstm-maint.txt"

//Build number file path
tdsops.buildFile = "build.txt"

// Audit configuration, valid options are: access and activity (default is access)
// access: logging will include login, logout and security violations
// activity:  will also include all user interactions with the application.
//tdstm.security.auditLogging = "access"

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
List staticSecurityRules = [
	[pattern: '/ws/**', access: 'isAuthenticated()'],
	[pattern: '/', access: 'permitAll'],
	[pattern: '/index', access: 'permitAll'],
	[pattern: '/assets/**', access: 'permitAll'],        // Don't believe it is used
	[pattern: '/auth/**', access: 'permitAll'],        // Authentication Controller
	[pattern: '/**/js/**', access: 'permitAll'],        // Javascript
	[pattern: '/**/css/**', access: 'permitAll'],
	[pattern: '/**/images/**', access: 'permitAll'],
	[pattern: '/i/**', access: 'permitAll'],
	[pattern: '/**/icons/**', access: 'permitAll'],
	[pattern: '/**/favicon.ico', access: 'permitAll'],
	[pattern: '/app-js/**', access: 'permitAll'], // Angular1.6 - resource]s
	[pattern: '/i18n/**', access: 'permitAll'], // Angular - Translate
	[pattern: '/tds/web-app/**', access: 'permitAll'], // Angular2* - resources
	[pattern: '/module/**', access: 'permitAll'], // Angular2  - router access
	[pattern: '/test/**', access: 'permitAll'], // Angular - Tes]t
	[pattern: '/dist/**', access: 'permitAll'],
	[pattern: '/greenmail/**', access: 'permitAll'],
	[pattern: '/components/**', access: 'permitAll'],
	[pattern: '/templates/**', access: 'permitAll'],
	[pattern: '/jasper/**', access: 'permitAll'],
	[pattern: '/oauth/access_token', access: 'permitAll'],
	[pattern: '/actuator/**', access: "hasPermission(request, '${Permission.AdminUtilitiesAccess}')"],
	[pattern: '/monitoring', access: "hasPermission(request, '${Permission.AdminUtilitiesAccess}')"]
]

grails {
	plugin {
		springsecurity {
			// Refer to spring security REST Plugin configuration:
			// http://alvarosanchez.github.io/grails-spring-security-rest/1.5.4/docs/guide/single.html#tokenValidation
			filterChain.chainMap = [
				[pattern: '/api/projects/heartbeat', filters: 'anonymousAuthenticationFilter,restTokenValidationFilter,restExceptionTranslationFilter,filterInvocationInterceptor'],
				[pattern: '/api/appVersion', filters: 'anonymousAuthenticationFilter,restTokenValidationFilter,restExceptionTranslationFilter,filterInvocationInterceptor'],
				[pattern: '/api/**', filters: 'JOINED_FILTERS,-anonymousAuthenticationFilter,-exceptionTranslationFilter,-authenticationProcessingFilter,-securityContextPersistenceFilter,-rememberMeAuthenticationFilter'],  // Stateless chain
				[pattern: '/**', filters: 'JOINED_FILTERS,-restTokenValidationFilter,-restExceptionTranslationFilter'] // Traditional chain
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
				loginFormUrl = '/module/auth/login'
				useForward = false
				ajaxLoginFormUrl = loginFormUrl // Overrides default redirect Login URL for AJAX requests.
			}
			dao {
				hideUserNotFoundExceptions = false
			}

			failureHandler.defaultFailureUrl = '/module/auth/login'

			// See https://grails-plugins.github.io/grails-spring-security-core/v2/guide/requestMappings.html for details
			// Note that when the two property values are reversed that Forbidden is thrown for unknown controllers while
			// logged in and NotFound when unauthenticated however it also throws a stack trace for ever 404. Therefore it
			// was choosen to have that inconsistence to avoid unnecessary noise in the application log files.
			rejectIfNoRule = true
			fii.rejectPublicInvocations = false
			//rejectIfNoRule = false
			//fii.rejectPublicInvocations = true

			controllerAnnotations.staticRules = staticSecurityRules
			ldap.active = false

			// http://alvarosanchez.github.io/grails-spring-security-rest/1.5.4/docs/guide/single.html#tokenValidation
			rest {
				token {
					validation {
						enableAnonymousAccess = true
					}
					storage {
						jwt {
							expiration = 14400 // default expiration to 4 hours
						}
					}
				}
			}

		}
	}
}

if (System.getProperty("tdstm.gconsole")) {
	grails {
		plugin {
			console.enabled = true
			springsecurity {
				staticSecurityRules << [pattern: '/h2-console/**', access: "hasPermission(request, '${Permission.AdminUtilitiesAccess}')"]
				staticSecurityRules << [pattern: '/static/console*/**', access: "hasPermission(request, '${Permission.AdminUtilitiesAccess}')"]
				staticSecurityRules << [pattern: '/console/**', access: "hasPermission(request, '${Permission.AdminUtilitiesAccess}')"]
				controllerAnnotations.staticRules = staticSecurityRules
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
grails.databinding.dateFormats = [
	TimeUtil.FORMAT_DATE_TIME_STANDARD_ISO8601,
	"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
	'yyyy-MM-dd'
]

grails.resources.pattern = '/**'

/*
The parameter disabled (false by default) just disables the monitoring.
 */
//javamelody.disabled = false

/*
The parameter system-actions-enabled (true by default) enables some system actions.
 */
//javamelody.'system-actions-enabled' = true


/*
Turn on Grails Service monitoring by adding 'spring' in displayed-counters parameter.
 */
javamelody.'displayed-counters' = 'http,sql,error,log,spring,jsp'

quartz {
    autoStartup = true
    jdbcStore = false
}

testing {
	foo {
		intVal = 123
		stringVal = 'abc'
		configVal {
			intVal = 123
			stringVal = 'abc'
		}
	}
}

/*
The parameter url-exclude-pattern is a regular expression to exclude some urls from monitoring as written above.
 */
//javamelody.'url-exclude-pattern' = '/static/.*'



/*
Specify jndi name of datasource to monitor in production environment
 */
/*environments {
    production {
        javamelody.datasources = 'java:comp/env/myapp/mydatasource'
    }
}*/

// TM-11135 Change so that GORM save defaults to failOnError:true
grails.gorm.failOnError = true

grails.plugin.databasemigration.updateOnStartFileName = 'changelog.groovy'

