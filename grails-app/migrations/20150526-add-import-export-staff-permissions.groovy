/**
 * This set of Database change that is required to add a new permission 'PersonImport' in 'permissions' table
 * and also assign  'ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR' and 'SUPERVISOR' to that permission in 'role_permissions' table.
 */

databaseChangeLog = {
	
	// this changeset is used to add 'PersonImport' permission item in permission table and assign 'ADMIN', 'CLIENT_ADMIN' and 'CLIENT_MGR' by default.
	changeSet(author: "arecordon", id: "20150526 TM-3912-1") {
		comment('Add "PersonImport" permission in permission table')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 'select count(*) from permissions where permission_group="MODEL" and permission_item = "PersonImport"')
		}
		sql("INSERT INTO permissions (permission_group, permission_item ) VALUES ('TASK', 'PersonImport')")
		def importRoles=['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR']
		importRoles.each{role->
			sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'TASK' and permission_item= 'PersonImport'), '${role}')""")
		}
	}

	// this changeset is used to add 'PersonExport' permission item in permission table and assign 'ADMIN', 'CLIENT_ADMIN' and 'CLIENT_MGR' by default.
	changeSet(author: "arecordon", id: "20150526 TM-3912-2") {
		comment('Add "PersonExport" permission in permission table')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 'select count(*) from permissions where permission_group="MODEL" and permission_item = "PersonExport"')
		}
		sql("INSERT INTO permissions (permission_group, permission_item ) VALUES ('TASK', 'PersonExport')")
		def importRoles=['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR']
		importRoles.each{role->
			sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'TASK' and permission_item= 'PersonExport'), '${role}')""")
		}
	}

}
