/**
 * This set of Database change that is required toto Rename "ShowMoveTechsAndAdmins" 
 * permission to "ViewSupervisorConsoles" in permission table and to add 
 * 'ViewTaskManager' permission item in permission table
 * and assign 'ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR' by default.
 */ 

databaseChangeLog = {
	
	// this changeset is used to Rename "ShowMoveTechsAndAdmins" permission to "ViewSupervisorConsoles" in permission table
	changeSet(author: "lokanada", id: "20140625 TM-2883-1") {
		comment('Rename "ShowMoveTechsAndAdmins" permission to "ViewSupervisorConsoles" in permission table')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'1', 'select count(*) from permissions where permission_group="CONSOLE" and permission_item = "ShowMoveTechsAndAdmins"')
		}
		sql("""UPDATE permissions SET permission_item = 'ViewSupervisorConsoles', 
			description ='View Supervisor admin and tech consoles' where permission_group='CONSOLE' 
			and permission_item='ShowMoveTechsAndAdmins';""")
	}
	
	
	// this changeset is used to add 'ViewTaskManager' permission item in permission table and assign 'ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR' by default.
	changeSet(author: "lokanada", id: "20140625 TM-2883-2") {
		comment('Add "ViewTaskManager" permission in permission table')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 'select count(*) from permissions where permission_group="TASK" and permission_item = "ViewTaskManager"')
		}
		
		sql("INSERT INTO permissions (permission_group, permission_item, description ) VALUES ('TASK', 'ViewTaskManager', 'View Task Manager')")
		
		def importRoles=['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR']
		importRoles.each{role->
			sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'TASK' and permission_item= 'ViewTaskManager'), '${role}')""")
		}
	}
}
