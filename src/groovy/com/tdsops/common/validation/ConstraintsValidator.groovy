package com.tdsops.common.validation

import com.tdssrc.grails.NumberUtil

class ConstraintsValidator {
	
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
			return false
		}
	}
}