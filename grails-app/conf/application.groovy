import net.transitionmanager.security.Permission


grails {
	profile = 'web'
	codegen {
		defaultPackage = 'net.transitionmanager'
	}
	gorm {
		reactor {
			events = false
		}
	}
}

server.contextPath = '/tdstm'
info {
	app {
		name = '@info.app.name@'
		version = '@info.app.version@'
		grailsVersion = '@info.app.grailsVersion@'
	}
}

spring {
	main {
		main['banner-mode'] = 'off'
	}
	groovy {
		template {
			template['check-template-location'] = false
		}
	}
}

grails {
	project.groupId = appName
	enable.native2ascii = true
	exceptionresolver.params.exclude = ['password']
	pagination.default = "20"
	pagination.max = "20"
	mail.default.from = "TDS Transition Manager <tds.transition.manager@gmail.com>"
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

endpoints {
	enabled = true

	jmx {
		enabled = true
		jmx['unique-names'] = true
	}
}

hibernate {
	cache {
		queries = false
		use_second_level_cache = true
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

}

grails {
	plugin {
		springsecurity {
			// Refer to spring security REST Plugin configuration:
			// http://alvarosanchez.github.io/grails-spring-security-rest/1.5.4/docs/guide/single.html#tokenValidation
			filterChain.chainMap = [
				[pattern: '/api/projects/heartbeat', filters: 'anonymousAuthenticationFilter,restTokenValidationFilter,restExceptionTranslationFilter,filterInvocationInterceptor'],
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

			//TODO fix after adding permission class
			controllerAnnotations.staticRules = [
				[pattern: '/ws/**'              , access: 'isAuthenticated()'],
				[pattern: '/'                   , access: 'permitAll'],
				[pattern: '/index'              , access: 'permitAll'],
				[pattern: '/index.gsp'          , access: 'permitAll'],
				[pattern: '/assets/**'          , access: 'permitAll'],        // Don't believe it is used
				[pattern: '/auth/**'            , access: 'permitAll'],        // Authentication Controller
				[pattern: '/**/js/**'           , access: 'permitAll'],        // Javascript
				[pattern: '/**/css/**'          , access: 'permitAll'],
				[pattern: '/**/images/**'       , access: 'permitAll'],
				[pattern: '/i/**'               , access: 'permitAll'],
				[pattern: '/**/icons/**'        , access: 'permitAll'],
				[pattern: '/**/favicon.ico'     , access: 'permitAll'],
				[pattern: '/app-js/**'          , access: 'permitAll'], // Angular1.6 - resource]s
				[pattern: '/i18n/**'            , access: 'permitAll'], // Angular - Translate
				[pattern: '/tds/web-app/**'     , access: 'permitAll'], // Angular2* - resources
				[pattern: '/module/**'          , access: 'permitAll'], // Angular2  - router access
				[pattern: '/test/**'            , access: 'permitAll'], // Angular - Tes]t
				[pattern: '/dist/**'            , access: 'permitAll'],
				[pattern: '/monitoring'         , access: "hasPermission(request, '${Permission.AdminUtilitiesAccess}')"],
				[pattern: '/greenmail/**'       , access: 'permitAll'],
				[pattern: '/components/**'      , access: 'permitAll'],
				[pattern: '/templates/**'       , access: 'permitAll'],
				[pattern: '/console/**'         , access: "hasPermission(request, '${Permission.AdminUtilitiesAccess}')"],
				[pattern: '/plugins/console*/**', access: "hasPermission(request, '${Permission.AdminUtilitiesAccess}')"],
				[pattern: '/jasper/**'          , access: 'permitAll'],
				[pattern: '/oauth/access_token' , access: 'permitAll'],
				[pattern: '/health/**'          , access: 'ROLE_ADMIN'],
				[pattern: '/info/**'            , access: 'ROLE_ADMIN']
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