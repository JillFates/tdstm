package net.transitionmanager.integration

import org.apache.http.HttpStatus

/**
 *
 */
class ReactionHttpStatus {

	// --- 1xx Informational ---
	public static final int CONTINUE = HttpStatus.SC_CONTINUE

	public static final int SWITCHING_PROTOCOLS = HttpStatus.SC_SWITCHING_PROTOCOLS
	public static final int PROCESSING = HttpStatus.SC_PROCESSING

	// --- 2xx Success ---
	public static final int OK = HttpStatus.SC_OK
	public static final int CREATED = HttpStatus.SC_CREATED
	public static final int ACCEPTED = HttpStatus.SC_ACCEPTED
	public static final int AUTHORITATIVE_INFORMATION = HttpStatus.SC_NON_AUTHORITATIVE_INFORMATION
	public static final int NO_CONTENT = HttpStatus.SC_NO_CONTENT
	public static final int RESET_CONTENT = HttpStatus.SC_RESET_CONTENT
	public static final int PARTIAL_CONTENT = HttpStatus.SC_PARTIAL_CONTENT

	// --- 3xx Redirection ---
	public static final int MULTIPLE_CHOICES = HttpStatus.SC_MULTIPLE_CHOICES
	public static final int MOVED_PERMANENTLY = HttpStatus.SC_MOVED_PERMANENTLY
	public static final int MOVED_TEMPORARILY = HttpStatus.SC_MOVED_TEMPORARILY
	public static final int SEE_OTHER = HttpStatus.SC_SEE_OTHER
	public static final int NOT_MODIFIED = HttpStatus.SC_NOT_MODIFIED
	public static final int USE_PROXY = HttpStatus.SC_USE_PROXY
	public static final int TEMPORARY_REDIRECT = HttpStatus.SC_TEMPORARY_REDIRECT

	// --- 4xx Client Error ---
	public static final int BAD_REQUEST = HttpStatus.SC_BAD_REQUEST
	public static final int UNAUTHORIZED = HttpStatus.SC_UNAUTHORIZED
	public static final int PAYMENT_REQUIRED = HttpStatus.SC_PAYMENT_REQUIRED
	public static final int FORBIDDEN = HttpStatus.SC_FORBIDDEN
	public static final int NOT_FOUND = HttpStatus.SC_NOT_FOUND
	public static final int METHOD_NOT_ALLOWED = HttpStatus.SC_METHOD_NOT_ALLOWED
	public static final int NOT_ACCEPTABLE = HttpStatus.SC_NOT_ACCEPTABLE
	public static final int PROXY_AUTHENTICATION_REQUIRED = HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED
	public static final int REQUEST_TIMEOUT = HttpStatus.SC_REQUEST_TIMEOUT
	public static final int CONFLICT = HttpStatus.SC_CONFLICT
	public static final int GONE = HttpStatus.SC_GONE
	public static final int LENGTH_REQUIRED = HttpStatus.SC_LENGTH_REQUIRED
	public static final int PRECONDITION_FAILED = HttpStatus.SC_PRECONDITION_FAILED
	public static final int REQUEST_TOO_LONG = HttpStatus.SC_REQUEST_TOO_LONG
	public static final int REQUEST_URI_TOO_LONG = HttpStatus.SC_REQUEST_URI_TOO_LONG
	public static final int UNSUPPORTED_MEDIA_TYPE = HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE
	public static final int REQUESTED_RANGE_NOT_SATISFIABLE = HttpStatus.SC_REQUESTED_RANGE_NOT_SATISFIABLE
	public static final int EXPECTATION_FAILED = HttpStatus.SC_EXPECTATION_FAILED

	// --- 5xx Server Error ---
	public static final int INTERNAL_SERVER_ERROR = HttpStatus.SC_INTERNAL_SERVER_ERROR
	public static final int NOT_IMPLEMENTED = HttpStatus.SC_NOT_IMPLEMENTED
	public static final int BAD_GATEWAY = HttpStatus.SC_BAD_GATEWAY
	public static final int SERVICE_UNAVAILABLE = HttpStatus.SC_SERVICE_UNAVAILABLE
	public static final int GATEWAY_TIMEOUT = HttpStatus.SC_GATEWAY_TIMEOUT
}
