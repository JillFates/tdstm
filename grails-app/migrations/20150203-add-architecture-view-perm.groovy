
/**
 * Add missing autoincrement fields
 */

databaseChangeLog = {
	changeSet(author: "arecordon", id: "20150203 TM-3685") {
		comment('Can view the Architecture Graph')
		
		grailsChange {
			change {
				def newPermissions = [
					'ArchitectureView' : 'Can view the Architecture Graph',
				]
				
				def newRolePermissions = [
					'ArchitectureView' :       ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'EDITOR', 'SUPERVISOR', 'USER'],
				]

				
				def group = 'ASSETENTITY'
				
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
