package com.tdsops.apiaction

import org.apache.commons.httpclient.HttpStatus

enum ReactionHttpStatusCodes {

    // --- 1xx Informational ---

    /** <tt>100 Continue</tt> (HTTP/1.1 - RFC 2616) */
    CONTINUE(HttpStatus.SC_CONTINUE),
    /** <tt>101 Switching Protocols</tt> (HTTP/1.1 - RFC 2616)*/
    SWITCHING_PROTOCOLS(HttpStatus.SC_SWITCHING_PROTOCOLS),
    /** <tt>102 Processing</tt> (WebDAV - RFC 2518) */
    PROCESSING(HttpStatus.SC_PROCESSING),

    // --- 2xx Success ---

    /** <tt>200 OK</tt> (HTTP/1.0 - RFC 1945) */
    OK(HttpStatus.SC_OK),
    /** <tt>201 Created</tt> (HTTP/1.0 - RFC 1945) */
    CREATED(HttpStatus.SC_CREATED),
    /** <tt>202 Accepted</tt> (HTTP/1.0 - RFC 1945) */
    ACCEPTED(HttpStatus.SC_ACCEPTED),
    /** <tt>203 Non Authoritative Information</tt> (HTTP/1.1 - RFC 2616) */
    AUTHORITATIVE_INFORMATION(HttpStatus.SC_NON_AUTHORITATIVE_INFORMATION),
    /** <tt>204 No Content</tt> (HTTP/1.0 - RFC 1945) */
    NO_CONTENT(HttpStatus.SC_NO_CONTENT),
    /** <tt>205 Reset Content</tt> (HTTP/1.1 - RFC 2616) */
//    HttpStatus.SC_RESET_CONTENT = 205,
    /** <tt>206 Partial Content</tt> (HTTP/1.1 - RFC 2616) */
//    HttpStatus.SC_PARTIAL_CONTENT = 206,
    /**
     * <tt>207 Multi-Status</tt> (WebDAV - RFC 2518) or <tt>207 Partial Update
     * OK</tt> (HTTP/1.1 - draft-ietf-http-v11-spec-rev-01?)
     */
//    HttpStatus.SC_MULTI_STATUS = 207,

    // --- 3xx Redirection ---

    /** <tt>300 Mutliple Choices</tt> (HTTP/1.1 - RFC 2616) */
//    HttpStatus.SC_MULTIPLE_CHOICES = 300,
    /** <tt>301 Moved Permanently</tt> (HTTP/1.0 - RFC 1945) */
//    HttpStatus.SC_MOVED_PERMANENTLY = 301,
    /** <tt>302 Moved Temporarily</tt> (Sometimes <tt>Found</tt>) (HTTP/1.0 - RFC 1945) */
//    HttpStatus.SC_MOVED_TEMPORARILY = 302,
    /** <tt>303 See Other</tt> (HTTP/1.1 - RFC 2616) */
//    HttpStatus.SC_SEE_OTHER = 303,
    /** <tt>304 Not Modified</tt> (HTTP/1.0 - RFC 1945) */
//    HttpStatus.SC_NOT_MODIFIED = 304,
    /** <tt>305 Use Proxy</tt> (HTTP/1.1 - RFC 2616) */
//    HttpStatus.SC_USE_PROXY = 305,
    /** <tt>307 Temporary Redirect</tt> (HTTP/1.1 - RFC 2616) */
//    HttpStatus.SC_TEMPORARY_REDIRECT = 307,

    // --- 4xx Client Error ---

