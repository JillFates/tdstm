

import grails.test.*

import java.text.SimpleDateFormat

import com.tdssrc.grails.GormUtil

class CustomTagLibTests extends GrailsUnitTestCase {

	// mocked "out" for taglib
    StringWriter out

    /** Setup metaclass fixtures for mocking. */
    protected void setUp() {
    	super.setUp()
        out = new StringWriter()
        CustomTagLib.metaClass.out = out
    }

    /** Remove metaclass fixtures for mocking. */
    protected void tearDown() {
     	super.tearDown()
        def remove = GroovySystem.metaClassRegistry.&removeMetaClass
        remove CustomTagLib
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

		format.each{ key, value ->

			value.each{ formatValue  ->

				formatValue.each{ timeZone, expectedDate ->

					assertEquals "Test format ${key} for timezone ${timeZone}", expectedDate, new CustomTagLib().convertDate(date:date, format:key, timeZone:timeZone).toString()
					// reset "out" buffer
					out.getBuffer().setLength(0)
				}
			}
		}
	}
	
	void testTextAsLink() {

		def justText = 'p:some more data that is not a URL'
		assertEquals 'Just Text', justText, new CustomTagLib().textAsLink([text:justText])?.toString()
		out.getBuffer().setLength(0)

		assertTrue 'Testing http', new CustomTagLib().textAsLink([text:'http://www.google.com', target:'_blank']).toString().startsWith("<a href")
		out.getBuffer().setLength(0)

		assertTrue 'Testing HTTP', new CustomTagLib().textAsLink([text:'HTTP://www.google.com', target:'_blank']).toString().startsWith("<a href")
		out.getBuffer().setLength(0)

		assertTrue 'Testing https', new CustomTagLib().textAsLink([text:'https://www.google.com', target:'_blank']).toString().startsWith("<a href")
		out.getBuffer().setLength(0)

		assertTrue 'Testing HTTPS', new CustomTagLib().textAsLink([text:'HTTPS://www.google.com', target:'_blank']).toString().startsWith("<a href")
		out.getBuffer().setLength(0)

		assertTrue 'Testing ftp', new CustomTagLib().textAsLink([text:'ftp://www.google.com', target:'_blank']).toString().startsWith("<a href")
		out.getBuffer().setLength(0)

		assertTrue 'Testing FTP', new CustomTagLib().textAsLink([text:'FTP://www.google.com', target:'_blank']).toString().startsWith("<a href")
		out.getBuffer().setLength(0)

		assertTrue 'Testing ftps', new CustomTagLib().textAsLink([text:'ftps://www.google.com', target:'_blank']).toString().startsWith("<a href")
		out.getBuffer().setLength(0)

		assertTrue 'Testing FTPS', new CustomTagLib().textAsLink([text:'FTPS://www.google.com', target:'_blank']).toString().startsWith("<a href")
		out.getBuffer().setLength(0)

		assertTrue 'Testing smb', new CustomTagLib().textAsLink([text:'smb://www.google.com', target:'_blank']).toString().startsWith("<a href")
		out.getBuffer().setLength(0)

		assertTrue 'Testing SMB', new CustomTagLib().textAsLink([text:'SMB://www.google.com', target:'_blank']).toString().startsWith("<a href")
		out.getBuffer().setLength(0)

		assertTrue 'Testing file', new CustomTagLib().textAsLink([text:'file://www.google.com', target:'_blank']).toString().startsWith("<a href")
		out.getBuffer().setLength(0)

		assertTrue 'Testing FILE', new CustomTagLib().textAsLink([text:'FILE://www.google.com', target:'_blank']).toString().startsWith("<a href")
		out.getBuffer().setLength(0)

		assertTrue 'Testing UNC', new CustomTagLib().textAsLink([text:'\\\\hola\\dir\\file']).toString().startsWith('<a href="file://hola/dir/file')
		out.getBuffer().setLength(0)

		assertTrue 'A Windows File', new CustomTagLib().textAsLink([text:'p:\\dir\\file name'])?.toString().startsWith('<a href="file://p%3A%2Fdir%2Ffile+name')
		out.getBuffer().setLength(0)

		assertEquals 'Testing Blank Text', '', new CustomTagLib().textAsLink([text:null]).toString()
		out.getBuffer().setLength(0)

		assertEquals 'Testing Null Text', '', new CustomTagLib().textAsLink([text:null]).toString()
		out.getBuffer().setLength(0)
	}
}
