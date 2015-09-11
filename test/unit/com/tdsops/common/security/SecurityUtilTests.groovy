package com.tdsops.common.security

import grails.test.*

class SecurityUtilTests extends GrailsUnitTestCase {

	/*
	 * Tests encrypt a valid value
	 */
	public void testEncryptLegacy() {
		def value = "testvalue"
		def encryptedValue = SecurityUtil.encryptLegacy(value)

		println "Encrypted value: $encryptedValue"

		assertTrue "Encrypted value not empty", (encryptedValue != null)
		assertTrue "Encrypted value not empty", (encryptedValue.size() > 0)
	}

	/*
	 * Tests encrypt a valid value using system salt key
	 */
	public void testEncrypt() {
		def value = "testvalue"
		def encryptedValue = SecurityUtil.encrypt(value)

		println "Encrypted value: $encryptedValue"

		assertTrue "Encrypted value not empty", (encryptedValue != null)
		assertTrue "Encrypted value not empty", (encryptedValue.size() > 0)
	}

	/*
	 * Tests encrypt a valid value using custom salt key
	 */
	public void testEncryptUsingCustomSaltKey() {
		def value = "testvalue"
		def saltKey = "saltkey"
		def encryptedValue = SecurityUtil.encrypt(value, saltKey)

		println "Encrypted value: $encryptedValue"

		assertTrue "Encrypted value not empty", (encryptedValue != null)
		assertTrue "Encrypted value not empty", (encryptedValue.size() > 0)
	}

	/*
	 * Tests encrypt null value
	 */
	public void testEncryptLegacyNull() {
		def value = null

		shouldFail(Exception) {
			SecurityUtil.encryptLegacy(value)
		}
	}

}