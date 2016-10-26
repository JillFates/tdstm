import net.transitionmanager.service.SecurityService

class CustomSecurityTagLib {

	static namespace = 'tds'

	SecurityService securityService

	/**
	 * @attr permission REQUIRED  the permission name
	 */
	def hasPermission = { attrs, body ->
		if (permitted(attrs)) {
			out << body()
		}
	}

	/**
	 * @attr permission REQUIRED  the permission name
	 */
	def lacksPermission = { attrs, body ->
		if (!permitted(attrs)) {
			out << body()
		}
	}

	private boolean permitted(Map attrs) {
		securityService.hasPermission(attrs.permission as String)
	}
}
