databaseChangeLog = {
	changeSet(author: "dscarpa", id: "20150803 TM-4044-1") {
		comment('Create prassword history and user audit tables')
		
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

		createTable(tableName: "user_audit") {
			column(name: "user_audit_id", type: "BIGINT(20)", autoIncrement: "true"){
				constraints( primaryKey:"true", nullable:"false")
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
			column(name: "ip_address", type: "VARCHAR(15)" ){
			    constraints(nullable:"false")
			}
			column(name: "severity", type: "VARCHAR(10)" ){
			    constraints(nullable:"false")
			}
			column(name: "security_relevant", type: "BIT(1)" ){
			    constraints(nullable:"false")
			}
			column(name: "classification", type: "VARCHAR(10)" ){
			    constraints(nullable:"false")
			}
			column(name: "message", type: "VARCHAR(255)" ){
			    constraints(nullable:"false")
			}
		}
		createIndex(tableName:'user_audit', indexName:'userAudit_UserLogin_idx', unique:'false') {
			column(name:'user_login_id')
		}
		createIndex(tableName:'user_audit', indexName:'userAudit_Project_idx', unique:'false') {
			column(name:'project_id')
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

		sql("ALTER TABLE user_login ADD column locked_out_until DATETIME DEFAULT null")
		sql("ALTER TABLE user_login ADD column password_expiration_date DATETIME DEFAULT null")
		sql("ALTER TABLE user_login ADD column failed_login_attempts INT DEFAULT 0")
		sql("ALTER TABLE user_login ADD column password_never_expires BIT(1) NOT NULL DEFAULT 0")

	}
}

