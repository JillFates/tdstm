databaseChangeLog = {
	changeSet(author: "dscarpa", id: "20150830 TM-4048-1") {
		comment('Add salt prefic to user login')
		
		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(tableName:'user_login', columnName:'salt_prefix' )
			}
		}

		sql("ALTER TABLE user_login ADD column salt_prefix VARCHAR(32) DEFAULT null")

		sql("UPDATE user_login SET password = 'locked out' WHERE (active='N') OR (expiry_date < (now() - INTERVAL 7 DAY))")
	}
}

