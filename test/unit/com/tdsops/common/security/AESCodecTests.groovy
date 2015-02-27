package com.tdsops.common.security

import grails.test.*

import spock.lang.Specification

/**
 * Unit test cases for the AESodec class
*/
class AESCodecTests extends Specification {

	/*
	 * Tests that after encode and decode a value the result is the same
	 */
	public void testEncodeDecode() {
		def value = "testvalue"
		def encodedValue = AESCodec.encode(value)
		def decodedValue = AESCodec.decode(encodedValue)

		println "value: $value"
		println "encodedValue: $encodedValue"
		println "decodedValue: $decodedValue"

		//Value encoded and decode should be equal
		expect:
			// Keeps equal to original
			value.equals(decodedValue)
	}

	/*
	 * Tests that after encode a value the new string is not empty
	 */
	public void testEncodedNotEmpty() {
		def value = "testvalue"
		def encodedValue = AESCodec.encode(value)

		//Value encoded should not be empty
		expect:
			// Encoded value not empty
			(encodedValue.size() > 0)
	}

	/*
	 * Tests that the encoded value is not equals that original
	 */
	public void testEncodedNotEquals() {
		def value = "testvalue"
		def encodedValue = AESCodec.encode(value)

		//Value encoded and value should not be equal
		expect:
			// Encoded value not equals to original
			(!value.equals(encodedValue))
	}

	/*
	 * Tests that after encode and decode a value the result is the same, using a custom salt
	 */
	public void testEncodeDecodeCustomSalt() {
		def value = "testvalue"
		def encodedValue = AESCodec.encode(value, "1234")
		def decodedValue = AESCodec.decode(encodedValue, "1234")

		println "value: $value"
		println "encodedValue: $encodedValue"
		println "decodedValue: $decodedValue"

		//Value encoded and decode should be equal
		expect:
			// Keeps equal to original
			value.equals(decodedValue)
	}

	/*
	 * Tests that after encode a value the new string is not empty, using a custom salt
	 */
	public void testEncodedNotEmptyCustomSalt() {
		def value = "testvalue"
		def encodedValue = AESCodec.encode(value, "1234")

		//Value encoded should not be empty
		expect:
			// Encoded value not empty
			(encodedValue.size() > 0)
	}

	/*
	 * Tests that the encoded value is not equals that original, using a custom salt
	 */
	public void testEncodedNotEqualsCustomSalt() {
		def value = "testvalue"
		def encodedValue = AESCodec.encode(value, "1234")

		//Value encoded and value should not be equal
		expect:
			// Encoded value not equals to original
			(!value.equals(encodedValue))
	}

	/*
	 * Tests that after encode and decode values are different is the salt is not equal
	 */
	public void testEncodeDecodeInvalid() {
		def value = "testvalue"
		def encodedValue = AESCodec.encode(value, "1234")
		def decodedValue = "testvalue"
		try {
			decodedValue = AESCodec.decode(encodedValue, "4321")
		} catch (e) {
			decodedValue = "can not decode"
		}
	
		println "value: $value"
		println "encodedValue: $encodedValue"
		println "decodedValue: $decodedValue"

		//Value encoded and decode should be equal
		expect:
			// Not equals
			(!value.equals(decodedValue))
	}

}