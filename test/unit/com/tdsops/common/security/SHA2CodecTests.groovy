package com.tdsops.common.security

import grails.test.*

class SHA2CodecTests extends GrailsUnitTestCase {

	/*
	 * Tests encode using system salt prefix
	 */
	public void testEncode() {
		def value = "password123!"
		def encodedValue = SHA2Codec.encode(value)

		//Value encoded should not be empty
		assertTrue "Encoded value not empty", (encodedValue.size() > 0)
	}

	/*
	 * Tests encode using random salt prefix
	 */
	public void testEncodeRandomSalt() {
		def saltPrefix = SecurityUtil.randomString(30)
		def value = "password123!"
		def encodedValue = SHA2Codec.encode(value, saltPrefix)

		//Value encoded should not be empty
		assertTrue "Encoded value not empty", (encodedValue.size() > 0)
	}

	/*
	 * Tests encode using random salt prefix and max length
	 */
	public void testEncodeRandomSaltAndMaxLength() {
		def saltPrefix = SecurityUtil.randomString(30)
		def value = "password123!"
		def encodedValue = SHA2Codec.encode(value, saltPrefix, 50)

		//Value encoded should not be empty
		assertTrue "Encoded value not empty", (encodedValue.size() == 50)
	}

	/*
	 * Tests encode using random salt prefix
	 */
	public void testEncodeTwoTimes() {
		def saltPrefix = SecurityUtil.randomString(30)
		def value = "password123!"
		def encodedValue1 = SHA2Codec.encode(value, saltPrefix)
		def encodedValue2 = SHA2Codec.encode(value, saltPrefix)

		// Both emcrypt values should be equals
		assertEquals "Keeps equal to original", encodedValue1, encodedValue2
	}

}