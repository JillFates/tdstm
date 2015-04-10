/**
 * Removes user login permissions from CLIEN_ADMIN role
 */
databaseChangeLog = {
	
	changeSet(author: "dscarpa", id: "20150409 TM-3801-2") {
		comment('Removes user login permissions from CLIEN_ADMIN role')
		sql("""DELETE FROM role_permissions WHERE
			permission_id = (select id from permissions where permission_group = 'USER' and permission_item= 'CreateUserLogin') AND role = 'CLIENT_ADMIN'""")
		sql("""DELETE FROM role_permissions WHERE
			permission_id = (select id from permissions where permission_group = 'USER' and permission_item= 'EditUserLogin') AND role = 'CLIENT_ADMIN'""")
		sql("""DELETE FROM role_permissions WHERE
			permission_id = (select id from permissions where permission_group = 'PROJECT' and permission_item= 'ProjectEditView') AND role = 'CLIENT_ADMIN'""")
		sql("""DELETE FROM role_permissions WHERE
			permission_id = (select id from permissions where permission_group = 'PROJECT' and permission_item= 'ProjectDelete') AND role = 'CLIENT_ADMIN'""")
	}
}