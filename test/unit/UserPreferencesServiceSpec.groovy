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
		when: 'Getting the preference CURR_TZ from the UserPreferenceService'
			def prefValue = service.getPreference(UserPreferenceEnum.CURR_TZ)

		then: 'the value in the session should be the same that the one in the preferences'
			session.getAttribute(UserPreferenceEnum.CURR_TZ.toString()) == prefValue
	}

	def 'test that the preference stored is the same type that the one on the session (TM-5572)'() {
		when: '''Getting the preference CURR_TZ from the UserPreferenceService and 
				getting the same value from the session'''
			def prefValue = service.getPreference(UserPreferenceEnum.CURR_TZ)
			def sessionValue = session.getAttribute(UserPreferenceEnum.CURR_TZ.toString())

		then: 'both values should be of the same type'
			prefValue.class == sessionValue.class
	}
}
