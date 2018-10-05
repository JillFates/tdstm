package net.transitionmanager.service

import grails.transaction.Transactional
import net.transitionmanager.domain.Permissions
import net.transitionmanager.domain.RolePermissions
import org.springframework.jdbc.core.JdbcTemplate

@Transactional
class PermissionsService {

	JdbcTemplate jdbcTemplate

	/**
	 * Retrieves a list of current available {@code Permissions} from Database
	 * @return a list of {@code Permissions}
	 */
	List<Permissions> findAll(){
		return Permissions.withCriteria {
			and {
				order('permissionItem','asc')
			}
		}
	}

	/**
	 * Updates all {@code Permissions}
	 * @param params a Map with request parameters
	 */
	void update(Map params){

		def paramList = params.column
		jdbcTemplate.update("delete from role_permissions")
		for (Permissions permission in Permissions.list()) {
			for (String role in Permissions.Roles.NAMES) {
				def param = params['role_' + permission.id + '_' + role]
				if (param == "on") {
					def rolePermissions = new RolePermissions(role: role, permission: permission)

					if (!rolePermissions.save()) {
						println "Error while updating rolePermissions : $rolePermissions"
						rolePermissions.errors.each { println it }
					}
				}
			}
		}
		for(String id in paramList){
			Permissions permissions = Permissions.get(id)
			if(permissions){
				permissions.description = params["description_"+id]
				if(!permissions.save(flush:true)){
					permissions.errors.allErrors.each {
						println it
					}
				}
			}
		}
	}

}
