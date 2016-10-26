package com.tdsops.common.validation

import com.tdssrc.grails.NumberUtil
import com.tdsops.common.lang.ExceptionUtil
import groovy.util.logging.Commons

@Commons
@Singleton
class ConstraintsValidator {

	static boolean validate(String value, Map<String, Object> constraints) {
		try {
			boolean validated = true

			constraints.each { String a, b ->
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
		}
		catch (e) {
			log.error "Unexpected error : ${e.class} $e.message\n${ExceptionUtil.stackTraceToString(e)}"
			throw e
		}
	}
}
