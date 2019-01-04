package com.tdsops.ldap

import groovy.transform.CompileStatic
import org.springframework.ldap.core.DirContextOperations
import org.springframework.ldap.core.support.BaseLdapPathContextSource
import org.springframework.security.core.Authentication
import org.springframework.security.ldap.authentication.BindAuthenticator

@CompileStatic
class TdsBindAuthenticator extends BindAuthenticator {

    static final String SOURCE_KEY = "TDS_AUTH_SOURCE"

    String sourceDomain

    TdsBindAuthenticator(BaseLdapPathContextSource contextSource) {
        super(contextSource)
    }

    @Override
    DirContextOperations authenticate(Authentication authentication) {
        DirContextOperations context = super.authenticate(authentication)
        context.setAttributeValue(SOURCE_KEY, sourceDomain)
        context
    }
}
