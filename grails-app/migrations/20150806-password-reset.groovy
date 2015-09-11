databaseChangeLog = {
	changeSet(author: "dscarpa", id: "20150806 TM-4077-1") {
		comment('Password Reset tables')
		
		preConditions(onFail:'MARK_RAN') {
			not {
				tableExists(tableName:'password_reset')
			}
		}

		createTable(tableName: "password_reset") {
			column(name: "password_reset_id", type: "BIGINT(20)", autoIncrement: "true"){
				constraints( primaryKey:"true", nullable:"false")
			}
			column(name: "token", type: "VARCHAR(30)" ){
			    constraints(nullable:"false")
			}
			column(name: 'user_login_id', type: 'BIGINT') {
				constraints(nullable: "false")
			}
			column(name: "status", type: "VARCHAR(9)" ){
			    constraints(nullable:"false")
			}
			column(name: "type", type: "VARCHAR(20)" ){
			    constraints(nullable:"false")
			}
			column(name: "ip_address", type: "VARCHAR(15)" ){
			    constraints(nullable:"true")
			}
			column(name: 'created_by_id', type: 'BIGINT') {
				constraints(nullable: "true")
			}
			column(name: 'email_dispatch_id', type: 'BIGINT') {
				constraints(nullable: "true")
			}
			column(name: 'expires_after', type: 'DATETIME') {
				constraints(nullable: "false")
			}
			column(name: 'created_date', type: 'DATETIME') {
				constraints(nullable: "false")
			}
			column(name: 'last_modified', type: 'DATETIME') {
				constraints(nullable: "false")
			}

		}
		createIndex(tableName:'password_reset', indexName:'passwordReset_Token_idx', unique:'true') {
			column(name:'token')
		}
		sql("""ALTER TABLE `password_reset`
			ADD CONSTRAINT `fk_passwordReset_UserLogin` FOREIGN KEY (`user_login_id`)
			REFERENCES `user_login` (`user_login_id`)
			ON DELETE CASCADE
			ON UPDATE CASCADE
		""")
		sql("""ALTER TABLE `password_reset`
			ADD CONSTRAINT `fk_passwordReset_Person` FOREIGN KEY (`created_by_id`)
			REFERENCES `person` (`person_id`)
			ON DELETE CASCADE
			ON UPDATE CASCADE
		""")
		sql("""ALTER TABLE `password_reset`
			ADD CONSTRAINT `fk_passwordReset_EmailDispatch` FOREIGN KEY (`email_dispatch_id`)
			REFERENCES `email_dispatch` (`email_dispatch_id`)
			ON DELETE CASCADE
			ON UPDATE CASCADE
		""")

	}
}

