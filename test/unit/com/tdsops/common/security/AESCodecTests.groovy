package com.tdsops.common.security

import java.security.GeneralSecurityException

import spock.lang.Specification

class AESCodecTests extends Specification {

	/*
	 * Tests that after encode and decode a value the result is the same
	 */
	void testEncodeDecode() {
		when:
		def value = "testvalue"
		def salt = AESCodec.instance.generateRandomSalt()
		def encodedValue = AESCodec.instance.encode(value, salt)
		def decodedValue = AESCodec.instance.decode(encodedValue, salt)

		//Value encoded and decode should be equal
		then:
		// Keeps equal to original
		value == decodedValue
	}

	/*
	 * Tests that after encode a value the new string is not empty
	 */
	void testEncodedNotEmpty() {
		when:
		def value = "testvalue"
		def salt = AESCodec.instance.generateRandomSalt()
		def encodedValue = AESCodec.instance.encode(value, salt)

		//Value encoded should not be empty
		then:
		// Encoded value not empty
		encodedValue
	}

	/*
	 * Tests that the encoded value is not equals that original
	 */
	void testEncodedNotEquals() {
		when:
		def value = "testvalue"
		def salt = AESCodec.instance.generateRandomSalt()
		def encodedValue = AESCodec.instance.encode(value, salt)

		//Value encoded and value should not be equal
		then:
		// Encoded value not equals to original
		value != encodedValue
	}

	/*
	 * Tests that after encode and decode a value the result is the same, using a custom salt
	 */
	void testEncodeDecodeCustomSalt() {
		when:
		def value = "testvalue"
		def encodedValue = AESCodec.instance.encode(value, "1234")
		def decodedValue = AESCodec.instance.decode(encodedValue, "1234")

		//Value encoded and decode should be equal
		then:
		// Keeps equal to original
		value == decodedValue
	}

	/*
	 * Tests that after encode a value the new string is not empty, using a custom salt
	 */
	void testEncodedNotEmptyCustomSalt() {
		when:
		def value = "testvalue"
		def encodedValue = AESCodec.instance.encode(value, "1234")

		//Value encoded should not be empty
		then:
		// Encoded value not empty
		encodedValue
	}

	/*
	 * Tests that the encoded value is not equals that original, using a custom salt
	 */
	void testEncodedNotEqualsCustomSalt() {
		when:
		def value = "testvalue"
		def encodedValue = AESCodec.instance.encode(value, "1234")

		//Value encoded and value should not be equal
		then:
		// Encoded value not equals to original
		value != encodedValue
	}

	/*
	 * Tests that decoding is not possible if the salt is not equal
	 */
	void testEncodeDecodeInvalid() {
		when:
		def encodedValue = AESCodec.instance.encode("testvalue", "1234")
		AESCodec.instance.decode(encodedValue, "4321")

		then:
		thrown GeneralSecurityException
	}
}
