/**
 * This set of Database change that is required to add a new permission 'EditModel' in 'permissions' table
 * and also assign  'ADMIN', 'CLIENT_ADMIN' to that permission in 'role_permissions' table.
 */

databaseChangeLog = {
	
	// this changeset is used to add 'RolePermissionView' permission item in permission table and assign 'ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR' by default.
	changeSet(author: "lokanada", id: "20140523 TM-2721-1") {
		comment('Add "EditModel" permission in permission table')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 'select count(*) from permissions where permission_group="MODEL" and permission_item = "EditModel"')
		}
		sql("INSERT INTO permissions (permission_group, permission_item ) VALUES ('MODEL', 'EditModel')")
		def importRoles=['ADMIN', 'CLIENT_ADMIN']
		importRoles.each{role->
			sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'MODEL' and permission_item= 'EditModel'), '${role}')""")
		}
	}
}
