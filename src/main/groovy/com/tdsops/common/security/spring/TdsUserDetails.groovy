package com.tdsops.common.security.spring

import grails.plugin.springsecurity.userdetails.GrailsUser
import groovy.transform.CompileStatic
import org.springframework.security.core.GrantedAuthority

/**
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
class TdsUserDetails extends GrailsUser {

	final long personId
	final String salt

	// since permission strings are cached at login it's important to update the cached values when they change
	final Set<String> permissions = []

	TdsUserDetails(
		String username,
		String password,
		boolean enabled,
		boolean accountNonExpired,
		boolean credentialsNonExpired,
		boolean accountNonLocked,
		Collection<GrantedAuthority> authorities,
		long id,
		long personId,
		String salt,
		Collection<String> permissions)
	{
		super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities, id)
		this.permissions.addAll permissions
		this.personId = personId
		this.salt = salt
	}

	boolean hasRole(String roleName) {
		String authority = roleName.startsWith('ROLE_') ? roleName : 'ROLE_' + roleName
		authorities.any { it.authority == authority }
	}

	boolean hasPermission(String permission) {
		permission in permissions
	}
}
