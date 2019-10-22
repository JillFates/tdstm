package net.transitionmanager.security

import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import spock.lang.Specification

@Integration
@Rollback
class UserLoginIntegrationSpec extends Specification {
	def 'test password change triggers Dirty flag in domain object TM-16095'() {
		setup:
			// UserLogin userLogin = new UserLogin()
			UserLogin userLogin = UserLogin.first()

		when:
			userLogin.applyPassword('le password')

		then:
			userLogin.isDirty() == true
			userLogin.isDirty('password') == true
	}
}
