import grails.test.mixin.TestFor
import net.transitionmanager.admin.WsUserController
import net.transitionmanager.service.UserPreferenceService
import test.AbstractUnitSpec
import spock.lang.Ignore

@Ignore
@TestFor(WsUserController)
class WsUserControllerSpec extends AbstractUnitSpec {

	void testPreferences() {
		given:
		boolean called = false
		String id = 'ShakenOrStirred,FavoriteBeatle'
		String shakenOrStirred = 'Stirred'
		String favorite = 'George'

		when:
		controller.userPreferenceService = new UserPreferenceService() {
			String getPreference(String preferenceCode) {
				called = true
				if (preferenceCode == 'ShakenOrStirred') {
					shakenOrStirred
				}
				else if (preferenceCode == 'FavoriteBeatle') {
					favorite
				}
				else {
					throw new IllegalArgumentException('Unknown pref code ' + preferenceCode)
				}
			}
		}

		controller.params.id = id
		controller.preferences()

		then:
		called
		assertSuccessJson controller.response

		when:
		def preferences = controller.response.json?.data?.preferences

		then:
		preferences
		preferences.size() == 2
		preferences.ShakenOrStirred == shakenOrStirred
		preferences.FavoriteBeatle == favorite
	}

	void testSavePreference() {
		given:
		boolean called = false
		String codeExpected = 'actual.code'
		String valueExpected = 'actual.value'

		controller.userPreferenceService = new UserPreferenceService() {
			void savePreference(String code, value) {
				assert code == codeExpected
				assert value == valueExpected
				called = true
			}
		}

		when:
		controller.params.code = codeExpected
		controller.params.value = valueExpected
		controller.savePreference()

		then:
		called
		assertSuccessJson controller.response
	}
}
