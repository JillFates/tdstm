package com.tdsops.common.validation

import com.tdssrc.grails.NumberUtil
import com.tdsops.common.lang.ExceptionUtil
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

@Singleton
class ConstraintsValidator {

	private static log

	ConstraintsValidator () {
		log = LogFactory.getLog(this.class)
	}

	static boolean validate (String value, Map constraints) {
		try {
			def passedConstraint = false
			def validated = true

			constraints.each { a, b ->
				switch (a) {
					case 'type':
						if ( b == 'boolean' && ! (value in ['y', 'n', 'yes', 'no', '1', '0', 'true', 'false', 1, 0, true, false]) )
							validated = false
						else if (b == 'integer' && ! NumberUtil.isLong(value))
							validated = false
						break
					case 'inList':
						if (! (value in b) )
							validated = false
						break
					case 'size':
						if (! (value.size() in b) )
							validated = false
						break
					case 'validator':
						if (! (b(value)) )
							validated = false
						break
				}
			}

			return validated
		} catch (Exception e) {
			log.error "Unexpected error : ${e.class} ${e.getMessage()}\n${ExceptionUtil.stackTraceToString(e)}"
			throw e
			return false
		}
	}
}
