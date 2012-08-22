

import grails.test.*

import java.text.SimpleDateFormat

import com.tdssrc.grails.GormUtil

class CustomTagLibTests extends GrailsUnitTestCase {
	
    protected void setUp() {
        super.setUp()
		mockTagLib( CustomTagLib )
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testConvertDate() {
		def format = ["MM/dd kk:mm:ss" : ["08/21 20:00:00","08/21 12:00:00","08/21 13:00:00","08/21 13:00:00","08/21 13:00:00","08/21 14:00:00","08/21 15:00:00","08/21 15:00:00","08/21 16:00:00"],
						"MM/dd":["08/21","08/20","08/20","08/20","08/20","08/20","08/20","08/20","08/20"],
						'null':["08/21/2012","08/20/2012","08/20/2012","08/20/2012","08/20/2012","08/20/2012","08/20/2012","08/20/2012","08/20/2012"]]
		
		def tagLib = new CustomTagLib()
		def date = new Date("08/21/2012 20:00:00")
		def stringBuffer = new StringBuffer()
		def formatValue=[]
		def timeZone
	
		format.key.each{
			formatValue = format.get(it)
		
			assertEquals  "Test format ${format[0]} for timezone GMT",stringBuffer.append(formatValue[0]).toString(), tagLib.convertDate(date:date, format:formatValue[0], timeZone:"GMT").toString()
			assertEquals  "Test format ${format[1]} for timezone PST",stringBuffer.append(formatValue[1]).toString(), tagLib.convertDate(date:date, format:formatValue[1], timeZone:"PST").toString()
			assertEquals  "Test format ${format[2]} for timezone PDT",stringBuffer.append(formatValue[2]).toString(), tagLib.convertDate(date:date, format:formatValue[2], timeZone:"PDT").toString()
			assertEquals  "Test format ${format[3]} for timezone MST",stringBuffer.append(formatValue[3]).toString(), tagLib.convertDate(date:date, format:formatValue[3], timeZone:"MST").toString()
			assertEquals  "Test format ${format[4]} for timezone MDT",stringBuffer.append(formatValue[4]).toString(), tagLib.convertDate(date:date, format:formatValue[4], timeZone:"MDT").toString()
			assertEquals  "Test format ${format[5]} for timezone CST",stringBuffer.append(formatValue[5]).toString(), tagLib.convertDate(date:date, format:formatValue[5], timeZone:"CST").toString()
			assertEquals  "Test format ${format[6]} for timezone CDT",stringBuffer.append(formatValue[6]).toString(), tagLib.convertDate(date:date, format:formatValue[6], timeZone:"CDT").toString()
			assertEquals  "Test format ${format[7]} for timezone EST",stringBuffer.append(formatValue[7]).toString(), tagLib.convertDate(date:date, format:formatValue[7], timeZone:"EST").toString()
			assertEquals  "Test format ${format[8]} for timezone EDT",stringBuffer.append(formatValue[8]).toString(), tagLib.convertDate(date:date, format:formatValue[8], timeZone:"EDT").toString()
		}
    }
}
