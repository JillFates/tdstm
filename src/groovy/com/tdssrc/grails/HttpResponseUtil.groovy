package com.tdssrc.grails

import grails.plugin.springsecurity.SpringSecurityUtils
import groovy.transform.CompileStatic
import org.apache.commons.lang.StringUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

import javax.servlet.http.HttpServletRequest
// import org.apache.http.HttpResponse
// import com.oracle.httpclient
// import grails.http.client.HttpClientResponse
import org.apache.http.HttpResponse

@CompileStatic
class HttpUtil {
	// Content types for Excel for (xls, xlt, and xla) and for  xlsx
	// https://blogs.msdn.microsoft.com/vsofficedeveloper/2008/05/08/office-2007-file-format-mime-types-for-http-content-streaming-2/
	static final List<String> EXCEL_CONTENT_TYPES = ['application/vnd.ms-excel', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'].asImmutable()

	/**
	 * Used to determine if a request returned Excel
	 * @param request - the HttpResponse
	 * @return true if the content is Excel
	 */
	static boolean contentIsExcel(final HttpResponse response) {
		String ctype = getContentType(response)
		return (ctype && EXCEL_CONTENT_TYPES.contains(ctype)  )
	}

	/**
	 * Used to determine if a request returned Excel (XLSX format)
	 * @param request - the HttpResponse
	 * @return true if the content is Excel
	 */
	static boolean contentIsExcelXlsx(final HttpResponse response) {
		String ctype = getContentType(response)
		return (ctype && EXCEL_CONTENT_TYPES[1] == ctype )
	}

	/**
	 * Used to retrieve a particular header by name
	 * @param headerName
	 * @return the value or null if not found
	 */
	// static List<String> getHeaders(HttpResponse response, String headerName) {
	// 	response.getHeaders(headerName)
	// }

	/**
	 * Used to retrieve the content type
	 * Note that getContentType returns a Header need to figure out how we want to use this class
	 */
	static String getContentType(HttpResponse response) {
		response.getEntity().getContentType()
	}
}
