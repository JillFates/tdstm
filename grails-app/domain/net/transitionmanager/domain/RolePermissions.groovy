package net.transitionmanager.domain

class RolePermissions {

	String role

	String toString() { role }

	static belongsTo = [permission: Permissions]

	static constraints = {
		role blank: false, size: 1..255
	}

	static mapping = {
		version false
	}
}
