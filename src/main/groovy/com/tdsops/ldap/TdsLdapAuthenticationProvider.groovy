package com.tdsops.ldap

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider

@InheritConstructors
@CompileStatic
class TdsLdapAuthenticationProvider extends LdapAuthenticationProvider {

    Object ldapDebug

    @Override
    Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            super.authenticate(authentication)
        } catch (InternalAuthenticationServiceException e) {
            if (ldapDebug == true) {
                println "Ignoring InternalAuthenticationServiceException:${e.cause.class} ${e.message}"
            }
            return null
        }
    }

}
