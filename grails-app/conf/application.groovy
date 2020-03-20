
grails {
	profile = 'web'
	session.timeout = 3600 //60 minute session timeout

	codegen {
		defaultPackage = 'net.transitionmanager'
	}

	gorm {
		reactor {
			events = false
		}
	}
}

//excluding autoconfig for ldap to work with Grails 4+
spring.autoconfigure.exclude=['org.springframework.boot.autoconfigure.ldap.LdapAutoConfiguration', 'org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration']

info {
	app {
		name = '@info.app.name@'
		version = '@info.app.version@'
		grailsVersion = '@info.app.grailsVersion@'
	}
}

grails.databinding.convertEmptyStringsToNull = false
