/* This script adds the UnlockUserLogin and ResetUserPassword permissions */

databaseChangeLog = {	
	// add UnlockUserLogin permission
	changeSet(author: "rmacfarlane", id: "20150810 TM-4075-1") {
		comment('Add UnlockUserLogin permission')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', "SELECT count(*) FROM permissions p WHERE p.permission_item='UnlockUserLogin'")
		}
		sql("INSERT INTO permissions (permission_group, permission_item, description) VALUES ('USER', 'UnlockUserLogin', 'Can unlock local user accounts after failed login attempts')")
		sql("INSERT INTO role_permissions (permission_id, role) VALUES ((SELECT id FROM permissions WHERE permission_item = 'UnlockUserLogin'), 'ADMIN')")
		sql("INSERT INTO role_permissions (permission_id, role) VALUES ((SELECT id FROM permissions WHERE permission_item = 'UnlockUserLogin'), 'CLIENT_ADMIN')")
	}

	// add ResetUserPassword permission
	changeSet(author: "rmacfarlane", id: "20150810 TM-4075-2") {
		comment('Add ResetUserPassword permission')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', "SELECT count(*) FROM permissions p WHERE p.permission_item='ResetUserPassword'")
		}
		sql("INSERT INTO permissions (permission_group, permission_item, description) VALUES ('USER', 'ResetUserPassword', 'Can invoke the password reset process for local user accounts')")
		sql("INSERT INTO role_permissions (permission_id, role) VALUES ((SELECT id FROM permissions WHERE permission_item = 'ResetUserPassword'), 'ADMIN')")
		sql("INSERT INTO role_permissions (permission_id, role) VALUES ((SELECT id FROM permissions WHERE permission_item = 'ResetUserPassword'), 'CLIENT_ADMIN')")
	}
}
