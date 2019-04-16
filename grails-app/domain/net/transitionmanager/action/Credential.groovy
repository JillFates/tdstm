package net.transitionmanager.action

import com.tdsops.tm.enums.domain.AuthenticationMethod
import com.tdsops.tm.enums.domain.AuthenticationRequestMode
import com.tdsops.tm.enums.domain.CredentialHttpMethod
import com.tdsops.tm.enums.domain.CredentialStatus
import com.tdsops.tm.enums.domain.CredentialEnvironment
import com.tdssrc.grails.StringUtil
import net.transitionmanager.credential.CredentialValidationExpression
import net.transitionmanager.project.Project

class Credential {
	Project  project
	Provider provider
	String   name
	String   description = ''

	CredentialStatus status

	// Used to indicate what environment that particular credential is meant to be used for
	CredentialEnvironment environment

	// Indicates the type of authentication that this Credential will use (e.g. BasicAuth, JWT, OAuth, etc)
	AuthenticationMethod authenticationMethod

	// When issuing the authentication request this will be used to determine if it is Form Variables or Basic Auth
	AuthenticationRequestMode requestMode = AuthenticationRequestMode.BASIC_AUTH

	// Determines how to authenticate
	CredentialHttpMethod httpMethod = CredentialHttpMethod.POST

	// The username / access key used for authentication that is encrypted
	String username

	// The password that is encrypted using the AES (256 bit encyption)
	String password

	// The salt will be combined with the system salt prefix to make it all the more difficult
	// to crack the encrypted values. The salt is a randomly generated string for each credential.
	// TODO : JPM 2/2018 : A new salt should be generated anytime that the password is being changed )
	String salt

	// The URL to the endpoint used to perform the authentication
	// Note that for AWS, the URL is a standard and therefore not required in the credential.
	String authenticationUrl=''

	// The URL to the endpoing that used to terminate or logout a previously created session with the provider
	String terminateUrl=''

	// The URL to the endpoint to renew tokens
	String renewTokenUrl=''

	// The HTTP Header token or Cookie name used to reference the session token
	String sessionName=''

	String validationExpression=''

	Date dateCreated
	Date lastUpdated

	static belongsTo = [
		project: Project,
		provider: Provider
	]

	static constraints = {
		name size: 1..255, unique: 'project'
		description size:0..255, blank:true
		authenticationUrl size:0..255, blank:true, validator: authenticationUrlValidator()
		renewTokenUrl size:0..255, blank: true, validator: renewTokenUrlValidator()
		terminateUrl size:0..255, blank: true
		username size:1..255
		password size:1..255
		salt size:1..16
		sessionName size:0..255, blank:true, validator: sessionNameValidator()
		validationExpression size: 0..255, blank: true, validator: validationExpressionValidator()
		httpMethod  nullable: true, validator: httpMethodValidator()
		requestMode nullable: true, validator: requestModeValidator()
		lastUpdated nullable: true
		provider ofSameProject:true
	}

	static mapping = {
		id column: 'credential_id'
		name sqlType: 'VARCHAR(255)'
		description sqlType: 'VARCHAR(255)'
		salt sqlType: 'VARCHAR(255)'
		username sqlType: 'VARCHAR(255)'
		password sqlType: 'VARCHAR(255)'
		authenticationUrl sqlType: 'VARCHAR(255)'
		renewTokenUrl sqlType: 'VARCHAR(255)'
		authenticationMethod enumType: 'String'
		requestMode enumType: 'String'
		environment enumType: 'String'
		httpMethod enumType: 'String'
		status enumType: 'String'
		sessionName sqlType: 'VARCHAR(255)'
		validationExpression sqlType: 'VARCHAR(255)'

		// TODO : JPM 2/2018 : Would like to sort on Provider name + Credential name
		sort 'name'
	}

