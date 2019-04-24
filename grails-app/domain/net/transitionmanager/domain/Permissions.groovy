package net.transitionmanager.domain

import groovy.transform.CompileStatic

class Permissions {

	@CompileStatic
	static enum Roles {
		ADMIN,
		CLIENT_ADMIN,
		CLIENT_MGR,
		SUPERVISOR,
		EDITOR,
		USER

		static final List<String> NAMES = values()*.name()
	}

	String permissionItem
	String description

	String toString() { permissionItem }

	static hasMany = [rolePermissions: RolePermissions]

	static constraints = {
		description nullable: true, size:0..255
		permissionItem blank: false, unique: true, size:1..255
		rolePermissions cascade: 'all-delete-orphan'
	}

	static mapping = {
		version false
	}
}
