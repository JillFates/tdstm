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
		try {
			renderSuccessJson(credentialService.findById(id).toMap())
		} catch (Exception e) {
			log.info ExceptionUtil.stackTraceToString(e)
			renderErrorJson(e.message)
		}
	}

	@HasPermission(Permission.CredentialCreate)
	def createCredential(CredentialCreateCO command) {
		if (!command.validate()) {
			throw new InvalidParamException('Invalid parameters')
		}
		renderSuccessJson(credentialService.createCredential(command).toMap())
	}

	@HasPermission(Permission.CredentialEdit)
	def updateCredential() {
		CredentialUpdateCO command = JsonUtil.readValue(request.JSON, CredentialUpdateCO.class)

		if (!command.validate()) {
			throw new InvalidParamException('Invalid parameters')
		}

		try {
			renderSuccessJson(credentialService.updateCredential(command).toMap())
		} catch (Exception e) {
			log.info ExceptionUtil.stackTraceToString(e)
			renderErrorJson(e.message)
		}
	}

	@HasPermission(Permission.CredentialView)
	def credentialEnums() {
		renderSuccessJson([
				'status': CredentialStatus.toMap(),
				'authenticationMethod': AuthenticationMethod.toMap(),
				'httpMethod': CredentialHttpMethod.names(),
				'environment': CredentialEnvironment.toMap()
		])
	}

}
