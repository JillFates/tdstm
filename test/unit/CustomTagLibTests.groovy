

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
		def format = "MM/dd kk:mm:ss"
		def tagLib = new CustomTagLib()
		def date = new Date("08/21/2012 20:00:00")
		def stringBuffer = new StringBuffer()
		
		assertEquals  stringBuffer.append("08/21 20:00:00").toString(), tagLib.convertDate(date:date, format:format, timeZone:"GMT").toString().toString()
		assertEquals  stringBuffer.append("08/21 12:00:00").toString(), tagLib.convertDate(date:date, format:format, timeZone:"PST").toString().toString()
		assertEquals  stringBuffer.append("08/21 13:00:00").toString(), tagLib.convertDate(date:date, format:format, timeZone:"PDT").toString()
		assertEquals  stringBuffer.append("08/21 13:00:00").toString(), tagLib.convertDate(date:date, format:format, timeZone:"MST").toString()
		assertEquals  stringBuffer.append("08/21 14:00:00").toString(), tagLib.convertDate(date:date, format:format, timeZone:"MDT").toString()
		assertEquals  stringBuffer.append("08/21 14:00:00").toString(), tagLib.convertDate(date:date, format:format, timeZone:"CST").toString()
		assertEquals  stringBuffer.append("08/21 15:00:00").toString(), tagLib.convertDate(date:date, format:format, timeZone:"CDT").toString()
		assertEquals  stringBuffer.append("08/21 15:00:00").toString(), tagLib.convertDate(date:date, format:format, timeZone:"EST").toString()
		assertEquals  stringBuffer.append("08/21 16:00:00").toString(), tagLib.convertDate(date:date, format:format, timeZone:"EDT").toString()
		
		format = "MM/dd"
		date = new Date("08/21/2012 01:00:00")
		assertEquals  stringBuffer.append("08/21").toString(), tagLib.convertDate(date:date, format:format, timeZone:"GMT").toString()
		assertEquals  stringBuffer.append("08/20").toString(), tagLib.convertDate(date:date, format:format, timeZone:"PST").toString()
		assertEquals  stringBuffer.append("08/20").toString(), tagLib.convertDate(date:date, format:format, timeZone:"PDT").toString()
		assertEquals  stringBuffer.append("08/20").toString(), tagLib.convertDate(date:date, format:format, timeZone:"MST").toString()
		assertEquals  stringBuffer.append("08/20").toString(), tagLib.convertDate(date:date, format:format, timeZone:"MDT").toString()
		assertEquals  stringBuffer.append("08/20").toString(), tagLib.convertDate(date:date, format:format, timeZone:"CST").toString()
		assertEquals  stringBuffer.append("08/20").toString(), tagLib.convertDate(date:date, format:format, timeZone:"CDT").toString()
		assertEquals  stringBuffer.append("08/20").toString(), tagLib.convertDate(date:date, format:format, timeZone:"EST").toString()
		assertEquals  stringBuffer.append("08/20").toString(), tagLib.convertDate(date:date, format:format, timeZone:"EDT").toString()
		
		format = null
		assertEquals  stringBuffer.append("08/21/2012").toString(), tagLib.convertDate(date:date, format:format, timeZone:"GMT").toString()
		assertEquals  stringBuffer.append("08/20/2012").toString(), tagLib.convertDate(date:date, format:format, timeZone:"PST").toString()
		assertEquals  stringBuffer.append("08/20/2012").toString(), tagLib.convertDate(date:date, format:format, timeZone:"PDT").toString()
		assertEquals  stringBuffer.append("08/20/2012").toString(), tagLib.convertDate(date:date, format:format, timeZone:"MST").toString()
		assertEquals  stringBuffer.append("08/20/2012").toString(), tagLib.convertDate(date:date, format:format, timeZone:"MDT").toString()
		assertEquals  stringBuffer.append("08/20/2012").toString(), tagLib.convertDate(date:date, format:format, timeZone:"CST").toString()
		assertEquals  stringBuffer.append("08/20/2012").toString(), tagLib.convertDate(date:date, format:format, timeZone:"CDT").toString()
		assertEquals  stringBuffer.append("08/20/2012").toString(), tagLib.convertDate(date:date, format:format, timeZone:"EST").toString()
		assertEquals  stringBuffer.append("08/20/2012").toString(), tagLib.convertDate(date:date, format:format, timeZone:"EDT").toString()
		
    }
}
