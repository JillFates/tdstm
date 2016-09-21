import groovy.sql.Sql

class DatabaseMigrationService {

	// The DB Migration Plugin is responsible for managing the transactions
	static transactional = false

	/**
	 * Utility method used to add permissions to the security tables
	 * @param sql - the SQL connection from the migration script
	 * @param perms - a map that consists of the permission properties and the roles to assign the permission to
	 */
	void addPermissions(Sql sql, Map perms) {
		assert perms

		String addPermSQL = """INSERT INTO permissions (permission_group, permission_item, description)
			VALUES (:group, :item, :description)"""

		String assocToRoleSQL = """INSERT INTO role_permissions (permission_id, role) VALUES
			((select id from permissions where permission_group=:group and permission_item=:item), :role)"""

		perms.each { item, map ->
			Map queryParams = [group:map.group, item:item, description:map.description]
			sql.execute(addPermSQL, queryParams)

			queryParams = [item:item, group: map.group]
			map.roles.each { role ->
				queryParams.role = role
				sql.execute(assocToRoleSQL, queryParams)
			}
		}
	}
}
