package com.tdsops.ldap

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import org.springframework.security.authentication.AccountStatusException

@InheritConstructors
@CompileStatic
class NoRolesException extends AccountStatusException {
}
