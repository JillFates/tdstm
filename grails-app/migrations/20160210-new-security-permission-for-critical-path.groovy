databaseChangeLog = {	
	changeSet(author: "dscarpa", id: "20160210 TM-4626-1") {
		comment('Critical Path Data of an event permissions')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 'select count(*) from permissions where permission_group="TASK" and permission_item = "CriticalPathExport"')
		}
		sql("INSERT INTO permissions (permission_group, permission_item, description) VALUES ('TASK', 'CriticalPathExport','Can access the Critical Path Data of an event')")

		['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR'].each {
			sql("""INSERT INTO role_permissions (permission_id, role) VALUES
				((select id from permissions where permission_group = 'TASK' and permission_item= 'CriticalPathExport'), '$it')""")
		}
	}
}
