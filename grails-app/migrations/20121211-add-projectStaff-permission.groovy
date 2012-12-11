
/**
 * This set of Database change that is required to add a new permission 'EditProjectStaff' in 'permissions' table and also assign  'ADMIN' to that
 * permission in 'role_permissions' table.
 */

databaseChangeLog = {
	
	changeSet(author: "lokanada", id: "20121211 TM-1136.1") {
		comment('Add "EditProjectStaff" permission in permission table')
		sql("INSERT INTO permissions (permission_group, permission_item ) VALUES ('PROJECT', 'EditProjectStaff')")
		sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'PROJECT' and permission_item= 'EditProjectStaff'), 'ADMIN')""")
	}
}