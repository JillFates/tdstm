databaseChangeLog = {
	changeSet(author: "dscarpa", id: "20150806 TM-4077-1") {
		comment('Email Dispatch tables')
		
		preConditions(onFail:'MARK_RAN') {
			not {
				tableExists(tableName:'email_dispatch')
			}
		}

		createTable(tableName: "email_dispatch") {
			column(name: "email_dispatch_id", type: "BIGINT(20)", autoIncrement: "true"){
				constraints( primaryKey:"true", nullable:"false")
			}
			column(name: "origin", type: "VARCHAR(15)" ){
			    constraints(nullable:"false")
			}
			column(name: "subject", type: "VARCHAR(50)" ){
			    constraints(nullable:"false")
			}
			column(name: "body_template", type: "VARCHAR(20)" ){
			    constraints(nullable:"false")
			}
			column(name: "params_json", type: "TEXT" ){
			    constraints(nullable:"true")
			}
			column(name: "from_address", type: "VARCHAR(100)" ){
			    constraints(nullable:"false")
			}
			column(name: "to_address", type: "VARCHAR(100)" ){
			    constraints(nullable:"false")
			}
			column(name: 'to_person_id', type: 'BIGINT') {
				constraints(nullable: "true")
			}
			column(name: 'created_by_id', type: 'BIGINT') {
				constraints(nullable: "true")
			}
			column(name: 'sent_date', type: 'DATETIME') {
				constraints(nullable: "true")
			}
			column(name: 'created_date', type: 'DATETIME') {
				constraints(nullable: "false")
			}
			column(name: 'last_modified', type: 'DATETIME') {
				constraints(nullable: "false")
			}
		}
		sql("""ALTER TABLE `email_dispatch`
			ADD CONSTRAINT `fk_emailDispath_ToPerson` FOREIGN KEY (`to_person_id`)
			REFERENCES `person` (`person_id`)
			ON DELETE CASCADE
			ON UPDATE CASCADE
		""")
		sql("""ALTER TABLE `email_dispatch`
			ADD CONSTRAINT `fk_emailDispath_CreatedBy` FOREIGN KEY (`created_by_id`)
			REFERENCES `person` (`person_id`)
			ON DELETE CASCADE
			ON UPDATE CASCADE
		""")

	}
}

