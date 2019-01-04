databaseChangeLog = {	
	changeSet(author: "dscarpa", id: "20150409 TM-3801-1") {
		comment('Add new Delete and View permissions for userLogin')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 'select count(*) from permissions where permission_group="USER" and (permission_item = "UserLoginDelete" OR permission_item = "UserLoginView") ')
		}
		sql("INSERT INTO permissions (permission_group, permission_item, description) VALUES ('USER', 'UserLoginDelete','Ability to delete User accounts')")
		sql("INSERT INTO permissions (permission_group, permission_item, description) VALUES ('USER', 'UserLoginView','Ability to view User accounts list and details')")

		['ADMIN', 'CLIENT_ADMIN'].each {
			sql("""INSERT INTO role_permissions (permission_id, role) VALUES
				((select id from permissions where permission_group = 'USER' and permission_item= 'UserLoginDelete'), '$it')""")
		}

		['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR'].each {
			sql("""INSERT INTO role_permissions (permission_id, role) VALUES
				((select id from permissions where permission_group = 'USER' and permission_item= 'UserLoginView'), '$it')""")
		}
	}
}
