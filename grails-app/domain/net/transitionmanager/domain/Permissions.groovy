package net.transitionmanager.domain

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
		description nullable: true
		permissionItem blank: false, unique: true
	}

	static mapping = {
		version false
		rolePermissions cascade: 'all-delete-orphan'
	}
}