	/**
	 * Used to validate if the sessionName property is set for AuthenticationMethods that require a value
	 * Validation ensures that sessionName is not empty for COOKIE, HEADER, JWT authentication methods as well
	 * as it matches the following pattern:
	 * - SessionHeaderName @ source : propertyName
	 *
	 * Validation of sessionName regex includes:
	 * - no white spaces
	 * - only 1 @ (at) symbol
	 * - only 1 : (colon) symbol
	 * - any alphanumeric value
	 * - underscores
	 * - hyphens
	 *
	 * So valid entries could be:
	 * - JSESSIONID@cookie:JSESSIONID
	 * - Authentication@json:access_token
	 * - api_key@header:WWW_Authenticate
	 *
	 * Examples:
	 * - assert ('Authentication@cookie:access_token' ==~ /^[A-Za-z0-9_\-]+@{1}?(header|cookie|json):{1}?[A-Za-z0-9_\-]+$/) == true
	 * - assert ('white-spaces @ json : access_token' ==~ /^[A-Za-z0-9_\-]+@{1}?(header|cookie|json):{1}?[A-Za-z0-9_\-]+$/) == false
	 * - assert ('double-at@@json:access_token' ==~ /^[A-Za-z0-9_\-]+@{1}?(header|cookie|json):{1}?[A-Za-z0-9_\-]+$/) == false
	 * - assert ('double-colon@json::access_token' ==~ /^[A-Za-z0-9_\-]+@{1}?(header|cookie|json):{1}?[A-Za-z0-9_\-]+$/) == false
	 */
	static Closure sessionNameValidator() {
		return { value, target ->
			List methodsThatRequireProp = [AuthenticationMethod.COOKIE, AuthenticationMethod.HEADER]

			if (target.authenticationMethod in methodsThatRequireProp) {

				if (StringUtil.isBlank(value)) {
					return 'default.blank.message'
				} else {

					if (!(value ==~ /^[A-Za-z0-9_\-]+@{1}?(header|cookie|json):{1}?[A-Za-z0-9_\-]+$/)) {
						return 'credential.invalid.sessionName.value'
					}
				}
			}
		}
	}

	/**
	 * Used to validate if the validationExpression is syntactically correct
	 */
	static Closure validationExpressionValidator() {
		return { value, target ->
			List methodsThatRequireProp = [AuthenticationMethod.BASIC_AUTH, AuthenticationMethod.COOKIE, AuthenticationMethod.HEADER]

			if (target.authenticationMethod in methodsThatRequireProp) {

				if (StringUtil.isBlank(value)) {
					return 'default.blank.message'
				} else {
					try {
						new CredentialValidationExpression(value)
						return true
					} catch (e) {
						return 'credential.invalid.validation.expression'
					}
				}
			}
		}
	}

	/**
	 * Used to validate if the authenticationUrl property is set for AuthenticationMethods that is require a value
	 * based on the authentication method. This property is required for COOKIE, HEADER and JWT methods.
	 */
	static Closure authenticationUrlValidator() {
		return { value, target ->
			List methodsThatRequireProp = [AuthenticationMethod.BASIC_AUTH, AuthenticationMethod.COOKIE, AuthenticationMethod.HEADER, AuthenticationMethod.JWT]

			if (target.authenticationMethod in methodsThatRequireProp) {

				if (StringUtil.isBlank(value)) {
					return 'default.blank.message'
				}
			}
		}
	}

	/**
	 * Used to validate if the renewTokenUrl property is set for AuthenticationMethods that require a value
	 */
	static Closure renewTokenUrlValidator() {
		return { value, target ->
			List methodsThatRequireProp = [AuthenticationMethod.JWT]

			if (target.authenticationMethod in methodsThatRequireProp) {

				if (StringUtil.isBlank(value)) {
					return 'default.blank.message'
				}

				if (value == target.authenticationUrl) { // renewTokenUrl cannot be the same as authenticationUrl
					return 'credential.invalid.url.value'
				}
			}
		}
	}

	/**
	 * Used to validate if the httpMethod property is set for AuthenticationMethods that require a value
	 */
	static Closure httpMethodValidator() {
		return { value, target ->
			// TODO : JPM 2/2018 : Need to validate if JWT is always POST
			List methodsThatRequireProp = [AuthenticationMethod.COOKIE, AuthenticationMethod.HEADER]
			if (target.authenticationMethod in methodsThatRequireProp) {
				if (value == null) {
					return 'default.null.message'
				}
			}
		}
	}

	/**
	 * Used to validate if the requestMode property is set for AuthenticationMethods that require a value
	 */
	static Closure requestModeValidator() {
		return { value, target ->
			List methodsThatRequireProp = [AuthenticationMethod.COOKIE, AuthenticationMethod.HEADER]
			if (target.authenticationMethod in methodsThatRequireProp) {
				if (value == null) {
					return 'default.null.message'
				}
			}
		}
	}

	/**
	 * Converts this credential object to a map
	 * NOTE: The password should NEVER be included in the Map
	 * @return
	 */
	Map toMap() {
		Map data = [
				id						: id,
				project					: [id: project.id, name: project.name],
				provider				: [id: provider.id, name: provider.name],
				name					: name,
				description             : description,
				environment				: environment.name(),
				status					: status.name(),
				authenticationMethod	: authenticationMethod.name(),
				requestMode             : (requestMode ? requestMode.name() : null),
				httpMethod              : (httpMethod ? httpMethod.name() : null),
				username        		: username,
				authenticationUrl       : authenticationUrl,
				terminateUrl            : terminateUrl,
				renewTokenUrl           : renewTokenUrl,
				sessionName             : sessionName,
				validationExpression	: validationExpression,
				dateCreated     		: dateCreated,
				lastUpdated     		: lastUpdated,
				version                 : version
		]
		return data.asImmutable()
	}

}
