package version.v5_0_0

import grails.plugin.springsecurity.SpringSecurityService
import org.apache.commons.lang3.RandomStringUtils

databaseChangeLog = {

	changeSet(author: 'tpelletier', id: '20190920 TM-14929-1') {
		comment("removes old password history")

		sql('TRUNCATE table password_history;')
	}

	changeSet(author: 'tpelletier', id: '20190920 TM-14929-2') {
		comment("sets new random passwords")

		grailsChange {
			change {

				SpringSecurityService springSecurityService = ctx.getBean("springSecurityService")

				def userLogins = sql.rows("SELECT user_login_id from user_login")
				String password

				userLogins.each { userLogin ->
					password = RandomStringUtils.randomAlphanumeric(10)
					password = springSecurityService?.passwordEncoder ? springSecurityService.encodePassword(password) : password
					sql.executeUpdate("UPDATE user_login set password = $password where user_login_id = $userLogin.user_login_id")
				}
			}
		}
	}


	changeSet(author: 'tpelletier', id: '20190920 TM-14929-3') {
		comment("Drop column salt_prefix")
		preConditions(onFail: 'MARK_RAN') {
			columnExists(tableName: 'user_login', columnName: 'salt_prefix')
		}
		dropColumn(tableName: 'user_login', columnName: 'salt_prefix')
	}
}
