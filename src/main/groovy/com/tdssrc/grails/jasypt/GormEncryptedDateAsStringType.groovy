package com.tdssrc.grails.jasypt

import groovy.transform.CompileStatic
import org.jasypt.hibernate5.type.EncryptedDateAsStringType

@CompileStatic
class GormEncryptedDateAsStringType extends JasyptConfiguredUserType<EncryptedDateAsStringType> {
}
