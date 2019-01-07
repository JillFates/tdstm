package com.tdsops.common.security

class SecurityUtilTests {

	/*
	 * Tests encrypt a valid value
	 */
	void testEncryptLegacy() {
		def value = "testvalue"
		def encryptedValue = SecurityUtil.encryptLegacy(value)

		println "Encrypted value: $encryptedValue"

		assertNotNull "Encrypted value not empty", encryptedValue
		assertTrue "Encrypted value not empty", (encryptedValue.size() > 0)
	}

	/*
	 * Tests encrypt a valid value using system salt key
	 */
	void testEncrypt() {
		def value = "testvalue"
		def encryptedValue = SecurityUtil.encrypt(value)

		println "Encrypted value: $encryptedValue"

		assertNotNull "Encrypted value not empty", encryptedValue
		assertTrue "Encrypted value not empty", (encryptedValue.size() > 0)
	}

	/*
	 * Tests encrypt a valid value using custom salt key
	 */
	void testEncryptUsingCustomSaltKey() {
		def value = "testvalue"
		def saltKey = "saltkey"
		def encryptedValue = SecurityUtil.encrypt(value, saltKey)

		println "Encrypted value: $encryptedValue"

		assertNotNull "Encrypted value not empty", encryptedValue
		assertTrue "Encrypted value not empty", (encryptedValue.size() > 0)
	}

	/*
	 * Tests encrypt null value
	 */
	void testEncryptLegacyNull() {
		shouldFail {
			SecurityUtil.encryptLegacy null
		}
	}
}
