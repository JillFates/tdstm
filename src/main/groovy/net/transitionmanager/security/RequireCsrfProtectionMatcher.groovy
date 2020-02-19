package net.transitionmanager.security

import groovy.transform.CompileStatic
import org.springframework.security.web.util.matcher.RequestMatcher

import javax.servlet.http.HttpServletRequest
import java.util.regex.Pattern

/**
 * A class for testing if a request url is valid for CSRF protection.
 */
@CompileStatic
class RequireCsrfProtectionMatcher implements RequestMatcher {

	// Always allow the HTTP GET method
	private Pattern allowedMethods = Pattern.compile('^GET$')
	static  List    ignoreUris     = [
		/^\/tdstm\/reports\/.*$/,
		/^\/tdstm\/apiCatalog\/.*$/,
		/^\/tdstm\/team\/.*$/,
		/^\/tdstm\/person\/.*$/,
		/^\/tdstm\/admin\/.*$/,
		/^\/tdstm\/ws\/admin\/.*$/,
		/^\/tdstm\/assetEntity\/.*$/,
		/^\/tdstm\/moveBundle\/.*$/,
		/^\/tdstm\/moveEvent\/.*$/,
		/^\/tdstm\/partyGroup\/.*$/,
		/^\/tdstm\/userLogin\/.*$/,
		/^\/tdstm\/manufacturer\/.*$/,
		/^\/tdstm\/newsEditor\/.*$/,
		/^\/tdstm\/reports\/metricDefinitions\/.*$/,
		/^\/tdstm\/model\/.*$/,
		/^\/tdstm\/permissions\/.*$/,
		/^\/tdstm\/room\/.*$/,
		/^\/tdstm\/rackLayouts\/.*$/,
		/^\/tdstm\/ws\/user\/.*$/,
		/^\/tdstm\/ws\/asset\/dependencies.*$/, // Remove when Architecture Graph is Angularized
		/^\/tdstm\/ws\/task\/generateTasks.*$/,
		/^\/tdstm\/ws\/cookbook\/.*$/,
		/^\/tdstm\/wsTimeline\/.*$/,
		/^\/tdstm\/task\/.*$/,	 // Remove when My Task get Angularized
		/^\/tdstm\/ws\/depAnalyzer\/.*$/, // Remove when DA get Angularized
		/^\/tdstm\/application\/.*$/, // Remove when DA get Angularized
		/^\/tdstm\/api\/.*$/
	]

	/**
	 * Checks a request to see if it's requestURI matches an ignore list. If the requestURI matches any of the ignore URIs or is a GET method
	 * the method returns false, true otherwise/
	 *
	 * @param request the request to check the requestURI of.
	 *
	 * @return False if the requestURI matches any of the ignoreUris or is a GET method, and True otherwise.
	 */
	@Override
	boolean matches(HttpServletRequest request) {

		if (allowedMethods.matcher(request.getMethod()).matches()) {
			return false
		}

		for (String uri : ignoreUris) {
			if (request.requestURI ==~ uri) {
				return false
			}
		}

		return true
	}

}
