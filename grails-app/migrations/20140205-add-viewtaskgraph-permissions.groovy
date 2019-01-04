/**
 * This migration script adds a new permission 'ViewTaskGraph' in 'permissions' table
 * and assigns all roles to it in the 'role_permissions' table.
 */

databaseChangeLog = {
	
	// this changeset is used to add the 'ViewTaskGraph' permission item in permission table and assign all roles to have access to it.
	changeSet(author: "rmacfarlane", id: "10140702 TM-2945") {
		
		// create the permission
		comment('Add "ViewTaskGraph" permission in permission table')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 'SELECT COUNT(*) FROM permissions WHERE permission_group="TASK" AND permission_item = "ViewTaskGraph"')
		}
		sql("INSERT INTO permissions (permission_group, permission_item) VALUES ('TASK', 'ViewTaskGraph')")
		
		// set the roles for the new permission
		def roles = ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'CLIENT_ADMIN', 'SUPERVISOR', 'EDITOR', 'USER']
		roles.each{ role ->
			sql("""INSERT INTO role_permissions (permission_id, role) VALUES
				((SELECT id FROM permissions WHERE permission_group = 'TASK' AND permission_item = 'ViewTaskGraph'), '${role}')
			""")
		}
	}
}
