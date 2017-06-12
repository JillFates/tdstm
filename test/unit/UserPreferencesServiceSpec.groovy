import com.tdsops.tm.enums.domain.UserPreferenceEnum
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.domain.UserPreference
import net.transitionmanager.service.CommentService
import net.transitionmanager.service.UserPreferenceService
import spock.lang.See
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

	@See ('https://support.transitionmanager.com/browse/TM-5696')
	def 'Test 1: Test that all SESSION ONLY Preferences have the default value'() {
		when: 'We collect all the preference values of the session only preferences as the service retrieves it'
			def sessionOnlyPrefs = UserPreferenceEnum.sessionOnlyPreferences
			def collectedValues = sessionOnlyPrefs.collectEntries{ pref ->
				def defaultValue = CommentService.CREATE_TASKS_DEFAULTS[pref]
				if(defaultValue instanceof UserPreferenceEnum){
					defaultValue = service.getPreference(null, defaultValue)
				}

				[(pref): service.getPreference(null, pref, defaultValue)]
			}

		then: 'all values should match the defaults'
			for (UserPreferenceEnum pref : sessionOnlyPrefs){
				def defaultValue = CommentService.CREATE_TASKS_DEFAULTS[pref]
				/*
				if the default value is another UserPreferenceEnum. this is an alias,
				get the alias directly from the service to validate
				*/
				if(defaultValue instanceof UserPreferenceEnum){
					defaultValue = service.getPreference(null, defaultValue)
				}

				assert defaultValue == collectedValues[pref]
			}

	}

	def 'Test 2: change a Session Only Value check the change, switch project and check that it returns to the default'(){
		when: 'we get the default value of the TASK_CREATE_STATUS Session lived Preference'
			def defaultValue = CommentService.CREATE_TASKS_DEFAULTS[UserPreferenceEnum.TASK_CREATE_STATUS.toString()]
			def prefValue = service.getPreference(null, UserPreferenceEnum.TASK_CREATE_STATUS, defaultValue)
		then: 'the obtained value and the default must be the same'
			defaultValue == prefValue

		when: 'we change the prefValue to something else'
			def newValue = "A NEW VALUE"
			service.setPreference(UserPreferenceEnum.TASK_CREATE_STATUS, newValue)
		and: 'request the value once again from the service'
			prefValue = service.getPreference(null, UserPreferenceEnum.TASK_CREATE_STATUS, defaultValue)

		then: 'the New Value and the one returned must be the same'
			newValue == prefValue

		when: 'we select a project'
			def projId = 123
			service.setCurrentProjectId(projId)
		and: 'request the prefValue again'
			prefValue = service.getPreference(null, UserPreferenceEnum.TASK_CREATE_STATUS, defaultValue)

		then: 'the expected value is the default one'
			defaultValue == prefValue
	}


}
