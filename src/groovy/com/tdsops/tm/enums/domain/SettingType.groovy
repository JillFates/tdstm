package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

@CompileStatic
enum SettingType {
    CUSTOM_DOMAIN_FIELD_SPEC, // CustomDomainFieldSpec
    SECURITY_LOCAL, // SecurityLocal
    SECURITY_LDAP, // SecurityLDAP
    MAIL, // Mail
    METRIC_DEF
}
