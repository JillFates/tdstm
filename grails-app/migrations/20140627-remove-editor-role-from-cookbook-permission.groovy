databaseChangeLog = {
	
	//This changeset will remove all unused rows from the table key_value
	changeSet(author: "lokanada", id: "20140627 TM-2910-1") {
		comment('Remove EDITOR permission from role_permission table from cookbook group')
		sql("""
			DELETE FROM role_permissions WHERE permission_id IN
				(SELECT id FROM permissions where permission_group='COOKBOOK') AND role='EDITOR' ;
		""")
	}
}