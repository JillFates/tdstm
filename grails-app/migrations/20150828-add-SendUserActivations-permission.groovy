/**
 * Adds a new permission 'PROJECT/SendUserActivations' and assigns it to 'ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR'.
 */

databaseChangeLog = {
	
	// this changeset is used to add 'ChangePendingStatus' permission item in permission table and assign 'ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR' and 'SUPERVISOR' by default.
	changeSet(author: "arecordon", id: "20150828 TM-4098") {
		comment('Add "SendUserActivations" permission in permission table')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 'select count(*) from permissions where permission_group="PROJECT" and permission_item = "SendUserActivations"')
		}
		sql("INSERT INTO permissions (permission_group, permission_item ) VALUES ('PROJECT', 'SendUserActivations')")
		def importRoles=['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR']
		importRoles.each{role->
			sql("""INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group = 'PROJECT' and permission_item= 'SendUserActivations'), '${role}')""")
		}
	}
}
