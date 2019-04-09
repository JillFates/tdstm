package net.transitionmanager.security

class RolePermissions {

	String role

	String toString() { role }

	static belongsTo = [permission: Permissions]

	static constraints = {
		role blank: false
	}

	static mapping = {
		version false
	}
}