    /** <tt>400 Bad Request</tt> (HTTP/1.1 - RFC 2616) */
    BAD_REQUEST(HttpStatus.SC_BAD_REQUEST),
    /** <tt>401 Unauthorized</tt> (HTTP/1.0 - RFC 1945) */
    UNAUTHORIZED(HttpStatus.SC_UNAUTHORIZED),
    /** <tt>402 Payment Required</tt> (HTTP/1.1 - RFC 2616) */
//    HttpStatus.SC_PAYMENT_REQUIRED = 402,
    /** <tt>403 Forbidden</tt> (HTTP/1.0 - RFC 1945) */
    FORBIDDEN(HttpStatus.SC_FORBIDDEN),
    /** <tt>404 Not Found</tt> (HTTP/1.0 - RFC 1945) */
    NOT_FOUND(HttpStatus.SC_NOT_FOUND),
    /** <tt>405 Method Not Allowed</tt> (HTTP/1.1 - RFC 2616) */
    METHOD_NOT_ALLOWED(HttpStatus.SC_METHOD_NOT_ALLOWED),
    /** <tt>406 Not Acceptable</tt> (HTTP/1.1 - RFC 2616) */
//    HttpStatus.SC_NOT_ACCEPTABLE = 406,
    /** <tt>407 Proxy Authentication Required</tt> (HTTP/1.1 - RFC 2616)*/
//    HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED = 407,
    /** <tt>408 Request Timeout</tt> (HTTP/1.1 - RFC 2616) */
//    HttpStatus.SC_REQUEST_TIMEOUT = 408,
    /** <tt>409 Conflict</tt> (HTTP/1.1 - RFC 2616) */
//    HttpStatus.SC_CONFLICT = 409,
    /** <tt>410 Gone</tt> (HTTP/1.1 - RFC 2616) */
//    HttpStatus.SC_GONE = 410,
    /** <tt>411 Length Required</tt> (HTTP/1.1 - RFC 2616) */
//    HttpStatus.SC_LENGTH_REQUIRED = 411,
    /** <tt>412 Precondition Failed</tt> (HTTP/1.1 - RFC 2616) */
//    HttpStatus.SC_PRECONDITION_FAILED = 412,
    /** <tt>413 Request Entity Too Large</tt> (HTTP/1.1 - RFC 2616) */
//    HttpStatus.SC_REQUEST_TOO_LONG = 413,
    /** <tt>414 Request-URI Too Long</tt> (HTTP/1.1 - RFC 2616) */
//    HttpStatus.SC_REQUEST_URI_TOO_LONG = 414,
    /** <tt>415 Unsupported Media Type</tt> (HTTP/1.1 - RFC 2616) */
//    HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE = 415,
    /** <tt>416 Requested Range Not Satisfiable</tt> (HTTP/1.1 - RFC 2616) */
//    HttpStatus.SC_REQUESTED_RANGE_NOT_SATISFIABLE = 416,
    /** <tt>417 Expectation Failed</tt> (HTTP/1.1 - RFC 2616) */
//    HttpStatus.SC_EXPECTATION_FAILED = 417,

    /**
     * Static constant for a 418 error.
     * <tt>418 Unprocessable Entity</tt> (WebDAV drafts?)
     * or <tt>418 Reauthentication Required</tt> (HTTP/1.1 drafts?)
     */
    // not used
    // HttpStatus.SC_UNPROCESSABLE_ENTITY = 418,

    /**
     * Static constant for a 419 error.
     * <tt>419 Insufficient Space on Resource</tt>
     * (WebDAV - draft-ietf-webdav-protocol-05?)
     * or <tt>419 Proxy Reauthentication Required</tt>
     * (HTTP/1.1 drafts?)
     */
//    HttpStatus.SC_INSUFFICIENT_SPACE_ON_RESOURCE = 419,
    /**
     * Static constant for a 420 error.
     * <tt>420 Method Failure</tt>
     * (WebDAV - draft-ietf-webdav-protocol-05?)
     */
//    HttpStatus.SC_METHOD_FAILURE = 420,
    /** <tt>422 Unprocessable Entity</tt> (WebDAV - RFC 2518) */
//    HttpStatus.SC_UNPROCESSABLE_ENTITY = 422,
    /** <tt>423 Locked</tt> (WebDAV - RFC 2518) */
//    HttpStatus.SC_LOCKED = 423,
    /** <tt>424 Failed Dependency</tt> (WebDAV - RFC 2518) */
//    HttpStatus.SC_FAILED_DEPENDENCY = 424,

    // --- 5xx Server Error ---

    /** <tt>500 Server Error</tt> (HTTP/1.0 - RFC 1945) */
    INTERNAL_SERVER_ERROR(HttpStatus.SC_INTERNAL_SERVER_ERROR),
    /** <tt>501 Not Implemented</tt> (HTTP/1.0 - RFC 1945) */
    NOT_IMPLEMENTED(HttpStatus.SC_NOT_IMPLEMENTED),
    /** <tt>502 Bad Gateway</tt> (HTTP/1.0 - RFC 1945) */
    BAD_GATEWAY(HttpStatus.SC_BAD_GATEWAY),
    /** <tt>503 Service Unavailable</tt> (HTTP/1.0 - RFC 1945) */
    SERVICE_UNAVAILABLE(HttpStatus.SC_SERVICE_UNAVAILABLE),
    /** <tt>504 Gateway Timeout</tt> (HTTP/1.1 - RFC 2616) */
    GATEWAY_TIMEOUT(HttpStatus.SC_GATEWAY_TIMEOUT),
    /** <tt>505 HTTP Version Not Supported</tt> (HTTP/1.1 - RFC 2616) */
//    HttpStatus.SC_HTTP_VERSION_NOT_SUPPORTED = 505,

    /** <tt>507 Insufficient Storage</tt> (WebDAV - RFC 2518) */
//    HttpStatus.SC_INSUFFICIENT_STORAGE = 507,

    private final int code

    ReactionHttpStatusCodes(int code){
        this.code = code
    }

    int getCode(){
        this.code
    }
}