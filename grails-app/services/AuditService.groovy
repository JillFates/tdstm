import org.apache.shiro.SecurityUtils
import org.codehaus.groovy.grails.commons.ConfigurationHolder

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

	def AuditService() {
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
	def auditRequest(request, params) {
		def auditUri = request.forwardURI - request.contextPath
		if (auditedUri(auditUri)) {
			def subject = SecurityUtils.subject
			params = filterParams(params)
			if (subject && subject.principal) {
				log.info "USER_ACTIVITY: $subject.principal invoked $request.method $auditUri on $params. From $request.remoteAddr."
			} else {
				log.info "USER_ACTIVITY: User invoked without session $request.method $auditUri on $params. From $request.remoteAddr."
			}
		}
	}

	/**
	 * Determine if the request should be logged or not
	 * 
	 * @param request to validate
	 */
	def auditedUri(uri) {
		return ((AUDIT_ACTIVITY) || (ACCESS_URI[uri] != null))
	}

	/**
	 * Filter parameters, keep in only the ones audited
	 * 
	 * @param request to validate
	 */
	def filterParams(params) {
		def result = [:]
		params.each{ key, value ->
			if (AUDITED_PARAMS[key]) {
				result[key] = value
			}
		}
		return result
	}

	/**
	 * Force log a security violation
	 * 
	 * @param user user involved in the security violation
	 * @param message message associated with the security violation
	 */
	def logSecurityViolation(user, message) {
		if (user) {
			log.info "USER_ACTIVITY: SECURITY_VIOLATION: $message by user $user"
		} else {
			log.info "USER_ACTIVITY: SECURITY_VIOLATION: $message"
		}
	}

	/**
	 * Check if the configuration property is set to activity
	 * 
	 * @return returns true if the configuration property is set to activity.
	 */
	def logActivityEnabled() {
		return AUDIT_ACTIVITY
	}

}
