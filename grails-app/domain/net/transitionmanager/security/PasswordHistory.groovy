package net.transitionmanager.security

import net.transitionmanager.security.UserLogin

class PasswordHistory {

	UserLogin userLogin
	String password
	Date createdDate = new Date()

	static constraints = {
		password blank: false, password: true
	}

	static mapping = {
		version false
		id column: 'password_history_id'
		password sqlType: 'varchar(100)'  // size must me more than 20 because it will store as encrypted code
		createdDate sqltype: 'DateTime'
	}
}
