
/**
 * Add missing autoincrement fields
 */

databaseChangeLog = {
	changeSet(author: "eluna", id: "20140203 TM-2424-1") {
		comment('Add missing autoincrement fields')
		
		grailsChange {
			change {
				def newPermissions = [
					'ViewRecipe' : 'Can view the Cookbook recipe panel',
					'CreateRecipe' : 'Can create new recipes',
					'EditRecipe' : 'Can edit existing recipes',
					'DeleteRecipe' : 'Can delete a recipe',
					'ReleaseRecipe' : 'Can release a modified or new recipe into production',
					'GenerateTasks' : 'Can generate tasks from an existing recipe',
					'PublishTasks' : 'Can publish tasks that are presently unpublished',
					'DeleteTaskBatch' : 'Can delete generated tasks',
					'RefreshTaskBatch' : 'Can refresh tasks to update schedule, durations, assignment, etc'
				]
				
				def newRolePermissions = [
					'ViewRecipe' :       ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR', 'USER'],
					'CreateRecipe' :     ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR'],
					'EditRecipe' :       ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR'],
					'DeleteRecipe' :     ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR'],
					'ReleaseRecipe' :    ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR'],
					'GenerateTasks' :    ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR'],
					'PublishTasks' :     ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR'],
					'DeleteTaskBatch' :  ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR'],
					'RefreshTaskBatch' : ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR']
				]
				
				def group = 'COOKBOOK'
				
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
