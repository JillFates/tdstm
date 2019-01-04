package com.tdsops.common.security.spring

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import net.transitionmanager.service.SecurityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.core.Authentication
import org.springframework.util.Assert

import javax.servlet.http.HttpServletRequest

/**
 * Implements the 'hasPermission' methods in SpEL for use in config attributes. The HasPermission
 * annotation will cover most cases since it's best to define the settings in the controllers, but
 * for static resources or other urls that can't or shouldn't be annotated it's possible to use SpEL
 * for those url patterns in controllerAnnotations.staticRules, e.g.
 *
 *    '/monitoring':     'hasPermission(request, "AdminUtilitiesAccess")'
 *
 * This will result in a call here with the current request as the 'target' and 'AdminUtilitiesAccess' as
 * the permission. The  url isn't passed to the hasPermission() method but it isn't needed; the
 * ConfigAttribute instances derived from the annotations and the static rules are stored with the
 * url pattern as the key, and a match between one of them and the current request url result in a
 * call being made here.
 *
 * The actual resolved url used for the lookups (it can be different from the 'real' url for url-mapped
 * requests) is stored as a request attribute and is used here for logging.
 *
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
@Slf4j(value='logger')
class TdsPermissionEvaluator implements PermissionEvaluator {

	private @Autowired SecurityService securityService

	boolean hasPermission(Authentication authentication, target, permission) {
		Assert.isInstanceOf(HttpServletRequest, target, 'Currently only HttpServletRequest is supported as a target')
		String url = ((HttpServletRequest) target).getAttribute(TdsAnnotationFilterInvocationDefinition.RESOLVED_URL_KEY)

		if (!securityService.loggedIn) {
			logger.debug 'Denying anonymous user access to {}', url
			return
		}

		Assert.isInstanceOf(String, permission, 'Permission must be a String')
		String permissionName = (String) permission

		TdsUserDetails principal = securityService.currentUserDetails
		Assert.notNull(principal, 'Authenticated user details should be a TdsUserDetails')
		boolean ok = principal.hasPermission permissionName
		
		if (ok) {
			logger.debug 'User {} has permission {} for {}', principal.username, permissionName, url
			true
		}
		else {
			logger.debug 'Denying user {} access to {}', principal.username, url
			false
		}
	}

	boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, permission) {
		throw new UnsupportedOperationException('Per-instance permission checks by type and id are not supported')
	}
}
