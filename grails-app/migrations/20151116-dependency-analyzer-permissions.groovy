databaseChangeLog = {	
	changeSet(author: "dscarpa", id: "20151116 TM-4213-1") {
		comment('Dependecy analyzer permissions')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 'select count(*) from permissions where permission_group="ASSETENTITY" and (permission_item = "DepAnalyzerView" OR permission_item = "DepAnalyzerGenerate") ')
		}
		sql("INSERT INTO permissions (permission_group, permission_item, description) VALUES ('ASSETENTITY', 'DepAnalyzerView','Can view the Dependency Analyzer')")
		sql("INSERT INTO permissions (permission_group, permission_item, description) VALUES ('ASSETENTITY', 'DepAnalyzerGenerate','Can generate dependency groups')")

		['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR', 'USER'].each {
			sql("""INSERT INTO role_permissions (permission_id, role) VALUES
				((select id from permissions where permission_group = 'ASSETENTITY' and permission_item= 'DepAnalyzerView'), '$it')""")
		}

		['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR'].each {
			sql("""INSERT INTO role_permissions (permission_id, role) VALUES
				((select id from permissions where permission_group = 'ASSETENTITY' and permission_item= 'DepAnalyzerGenerate'), '$it')""")
		}
	}
}
