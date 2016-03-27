/**
 * @author @tavo_luna
 * This set of Database change that is required to add a new permission 'monitoring' in 'permissions' table 
 * and also assign  'ADMIN' to that permission in 'role_permissions' table.
 */

databaseChangeLog = {
	// this changeset is used to add 'monitoring' permission item in permission table and assign 'ADMIN'  by default.
	changeSet(author: "oluna", id: "20160310 TM-3662-fix") {
		comment('Add "monitoring" permission in permission table')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 'select count(*) from permissions where permission_group="ADMIN" and permission_item = "ViewAdminTools"')
		}
        // Delete the previously create permission that was incorrectly created
		sql('DELETE from permissions where permission_item="monitoring"')

		sql("""INSERT INTO permissions (permission_group, permission_item, description) 
			VALUES ('ADMIN', 'ViewAdminTools', 'Can view various administration tools')""")

		List importRoles=['ADMIN']
		importRoles.each{ role->
			sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_item= 'ViewAdminTools'), '$role')""")
		}
	}
}
