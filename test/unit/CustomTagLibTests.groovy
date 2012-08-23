

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
}
