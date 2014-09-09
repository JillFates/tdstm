
/**
 * Add bulk delete permissions
 */
databaseChangeLog = {
	changeSet(author: "eluna", id: "20140908 TM-3207-1") {
		comment('Add bulk delete permissions')
		
		grailsChange {
			change {
				def newPermissions = [
					'BulkDeletePerson' : 'Can bulk delete person accounts'
				]
				
				def newRolePermissions = [
					'BulkDeletePerson' :       ['ADMIN']
				]
				
				def group = 'PERSON'
				
				for (e in newPermissions) {
					sql.execute("""INSERT INTO permissions(permission_group, permission_item, description) VALUES(${group}, ${e.key}, ${e.value})""")
	
				}
				
				for (e in newRolePermissions) {
					def permission = sql.firstRow("""SELECT id FROM permissions WHERE permission_group = ${group} AND permission_item = ${e.key}""")
					for (i in e.value) {
						def permissionId = permission.id.toString();
						sql.execute("""INSERT INTO role_permissions(permission_id, role) VALUES(${permissionId}, ${i})""")
					}
				}
			}
		}
	}
}
