import net.transitionmanager.security.SecurityService

class CustomSecurityTagLib {

	static namespace = 'tds'

	SecurityService securityService

	/**
	 * @attr permission REQUIRED  the permission name (String)
	 * 		Use Permission.<Constant> for permission reference
	 * @see net.transitionmanager.security.Permission
	 */
	def hasPermission = { attrs, body ->
		if (permitted(attrs)) {
			out << body()
		}
	}

	/**
	 * @attr permission REQUIRED  the permission name (String)
	 * 		Use Permission.<Constant> for permission reference
	 * @see net.transitionmanager.security.Permission
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
