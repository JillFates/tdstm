package net.transitionmanager.service

import groovy.sql.Sql

class DatabaseMigrationService implements ServiceMethods {

	// The DB Migration Plugin is responsible for managing the transactions
	static transactional = false

	/**
	 * Utility method used to add permissions to the security tables
	 * @param sql - the SQL connection from the migration script
	 * @param perms - a map that consists of the permission properties and the roles to assign the permission to
	 */
	void addPermissions(Sql sql, Map perms) {
		assert perms
		Map firstItem = perms.iterator().next().getValue()
		if (firstItem.containsKey('group') && firstItem.group != 'NONE') {
			addPermissionsWithGroup(sql, perms)
		} else {
			addPermissionsWithoutGroup(sql, perms)
		}
	}

	/**
	 * Method used to add permissions to the security tables BEFORE we dropped the dependency_group column
	 * @param sql - the SQL connection from the migration script
	 * @param perms - a map that consists of the permission properties and the roles to assign the permission to
	 */
	private void addPermissionsWithGroup(Sql sql, Map perms) {
		String addPermSQL = '''INSERT INTO permissions (permission_group, permission_item, description)
			VALUES (:group, :item, :description)'''

		String assocToRoleSQL = '''INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group=:group and permission_item=:item), :role)'''

		perms.each { item, map ->
			Map queryParams = [group:map.group, item:item, description:map.description]
			sql.execute(addPermSQL, queryParams)

			queryParams = [item:item, group:map.group]
			map.roles.each { role ->
				queryParams.role = role
				sql.execute(assocToRoleSQL, queryParams)
			}
		}
	}

	/**
	 * Method used to add permissions to the security tables AFTER we dropped the dependency_group column
	 * @param sql - the SQL connection from the migration script
	 * @param perms - a map that consists of the permission properties and the roles to assign the permission to
	 */
	private void addPermissionsWithoutGroup(Sql sql, Map perms) {
		assert perms

		String addPermSQL = "INSERT INTO permissions (permission_item, description) VALUES (:item, :description)"

		String assocToRoleSQL = """INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_item=:item), :role)"""

		perms.each { item, map ->
			Map queryParams = [item:item, description:map.description]
			sql.execute(addPermSQL, queryParams)

			queryParams = [item:item]
			map.roles.each { role ->
				queryParams.role = role
				sql.execute(assocToRoleSQL, queryParams)
			}
		}
	}

	/**
	 * Utility method used to change the name of a permissions in the security tables
	 * @param sql - the SQL connection from the migration script
	 * @param perms - a map that consists of the permission's old names, new names, and new descriptions if needed
	 */
	void renamePermissions(Sql sql, Map perms) {
		assert perms

		String RenamePermSQL = """UPDATE permissions
			SET permission_item = :newName
			WHERE permission_item = :oldName"""

		String RenamePermAndDescriptionSQL = """UPDATE permissions
			SET permission_item = :newName,
			description = :newDescription
			WHERE permission_item = :oldName"""

		perms.each { newName, data ->
			def oldName = data.oldName ? data.oldName : newName
			if (data.description) {
				Map queryParams = [oldName:oldName, newName:newName, newDescription:data.description]
				sql.execute(RenamePermAndDescriptionSQL, queryParams)
			} else {
				Map queryParams = [oldName:oldName, newName:newName]
				sql.execute(renamePermSQL, queryParams)
			}
		}
	}

	/**
	 * Utility method used to remove permissions from the security tables
	 * @param sql - the SQL connection from the migration script
	 * @param perms - a list of the names of permissions to remove
	 */
	void removePermissions(Sql sql, List<String> perms) {
		assert perms

		String RemovePermSQL = """DELETE FROM permissions WHERE permission_item = :permName"""

		String RemoveRolesSQL = """DELETE FROM role_permissions WHERE permission_id = (SELECT id FROM permissions WHERE permission_item = :permName)"""

		perms.each { item ->
			sql.execute(RemoveRolesSQL, [permName:item])
			sql.execute(RemovePermSQL, [permName:item])
		}
	}
}
