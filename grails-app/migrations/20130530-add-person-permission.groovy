/**
 * This set of Database change that is required to add a new permission 'EditTDSPerson' in 'permissions' table 
 *  and also assign  'ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR' to that permission in 'role_permissions' table.
 */

databaseChangeLog = {
	
	// this changeset is used to add editTDSPerson permission item in permission table and assign 'ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR' by default.
	changeSet(author: "lokanada", id: "20130530 TM-1921-1") {
		comment('Add "EditTDSPerson" permission in permission table')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 'select count(*) from permissions where permission_group="PERSON" and permission_item = "EditTDSPerson"')
		}
		sql("INSERT INTO permissions (permission_group, permission_item ) VALUES ('PERSON', 'EditTDSPerson')")
		def importRoles=['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR']
		importRoles.each{role->
			sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'PERSON' and permission_item= 'EditTDSPerson'), '${role}')""")
		}
	}
}