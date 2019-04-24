import com.tdsops.common.security.spring.HasPermission
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.Permissions
import net.transitionmanager.domain.RolePermissions
import net.transitionmanager.security.Permission
import com.tdssrc.grails.GormUtil
import org.springframework.jdbc.core.JdbcTemplate
import grails.transaction.Transactional
import grails.plugin.springsecurity.annotation.Secured

@Secured('isAuthenticated()')
class PermissionsController implements ControllerMethods {

	static defaultAction = 'list'

	JdbcTemplate jdbcTemplate

	@HasPermission(Permission.RolePermissionView)
	def show() {
		def permissions = Permissions.withCriteria {
			and {
			   order('permissionItem','asc')
			}
		}
		[permissions:permissions]
	}

	@HasPermission(Permission.RolePermissionEdit)
	def edit() {
		List permissions = Permissions.withCriteria {
			and {
				order('permissionItem','asc')
			}
		}
		[permissions:permissions]
	}

	@HasPermission(Permission.RolePermissionEdit)
	def update() {
		withForm {
			List paramList = params.column
			String errorMsg

			Permissions.withTransaction { status ->

				// Delete all role permissions (yikes - wtf??)
				// TODO : JPM 4/2019 : Change to play a lot nicer TM-12657
				jdbcTemplate.update("delete from role_permissions")

				for (Permissions permission in Permissions.list()) {
					for (String role in Permissions.Roles.NAMES) {
						String paramName = "role_${permission.id}_${role}"
						if (params[paramName] == 'on') {
							RolePermissions rolePermissions = new RolePermissions(role: role, permission: permission)
							if (!rolePermissions.save(flush: true)) {
								log.error "Error while updating permission=$permission, role=$role, error(s)=${GormUtil.allErrorsString(rolePermissions)}"
								errorMsg = "An error occurred while attempting to update the role permissions (permission=$permission, role=$role)"
								break
							}
						}
					}
					if (errorMsg) {
						break
					}
				}

				if (! errorMsg) {
					// Update the Permission Descriptions
					for (String id in paramList) {
						Permissions permissions = Permissions.get(id)
						if (permissions) {
							String newDescription = params["description_"+id]
							if (permissions.description != newDescription) {
								permissions.description = newDescription
								if (! permissions.save(flush:true)) {
									log.error "Error while updating description of permission=$permissions, error(s)=${GormUtil.allErrorsString(permissions)}"
									errorMsg = "An error occurred while updating description of permission ${permissions}"
									break
								}
							}
						}
					}
				}

				if (errorMsg) {
					status.setRollbackOnly()
				}
			}

			flash.message = errorMsg ?: 'Role permissions were updated successfully'

		}.invalidToken {
			flash.message = INVALID_CSRF_TOKEN
		}
			redirect(action:"show")
	}
}
