/**
 * Update permissions for CLIEN_ADMIN role and CLIEN_MGR role
 */
databaseChangeLog = {
	
	changeSet(author: "dscarpa", id: "20150420 TM-3801-3") {
		comment('Update permissions for CLIEN_ADMIN role and CLIEN_MGR role')
		sql("""DELETE FROM role_permissions WHERE
			permission_id = (select id from permissions where permission_group = 'USER' and permission_item= 'CreateUserLogin') AND role = 'CLIENT_MGR'""")
		sql("""DELETE FROM role_permissions WHERE
			permission_id = (select id from permissions where permission_group = 'USER' and permission_item= 'EditUserLogin') AND role = 'CLIENT_MGR'""")
		sql("""DELETE FROM role_permissions WHERE
			permission_id = (select id from permissions where permission_group = 'PROJECT' and permission_item= 'ProjectEditView') AND role = 'CLIENT_MGR'""")
		sql("""DELETE FROM role_permissions WHERE
			permission_id = (select id from permissions where permission_group = 'PROJECT' and permission_item= 'ProjectDelete') AND role = 'CLIENT_MGR'""")

		sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'USER' and permission_item= 'CreateUserLogin'), 'CLIENT_ADMIN')""")
		sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'USER' and permission_item= 'EditUserLogin'), 'CLIENT_ADMIN')""")
		sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'PROJECT' and permission_item= 'ProjectEditView'), 'CLIENT_ADMIN')""")
		sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'PROJECT' and permission_item= 'ProjectDelete'), 'CLIENT_ADMIN')""")
	}
}
