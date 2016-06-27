import org.apache.shiro.SecurityUtils
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.springframework.transaction.annotation.Transactional
import com.tdssrc.grails.GormUtil
import net.transitionmanager.UserAudit

/**
 * Service used to log audit information
 *
 * @author Diego Scarpa
 */
class AuditService {

	static final String AUDIT_TYPE_ACCESS = "access"	
	static final String AUDIT_TYPE_ACTIVITY = "activity"

	static String AUDIT_TYPE = AUDIT_TYPE_ACCESS
	static Boolean AUDIT_ACTIVITY = true

	static ACCESS_URI = ["/auth/signIn": true, "/auth/signOut": true]
	static AUDITED_PARAMS = ["username": true, "id": true, "_action_Delete": true, "moveBundleId": true, "assetEntityId": true, "moveEventId": true]

	// Constructor - should be using the Spring post construction method...
	AuditService() {
		AUDIT_TYPE = ConfigurationHolder.config.tdstm.security.auditLogging
		if (!AUDIT_TYPE) {
			AUDIT_TYPE = AUDIT_TYPE_ACCESS
		}
		AUDIT_ACTIVITY = (AUDIT_TYPE == AUDIT_TYPE_ACTIVITY)
	}

	/**
	 * Audit information based on request information
	 * 
	 * @param subject subject associated with the request, could be null
	 * @param request request made
	 * @param params parameters associated with the request
	 */
	void auditRequest(request, params) {
		def auditUri = request.forwardURI - request.contextPath
		if (auditedUri(auditUri)) {
			def subject = SecurityUtils.subject
			params = filterParams(params)
			String paramsMsg = params.size() ? "$params " : ''
			String user = (subject && subject.principal) ? subject.principal : 'ANONYMOUS_USER'

			log.info "USER_ACTIVITY: $user invoked $request.method $auditUri ${paramsMsg}from ${request.remoteAddr}"
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
	Map filterParams(params) {
		def result = [:]
		if (params.size()) {
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
	boolean isValidParam(param){
		return (param instanceof String && param.trim().size()) || (param instanceof Collection<?> && !param.isEmpty())
	}

	/**
	 * Force log a security violation
	 * 
	 * @param user user involved in the security violation
	 * @param message message associated with the security violation
	 */
	void logSecurityViolation(user, message) {
		user = user ?: 'ANONYMOUS_USER'
		log.info "SECURITY_VIOLATION: $message by $user"
	}

	/**
	 * Used to simply log a message 
	 * 
	 * @param message message associated with the security violation
	 */
	def logMessage(message) {
		log.info "USER_ACTIVITY: $message"
	}

	def logWarning(message) {
		log.warn "USER_ACTIVITY: $message"
	}

	/**
	 * Check if the configuration property is set to activity
	 * 
	 * @return returns true if the configuration property is set to activity.
	 */
	def logActivityEnabled() {
		return AUDIT_ACTIVITY
	}

	/**
	 * Used to store a UserAudit activity
	 */
	def saveUserAudit(UserAudit userAudit) {
		if (!userAudit.validate() || !userAudit.save(flush:true)) {
			log.error "saveUserAudit() Unable to save UserAudit : " + GormUtil.allErrorsString(userAudit)
		}
		// TODO : JPM 9/2015 : Record message to log file too
	}

}
