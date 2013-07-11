/**
 * This set of Database change that is required to add a new permission 'RolePermissionView' in 'permissions' table 
 * and also assign  'ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR' to that permission in 'role_permissions' table.
 */

databaseChangeLog = {
	
	// this changeset is used to add 'RolePermissionView' permission item in permission table and assign 'ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR' by default.
	changeSet(author: "lokanada", id: "20130423 TM-1937-1") {
		comment('Add "RolePermissionView" permission in permission table')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 'select count(*) from permissions where permission_group="NAVIGATION" and permission_item = "RolePermissionView"')
		}
		sql("INSERT INTO permissions (permission_group, permission_item ) VALUES ('NAVIGATION', 'RolePermissionView')")
		def importRoles=['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR']
		importRoles.each{role->
			sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'NAVIGATION' and permission_item= 'RolePermissionView'), '${role}')""")
		}
	}
}