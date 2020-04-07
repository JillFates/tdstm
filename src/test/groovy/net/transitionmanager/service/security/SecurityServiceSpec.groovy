package net.transitionmanager.service.security

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import net.transitionmanager.person.Person
import net.transitionmanager.project.Project
import net.transitionmanager.security.PasswordHistory
import net.transitionmanager.security.SecurityService
import net.transitionmanager.security.UserLogin
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import spock.lang.Shared
import spock.lang.Specification

class SecurityServiceSpec extends Specification implements ServiceUnitTest<SecurityService>, DataTest {

	@Shared
	Project project

	@Shared
	UserLogin userLogin


	void setupSpec() {
		mockDomains PasswordHistory, UserLogin, Person
	}

	void setup() {
		service.passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()
		Person person = new Person(firstName: 'Sam', middleName: 'S', lastName: 'Onite').save(flush: true, failOnError: true)
		userLogin = new UserLogin(username: 'test', active: 'Y', expiryDate: new Date() + 1000, person: person).save(flush: true, failOnError: true)

	}

	void 'Test verifyPasswordHistory passwordHistoryRetentionCount : 3'() {
		setup: 'given passwordHistoryRetentionCount = 3 and passwordHistoryRetentionDays = 0'
			service.userLocalConfigMap = [
				passwordHistoryRetentionCount: 3,
				passwordHistoryRetentionDays : 0
			]
		when: 'verifying a password with no history'
			boolean verified = service.verifyPasswordHistory(userLogin, 'zelda123!')

		then: 'the password is verified'
			verified
		when:'a the password is added to history and verifyPasswordHistory is checked for the same password'
			new PasswordHistory(
				userLogin: userLogin,
				password: '{bcrypt}$2a$10$5W4HoWPfHFeTE0meKlhImuRJxZsu6uygYrBbx3JDypHRiBhY6Twt2',
				createdDate: Date.parse("yyyy-MM-dd", "2019-02-28")
			).save(flush: true, failOnError: true)
			verified = service.verifyPasswordHistory(userLogin, 'zelda123!')
		then:'the password is not verified'
			!verified
	}

	void 'Test verifyPasswordHistory passwordHistoryRetentionCount : 3 out of range'() {
		setup:'given passwordHistoryRetentionCount = 3 and passwordHistoryRetentionDays = 0 and password history with the new password past the count'
			service.userLocalConfigMap = [
				passwordHistoryRetentionCount: 3,
				passwordHistoryRetentionDays : 0
			]

			new PasswordHistory(
				userLogin: userLogin,
				password: '{bcrypt}$2a$10$5W4HoWPfHFeTE0meKlhImuRJxZsu6uygYrBbx3JDypHRiBhY6Twt5',
				createdDate: Date.parse("yyyy-MM-dd", "2019-11-13")
			).save(flush: true, failOnError: true)


			new PasswordHistory(
				userLogin: userLogin,
				password: '{bcrypt}$2a$10$5W4HoWPfHFeTE0meKlhImuRJxZsu6uygYrBbx3JDypHRiBhY6Twt4',
				createdDate: Date.parse("yyyy-MM-dd", "2019-11-12")
			).save(flush: true, failOnError: true)


			new PasswordHistory(
				userLogin: userLogin,
				password: '{bcrypt}$2a$10$5W4HoWPfHFeTE0meKlhImuRJxZsu6uygYrBbx3JDypHRiBhY6Twt3',
				createdDate: Date.parse("yyyy-MM-dd", "2019-11-11")
			).save(flush: true, failOnError: true)


			new PasswordHistory(
				userLogin: userLogin,
				password: '{bcrypt}$2a$10$5W4HoWPfHFeTE0meKlhImuRJxZsu6uygYrBbx3JDypHRiBhY6Twt2',
				createdDate: Date.parse("yyyy-MM-dd", "2019-11-10")
			).save(flush: true, failOnError: true)
		when: 'checking verifyPasswordHistory'
			boolean verified = service.verifyPasswordHistory(userLogin, 'zelda123!')

		then: 'the password is verified'
			verified
	}


	void 'Test verifyPasswordHistory passwordHistoryRetentionDays : 3 out of range'() {
		setup: 'given passwordHistoryRetentionCount = 0 and passwordHistoryRetentionDays = 3 and password history with the new password past the retention days '
			service.userLocalConfigMap = [
				passwordHistoryRetentionCount: 0,
				passwordHistoryRetentionDays : 3
			]

			new PasswordHistory(
				userLogin: userLogin,
				password: '{bcrypt}$2a$10$5W4HoWPfHFeTE0meKlhImuRJxZsu6uygYrBbx3JDypHRiBhY6Twt5',
				createdDate: new Date()
			).save(flush: true, failOnError: true)


			new PasswordHistory(
				userLogin: userLogin,
				password: '{bcrypt}$2a$10$5W4HoWPfHFeTE0meKlhImuRJxZsu6uygYrBbx3JDypHRiBhY6Twt4',
				createdDate: new Date() - 2
			).save(flush: true, failOnError: true)


			new PasswordHistory(
				userLogin: userLogin,
				password: '{bcrypt}$2a$10$5W4HoWPfHFeTE0meKlhImuRJxZsu6uygYrBbx3JDypHRiBhY6Twt3',
				createdDate: new Date() - 3
			).save(flush: true, failOnError: true)


			new PasswordHistory(
				userLogin: userLogin,
				password: '{bcrypt}$2a$10$5W4HoWPfHFeTE0meKlhImuRJxZsu6uygYrBbx3JDypHRiBhY6Twt2',
				createdDate: new Date() - 4
			).save(flush: true, failOnError: true)
		when: 'checking verifyPasswordHistory'
			boolean verified = service.verifyPasswordHistory(userLogin, 'zelda123!')

		then: 'the password is verified'
			verified
	}

	void 'Test verifyPasswordHistory passwordHistoryRetentionDays : 3 in range'() {
			setup:'given passwordHistoryRetentionCount = 0 and passwordHistoryRetentionDays = 3 and password history with the new password in the retention days '
				service.userLocalConfigMap = [
					passwordHistoryRetentionCount: 0,
					passwordHistoryRetentionDays : 3
				]

				new PasswordHistory(
					userLogin: userLogin,
					password: '{bcrypt}$2a$10$5W4HoWPfHFeTE0meKlhImuRJxZsu6uygYrBbx3JDypHRiBhY6Twt5',
					createdDate: new Date()
				).save(flush: true, failOnError: true)


				new PasswordHistory(
					userLogin: userLogin,
					password: '{bcrypt}$2a$10$5W4HoWPfHFeTE0meKlhImuRJxZsu6uygYrBbx3JDypHRiBhY6Twt2',
					createdDate: new Date() - 2
				).save(flush: true, failOnError: true)


				new PasswordHistory(
					userLogin: userLogin,
					password: '{bcrypt}$2a$10$5W4HoWPfHFeTE0meKlhImuRJxZsu6uygYrBbx3JDypHRiBhY6Twt2',
					createdDate: new Date() - 3
				).save(flush: true, failOnError: true)


				new PasswordHistory(
					userLogin: userLogin,
					password: '{bcrypt}$2a$10$5W4HoWPfHFeTE0meKlhImuRJxZsu6uygYrBbx3JDypHRiBhY6Twt2',
					createdDate: new Date() - 4
				).save(flush: true, failOnError: true)
			when: 'checking verifyPasswordHistory'
				boolean verified = service.verifyPasswordHistory(userLogin, 'zelda123!')

			then: 'the password is  not verified'
				!verified
		}

}
