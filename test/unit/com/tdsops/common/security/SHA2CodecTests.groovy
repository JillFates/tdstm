package com.tdsops.common.security

import grails.test.GrailsUnitTestCase

class SHA2CodecTests extends GrailsUnitTestCase {

	/*
	 * Tests encode using system salt prefix
	 */
	void testEncode() {
		def value = "password123!"
		def encodedValue = SHA2Codec.encode(value)

		assertTrue "Encoded value not empty", encodedValue.size() > 0
	}

	/*
	 * Tests encode using random salt prefix
	 */
	void testEncodeRandomSalt() {
		def saltPrefix = SecurityUtil.randomString(30)
		def value = "password123!"
		def encodedValue = SHA2Codec.encode(value, saltPrefix)

		assertTrue "Encoded value not empty", encodedValue.size() > 0
	}

	/*
	 * Tests encode using random salt prefix and max length
	 */
	void testEncodeRandomSaltAndMaxLength() {
		def saltPrefix = SecurityUtil.randomString(30)
		def value = "password123!"
		def encodedValue = SHA2Codec.encode(value, saltPrefix, 50)

		assertEquals "Encoded value not empty", 50, encodedValue.size()
	}

	/*
	 * Tests encode using random salt prefix
	 */
	void testEncodeTwoTimes() {
		def saltPrefix = SecurityUtil.randomString(30)
		def value = "password123!"
		def encodedValue1 = SHA2Codec.encode(value, saltPrefix)
		def encodedValue2 = SHA2Codec.encode(value, saltPrefix)

		assertEquals "Keeps equal to original", encodedValue1, encodedValue2
	}
}
