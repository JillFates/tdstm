import java.text.SimpleDateFormat
import com.tdssrc.grails.GormUtil

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * Unit test cases for the GormUtil class
 */
class GormUtilTests extends Specification {

    protected void setup() {
    }

    protected void cleanup() {
    }

    void testConvertInToUserTZ() {
		def format = "MM/dd/yyyy kk:mm:ss"
		def formatter = new SimpleDateFormat(format)
		def date = new Date("08/21/2012 20:00:00")
		expect:
			"08/21/2012 20:00:00".equals(formatter.format(GormUtil.convertInToUserTZ(date,"GMT")))
			"08/21/2012 12:00:00".equals(formatter.format(GormUtil.convertInToUserTZ(date,"PST")))
			"08/21/2012 13:00:00".equals(formatter.format(GormUtil.convertInToUserTZ(date,"PDT")))
			"08/21/2012 13:00:00".equals(formatter.format(GormUtil.convertInToUserTZ(date,"MST")))
			"08/21/2012 14:00:00".equals(formatter.format(GormUtil.convertInToUserTZ(date,"MDT")))
			"08/21/2012 14:00:00".equals(formatter.format(GormUtil.convertInToUserTZ(date,"CST")))
			"08/21/2012 15:00:00".equals(formatter.format(GormUtil.convertInToUserTZ(date,"CDT")))
			"08/21/2012 15:00:00".equals(formatter.format(GormUtil.convertInToUserTZ(date,"EST")))
			"08/21/2012 16:00:00".equals(formatter.format(GormUtil.convertInToUserTZ(date,"EDT")))
    }
}
