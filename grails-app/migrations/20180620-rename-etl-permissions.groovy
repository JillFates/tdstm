databaseChangeLog = {

	changeSet(author: 'arecordon', id: 'TM-11006-1') {
		comment("Rename ETL Script related permissions to use 'ETL Script' instead of 'DataScript'.")
		grailsChange {
			change {
				List<Map<String, String>> permissionsList = [
					[
						newPermissionItem: 'ETLScriptCreate',
						oldPermissionItem: 'DataScriptCreate',
						description: 'Can create new ETL Scripts for import and exports.'
					],
					[
						newPermissionItem: 'ETLScriptDelete',
						oldPermissionItem: 'DataScriptDelete',
						description: 'Can delete existing ETL Scripts.'
					],
					[
						newPermissionItem: 'ETLScriptUpdate',
						oldPermissionItem: 'DataScriptUpdate',
						description: 'Can modify existing ETL Scripts.'
					],
					[
						newPermissionItem: 'ETLScriptView',
						oldPermissionItem: 'DataScriptView',
						description: 'Can view ETL Script dialogs and lists.'
					],
				]

				for (permissionMap in permissionsList) {
					sql.executeUpdate("""
						UPDATE permissions 
						SET permission_item = :newPermissionItem, description = :description
						WHERE permission_item = :oldPermissionItem
					""", permissionMap)
				}
			}
		}
	}
}
