/**
 * This set of Database change that is required to add a new permission 'ChangePendingStatus' in 'permissions' table
 * and also assign  'ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR' and 'SUPERVISOR' to that permission in 'role_permissions' table.
 */

databaseChangeLog = {
	
	// this changeset is used to add 'ChangePendingStatus' permission item in permission table and assign 'ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR' and 'SUPERVISOR' by default.
	changeSet(author: "arecordon", id: "20150114 TM-3650") {
		comment('Add "ChangePendingStatus" permission in permission table')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 'select count(*) from permissions where permission_group="MODEL" and permission_item = "ChangePendingStatus"')
		}
		sql("INSERT INTO permissions (permission_group, permission_item ) VALUES ('TASK', 'ChangePendingStatus')")
		def importRoles=['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR']
		importRoles.each{role->
			sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'TASK' and permission_item= 'ChangePendingStatus'), '${role}')""")
		}
	}
}
