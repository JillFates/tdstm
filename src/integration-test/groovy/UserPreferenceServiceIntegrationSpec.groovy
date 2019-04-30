import com.tdsops.tm.enums.domain.SecurityRole
import com.tdsops.tm.enums.domain.UserPreferenceEnum
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import grails.util.GrailsWebMockUtil
import net.transitionmanager.person.Person
import net.transitionmanager.security.UserLogin
import net.transitionmanager.security.SecurityService
import net.transitionmanager.person.UserPreferenceService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

@Integration
@Rollback
class UserPreferenceServiceIntegrationSpec extends Specification {

	// IOC
	@Autowired
	SecurityService securityService
	@Autowired
	UserPreferenceService userPreferenceService

	// Shared variables
	private PersonTestHelper personHelper
	private UserLogin userLogin
	private Person person
	private UserPreferenceEnum pref = UserPreferenceEnum.CURR_TZ
	private String prefCode = pref.name()
	UserPreferenceEnum soPref = UserPreferenceEnum.sessionOnlyPreferences[0]
	String soPrefCode = soPref.name()
	String result

	void setup() {
		personHelper  = new PersonTestHelper()
		person = personHelper.createPerson()
		userLogin = personHelper.createUserLoginWithRoles(person, ["${SecurityRole.ROLE_ADMIN}"])
		securityService.assumeUserIdentity(userLogin.username, false)
		GrailsWebMockUtil.bindMockWebRequest()
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
		and: 'the preference should be in the database table'
			userPreferenceService.getPreference(userLogin, prefCode)
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
			userPreferenceService.getPreference(userLogin, prefCode) == null
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

	def '6. Validate that updating preferences also updates the session so getPreference returns the correct value'() {
		setup: 'Some values required.'
		String preferenceValue = "100"
		String anotherValue = "200"
		UserPreferenceEnum preferenceCode = UserPreferenceEnum.ASSET_LIST_SIZE

		when: 'setting the preference for the first time'
			userPreferenceService.setPreference(userLogin, preferenceCode, preferenceValue)
		then: 'the session has the correct value'
			userPreferenceService.session.getAttribute(preferenceCode.name()) == preferenceValue
		and: 'getPreference returns the correct value'
			userPreferenceService.getPreference(userLogin, preferenceCode) == preferenceValue

		when: 'updating an existing preference'
			userPreferenceService.setPreference(userLogin, preferenceCode, anotherValue)
		then: 'the session is updated correctly'
			userPreferenceService.session.getAttribute(preferenceCode.name()) == anotherValue
		and: 'the new value is retrieved when getting the preference'
			userPreferenceService.getPreference(userLogin, preferenceCode) == anotherValue

	}

}
