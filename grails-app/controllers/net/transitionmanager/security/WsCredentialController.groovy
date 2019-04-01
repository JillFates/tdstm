package net.transitionmanager.security

import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.AuthenticationMethod
import com.tdsops.tm.enums.domain.AuthenticationRequestMode
import com.tdsops.tm.enums.domain.CredentialEnvironment
import com.tdsops.tm.enums.domain.CredentialHttpMethod
import com.tdsops.tm.enums.domain.CredentialStatus
import com.tdssrc.grails.JsonUtil
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.command.CredentialCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.credential.CredentialValidationExpression
import net.transitionmanager.domain.Credential
import net.transitionmanager.security.Permission
import net.transitionmanager.service.CredentialService
import net.transitionmanager.service.InvalidParamException

@Slf4j
@Secured("isAuthenticated()")
class WsCredentialController implements ControllerMethods {
	CredentialService credentialService

	@HasPermission(Permission.CredentialView)
	def list() {
		List<Credential> credentialList = credentialService.findAllByCurrentUserProject()
		renderSuccessJson(credentialList*.toMap())
	}

	@HasPermission(Permission.CredentialView)
	def fetch(Long id) {
		renderSuccessJson(credentialService.findById(id).toMap())
	}

	@HasPermission(Permission.CredentialCreate)
	def create() {
		CredentialCommand command = populateCommandObject(CredentialCommand)

		validateCommandObject(command)
		def result = credentialService.create(command)
		renderSuccessJson(result.toMap())
	}

	@HasPermission(Permission.CredentialEdit)
	def update(Long id) {
		CredentialCommand command = populateCommandObject(CredentialCommand)
		validateCommandObject(command)
		renderSuccessJson(credentialService.update(id, command, domainVersion).toMap())
	}

	@HasPermission(Permission.CredentialCreate)
	def delete(Long id) {
		credentialService.delete(id)
		renderSuccessJson(deleted:true)
	}

	/**
	 * Returns a JSON map containing the values of all of the enums used to
	 * support the Credential domain.
	 */
	@HasPermission(Permission.CredentialView)
	def enums() {
		renderSuccessJson([
				'status': CredentialStatus.toMap(),
				'authenticationMethod': AuthenticationMethod.toMap(),
				'requestMode': AuthenticationRequestMode.toMap(),
				'httpMethod': CredentialHttpMethod.names(),
				'environment': CredentialEnvironment.toMap()
		])
	}

	/**
	 * Used to test that a particular Credential can authenticate
	 * @param id - the credential ID to validate
	 * @return a map with the results or error? TBD
	 */
	@HasPermission(Permission.CredentialView)
	def testAuthentication(Long id) {
		Map authentication = credentialService.authenticate(id)
		if (authentication?.error) {
			renderErrorJson(authentication.error)
		} else {
			renderSuccessJson(authentication)
		}
	}

	/**
	 * Used to validate that a Credential Validation Expression has correct syntax
	 * @param expression as JSON
	 * @return success or error message from the DSL parser
	 */
	@HasPermission(Permission.CredentialEdit)
	def checkValidExprSyntax() {
		try {
			String expression = request.JSON.expression
			new CredentialValidationExpression(expression)
			renderSuccessJson(valid:true)
		} catch (e) {
			Map data = [
				valid: false,
				errors: [ e.getMessage() ]
			]
			renderSuccessJson(data)
		}
	}

}
