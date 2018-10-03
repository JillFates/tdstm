package net.transitionmanager.service

import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.JsonUtil
import groovy.sql.Sql
import grails.transaction.Transactional
import net.transitionmanager.domain.Project
import org.codehaus.groovy.grails.web.json.JSONObject

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

	/**
	 * Update a JSON field for a list of domain objects by executing the given script.
	 * This script must return the transformed JSON so this method can update the corresponding
	 * field appropriately.
	 *
	 * @param domainObjects - list of domain objects
	 * @param jsonField - name of the field to be updated.
	 * @param changeScript - the script to be executed.
	 */
	@Transactional
	void updateJsonObjects(List domainObjects, String jsonField, Closure changeScript) {
		for (domainObject in domainObjects) {
			// Parse the given field to a JSON object.
			JSONObject originalJson = JsonUtil.parseJson(domainObject[jsonField])
			// Execute the given script using the JSON object.
			JSONObject transformedJson = changeScript(originalJson)
			// Assign the result of the script execution to the same field, which may be a different object.
			domainObject[jsonField] = JsonUtil.validateJsonAndConvertToString(transformedJson)
			// Save the changes or throw an exception.
			if (!domainObject.save(flush:true, failOnError: true)) {
				throw new DomainUpdateException(GormUtil.allErrorsString(domainObject))
			}
		}
	}
    /**
     *
     * Adds a new System Dataview defining the id parameter a the Dataview spec in JSON format
     *
     * @param sql - the SQL connection from the migration script
     * @param id - a new Id between 1-1000 to be used in Dataview creation
     * @param name - the name of the new system Dataview
     * @param jsonSpec - a json spec for the new Dataview.     */
	void addSystemView(Sql sql, Long id, String name, String jsonSpec) {

        List params = [id, name, Project.DEFAULT_PROJECT_ID, true, new Date(), jsonSpec]
        sql.execute 'insert into dataview(id, name, project_id, is_system, date_created, report_schema) values(?,?,?,?,?,?)',  params
	}
}
