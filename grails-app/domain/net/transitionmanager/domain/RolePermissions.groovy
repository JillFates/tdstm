package net.transitionmanager.domain

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
