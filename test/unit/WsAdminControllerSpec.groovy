import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.service.SecurityService
import test.AbstractUnitSpec

@Mock(UserLogin)
@TestFor(WsAdminController)
class WsAdminControllerSpec extends AbstractUnitSpec {

	@SuppressWarnings('GroovyVariableNotAssigned')
	void testUnlockAccount() {
		given:
		UserLogin user
		boolean called = false

		controller.securityService = new SecurityService() {
			void unlockAccount(UserLogin userLogin) {
				assert user.is(userLogin)
				called = true
			}
		}

		when:
		user = login()

		then:
		assert user

		when:
		controller.params.id = user.id.toString()
		controller.unlockAccount()

		then:
		called
		assertSuccessJson controller.response
	}
}
