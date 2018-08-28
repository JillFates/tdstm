import com.tdssrc.grails.TimeUtil
import grails.test.GrailsMock
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

import net.transitionmanager.domain.UserLogin

import spock.lang.Specification
import test.AbstractUnitSpec

@TestFor(UserLoginController)
@Mock([UserLogin])
class UserLoginControllerSpec extends AbstractUnitSpec {

	void testLockoutDateIndefinite() {
		setup:
		Calendar cal = Calendar.getInstance();
		Date today = cal.getTime();
		cal.add(Calendar.YEAR, 10);
		cal.add(Calendar.DATE, 20);
		Date lockedTime = cal.getTime();
		def lockoutString = controller.generateLockoutTimeString(lockedTime)
		
		expect:
		lockoutString == 'Indefinitely'
	}
	
	void testLockoutDateIndefinite() {
		setup:
		Calendar cal = Calendar.getInstance();
		Date today = cal.getTime();
		cal.add(Calendar.DATE, 20);
		Date lockedTime = cal.getTime();
		def lockoutString = controller.generateLockoutTimeString(lockedTime)
		
		expect:
		lockoutString != 'Indefinitely'
	}
}