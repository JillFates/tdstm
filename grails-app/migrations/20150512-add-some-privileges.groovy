databaseChangeLog = {	
	// Add priv CreateUserLogin, EditUserLogin, ProjectEditView, EditProjectFieldSettings, EditProjectStaff to CLIENT_MGR
	changeSet(author: "jmartin", id: "20150512 TM-3801-1") {
		comment('Add CreateUserLogin privilege to CLIENT_MGR role')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', "SELECT count(*) FROM role_permissions rp JOIN permissions p ON p.id = rp.permission_id WHERE p.permission_item='CreateUserLogin' AND rp.role='CLIENT_MGR'")
		}
		sql("INSERT INTO role_permissions (permission_id, role) VALUES ((select id from permissions where permission_item= 'UserLoginView'), 'CLIENT_MGR')")
	}

	changeSet(author: "jmartin", id: "20150512 TM-3801-2") {
		comment('Add EditUserLogin privilege to CLIENT_MGR role')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', "SELECT count(*) FROM role_permissions rp JOIN permissions p ON p.id = rp.permission_id WHERE p.permission_item='EditUserLogin' AND rp.role='CLIENT_MGR'")
		}
		sql("INSERT INTO role_permissions (permission_id, role) VALUES ((select id from permissions where permission_item= 'EditUserLogin'), 'CLIENT_MGR')")
	}

	changeSet(author: "jmartin", id: "20150512 TM-3801-3") {
		comment('Add ProjectEditView privilege to CLIENT_MGR role')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', "SELECT count(*) FROM role_permissions rp JOIN permissions p ON p.id = rp.permission_id WHERE p.permission_item='ProjectEditView' AND rp.role='CLIENT_MGR'")
		}
		sql("INSERT INTO role_permissions (permission_id, role) VALUES ((select id from permissions where permission_item= 'ProjectEditView'), 'CLIENT_MGR')")
	}

	changeSet(author: "jmartin", id: "20150512 TM-3801-4") {
		comment('Add EditProjectFieldSettings privilege to CLIENT_MGR role')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', "SELECT count(*) FROM role_permissions rp JOIN permissions p ON p.id = rp.permission_id WHERE p.permission_item='EditProjectFieldSettings' AND rp.role='CLIENT_MGR'")
		}
		sql("INSERT INTO role_permissions (permission_id, role) VALUES ((select id from permissions where permission_item= 'EditProjectFieldSettings'), 'CLIENT_MGR')")
	}

	changeSet(author: "jmartin", id: "20150512 TM-3801-5") {
		comment('Add EditProjectStaff privilege to CLIENT_MGR role')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', "SELECT count(*) FROM role_permissions rp JOIN permissions p ON p.id = rp.permission_id WHERE p.permission_item='EditProjectStaff' AND rp.role='CLIENT_MGR'")
		}
		sql("INSERT INTO role_permissions (permission_id, role) VALUES ((select id from permissions where permission_item= 'EditProjectStaff'), 'CLIENT_MGR')")
	}

}
