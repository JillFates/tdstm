import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.AuthenticationMethod
import com.tdsops.tm.enums.domain.CredentialEnvironment
import com.tdsops.tm.enums.domain.CredentialHttpMethod
import com.tdsops.tm.enums.domain.CredentialStatus
import com.tdssrc.grails.JsonUtil
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.command.CredentialCreateCO
import net.transitionmanager.command.CredentialUpdateCO
import net.transitionmanager.controller.ControllerMethods
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
	def getCredential(Long id) {
		renderSuccessJson(credentialService.findById(id).toMap())
	}

	@HasPermission(Permission.CredentialCreate)
	def createCredential(CredentialCreateCO command) {
		validateCommandObject(command)
		renderSuccessJson(credentialService.createCredential(command).toMap())
	}

	@HasPermission(Permission.CredentialEdit)
	def updateCredential(Long id) {
		// NOTE: For PUT command does populate the command objects properly
		// SEE: https://github.com/grails/grails-core/issues/9172
		CredentialUpdateCO command = populateCommandObject(CredentialUpdateCO)

		validateCommandObject(command)
		renderSuccessJson(credentialService.updateCredential(id, command).toMap())
	}

	/**
	 * Returns a JSON map containing the values of all of the enums used to
	 * support the Credential domain.
	 */
	@HasPermission(Permission.CredentialView)
	def credentialEnums() {
		renderSuccessJson([
				'status': CredentialStatus.toMap(),
				'authenticationMethod': AuthenticationMethod.toMap(),
				'httpMethod': CredentialHttpMethod.names(),
				'environment': CredentialEnvironment.toMap()
		])
	}

	/**
	 * Used to test that a particular Credential can authenticate
	 * @param id - the credential ID to validate
	 * @return a map with the results or error? TBD
	 */
	def testAuthentication(Long id) {
		Map authentication = credentialService.authenticate(id)
		if (authentication?.error) {
			renderErrorJson(authentication.error)
		} else {
			renderSuccessJson(authentication)
		}
	}
}
