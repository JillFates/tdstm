/**
 * tdstm-config.groovy.default
 *
 * This is an optional configuration file that is used to override settings in the grails-app/conf/*.groovy files. To use this,
 * copy this file to a configuration directory  renaming it appropriately (e.g. /etc/tdstm-config.groovy) and then provide a
 * JVM -D argument in the start up of the application as shown:
 *
 *	grails -Dtdstm.config.location=/etc/tdstm-config.groovy run-app
 *	java -Dtdstm.config.location=/etc/tdstm-config.groovy ...
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
            forceUseNewEncryption = true
        }
    }
}
grails.mail.default.from = "TDS TransitionManager <tm-prod@transitionaldata.com>"

