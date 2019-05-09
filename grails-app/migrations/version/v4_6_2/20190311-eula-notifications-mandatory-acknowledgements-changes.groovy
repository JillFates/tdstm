package version.v4_6_2

databaseChangeLog = {

	changeSet(author: 'slopez', id: '20190311 TM-13925-1') {
		comment("Add acknowledgement required columns to notice table")

		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'notice', columnName: 'need_acknowledgement')
				columnExists(tableName: 'notice', columnName: 'acknowledge_label')
				columnExists(tableName: 'notice', columnName: 'sequence')
				columnExists(tableName: 'notice', columnName: 'locked')
			}
		}

		// drop unneeded column
		dropColumn(tableName: 'notice', columnName: 'acknowledgeable')
//		sql ("""
//				ALTER TABLE `notice` DROP COLUMN `acknowledgeable`;
//		""")

		// add new columns to notice table
//		sql ("""
//				ALTER TABLE `notice` ADD COLUMN `need_acknowledgement` TINYINT NOT NULL DEFAULT 0;
//				ALTER TABLE `notice` ADD COLUMN `acknowledge_label` VARCHAR(255) NOT NULL DEFAULT '';
//				ALTER TABLE `notice` ADD COLUMN `sequence` TINYINT NOT NULL DEFAULT 0;
//				ALTER TABLE `notice` ADD COLUMN `locked` TINYINT NOT NULL DEFAULT 0;
//		""")

		addColumn(tableName: 'notice') {
			column(name: 'need_acknowledgement', type: 'TINYINT', defaultValue: '0') {
				constraints(nullable: 'false')
			}
		}
		addColumn(tableName: 'notice') {
			column(name: 'acknowledge_label', type: 'VARCHAR(255)', defaultValue: '') {
				constraints(nullable: 'false')
			}
		}
		addColumn(tableName: 'notice') {
			column(name: 'sequence', type: 'TINYINT', defaultValue: '0') {
				constraints(nullable: 'false')
			}
		}
		addColumn(tableName: 'notice') {
			column(name: 'locked', type: 'TINYINT', defaultValue: '0') {
				constraints(nullable: 'false')
			}
		}


	}

	changeSet(author: 'slopez', id: '20190311 TM-13925-2') {
		comment("Add notices acknowledgements required columns and restrictions to notice acknowledgement table")

		preConditions(onFail: 'MARK_RAN') {
			not {
				tableExists(tableName: 'notice_acknowledgement')
			}
		}

		// drop unneeded old table
		sql (""" 
				DROP TABLE `notice_acknowledgment`; 
		""")

		createTable(tableName: 'notice_acknowledgement') {
			column(autoIncrement: 'true', name: 'notice_acknowledgement_id', type: 'BIGINT') {
				constraints(nullable: 'false', primaryKey: 'true', primaryKeyName: 'pk_noticeAcknowledgement')
			}

			column(name: 'ip_address', type: 'VARCHAR(15)', defaultValue: '') {
				constraints(nullable: 'false')
			}

			column(name: 'browser_type', type: 'VARCHAR(255)', defaultValue: '') {
				constraints(nullable: 'false')
			}

			column(name: 'notice_id', type: 'BIGINT(20)') {
				constraints(nullable: 'false')
			}

			column(name: 'person_id', type: 'BIGINT(20)') {
				constraints(nullable: 'false')
			}

			column(name: 'date_created', type: 'DATETIME') {
				constraints(nullable: 'false')
			}

		}

		// add notice acknowledgement FKs
		addForeignKeyConstraint(
				constraintName: 'fk_notice_acknowledgement_notice',
				onDelete: 'CASCADE',
				baseTableName: 'notice_acknowledgement',
				baseColumnNames: 'notice_id',
				referencedTableName: 'notice',
				referencedColumnNames: 'notice_id'
		)

		addForeignKeyConstraint(
				constraintName: 'fk_notice_acknowledgement_person',
				baseTableName: 'notice_acknowledgement',
				baseColumnNames: 'person_id',
				referencedTableName: 'person',
				referencedColumnNames: 'person_id',
				onDelete: 'CASCADE'
		)
	}

	changeSet(author: 'slopez', id: '20190311 TM-14671-1') {
		comment("Remove last_modified not null constraint on notice table")

		dropNotNullConstraint(
				columnDataType: 'DATE',
				columnName: 'last_modified',
				tableName: 'notice'
		)
	}

}
