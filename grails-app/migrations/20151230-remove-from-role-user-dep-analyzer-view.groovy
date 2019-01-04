/**
 * This changeset will remove from USER role permission DepAnalyzerView
 */
databaseChangeLog = {
		
	//This changeset will remove from USER role permission DepAnalyzerView
	changeSet(author: "dscarpa", id: "20151230 TM-4328-1") {
		comment('Remove from role USER the permission DepAnalyzerView')
		sql("""
			DELETE FROM role_permissions WHERE
			permission_id = (select id from permissions where permission_group = 'ASSETENTITY' and permission_item= 'DepAnalyzerView') AND role = 'USER'
		""")
	}
}
