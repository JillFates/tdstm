import grails.test.*

import java.text.SimpleDateFormat

import com.tdssrc.grails.GormUtil
import grails.test.mixin.TestFor
import spock.lang.Specification
import com.tdssrc.grails.TimeUtil
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsHttpSession
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest

@TestFor(CustomTagLib)
class CustomTagLibTests extends Specification {

	/** Setup metaclass fixtures for mocking. */
	protected void setup() {
	}

	/** Remove metaclass fixtures for mocking. */
	def cleanup() {
	}

	private createMockSession(String userDateFormat) {
		def request = new GrailsMockHttpServletRequest()
		def mockSession = new GrailsHttpSession(request)

		// Set the User Date Format on the session
		mockSession.setProperty('CURR_DT_FORMAT', [CURR_DT_FORMAT: userDateFormat] )

		return mockSession		
	}

	// Creates a reference test date to be used for all of the tests
	private getTestDate() {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		// def date = sdf.parse("2012-08-21T01:00:00-0000")
		def date = TimeUtil.parseDateTimeWithFormatter('GMT', '2012-08-21T01:00:00-0000', sdf)

		return date
	}

	// Returns the <tds:convertDate> taglet HTML mockup
	private getConvertDateTag() {		
		return '<tds:convertDate date="${date}" format="${format}" mockSession="${mockSession}"/>'
	}

	// Returns the <tds:convertDateTime> taglet HTML mockup
	private getConvertDateTimeTag() {		
		return '<tds:convertDateTime date="${date}" timeZone="${timeZone}" format="${format}" mockSession="${mockSession}"/>'
	}


	def 'Test tds:convertDate tag with MIDDLE_ENDIAN'() {
		setup:
			def mockSession=createMockSession(TimeUtil.MIDDLE_ENDIAN)
			Date date = getTestDate()
			String dateTag = getConvertDateTag()

		expect: 'Test DateTime with MIDDLE_ENDIAN'
			mockSession.setAttribute('CURR_TZ', [ 'CURR_TZ': timezone ] ) == null
			applyTemplate(dateTag, [date: date, format: format, mockSession:mockSession]) == expectedValue

		where:
			timezone                            | format 				| expectedValue
			'GMT'                               | TimeUtil.FORMAT_DATE 	| '08/21/2012'
			'America/Argentina/Buenos_Aires'    | TimeUtil.FORMAT_DATE 	| '08/21/2012'
			'America/New_York'                  | TimeUtil.FORMAT_DATE 	| '08/21/2012'
	}

	def 'Test tds:convertDate tag with LITTLE_ENDIAN'() {
		setup:
			def mockSession=createMockSession(TimeUtil.LITTLE_ENDIAN)
			Date date = getTestDate()
			String dateTag = getConvertDateTag()

		expect: 'Test DateTime with LITTLE_ENDIAN'
			mockSession.setAttribute('CURR_TZ', [ 'CURR_TZ': timezone ] ) == null
			applyTemplate(dateTag, [date: date, format: format, mockSession:mockSession]) == expectedValue

		where:
			timezone                            | format 				| expectedValue
			'GMT'                               | TimeUtil.FORMAT_DATE 	| '21/08/2012'
			'America/Argentina/Buenos_Aires'    | TimeUtil.FORMAT_DATE 	| '21/08/2012'
			'America/New_York'                  | TimeUtil.FORMAT_DATE 	| '21/08/2012'
	}

	def 'Test tds:convertDateTime tag with MIDDLE_ENDIAN'() {
		setup:
			def mockSession=createMockSession(TimeUtil.MIDDLE_ENDIAN)
			Date date = getTestDate()
			String dateTimeTag = getConvertDateTimeTag()

		expect: 'Test DateTime with MIDDLE_ENDIAN'
			mockSession.setAttribute('CURR_TZ', [ 'CURR_TZ': timezone ] ) == null
			applyTemplate(dateTimeTag, [date: date, format: format, mockSession:mockSession]) == expectedValue

		where:
			timezone                            | format 					| expectedValue
			'GMT'                               | TimeUtil.FORMAT_DATE_TIME | '08/21/2012 01:00 AM'
			'America/Argentina/Buenos_Aires'    | TimeUtil.FORMAT_DATE_TIME | '08/20/2012 10:00 PM'
			'America/New_York'                  | TimeUtil.FORMAT_DATE_TIME | '08/20/2012 09:00 PM'
	}

