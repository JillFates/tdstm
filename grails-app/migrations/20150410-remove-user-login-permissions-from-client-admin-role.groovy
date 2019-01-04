databaseChangeLog = {
	
	changeSet(author: "dscarpa", id: "20150409 TM-3801-2") {
		comment('Remove project and userlogin delete permissions from CLIENT_MGR role')
		sql("""DELETE FROM role_permissions WHERE
			permission_id = (select id from permissions where permission_group = 'USER' and permission_item= 'EditUserLogin') AND role = 'CLIENT_MGR'""")
		sql("""DELETE FROM role_permissions WHERE
			permission_id = (select id from permissions where permission_group = 'PROJECT' and permission_item= 'ProjectDelete') AND role = 'CLIENT_MGR'""")
	}
}
