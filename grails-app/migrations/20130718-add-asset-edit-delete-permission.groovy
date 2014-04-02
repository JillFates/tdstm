/**
 * This set of Database change that is required to add a new permission 'AssetDelete' in 'permissions' table and also assign  'ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR','EDITOR'
 *  to that permission in 'role_permissions' table.
 */


databaseChangeLog = {
	
	// this changeset is used to add AssetDelete permission item in permission table and assign 'ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR' by default.
	changeSet(author: "lokanada", id: "20130718 TM-2021-2") {
		comment('Add "AssetDelete" permission in permission table')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 'select count(*) from permissions where permission_group="ASSETENTITY" and permission_item = "AssetDelete"')
		}

			sql("INSERT INTO permissions (permission_group, permission_item ) VALUES ('ASSETENTITY', 'AssetDelete')")
			def importRoles=['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR']
			importRoles.each{role->
				sql("""INSERT INTO role_permissions (permission_id, role) VALUES
				((select id from permissions where permission_group = 'ASSETENTITY' and permission_item= 'AssetDelete'), '${role}')""")
			}
	}
	changeSet(author: "lokanada", id: "20140402 TM-2581-1") {
		comment('updated "EditAndDelete" with "AssetEdit" permission in permission table')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'1', 'select count(*) from permissions where permission_group="ASSETENTITY" and permission_item = "EditAndDelete"')
		}

			sql("UPDATE permissions SET permission_item = 'AssetEdit' WHERE permission_group='ASSETENTITY' AND permission_item = 'EditAndDelete'")
	}
	
}