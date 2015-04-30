/**
 * Add new permissions for person and project staff
 */
databaseChangeLog = {

	changeSet(author: "dscarpa", id: "20150428 TM-3801-4") {
		comment('Add permission PersonDeleteView to roles: ADMIN, CLIENT_ADMIN, CLIENT_MGR')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 'select count(*) from permissions where permission_group="PERSON" and permission_item = "PersonDeleteView"')
		}
		sql("INSERT INTO permissions (permission_group, permission_item, description) VALUES ('PERSON', 'PersonDeleteView','Ability to delete a person')")

		sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'PERSON' and permission_item= 'PersonDeleteView'), 'ADMIN')""")
		sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'PERSON' and permission_item= 'PersonDeleteView'), 'CLIENT_ADMIN')""")
		sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'PERSON' and permission_item= 'PersonDeleteView'), 'CLIENT_MGR')""")
	}

	changeSet(author: "dscarpa", id: "20150428 TM-3801-5") {
		comment('Add permission PersonListView to roles: ADMIN, CLIENT_ADMIN, CLIENT_MGR')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 'select count(*) from permissions where permission_group="PERSON" and permission_item = "PersonListView"')
		}
		sql("INSERT INTO permissions (permission_group, permission_item, description) VALUES ('PERSON', 'PersonListView','Ability to delete a person')")

		sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'PERSON' and permission_item= 'PersonListView'), 'ADMIN')""")
		sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'PERSON' and permission_item= 'PersonListView'), 'CLIENT_ADMIN')""")
		sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'PERSON' and permission_item= 'PersonListView'), 'CLIENT_MGR')""")
	}

	changeSet(author: "dscarpa", id: "20150428 TM-3801-6") {
		comment('Add permission PersonListView to roles: ADMIN, CLIENT_ADMIN, CLIENT_MGR, SUPERVISOR, EDITOR, USER')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 'select count(*) from permissions where permission_group="PROJECT" and permission_item = "ProjectStaffList"')
		}
		sql("INSERT INTO permissions (permission_group, permission_item, description) VALUES ('PROJECT', 'ProjectStaffList','Ability to view list of project staff members')")

		sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'PROJECT' and permission_item= 'ProjectStaffList'), 'ADMIN')""")
		sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'PROJECT' and permission_item= 'ProjectStaffList'), 'CLIENT_ADMIN')""")
		sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'PROJECT' and permission_item= 'ProjectStaffList'), 'CLIENT_MGR')""")
		sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'PROJECT' and permission_item= 'ProjectStaffList'), 'SUPERVISOR')""")
		sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'PROJECT' and permission_item= 'ProjectStaffList'), 'EDITOR')""")
		sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'PROJECT' and permission_item= 'ProjectStaffList'), 'USER')""")
	}

	changeSet(author: "dscarpa", id: "20150428 TM-3801-7") {
		comment('Add permission ProjectStaffShow to roles: ADMIN, CLIENT_ADMIN, CLIENT_MGR, SUPERVISOR, EDITOR, USER')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 'select count(*) from permissions where permission_group="PROJECT" and permission_item = "ProjectStaffShow"')
		}
		sql("INSERT INTO permissions (permission_group, permission_item, description) VALUES ('PROJECT', 'ProjectStaffShow','Ability to view detail of project staff members')")

		sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'PROJECT' and permission_item= 'ProjectStaffShow'), 'ADMIN')""")
		sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'PROJECT' and permission_item= 'ProjectStaffShow'), 'CLIENT_ADMIN')""")
		sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'PROJECT' and permission_item= 'ProjectStaffShow'), 'CLIENT_MGR')""")
		sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'PROJECT' and permission_item= 'ProjectStaffShow'), 'SUPERVISOR')""")
		sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'PROJECT' and permission_item= 'ProjectStaffShow'), 'EDITOR')""")
		sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'PROJECT' and permission_item= 'ProjectStaffShow'), 'USER')""")
	}

	changeSet(author: "dscarpa", id: "20150428 TM-3801-8") {
		comment('Add permission BulkDeletePerson to roles: CLIENT_ADMIN')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', """
				select count(*) from role_permissions where 
					permission_id = (select id from permissions where permission_group = 'PERSON' and permission_item= 'BulkDeletePerson')
					AND role = 'CLIENT_ADMIN'
				""")
		}
		sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'PERSON' and permission_item= 'BulkDeletePerson'), 'CLIENT_ADMIN')""")
	}

}
