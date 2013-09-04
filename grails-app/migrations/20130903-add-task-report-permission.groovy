/**
 * This set of Database change that is required to add a new permission 'ShowPlanning' in 'permissions' table and also assign  
 * 'ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR' to that permission in 'role_permissions' table.
 */

databaseChangeLog = {
	// this changeset is used to add ShowPlanning permission item in permission table and assign 'ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR' by default.
	changeSet(author: "lokanada", id: "20130903 TM-2027-1") {
		comment('Add "ShowPlanning" permission in permission table')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 'select count(*) from permissions where permission_group="REPORTS" and permission_item = "ShowPlanning"')
		}
		sql("INSERT INTO permissions (permission_group, permission_item ) VALUES ('REPORTS', 'ShowPlanning')")
		def importRoles=['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR']
		importRoles.each{role->
			sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'REPORTS' and permission_item= 'ShowPlanning'), '${role}')""")
		}
	}
}