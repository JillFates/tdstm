package net.transitionmanager.security

import grails.gorm.transactions.Transactional
import net.transitionmanager.security.Permissions
import net.transitionmanager.security.RolePermissions
import org.springframework.jdbc.core.JdbcTemplate

@Transactional
class PermissionsService {

	JdbcTemplate jdbcTemplate

	/**
	 * Retrieves a list of current available {@code Permissions} from Database
	 * @return a list of {@code Permissions}
	 */
	List<Permissions> findAll() {
		return Permissions.executeQuery('''
			SELECT distinct(p) FROM Permissions p
			LEFT JOIN p.rolePermissions
			ORDER BY p.permissionItem ASC
		''')
	}

	/**
	 * Updates all {@code Permissions}
	 * @param params a Map with request parameters
	 */
	void update(Map params){

		def paramList = params.column
		RolePermissions.executeUpdate('delete from RolePermissions')

		for (Permissions permission in Permissions.list()) {

			for (String role in Permissions.Roles.NAMES) {
				def param = params['role_' + permission.id + '_' + role]

				if (param == "on") {
					def rolePermissions = new RolePermissions(role: role, permission: permission)
					rolePermissions.save()
				}
			}
		}

		for(String id in paramList){
			Permissions permissions = Permissions.get(id)

			if(permissions){
				permissions.description = params["description_"+id]
				permissions.save()
			}
		}
	}

}
