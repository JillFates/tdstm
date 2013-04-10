
/**
 * This set of Database change that is required to add a new permission 'NewModelsFromImport' in 'permissions' table and also assign  'ADMIN' to that
 * permission in 'role_permissions' table.
 */

databaseChangeLog = {
	
	changeSet(author: "lokanada", id: "20130409 TM-1290 ") {
		comment('Add "NewModelsFromImport" permission in permission table')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 'select count(*) from permissions where permission_group="MODEL" and permission_item = "NewModelsFromImport"')
		}
		sql("INSERT INTO permissions (permission_group, permission_item ) VALUES ('MODEL', 'NewModelsFromImport')")
		sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'MODEL' and permission_item= 'NewModelsFromImport'), 'ADMIN')""")
	}
}