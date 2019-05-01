package net.transitionmanager.security

import groovy.transform.CompileStatic

class Permissions {

	@CompileStatic
	static enum Roles {
		ROLE_ADMIN,
		ROLE_CLIENT_ADMIN,
		ROLE_CLIENT_MGR,
		ROLE_SUPERVISOR,
		ROLE_EDITOR,
		ROLE_USER

		static final List<String> NAMES = values()*.name()
	}

	String permissionItem
	String description

	String toString() { permissionItem }

	static hasMany = [rolePermissions: RolePermissions]

	static constraints = {
		description nullable: true, size:0..255
		permissionItem blank: false, unique: true, size:1..255
	}

	static mapping = {
		version false
		rolePermissions cascade: 'all-delete-orphan'
	}
}
