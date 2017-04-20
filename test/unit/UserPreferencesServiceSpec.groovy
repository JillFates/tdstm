import com.tdsops.tm.enums.domain.UserPreferenceEnum
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.domain.UserPreference
import net.transitionmanager.service.UserPreferenceService
import test.AbstractUnitSpec

/**
 * Created by octavio on 4/20/17.
 */

@TestFor(UserPreferenceService)
@Mock([UserLogin, UserPreference])
@TestMixin(ControllerUnitTestMixin)
class UserPreferencesServiceSpec extends AbstractUnitSpec {
	def userLogin
	def setup() {
		userLogin = login()
	}

	def 'test preference stored in session is the actual value'() {
		when:
			def prefValue = service.getPreference(UserPreferenceEnum.CURR_TZ)

		then:
			session.getAttribute(UserPreferenceEnum.CURR_TZ.toString()) == prefValue
	}

	def 'test that the preference stored is not a Map (TM-5572)'() {
		when:
			def prefValue = service.getPreference(UserPreferenceEnum.CURR_TZ)

		then:
			!(prefValue instanceof Map)
	}
}
