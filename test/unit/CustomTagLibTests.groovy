

import grails.test.*

import java.text.SimpleDateFormat

import com.tdssrc.grails.GormUtil
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(CustomTagLib)
class CustomTagLibTests extends Specification {

	/** Setup metaclass fixtures for mocking. */
	protected void setup() {
	}

	/** Remove metaclass fixtures for mocking. */
	def cleanup() {
	}

	void testConvertDate() {
		def format = [
			"MM/dd kk:mm:ss" : [["GMT":"08/21 20:00:00"],["PST":"08/21 12:00:00"],["PDT":"08/21 13:00:00"],["MST":"08/21 13:00:00"],["MDT":"08/21 14:00:00"],
								["CST":"08/21 14:00:00"],["CDT":"08/21 15:00:00"],["EST":"08/21 15:00:00"],["EDT":"08/21 16:00:00"]],
			"MM/dd": [["GMT":"08/21"],["PST":"08/21"],["PDT":"08/21"],["MST":"08/21"],["MDT":"08/21"],
					  ["CST":"08/21"],["CDT":"08/21"],["EST":"08/21"],["EDT":"08/21"]],
			"null": [["GMT":"08/21/2012"],["PST":"08/21/2012"],	["PDT":"08/21/2012"],["MST":"08/21/2012"],["MDT":"08/21/2012"],
					 ["CST":"08/21/2012"],["CDT":"08/21/2012"],["EST":"08/21/2012"],["EDT":"08/21/2012"]]
		]

		def date = new Date("08/21/2012 20:00:00")

		def correct = true
		def convertedDate

		format.each{ key, value ->

			value.each{ formatValue  ->

				formatValue.each{ timeZone, expectedDate ->
					convertedDate = applyTemplate('<tds:convertDate date="${date}" timeZone="${timeZone}" format="${format}" />', [date:date, timeZone:timeZone, format:key])
					correct = correct && expectedDate.equals(convertedDate)
				}
			}
		}

		expect:
			correct
	}

	void testTextAsLink() {

		expect:
			// Just Text
			applyTemplate('<tds:textAsLink text="${text}" />', [text: "p:some more data that is not a URL"]).equals("p:some more data that is not a URL")
			// Testing http
			applyTemplate('<tds:textAsLink text="${text}" target="${target}" />', [text: "http://www.google.com", target:"_blank"]).startsWith("<a href")
			// Testing HTTP
			applyTemplate('<tds:textAsLink text="${text}" target="${target}" />', [text: "HTTP://www.google.com", target:"_blank"]).startsWith("<a href")
			// Testing https
			applyTemplate('<tds:textAsLink text="${text}" target="${target}" />', [text: "https://www.google.com", target:"_blank"]).startsWith("<a href")
			// Testing HTTPS
			applyTemplate('<tds:textAsLink text="${text}" target="${target}" />', [text: "HTTPS://www.google.com", target:"_blank"]).startsWith("<a href")
			// Testing ftp
			applyTemplate('<tds:textAsLink text="${text}" target="${target}" />', [text: "ftp://www.google.com", target:"_blank"]).startsWith("<a href")
			// Testing FTP
			applyTemplate('<tds:textAsLink text="${text}" target="${target}" />', [text: "FTP://www.google.com", target:"_blank"]).startsWith("<a href")
			// Testing ftps
			applyTemplate('<tds:textAsLink text="${text}" target="${target}" />', [text: "ftps://www.google.com", target:"_blank"]).startsWith("<a href")
			// Testing FTPS
			applyTemplate('<tds:textAsLink text="${text}" target="${target}" />', [text: "FTPS://www.google.com", target:"_blank"]).startsWith("<a href")
			// Testing smb
			applyTemplate('<tds:textAsLink text="${text}" target="${target}" />', [text: "smb://www.google.com", target:"_blank"]).startsWith("<a href")
			// Testing SMB
			applyTemplate('<tds:textAsLink text="${text}" target="${target}" />', [text: "SMB://www.google.com", target:"_blank"]).startsWith("<a href")
			// Testing file
			applyTemplate('<tds:textAsLink text="${text}" target="${target}" />', [text: "file://www.google.com", target:"_blank"]).startsWith("<a href")
			// Testing FILE
			applyTemplate('<tds:textAsLink text="${text}" target="${target}" />', [text: "FILE://www.google.com", target:"_blank"]).startsWith("<a href")
			// Testing UNC
			applyTemplate('<tds:textAsLink text="${text}" />', [text: '\\\\hola\\dir\\file']).startsWith('<a href="file://hola/dir/file')
			// A Windows File
			applyTemplate('<tds:textAsLink text="${text}" />', [text: 'p:\\dir\\file name']).startsWith('<a href="file://p%3A%2Fdir%2Ffile+name')
			// Testing Blank Text
			applyTemplate('<tds:textAsLink text="${text}" />', [text: '']).equals('')
			// Testing Null Text
			applyTemplate('<tds:textAsLink text="${text}" />', [text: null]).equals('')
	}

}
