package com.tdsops.common.security.spring

import grails.plugin.springsecurity.userdetails.GrailsUserDetailsService
import grails.plugin.springsecurity.userdetails.NoStackUsernameNotFoundException
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import net.transitionmanager.security.UserLogin
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException

@CompileStatic
@Slf4j(value='logger')
@Transactional(readOnly = true, noRollbackFor = [IllegalArgumentException, UsernameNotFoundException])
class TdsUserDetailsService implements GrailsUserDetailsService {

	UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		loadUserByUsername username, true
	}

	UserDetails loadUserByUsername(String username, boolean loadRoles) throws UsernameNotFoundException {
		// checkUserLoginExist

		UserLogin userLogin = UserLogin.findWhere(username: username)
		if (!userLogin) {
			logger.info 'loadUserByUsername() User not found: {}', username
			throw new NoStackUsernameNotFoundException()
		}

		Collection<GrantedAuthority> authorities = []
		Collection<String> permissions = []
		if (loadRoles) {
			Collection<String> roleNames = (Collection) UserLogin.executeQuery('''
				select roleType.id from PartyRole
				where party.id=:pid''', [pid: userLogin.person.id])

			if (roleNames) {
				authorities = (Collection) roleNames.collect {Object role-> new SimpleGrantedAuthority((String)role) }

				String query = '''
					select distinct permissionItem from Permissions
					where id in (select permission from RolePermissions
					             where role in (:roleNames))
					'''
				// String query = 'select permissionItem from Permissions where id in (select permission from RolePermissions where role in (:roleNames))'
				permissions = (Collection) UserLogin.executeQuery(query, [roleNames: roleNames]).unique().sort()
			} else {
				logger.info 'loadUserByUsername() No security roles found for user: {}', username
			}
		}

		new TdsUserDetails(
			userLogin.username,
			userLogin.password,
			!userLogin.isDisabled(), // enabled
			!userLogin.hasExpired(), // accountNonExpired
			!userLogin.hasPasswordExpired(), //credentialsNonExpired
			!userLogin.isLockedOut(), // accountNonLocked
			authorities,
			(long) userLogin.id,
			(long) userLogin.person.id,
			permissions)
	}
}
