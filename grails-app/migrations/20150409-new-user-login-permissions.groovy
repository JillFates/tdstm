/**
 * This script add new permissions for user login
 */
databaseChangeLog = {
	
	changeSet(author: "dscarpa", id: "20150409 TM-3801-1") {
		comment('Add new permissions for user login')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 'select count(*) from permissions where permission_group="USER" and (permission_item = "UserLoginDelete" OR permission_item = "UserLoginView") ')
		}
		sql("INSERT INTO permissions (permission_group, permission_item, description) VALUES ('USER', 'UserLoginDelete','Ability to delete User accounts')")
		sql("INSERT INTO permissions (permission_group, permission_item, description) VALUES ('USER', 'UserLoginView','Ability to view User accounts list and details')")
		sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'USER' and permission_item= 'UserLoginDelete'), 'ADMIN')""")
		sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'USER' and permission_item= 'UserLoginView'), 'ADMIN')""")
	}
}