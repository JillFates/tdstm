package net.transitionmanager.exception

import com.tdssrc.grails.GormUtil
import groovy.transform.CompileStatic

@CompileStatic
class DomainUpdateException extends RuntimeException {
	DomainUpdateException(CharSequence message, domainObj = null) {
		super(message?.toString() + (domainObj ? ' : ' + GormUtil.allErrorsString(domainObj) : ''))
	}
}
