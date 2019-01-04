databaseChangeLog = {
	
	//This changeset will remove EDITOR permission from role_permission table from cookbook group
	changeSet(author: "lokanada", id: "20140627 TM-2910-1") {
		comment('Remove EDITOR permission from role_permission table from cookbook group')
		sql("""
			DELETE FROM role_permissions WHERE permission_id IN
				(SELECT id FROM permissions where permission_group='COOKBOOK') AND role='EDITOR' ;
		""")
	}
	
	//This changeset will remove USER permission from role_permission table from cookbook group
	changeSet(author: "lokanada", id: "20140627 TM-2910-2") {
		comment('Remove USER permission from role_permission table from cookbook group')
		sql("""
			DELETE FROM role_permissions WHERE permission_id IN
				(SELECT id FROM permissions where permission_group='COOKBOOK') AND role='USER' ;
		""")
	}
}
