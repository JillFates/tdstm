package com.tdsops.ldap

import com.tdsops.common.security.SecurityUtil
import com.tdsops.common.security.spring.TdsPreAuthenticationChecks
import groovy.util.logging.Slf4j
import net.transitionmanager.security.UserLogin
import net.transitionmanager.service.UserService
import grails.core.GrailsApplication
import grails.core.support.GrailsApplicationAware
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.ldap.core.DirContextAdapter
import org.springframework.ldap.core.DirContextOperations
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper

@Slf4j
class TdsLdapUserDetailsMapper implements UserDetailsContextMapper, GrailsApplicationAware {

    @Autowired
    UserService userService

    @Autowired
    UserDetailsService userDetailsService

    @Autowired
    TdsPreAuthenticationChecks preAuthenticationChecks

    ConfigObject ldap

    @Override
    UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {

        String authority = ctx.getStringAttribute(TdsBindAuthenticator.SOURCE_KEY)
        Map domain = (Map)ldap.domains[authority]
        String company = domain.company
        Map userInfo = [:]

        userInfo.companyId = company
        userInfo.username = username
        userInfo.firstName = ctx.getStringAttribute('givenname') ?: ''
        userInfo.lastName = ctx.getStringAttribute('sn') ?: ''
        userInfo.fullName = ctx.getStringAttribute('cn') ?: ''
        userInfo.email = ctx.getStringAttribute('mail') ?: ''
        userInfo.telephone = ctx.getStringAttribute('telephonenumber') ?: ''
        userInfo.mobile = ctx.getStringAttribute('mobile') ?: ''
        String objectGuid = ctx.getStringAttribute('objectguid') ?: ''
        String distinguishedName = ctx.getStringAttribute('distinguishedname') ?: ''
        userInfo.guid = (objectGuid ? SecurityUtil.guidToString(objectGuid) : distinguishedName)

        // TM-7169 - Use the name attribute if givename wasn't populated
        if (!userInfo.firstName && !userInfo.fullName) {
             userInfo.fullName = ctx.getStringAttribute('name') ?: ''
        }

        // User Security Roles will be assigned one of two ways:
        //
        // 1. When domain.updateRoles == true
        //    Then use LDAP roles assigned to user that are crossed references through the domain.roleMap
        //
        // 2. When domain.updateRoles == false
        //    Then the user when new is assigned the domain.defaultRole
        //    And when user is pre-existing their role(s) should be loaded from PartyRole appropriately

        List<String> roles = []
        List<String> ldapRoles = authorities?.collect { it.authority }
        if (domain.updateRoles && domain.updateRoles == true) {
            Map<String, String> roleMap = [:]
            ((Map) domain.roleMap)?.entrySet()?.each { Map.Entry<String, String> entry ->
                roleMap.put(entry.value.replaceFirst(/(c|C)(n|N)=/, '').toUpperCase(), entry.key)
            }
            ldapRoles.each { String role ->
                if (roleMap.containsKey(role)) {
                    roles.add(roleMap.get(role))
                }
            }
        } else {
            if (domain.defaultRole) {
                roles.add(domain.defaultRole)
            }
        }

        if (roles.empty) {
            String msg = "User ${username} has no roles defined in the roleMap. LDAP roles returned: ${ldapRoles}"
            if (ldap.debug == true) {
                log.debug(msg)
            }
            throw new NoRolesException(msg)
        }

        userInfo.roles = roles

        UserLogin userLogin = userService.findOrProvisionUser(userInfo, ldap, authority)

        UserDetails userDetails = userDetailsService.loadUserByUsername(username)
        preAuthenticationChecks.check(userDetails)

        if (ldap.debug) {
            log.debug('Successfully mapped ldap context to user for username: {} and LDAP roles: {}', username, ldapRoles)
            log.debug('UserLogin: {}', userLogin.toString())
            log.debug('UserDetails: {}', userDetails.toString())
        }
        userDetails
    }

    @Override
    void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
        //no op
    }

    @Override
    void setGrailsApplication(GrailsApplication grailsApplication) {
        ldap = grailsApplication.config.tdstm.security.ldap
    }
}
