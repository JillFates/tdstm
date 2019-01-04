package com.tdsops.common.exceptions

import groovy.transform.CompileStatic

/**
 * Signals a configuration issue with the application.
 */
@CompileStatic
class InvalidLicenseException extends RuntimeException {
	InvalidLicenseException(String message) {
		super(message)
	}
}
