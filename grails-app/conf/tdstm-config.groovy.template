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
grails.mail.default.from = "TDS TransitionManager <tm-prod@transitionaldata.com>"


