/**
 * @author @tavo_luna
 * This set is required to add a new permission 'RestartApplication' in 'permissions' table 
 * and also assign 'ADMIN' to that permission in 'role_permissions' table.
 */

databaseChangeLog = {
	// this changeset is used to add 'RestartApplication' permission item in permission table and assign 'ADMIN' by default.
	changeSet(author: "oluna", id: "20160321 TM-4703") {
		comment('Add "RestartApplication" permission in permission table')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 'select count(*) from permissions where permission_group="ADMIN" and permission_item = "RestartApplication"')
		}
		sql("""INSERT INTO permissions (permission_group, permission_item, description) 
						VALUES ('ADMIN', 'RestartApplication', 'Can Restart the Server Application running the configures Script')""")
		def importRoles=['ADMIN']
		importRoles.each{ role->
			sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'ADMIN' and permission_item= 'RestartApplication'), '${role}')""")
		}
	}
}
