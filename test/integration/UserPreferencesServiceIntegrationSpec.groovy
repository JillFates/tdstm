import spock.lang.Specification
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import spock.lang.See

import net.transitionmanager.domain.Person
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.domain.UserPreference
import com.tdsops.tm.enums.domain.UserPreferenceEnum
import net.transitionmanager.service.UserPreferenceService
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.UserPreferenceService
import com.tdsops.tm.enums.domain.SecurityRole

class UserPreferencesServiceIntegrationSpec extends Specification {

	// IOC
	SecurityService securityService
	UserPreferenceService userPreferenceService

	// Shared variables
	private ProjectTestHelper projectHelper = new ProjectTestHelper()
	private PersonTestHelper personHelper = new PersonTestHelper()
	private UserLogin userLogin
	private Person person
	private UserPreferenceEnum pref = UserPreferenceEnum.CURR_TZ
	private String prefCode = pref.toString()
	UserPreferenceEnum soPref = UserPreferenceEnum.sessionOnlyPreferences[0]
	String soPrefCode = soPref.toString()
	String result

	void setup() {
		person = personHelper.createPerson()
		userLogin = personHelper.createUserLoginWithRoles(person, ["${SecurityRole.ADMIN}"])
		securityService.assumeUserIdentity(userLogin.username, false)
    }

	def '1. Test a user without an existing preference'() {
		setup: 'some useful values'
			String defaultResult = 'America/Boston'

		when: 'getting a non-existent preference'
			result = userPreferenceService.getPreference(userLogin, pref)
		then: 'the result should be null'
			result == null
		and: 'the http session should not contain the preference'
			userPreferenceService.session.getAttribute(prefCode) == null

		when: 'getting a non-existent preference while providing a default value'
			result = userPreferenceService.getPreference(userLogin, pref, defaultResult)
		then: 'the result should match the default value'
			defaultResult == result
		and: 'the http session should now contain the preference'
			defaultResult == userPreferenceService.session.getAttribute(prefCode)
	}

	def '2. Test a user with a persisted preference'() {
		setup: 'some useful values'
			String prefValue = 'America/Boston'
		and: 'create a user persisted preference'
			userPreferenceService.setPreference(userLogin, pref, prefValue)

		when: 'getting a persisted preference'
			result = userPreferenceService.getPreference(userLogin, pref)
		then: 'the result should match the previously persisted value'
			prefValue == result
		and: 'the http session should also contain the preference'
			prefValue == userPreferenceService.session.getAttribute(prefCode)
		and: 'the perference should be in the database table'
			userPreferenceService.getUserPreference(userLogin, prefCode)

		when: 'getting a persisted preference while providing a default value'
			result = userPreferenceService.getPreference(userLogin, pref, 'bogus value')
		then: 'the result should match the previously persisted value'
			prefValue == result
	}

	def '3. Test a user accessing a Session Only preference'() {
		setup: 'some useful values'
			String prefValue = 'SessionOnly'

		when: 'attempt to access Session Only preference that has not been set'
			result = userPreferenceService.getPreference(userLogin, soPref)
		then: 'the result should be null'
			result == null
		and: 'the http session should also be null'
			userPreferenceService.session.getAttribute(soPrefCode) == null

		when: 'attempt to access Session Only preference that has not been set but a default is included'
			result = userPreferenceService.getPreference(userLogin, soPref, prefValue)
		then: 'the result should return the default value'
			prefValue == result
		and: 'the http session should remain null'
			userPreferenceService.session.getAttribute(soPrefCode) == null

		when: 'a Session Only preference is set'
			result = userPreferenceService.setPreference(userLogin, soPref, prefValue)
		then: 'the accessing the preference with no default should return the saved value'
			prefValue == userPreferenceService.getPreference(userLogin, soPref)
		and: 'the http session should contain the same value'
			prefValue == userPreferenceService.session.getAttribute(soPrefCode)
		and: 'there should be NO database preference record'
			userPreferenceService.getUserPreference(userLogin, prefCode) == null
	}

	def '4. Validate that clearSessionOnlyPreferences removes SessionOnly set preferences'() {
		setup: 'some useful values'
			String prefValue = 'SessionOnly'

		when: 'a Session Only preference is set'
			result = userPreferenceService.setPreference(userLogin, soPref, prefValue)
		then: 'the http session should contain the set value'
			prefValue == userPreferenceService.session.getAttribute(soPrefCode)

		when: 'the clearSessionOnlyPreferences method is called'
			userPreferenceService.clearSessionOnlyPreferences()
		then: 'the http session should no longer contain the previously set attribute'
			userPreferenceService.session.getAttribute(soPrefCode) == null
	}

	def '5. Validate that switching projects removes SessionOnly preferences'() {
		setup: 'some useful values'
			String prefValue = 'SessionOnly'

		when: 'a Session Only preference is set'
			result = userPreferenceService.setPreference(userLogin, soPref, prefValue)
		then: 'the http session should contain the set value'
			prefValue == userPreferenceService.session.getAttribute(soPrefCode)

		when: 'the setCurrentProjectId method is called'
			userPreferenceService.setCurrentProjectId(1234L)
		then: 'the http session should no longer contain the previously set attribute'
			userPreferenceService.session.getAttribute(soPrefCode) == null
	}

}
