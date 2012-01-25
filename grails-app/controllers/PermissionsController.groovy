
class PermissionsController {

	def jdbcTemplate
	
	def index = {
		redirect(action:show,params:params)
	}
	
	def show = {
		def permissions = Permissions.list()
		def rolePermissions = RolePermissions.list()
		[permissions:permissions]
	} 
	
	def edit = {
		def permissions = Permissions.list()
		[permissions:permissions]
	}
	
	def update = {
		jdbcTemplate.update("delete from role_permissions")
		def permissions = Permissions.list()
		def roles = Permissions.Roles.values()
		permissions.each{ permission ->
			roles.each { role ->
				def param = params["role_${permission.id}_${role.id}"]
				if(param && param == "on"){
					def rolePermissions = new RolePermissions(
												role : role,
												permission : permission
											)
					if(!rolePermissions.save(flush:true)){
						println"Error while updating rolePermissions : ${rolePermissions}"
						rolePermissions.errors.each { println it }
					}
				}
			}
		}
		redirect(action:show)
	}
}
