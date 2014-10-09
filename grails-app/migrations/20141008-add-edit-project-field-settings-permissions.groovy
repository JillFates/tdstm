/**
 * Add edit project fields settings permissions
 */
databaseChangeLog = {
	changeSet(author: "dscarpa", id: "20141008 TM-3175-1") {
		comment('Add edit project fields settings permissions')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', "select count(*) from permissions where permission_group='PROJECT' and permission_item='EditProjectFieldSettings'")
		}
		grailsChange {
			change {
				def newPermissions = [
					'EditProjectFieldSettings' : 'User can modify project field settings'
				]
				
				def newRolePermissions = [
					'EditProjectFieldSettings' :  ['ADMIN', 'CLIENT_ADMIN']
				]
				
				def group = 'PROJECT'
				
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
