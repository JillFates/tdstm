
class PermissionsController {

	def jdbcTemplate
	
	def index = {
		redirect(action:show,params:params)
	}
	
	def show = {
		def permissions = Permissions.withCriteria {
			and {
			   order('permissionGroup','asc')
			   order('permissionItem','asc')
			}
		}
		def rolePermissions = RolePermissions.list()
		[permissions:permissions]
	} 
	
	def edit = {
		def permissions = Permissions.withCriteria {
			and {
			   order('permissionGroup','asc')
			   order('permissionItem','asc')
			}
		}
		[permissions:permissions]
	}
	
	def update = {
		def paramList = params.column
		jdbcTemplate.update("delete from role_permissions")
		def permissions = Permissions.list()
		def roles = Permissions.Roles.values()
		permissions.each{ permission ->
			roles.each { role ->
				def param = params["role_${permission.id}_${role.toString()}"]
				if(param && param == "on"){
					def rolePermissions = new RolePermissions(
												role : role.toString(),
												permission : permission,
											)
					if(!rolePermissions.save(flush:true)){
						println"Error while updating rolePermissions : ${rolePermissions}"
						rolePermissions.errors.each { println it }
					}
				}
			}
		}
		for(int i : paramList){
			def permissionInstansce = Permissions.findById(i)
			if(permissionInstansce){
				permissionInstansce.description = params["description_"+i]
				if(!permissionInstansce.save(flush:true)){
					permissionInstansce.errors.allErrors.each {  
						println it
				    }
			    }
			}
		}
		redirect(action:show)
	}
}
