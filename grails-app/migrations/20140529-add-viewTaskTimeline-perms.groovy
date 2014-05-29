
/**
 * Add missing autoincrement fields
 */

databaseChangeLog = {
	changeSet(author: "jmartin", id: "20140529 TM-2751-1") {
		comment('Add role permission ViewTaskTimeline')
		
		grailsChange {
			change {
				def newPermissions = [
					'ViewTaskTimeline' : 'Can view the Task Timeline',
				]
				
				def newRolePermissions = [
					'ViewTaskTimeline' :       ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR'],
				]
				
				def group = 'TASK'
				
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
