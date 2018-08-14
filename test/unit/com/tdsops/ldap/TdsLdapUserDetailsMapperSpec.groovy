package com.tdsops.ldap

import com.tdsops.common.security.spring.TdsPreAuthenticationChecks
import net.transitionmanager.service.UserService
import org.springframework.ldap.core.DirContextAdapter
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetailsService
import spock.lang.Specification
import spock.lang.Subject

@Subject([TdsLdapUserDetailsMapper])
class TdsLdapUserDetailsMapperSpec extends Specification {

    void "test mapUserFromContext"() {
        given:
			def domain = [
					debug:true,
					domains: [
							TDS: [
									updateRoles: true,
									company: 20,
									roleMap: [
											user: 'CN=App_TMDev_User',
											admin: 'CN=App_TMDev_Admin',
											editor: 'CN=App_TMDev_Editor',
											client_mgr: 'CN=App_TMDev_ClientMgr',
											supervisor: 'CN=App_TMDev_Supervisor',
											client_admin: 'CN=App_TMDev_ClientAdmin'
									]
							]
					]
			]
			def config = new ConfigObject(domain)
			def context = new DirContextAdapter()
			context.setAttributeValue(TdsBindAuthenticator.SOURCE_KEY, "TDS")
			context.setAttributeValue('givenname', "Sally")
			context.setAttributeValue('sn', "Fields")
			context.setAttributeValue('cn', "Sally Fields")
			context.setAttributeValue('mail', "sally.fields@tds.com")
			context.setAttributeValue('mobile', "3308675309")
			context.setAttributeValue('objectguid', String.valueOf([65533,77,34,1,65533,2,65533,64,65533,96,65533,65533,65533] as char[]))
			def mapper = new TdsLdapUserDetailsMapper()
			mapper.userService = Mock(UserService)
			mapper.userDetailsService = Mock(UserDetailsService)
			mapper.preAuthenticationChecks = Mock(TdsPreAuthenticationChecks)
			mapper.ldap = config
			List<SimpleGrantedAuthority> ldapRoles = [new SimpleGrantedAuthority('APP_TMDEV_CLIENTMGR'), new SimpleGrantedAuthority('APP_TMDEV_EDITOR')]

        when:
        	mapper.mapUserFromContext(context, "foobar", ldapRoles)

        then:
			1 * mapper.userService.findOrProvisionUser({
				it.companyId == "20" &&
				it.username == "foobar" &&
				it.firstName == "Sally" &&
				it.lastName == "Fields" &&
				it.fullName == "Sally Fields" &&
				it.email == "sally.fields@tds.com" &&
				it.telephone == "" &&
				it.mobile == "3308675309" &&
				it.guid == "fd4d2201fd02fd40fd60fdfdfd" &&
				it.roles.size() == 2 &&
				it.roles.contains("editor") &&
				it.roles.contains("client_mgr")
			}, config, "TDS")
			1 * mapper.userDetailsService.loadUserByUsername("foobar")

    }

    void "test mapUserFromContext no roleMap and defaultRole provided and the user is new"() {
        given:
			def domain = [
					debug: true,
					domains: [
							TDS: [
									company: 20,
									updateRoles: false,
									defaultRole: 'client_mgr',
									roleMap: null
							]
					]
			]
			def config = new ConfigObject(domain)
			def context = new DirContextAdapter()
			context.setAttributeValue(TdsBindAuthenticator.SOURCE_KEY, "TDS")
			context.setAttributeValue('givenname', "Sally")
			context.setAttributeValue('sn', "Fields")
			context.setAttributeValue('cn', "Sally Fields")
			context.setAttributeValue('mail', "sally.fields@tds.com")
			context.setAttributeValue('mobile', "3308675309")
			context.setAttributeValue('objectguid', String.valueOf([65533,77,34,1,65533,2,65533,64,65533,96,65533,65533,65533] as char[]))
			def mapper = new TdsLdapUserDetailsMapper()
			mapper.userService = Mock(UserService)
			mapper.userDetailsService = Mock(UserDetailsService)
			mapper.preAuthenticationChecks = Mock(TdsPreAuthenticationChecks)
			mapper.ldap = config
			List<SimpleGrantedAuthority> ldapRoles = [new SimpleGrantedAuthority('APP_TMDEV_CLIENTMGR'), new SimpleGrantedAuthority('APP_TMDEV_EDITOR')]

        when:
        	mapper.mapUserFromContext(context, "newuser", ldapRoles)

        then:
			1 * mapper.userService.findOrProvisionUser({
				it.companyId == "20" &&
						it.username == "newuser" &&
						it.firstName == "Sally" &&
						it.lastName == "Fields" &&
						it.fullName == "Sally Fields" &&
						it.email == "sally.fields@tds.com" &&
						it.telephone == "" &&
						it.mobile == "3308675309" &&
						it.guid == "fd4d2201fd02fd40fd60fdfdfd" &&
						it.roles.size() == 1 &&
						it.roles.contains("client_mgr")
			}, config, "TDS")
			1 * mapper.userDetailsService.loadUserByUsername("newuser")
    }
}
