package com.tdsops.common.security

import java.security.GeneralSecurityException

import spock.lang.Specification

class AESCodecTests extends Specification {

	/*
	 * Tests that after encode and decode a value the result is the same
	 */
	void testEncodeDecode() {
		when: 'Encoding and decoding a value'
			def value = "testvalue"
			def salt = AESCodec.instance.generateRandomSalt()
			def encodedValue = AESCodec.instance.encode(value, salt)
			def decodedValue = AESCodec.instance.decode(encodedValue, salt)
		then: 'Value encoded and decode should be equal'
			value == decodedValue
	}

	/*
	 * Tests that after encode a value the new string is not empty
	 */
	void testEncodedNotEmpty() {
		when: 'Encoding a value'
			def value = "testvalue"
			def salt = AESCodec.instance.generateRandomSalt()
			def encodedValue = AESCodec.instance.encode(value, salt)
		then: 'Value encoded should not be empty'
			encodedValue
	}

	/*
	 * Tests that the encoded value is not equals that original
	 */
	void testEncodedNotEquals() {
		when: 'Encoding a value'
			def value = "testvalue"
			def salt = AESCodec.instance.generateRandomSalt()
			def encodedValue = AESCodec.instance.encode(value, salt)
		then: 'Value encoded and value should not be equal'
			value != encodedValue
	}

	/*
	 * Tests that after encode and decode a value the result is the same, using a custom salt
	 */
	void testEncodeDecodeCustomSalt() {
		when: 'Encoding a value using custom salt'
			def value = "testvalue"
			def encodedValue = AESCodec.instance.encode(value, "1234")
			def decodedValue = AESCodec.instance.decode(encodedValue, "1234")
		then: 'Value encoded and decode should be equal'
			value == decodedValue
	}

	/*
	 * Tests that after encode a value the new string is not empty, using a custom salt
	 */
	void testEncodedNotEmptyCustomSalt() {
		when: 'Encoding a value using custom salt'
			def value = "testvalue"
			def encodedValue = AESCodec.instance.encode(value, "1234")
		then: 'Value encoded should not be empty'
			encodedValue
		and: 'Value encoded and original value should not be equal'
			value != encodedValue
	}

	/*
	 * Tests that decoding is not possible if the salt is not equal
	 */
	void testEncodeDecodeInvalid() {
		when: 'Encoding a value and when decoding using a wrong or invalid salt'
			def encodedValue = AESCodec.instance.encode("testvalue", "1234")
			AESCodec.instance.decode(encodedValue, "4321")
		then: 'A general security exception is thrown'
			GeneralSecurityException e = thrown()
			e.message ==~ /^Given final block not properly padded.*$/
	}

	/**
	 * Tests that encoding the same value multiple times generates always different encoded values
	 */
	void testEncodingValueUsingTheSameSaltDoesNotGenerateTheSameOutputUsingChainingBlockCipher() {
		when: 'Encoding the same value twice'
			def value = "testvalue"
			def salt = AESCodec.instance.generateRandomSalt()
			def firstEncodedValue = AESCodec.instance.encode(value, salt)
			def secondEncodedValue = AESCodec.instance.encode(value, salt)
		then: 'The resultant encoded values are not the same'
			firstEncodedValue != secondEncodedValue
		and: 'Decoding encrypted values'
			def firstDecodedValue = AESCodec.instance.decode(firstEncodedValue, salt)
			def secondDecodedValue = AESCodec.instance.decode(secondEncodedValue, salt)
		then: 'The values must be the same'
			firstDecodedValue == secondDecodedValue
	}

}
