package net.transitionmanager.domain

import com.tdsops.tm.enums.domain.PermissionGroup
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
	PermissionGroup permissionGroup
	String description

	String toString() { permissionItem }

	static hasMany = [rolePermissions: RolePermissions]

	static constraints = {
		description nullable: true
		permissionItem blank: false, unique: true
		rolePermissions cascade: 'all-delete-orphan'
	}

	static mapping = {
		version false
	}
}