	def 'Test tds:convertDateTime tag with LITTLE_ENDIAN'() {
		setup:
			def mockSession=createMockSession(TimeUtil.LITTLE_ENDIAN)
			Date date = getTestDate()
			String dateTimeTag = getConvertDateTimeTag()

		expect: 'Test DateTime with MIDDLE_ENDIAN'
			mockSession.setAttribute('CURR_TZ', [ 'CURR_TZ': timezone ] ) == null
			applyTemplate(dateTimeTag, [date: date, format: format, mockSession:mockSession]) == expectedValue

		where:
			timezone                            | format 					| expectedValue
			'GMT'                               | TimeUtil.FORMAT_DATE_TIME | '21/08/2012 01:00 AM'
			'America/Argentina/Buenos_Aires'    | TimeUtil.FORMAT_DATE_TIME | '20/08/2012 10:00 PM'
			'America/New_York'                  | TimeUtil.FORMAT_DATE_TIME | '20/08/2012 09:00 PM'
	}

	void testTextAsLink() {

		expect:
		// Just Text
		applyTemplate('<tds:textAsLink text="${text}" />', [text: "p:some more data that is not a URL"]).equals("p:some more data that is not a URL")
		// Testing http
		applyTemplate('<tds:textAsLink text="${text}" target="${target}" />', [text: "http://www.google.com", target: "_blank"]).startsWith("<a href")
		// Testing HTTP
		applyTemplate('<tds:textAsLink text="${text}" target="${target}" />', [text: "HTTP://www.google.com", target: "_blank"]).startsWith("<a href")
		// Testing https
		applyTemplate('<tds:textAsLink text="${text}" target="${target}" />', [text: "https://www.google.com", target: "_blank"]).startsWith("<a href")
		// Testing HTTPS
		applyTemplate('<tds:textAsLink text="${text}" target="${target}" />', [text: "HTTPS://www.google.com", target: "_blank"]).startsWith("<a href")
		// Testing ftp
		applyTemplate('<tds:textAsLink text="${text}" target="${target}" />', [text: "ftp://www.google.com", target: "_blank"]).startsWith("<a href")
		// Testing FTP
		applyTemplate('<tds:textAsLink text="${text}" target="${target}" />', [text: "FTP://www.google.com", target: "_blank"]).startsWith("<a href")
		// Testing ftps
		applyTemplate('<tds:textAsLink text="${text}" target="${target}" />', [text: "ftps://www.google.com", target: "_blank"]).startsWith("<a href")
		// Testing FTPS
		applyTemplate('<tds:textAsLink text="${text}" target="${target}" />', [text: "FTPS://www.google.com", target: "_blank"]).startsWith("<a href")
		// Testing smb
		applyTemplate('<tds:textAsLink text="${text}" target="${target}" />', [text: "smb://www.google.com", target: "_blank"]).startsWith("<a href")
		// Testing SMB
		applyTemplate('<tds:textAsLink text="${text}" target="${target}" />', [text: "SMB://www.google.com", target: "_blank"]).startsWith("<a href")
		// Testing file
		applyTemplate('<tds:textAsLink text="${text}" target="${target}" />', [text: "file://www.google.com", target: "_blank"]).startsWith("<a href")
		// Testing FILE
		applyTemplate('<tds:textAsLink text="${text}" target="${target}" />', [text: "FILE://www.google.com", target: "_blank"]).startsWith("<a href")
		// Testing UNC
		applyTemplate('<tds:textAsLink text="${text}" />', [text: '\\\\hola\\dir\\file']).startsWith('<a href="file://hola/dir/file')
		// A Windows File
		applyTemplate('<tds:textAsLink text="${text}" />', [text: 'p:\\dir\\file name']).startsWith('<a href="file://p%3A%2Fdir%2Ffile+name')
		// Testing Blank Text
		applyTemplate('<tds:textAsLink text="${text}" />', [text: '']).equals('')
		// Testing Null Text
		applyTemplate('<tds:textAsLink text="${text}" />', [text: null]).equals('')
	}

	void testSVGIcon() {
		expect:
		// Verify it render the svg
		applyTemplate('<tds:svgIcon name="${name}" />', [name: "application"]).startsWith('<svg')
		// Test it contacts properly the svg method
		applyTemplate('<tds:svgIcon name="${name}" />', [name: "application"]).contains('application.svg')
		// Prevent Directory traversal
		applyTemplate('<tds:svgIcon name="${name}" />', [name: "../application"]).contains('application.svg')
		// Css attached to the element
		applyTemplate('<tds:svgIcon name="${name}" styleClass="${styleClass}" />', [name: "../application", styleClass: "myClass"]).contains('myClass')
		// Do not fail on empty name
		applyTemplate('<tds:svgIcon name="${name}" />', [name: ""]).isEmpty()
	}

	void testAppURL() {
		expect:
		// it should generate from the root to the anchor used by the ui routing
		applyTemplate('<tds:appURL controller="${controller}" fragment="list?status=active" />', [controller: "project", fragment: "list?status=active"]).contains('/tdstm/app#project/list?status=active')
		// it should generate from the root to the anchor used by the ui routing
		applyTemplate('<tds:appURL controller="${controller}" fragment="list" />', [controller: "task", fragment: "list"]).contains('/tdstm/app#task/list')
	}

}
