databaseChangeLog = {
	changeSet(author: "dscarpa", id: "20150803 TM-4044-1.1") {
		comment('Create prassword_history table')
		
		preConditions(onFail:'MARK_RAN') {
			not {
				tableExists(tableName:'password_history')
			}
		}

		createTable(tableName: "password_history") {
			column(name: "password_history_id", type: "BIGINT(20)", autoIncrement: "true"){
				constraints( primaryKey:"true", nullable:"false")
			}
			column(name: 'user_login_id', type: 'BIGINT') {
				constraints(nullable: "false")
			}
			column(name: 'created_date', type: 'DATETIME') {
				constraints(nullable: "false")
			}
			column(name: "password", type: "VARCHAR(100)" ){
			    constraints(nullable:"false")
			}
		}
		createIndex(tableName:'password_history', indexName:'passwordHistory_UserLogin_idx', unique:'false') {
			column(name:'user_login_id')
		}
		sql("""ALTER TABLE `password_history`
			ADD CONSTRAINT `fk_passwordHistory_UserLogin` FOREIGN KEY (`user_login_id`)
			REFERENCES `user_login` (`user_login_id`)
			ON DELETE CASCADE
			ON UPDATE CASCADE
		""")
	}

	changeSet(author: "dscarpa", id: "20150803 TM-4044-1.2") {
		comment('Create user_audit table')
		preConditions(onFail:'MARK_RAN') {
			not {
				tableExists(tableName:'user_audit')
			}
		}
		createTable(tableName: "user_audit") {
			column(name: "user_audit_id", type: "BIGINT(20)", autoIncrement: "true") {
				constraints(primaryKey: "true", nullable: "false")
			}
			column(name: 'user_login_id', type: 'BIGINT') {
				constraints(nullable: "false")
			}
			column(name: 'project_id', type: 'BIGINT') {
				constraints(nullable: "true")
			}
			column(name: 'created_date', type: 'DATETIME') {
				constraints(nullable: "false")
			}
			column(name: "ip_address", type: "VARCHAR(15)") {
				constraints(nullable: "false")
			}
			column(name: "severity", type: "VARCHAR(10)") {
				constraints(nullable: "false")
			}
			column(name: "security_relevant", type: "BIT(1)") {
				constraints(nullable: "false")
			}
			column(name: "classification", type: "VARCHAR(10)") {
				constraints(nullable: "false")
			}
			column(name: "message", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
		createIndex(tableName: 'user_audit', indexName: 'userAudit_UserLogin_idx', unique: 'false') {
			column(name: 'user_login_id')
		}
		createIndex(tableName: 'user_audit', indexName: 'userAudit_Project_idx', unique: 'false') {
			column(name: 'project_id')
		}
		sql("""ALTER TABLE `user_audit`
				ADD CONSTRAINT `fk_userAudit_UserLogin` FOREIGN KEY (`user_login_id`)
				REFERENCES `user_login` (`user_login_id`)
				ON DELETE CASCADE
				ON UPDATE CASCADE
			""")
		sql("""ALTER TABLE `user_audit`
				ADD CONSTRAINT `fk_userAudit_project` FOREIGN KEY (`project_id`)
				REFERENCES `project` (`project_id`)
				ON DELETE SET NULL
				ON UPDATE CASCADE
			""")
	}



	//Adding some missing collumns to user_login
	changeSet(author: "oluna", id: "20161110 TM-4044-2.1") {
		comment('Check that the Person.failedLoginAttempts exists')
		preConditions(onFail:'MARK_RAN', onFailMessage: 'the column failed_login_attempts exists already' ){
			not {
				columnExists(tableName:'user_login', columnName:'failed_login_attempts')
			}
		}
		addColumn(tableName: "user_login") {
			column(name: "failed_login_attempts", type: "int(11)", defaultValueNumeric:0)
		}
	}
	changeSet(author: "oluna", id: "20161110 TM-4044-2.2") {
		comment('Check that the Person.lockedOutUntil exists')
		preConditions(onFail:'MARK_RAN', onFailMessage: 'the column locked_out_until exists already' ){
			not {
				columnExists(tableName:'user_login', columnName:'locked_out_until')
			}
		}
		addColumn(tableName: "user_login") {
			column(name: "locked_out_until", type: "datetime")
		}
	}
	changeSet(author: "oluna", id: "20161110 TM-4044-2.3") {
		comment('Check that the Person.passwordExpirationDate exists')
		preConditions(onFail:'MARK_RAN', onFailMessage: 'the column password_expiration_date exists already' ){
			not {
				columnExists(tableName:'user_login', columnName:'password_expiration_date')
			}
		}
		addColumn(tableName: "user_login") {
			column(name: "password_expiration_date", type: "datetime")
		}
	}
	changeSet(author: "oluna", id: "20161110 TM-4044-2.4") {
		comment('Check that the Person.passwordNeverExpires exists')
		preConditions(onFail:'MARK_RAN', onFailMessage: 'the column password_never_expires exists already' ){
			not {
				columnExists(tableName:'user_login', columnName:'password_never_expires')
			}
		}
		addColumn(tableName: "user_login") {
			column(name: "password_never_expires", type: " bit(1)", defaultValueNumeric:0)
		}
	}
}

