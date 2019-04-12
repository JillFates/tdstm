package net.transitionmanager.security

import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.security.Permission

/**
 * @author oluna@tdsi.com
 */
@Secured('isAuthenticated()')
@Slf4j
class WsSecurityController implements ControllerMethods {

	@HasPermission(Permission.UserGeneralAccess)
	def permissions(){
		renderSuccessJson(securityService.currentUserPermissionMap())
	}
}
