databaseChangeLog = {
	changeSet(author: "jmartin", id: "20160601-TM-4944") {
		comment('Drop the NOT NULL on the UserLogin.passwordChangedDate property')
		sql("ALTER TABLE user_login CHANGE COLUMN password_changed_date password_changed_date DATETIME")
	}
}
