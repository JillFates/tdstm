package net.transitionmanager.service

import com.tdssrc.grails.HtmlUtil
import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import net.transitionmanager.UserAudit
import org.springframework.beans.factory.InitializingBean

/**
 * Logs audit information
 *
 * @author Diego Scarpa
 */
@Slf4j(value='logger')
class AuditService implements InitializingBean, ServiceMethods {

	private static final String AUDIT_TYPE_ACCESS = 'access'
	private static final String AUDIT_TYPE_ACTIVITY = 'activity'

	private static String AUDIT_TYPE
	private static boolean AUDIT_ACTIVITY

	private static Map<String, String> ACCESS_URI = [
		'/auth/signIn': true,
		'/auth/signOut': true
	]

	private static final Map AUDITED_PARAMS = [username: true, id: true, _action_Delete: true, moveBundleId: true,
	                                           assetEntityId: true, moveEventId: true]

	void afterPropertiesSet() {
		AUDIT_TYPE = grailsApplication.config.tdstm.security.auditLogging ?: AUDIT_TYPE_ACCESS
		AUDIT_ACTIVITY = AUDIT_TYPE == AUDIT_TYPE_ACTIVITY
	}

	/**
	 * Audit information based on request information
	 *
	 * @param subject subject associated with the request, could be null
	 * @param request HTTP request
	 * @param params request parameters
	 */
	void auditRequest(request, Map params) {
		def auditUri = request.forwardURI - request.contextPath
		if (auditedUri(auditUri)) {
			params = filterParams(params)

			String paramsMsg = params.size() ? params.toString() + ' :' : ''
			String user = securityService.currentUsername ?: 'ANONYMOUS_USER'
			String remoteIp = HtmlUtil.getRemoteIp(request)
			logger.info 'USER_ACTIVITY: {} invoked {} {} {}', 
				user, request.method, auditUri, remoteIp
		}
	}

	/**
	 * Determine if the request should be logged or not
	 *
	 * @param request to validate
	 */
	boolean auditedUri(uri) {
		return ((AUDIT_ACTIVITY) || (ACCESS_URI[uri] != null))
	}

	/**
	 * Filter parameters, keep in only the ones audited
	 *
	 * @param request to validate
	 */
	Map filterParams(Map params) {
		Map result = [:]
		if (params) {
			AUDITED_PARAMS.each { k, v ->
				if (params.containsKey(k) && v && isValidParam(params[k])) {
					result[k] = params[k]
				}
			}
		}
		return result
	}

	/**
	 * This method checks whether the param is of
	 * a valid type (Collection or String) as is not empty.
	 */
	boolean isValidParam(param) {
		return (param instanceof String && param.trim().size()) || (param instanceof Collection<?> && !param.isEmpty())
	}

	void logSecurityViolation(String username = null, message) {
		if (username == null) {
			username = securityService.currentUsername
		}
		logger.info 'SECURITY_VIOLATION: {} by {}', message, username
	}

	void logMessage(message) {
		logger.info 'USER_ACTIVITY: {}', message
	}

	void logWarning(message) {
		logger.warn 'USER_ACTIVITY: {}', message
	}

	/**
	 * Check if the configuration property is set to activity
	 *
	 * @return returns true if the configuration property is set to activity.
	 */
	boolean logActivityEnabled() {
		return AUDIT_ACTIVITY
	}

	@Transactional
	void saveUserAudit(UserAudit userAudit) {
		save userAudit
		// TODO : JPM 9/2015 : Record message to log file too
	}
}
