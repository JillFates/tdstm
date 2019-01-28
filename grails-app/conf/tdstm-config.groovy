/**
* tdstm-config.groovy.default
*
* This is an optional configuration file that is used to override settings in the grails-app/conf/*.groovy files. To use this,
* copy this file to a configuration directory  renaming it appropriately (e.g. /etc/tdstm-config.groovy) and then provide a
* JVM -D argument in the start up of the application as shown:
*
*   grails -Dtdstm.config.location=/etc/tdstm-config.groovy run-app
*   java -Dtdstm.config.location=/etc/tdstm-config.groovy ...
*
**/
 
 
//
// Database Properties
//
 
dataSource {
    // TDS Transitional Manager
    url = "jdbc:mysql://localhost/tdstm"
    username = "tdstm"
    password = "tdstm"
}
 
//
// Mail Properties
//
grails {
    mail {
        host = "smtp.transitionaldata.com"
        port = 587
        username = 'tm-prod'
        password = "7UJuhepR"
        props = [
            "mail.smtp.auth":"true",
            "mail.smtp.socketFactory.port":"587",
            "mail.smtp.socketFactory.class":"javax.net.ssl.SSLSocketFactory",
            "mail.smtp.socketFactory.fallback":"false"
            ]
    }
}
//
// Local User Configuration Settings
//
tdstm {
    security {
        // Then number of days after which a user account will be automatically locked out
        // indefinitely upon logging in. Set to zero (0) to disable this feature.
        inactiveDaysLockout = 60
 
        // A list of usernames that should be excluded from the Inactivity Lockout behavior.
        inactivityWhitelist = [ 'tdsadmin' ]
 
        localUser {
 
            // Flag if the local user accounts are supported, values true|false (default true)
            enabled = true
 
            // Minimum number of characters the password must be (default 8)
            minPasswordLength = 8
 
            // Number of times user can fail login before the account is locked out, set to zero (0) prevents lockout, default 5
            maxLoginFailureAttempts = 5
 
            // How long to disable an account that failed logins to many times, set to zero (0) will lockout the account in definitely, default 30 minutes
            failedLoginLockoutPeriodMinutes = 30
 
            // As a fail-safe, locked out accounts could be cleared when the application is restarted in the event of a DOS style attack (default true)
            clearLockoutsOnRestart = true
 
            // How long to retain password history to prevent re-use, set to zero (0) disables password history (default 2 years). This is mutually exclusive with passwordHistoryRetentionCount.
            passwordHistoryRetentionDays = 365 * 2
 
            // How many previous passwords to retain to prevent the user from reusing. This is mutually exclusive with passwordHistoryRetentionDays
            passwordHistoryRetentionCount = 0
 
            // The maximum number of days a password can be used before the user must change it, set to zero (0) disable the feature (default 0 days)
            maxPasswordAgeDays = 90
 
            // forgot my password reset time limit (minutes)
            forgotMyPasswordResetTimeLimit = 60
 
            // forgot my password welcome email time limit
            accountActivationTimeLimit = 2*24*60
 
            // forgot my password retain history days
            forgotMyPasswordRetainHistoryDays = 30
 
            // prevent the user from changing their password (hours)
            minPeriodToChangePswd = 24
 
            // Disable the forcing of users from changing their passwords if the hash method used was the obsolete one.
            forceUseNewEncryption = false
        }
    }
}
grails.mail.default.from = "TDS TransitionManager <tm-prod@transitionaldata.com>"
 
log4j = {
    // Set level for all application artifacts
    info    'grails.app'
    info    'liquibase'
 
    // Enable Hibernate SQL logging with param values
    // debug 'org.hibernate.SQL'
    // trace 'org.hibernate.type.descriptor.sql.BasicBinder'
 
    // enable *debug* to track security issues
//  info    'grails.plugin.springsecurity',
//          'org.springframework.security'
 
    //   'controllers.AuthController'
    //   'org.hibernate.SQL'
    warn    'org.codehaus.groovy.grails.web.servlet',           // controllers
            'org.codehaus.groovy.grails.web.pages',             // GSP
            'org.codehaus.groovy.grails.web.sitemesh',          // layouts
            'org.codehaus.groovy.grails.web.mapping.filter',    // URL mapping
            'org.codehaus.groovy.grails.web.mapping',           // URL mapping
            'org.codehaus.groovy.grails.commons',               // core / classloading
            'org.codehaus.groovy.grails.plugins',               // plugins
            'org.codehaus.groovy.grails.orm.hibernate',         // hibernate integration
            'org.codehaus.groovy.grails',                       // Most of all grails code base
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
 
    error   'org.hibernate.hql.internal.ast.HqlSqlWalker',
            'grails.plugin.hibernate4',
            'org.apache.tomcat',
            'liquibase',
            'net.bull.javamelody'
 
    // ** Enable Hibernate SQL logging with param values *********
    // trace 'org.hibernate.type'
    // debug 'org.hibernate.SQL'
 
    appenders {
        String logAppName = 'tdstm'    // If not defined (for local config)
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
        //'null' name:'stacktrace'
        error stdout:"StackTrace"
    }
 
    root {
        info 'stdout', 'applicationLog'
    }
 
    // Send debug logging to application log (and console since additivity is true)
    debug  applicationLog: 'grails.app', additivity: true
 
    // Setup Audit Logging messages to go to their own log file in addition to the application log
    info  auditLog: 'net.transitionmanager.service.AuditService', additivity: true
}
 
 
tdstm.license.enabled = false
grails.plugin.databasemigration.updateOnStart = false
 
tdstm.license = [
    // enabled: false,  // defaults to true, if present and false, disables license checks
    request_email: 'tpelletier@tdsi.com',
    key:'/Users/tpelletier/.grails/licensePublicKey.key',
    password:'O3rM&mWkNMGf&q'  // must match the password used in the used license manager
]
 
javamelody.disabled = true
grails.serverURL ="http://localhost:8080/tdstm"
 
grails.plugin.springsecurity.rest.token.storage.jwt.secret = 'Some secret key to dev, hope this is long enough.'