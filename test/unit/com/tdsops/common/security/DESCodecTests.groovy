package com.tdsops.common.security

import grails.test.*

class DESCodecTests extends GrailsUnitTestCase {

	/*
	 * Tests that after encode and decode a value the result is the same
	 */
	public void testEncodeDecode() {
		def value = "testvalue"
		def encodedValue = DESCodec.encode(value)
		def decodedValue = DESCodec.decode(encodedValue)

		println "value: $value"
		println "encodedValue: $encodedValue"
		println "decodedValue: $decodedValue"

		//Value encoded and decode should be equal
		assertEquals "Keeps equal to original", value, decodedValue
	}

	/*
	 * Tests that after encode a value the new string is not empty
	 */
	public void testEncodedNotEmpty() {
		def value = "testvalue"
		def encodedValue = DESCodec.encode(value)

		//Value encoded should not be empty
		assertTrue "Encoded value not empty", (encodedValue.size() > 0)
	}

	/*
	 * Tests that the encoded value is not equals that original
	 */
	public void testEncodedNotEquals() {
		def value = "testvalue"
		def encodedValue = DESCodec.encode(value)

		//Value encoded and value should not be equal
		assertTrue "Encoded value not equals to original", (!value.equals(encodedValue))
	}

}