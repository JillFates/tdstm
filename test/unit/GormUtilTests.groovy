

import grails.test.*
import java.text.SimpleDateFormat
import com.tdssrc.grails.GormUtil

class GormUtilTests extends GrailsUnitTestCase {
	
    protected void setUp() {
        super.setUp()
		mockTagLib( CustomTagLib )
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testConvertInToUserTZ() {
		def format = "MM/dd/yyyy kk:mm:ss"
		def formatter = new SimpleDateFormat(format)
		def date = new Date("08/21/2012 20:00:00")
		assertEquals  "08/21/2012 20:00:00", formatter.format(GormUtil.convertInToUserTZ(date,"GMT"))
		assertEquals  "08/21/2012 12:00:00", formatter.format(GormUtil.convertInToUserTZ(date,"PST"))
		assertEquals  "08/21/2012 13:00:00", formatter.format(GormUtil.convertInToUserTZ(date,"PDT"))
		assertEquals  "08/21/2012 13:00:00", formatter.format(GormUtil.convertInToUserTZ(date,"MST"))
		assertEquals  "08/21/2012 14:00:00", formatter.format(GormUtil.convertInToUserTZ(date,"MDT"))
		assertEquals  "08/21/2012 14:00:00", formatter.format(GormUtil.convertInToUserTZ(date,"CST"))
		assertEquals  "08/21/2012 15:00:00", formatter.format(GormUtil.convertInToUserTZ(date,"CDT"))
		assertEquals  "08/21/2012 15:00:00", formatter.format(GormUtil.convertInToUserTZ(date,"EST"))
		assertEquals  "08/21/2012 16:00:00", formatter.format(GormUtil.convertInToUserTZ(date,"EDT"))
		
    }
}
