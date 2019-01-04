package com.tdsops.common.license

import net.nicholaswilliams.java.licensing.encryption.RSAKeyPairGenerator
import net.nicholaswilliams.java.licensing.exception.AlgorithmNotSupportedException
import net.nicholaswilliams.java.licensing.exception.InappropriateKeyException
import net.nicholaswilliams.java.licensing.exception.InappropriateKeySpecificationException
import net.nicholaswilliams.java.licensing.exception.RSA2048NotSupportedException

import java.security.KeyPair

/**
 * Script used to generate a license pair.
 */
String password = "O3rM&mWkNMGf&q"

RSAKeyPairGenerator generator = new RSAKeyPairGenerator()

KeyPair keyPair
try {
	keyPair = generator.generateKeyPair()
} catch(RSA2048NotSupportedException e) { return }

try {
	generator.saveKeyPairToFiles(keyPair, "licenseManager.key", "licensePublic.key", password.toCharArray())
	println("License cert files generated!")
} catch(IOException | AlgorithmNotSupportedException | InappropriateKeyException | InappropriateKeySpecificationException e) {
	e.printStackTrace()
	return
}
