import asset.pipeline.grails.AssetProcessorService
import com.tdssrc.grails.TimeUtil
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.domain.UserPreference
import org.grails.web.mapping.DefaultLinkGenerator
import test.AbstractUnitSpec

import java.text.SimpleDateFormat

@TestFor(CustomTagLib)
@Mock([UserLogin, UserPreference, Person])
class CustomTagLibTests extends AbstractUnitSpec {

	// the <tds:convertDate> taglet HTML mockup
	private static final String convertDateTag = '<tds:convertDate date="${date}" format="${format}"/>'

	// the <tds:convertDateTime> taglet HTML mockup
	private static final String convertDateTimeTag = '<tds:convertDateTime date="${date}" timeZone="${timeZone}" format="${format}"/>'

	private Date testDate

	void setup() {
		testDate = TimeUtil.parseDateTimeWithFormatter('GMT', '2012-08-20T01:00:00-0000', new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"))

		login()
	}

	// occasionally (I haven't gotten much of a clue about what combination of factors causes this)
	// the first test will fail because something internal doesn't get correctly configured during
	// setup. in those cases having a dummy first test that doesn't use any of the configured test
	// infrastructure allows it to be 'misconfigured' but have no effect
	void 'test nothing'() {
		expect:
		'free beer'
	}

	void 'Test tds:convertDate tag with MIDDLE_ENDIAN'() {
		setup:
		setUserDateFormat TimeUtil.MIDDLE_ENDIAN

		when:
		setTimeZone timezone

		then: 'Test DateTime with MIDDLE_ENDIAN'
		applyTemplate(convertDateTag, [date: testDate, format: format]) == expectedValue

		where:
		timezone                            | format                | expectedValue
		'GMT'                               | TimeUtil.FORMAT_DATE  | '08/19/2012'
		'America/Argentina/Buenos_Aires'    | TimeUtil.FORMAT_DATE  | '08/19/2012'
		'America/New_York'                  | TimeUtil.FORMAT_DATE  | '08/19/2012'
	}

	void 'Test tds:convertDate tag with LITTLE_ENDIAN'() {
		setup:
		setUserDateFormat(TimeUtil.LITTLE_ENDIAN)

		when:
		setTimeZone timezone

		then: 'Test DateTime with LITTLE_ENDIAN'
		applyTemplate(convertDateTag, [date: testDate, format: format]) == expectedValue

		where:
		timezone                            | format                | expectedValue
		'GMT'                               | TimeUtil.FORMAT_DATE  | '19/08/2012'
		'America/Argentina/Buenos_Aires'    | TimeUtil.FORMAT_DATE  | '19/08/2012'
		'America/New_York'                  | TimeUtil.FORMAT_DATE  | '19/08/2012'
	}

	void 'Test tds:convertDateTime tag with MIDDLE_ENDIAN'() {
		setup:
		setUserDateFormat(TimeUtil.MIDDLE_ENDIAN)

		when:
		setTimeZone timezone

		then: 'Test DateTime with MIDDLE_ENDIAN'
		applyTemplate(convertDateTimeTag, [date: testDate, format: format]) == expectedValue

		where:
		timezone                            | format                    | expectedValue
		'GMT'                               | TimeUtil.FORMAT_DATE_TIME | '08/20/2012 01:00 AM'
		'America/Argentina/Buenos_Aires'    | TimeUtil.FORMAT_DATE_TIME | '08/19/2012 10:00 PM'
		'America/New_York'                  | TimeUtil.FORMAT_DATE_TIME | '08/19/2012 09:00 PM'
	}

	void 'Test tds:convertDateTime tag with LITTLE_ENDIAN'() {
		setup:
		setUserDateFormat(TimeUtil.LITTLE_ENDIAN)

		when:
		setTimeZone timezone

		then: 'Test DateTime with MIDDLE_ENDIAN'
		applyTemplate(convertDateTimeTag, [date: testDate, format: format]) == expectedValue

		where:
		timezone                            | format                    | expectedValue
		'GMT'                               | TimeUtil.FORMAT_DATE_TIME | '20/08/2012 01:00 AM'
		'America/Argentina/Buenos_Aires'    | TimeUtil.FORMAT_DATE_TIME | '19/08/2012 10:00 PM'
		'America/New_York'                  | TimeUtil.FORMAT_DATE_TIME | '19/08/2012 09:00 PM'
	}

	void testTextAsLink() {

		expect:
		// Just Text
		applyTemplate('<tds:textAsLink text="${text}" />', [text: "p:some more data that is not a URL"]) == "p:some more data that is not a URL"
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
		applyTemplate('<tds:textAsLink text="${text}" />', [text: '']) == ''
		// Testing Null Text
		applyTemplate('<tds:textAsLink text="${text}" />', [text: null]) == ''
	}

	void testSVGIcon() {
		setup:
			tagLib.assetProcessorService = [asset: { final Map attrs, final DefaultLinkGenerator linkGenerator ->
				String name = attrs.file
				return "icons/svg/${name}".toString()
			}] as AssetProcessorService
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

	private void setUserDateFormat(String userDateFormat) {
		session.setAttribute('CURR_DT_FORMAT', userDateFormat)
	}

	private void setTimeZone(String timeZoneId) {
		session.setAttribute('CURR_TZ', timeZoneId)
	}
}
