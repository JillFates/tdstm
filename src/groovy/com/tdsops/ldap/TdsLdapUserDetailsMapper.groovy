package com.tdsops.ldap

import com.tdsops.common.security.SecurityUtil
import net.transitionmanager.service.UserService
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.ldap.core.DirContextAdapter
import org.springframework.ldap.core.DirContextOperations
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper

/**
 * Created by jameskleeh on 2/1/17.
 */
class TdsLdapUserDetailsMapper implements UserDetailsContextMapper, GrailsApplicationAware {

    @Autowired
    UserService userService

    @Autowired
    UserDetailsService userDetailsService

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

        List<String> ldapRoles = authorities.collect { it.authority }
        List<String> roles = []
        Map<String, String> roleMap = [:]
        ((Map)domain.roleMap).entrySet().each { Map.Entry<String,String> entry ->
            roleMap.put(entry.value.replaceFirst(/(c|C)(n|N)=/,'').toUpperCase(), entry.key)
        }
        ldapRoles.each { String role ->
            if (roleMap.containsKey(role)) {
                roles.add(roleMap.get(role))
            }
        }

        userInfo.roles = roles

        userService.findOrProvisionUser(userInfo, ldap, authority)

        userDetailsService.loadUserByUsername(username)
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
