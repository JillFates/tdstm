package com.tdsops.common.security

import spock.lang.Specification

import java.security.GeneralSecurityException

class DESCodecTests extends Specification {

	/*
	 * Tests that after encode and decode a value the result is the same
	 */
	void testEncodeDecode() {
		when:
		def value = "testvalue"
		def encodedValue = DESCodec.encode(value)
		def decodedValue = DESCodec.decode(encodedValue)

		then:
		value == decodedValue
	}

	/*
	 * Tests that after encode a value the new string is not empty
	 */
	void testEncodedNotEmpty() {
		when:
		def value = "testvalue"
		def encodedValue = DESCodec.encode(value)

		then:
		encodedValue
	}

	/*
	 * Tests that the encoded value is not equals that original
	 */
	void testEncodedNotEquals() {
		when:
		def value = "testvalue"
		def encodedValue = DESCodec.encode(value)

		then:
		value != encodedValue
	}

	/*
	 * Tests that after encode and decode a value the result is the same, using a custom salt
	 */
	void testEncodeDecodeCustomSalt() {
		when:
		def value = "testvalue"
		def encodedValue = DESCodec.encode(value, "1234")
		def decodedValue = DESCodec.decode(encodedValue, "1234")

		then:
		value == decodedValue
	}

	/*
	 * Tests that after encode a value the new string is not empty, using a custom salt
	 */
	void testEncodedNotEmptyCustomSalt() {
		when:
		def value = "testvalue"
		def encodedValue = DESCodec.encode(value, "1234")

		then:
		encodedValue
	}

	/*
	 * Tests that the encoded value is not equals that original, using a custom salt
	 */
	void testEncodedNotEqualsCustomSalt() {
		when:
		def value = "testvalue"
		def encodedValue = DESCodec.encode(value, "1234")

		then:
		value != encodedValue
	}

	void testEncodeDecodeInvalid() {
		when:
		def encodedValue = DESCodec.encode("testvalue", "1234")
		DESCodec.decode(encodedValue, "4321")

		then:
		thrown GeneralSecurityException
	}
}
