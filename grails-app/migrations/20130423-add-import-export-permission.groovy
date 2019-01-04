/**
 * This set of Database change that is required to add a new permission 'Import' and 'Export' in 'permissions' table and also assign  'ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR','EDITOR'
 *  to that permission in 'role_permissions' table.
 */

databaseChangeLog = {
	
	// this changeset is used to add Import permission item in permission table and assign 'ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR' by default.
	changeSet(author: "lokanada", id: "20130423 TM-1875-1") {
		comment('Add "Import" permission in permission table')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 'select count(*) from permissions where permission_group="ASSETENTITY" and permission_item = "Import"')
		}
		sql("INSERT INTO permissions (permission_group, permission_item ) VALUES ('ASSETENTITY', 'Import')")
		def importRoles=['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR']
		importRoles.each{role->
			sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'ASSETENTITY' and permission_item= 'Import'), '${role}')""")
		}
	}
	
	// this changeset is used to add Export permission item in permission table and assign 'ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR','EDITOR' by default.
	changeSet(author: "lokanada", id: "20130423 TM-1875-2") {
		comment('Add "Export" permission in permission table')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 'select count(*) from permissions where permission_group="ASSETENTITY" and permission_item = "Export"')
		}
		sql("INSERT INTO permissions (permission_group, permission_item ) VALUES ('ASSETENTITY', 'Export')")
		def exportRoles=['ADMIN','CLIENT_ADMIN','CLIENT_MGR','SUPERVISOR','EDITOR']
		exportRoles.each{role->
			sql("""INSERT INTO role_permissions (permission_id, role) VALUES
				((select id from permissions where permission_group = 'ASSETENTITY' and permission_item= 'Export'), '${role}')""")
		}
	}
}
