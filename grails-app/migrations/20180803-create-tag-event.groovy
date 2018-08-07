databaseChangeLog = {

	changeSet(author: 'tpelletier', id: '20180803 TM-11431-1') {
		comment("Create tag_event table")

		preConditions(onFail: 'MARK_RAN') {
			not {
				tableExists(tableName: 'tag_event')
			}
		}

		createTable(tableName: 'tag_event') {
			column(autoIncrement: 'true', name: 'tag_event_id', type: 'BIGINT(20)') {
				constraints(nullable: 'false', primaryKey: 'true', primaryKeyName: 'tag_eventPK')
			}

			column(name: 'event_id', type: 'BIGINT(20)') {
				constraints(nullable: 'false')
			}

			column(name: 'tag_id', type: 'BIGINT(20)') {
				constraints(nullable: 'false')
			}

			column(name: 'date_created', type: 'DATETIME') {
				constraints(nullable: 'false')
			}
		}

		createIndex(indexName: 'FK_tag_event_tag', tableName: 'tag_event') {
			column(name: 'tag_id')
		}

		addForeignKeyConstraint(
			baseColumnNames: 'tag_id',
			baseTableName: 'tag_event',
			constraintName: 'FK_tag_event_tag',
			onDelete: 'CASCADE',
			deferrable: 'false',
			initiallyDeferred: 'false',
			referencedColumnNames: 'tag_id',
			referencedTableName: 'tag',
			referencesUniqueColumn: 'false'
		)

		createIndex(indexName: 'FK_tag_event_event', tableName: 'tag_event') {
			column(name: 'event_id')
		}

		addForeignKeyConstraint(
			baseColumnNames: 'event_id',
			baseTableName: 'tag_event',
			constraintName: 'FK_tag_event_event',
			onDelete: 'CASCADE',
			deferrable: 'false',
			initiallyDeferred: 'false',
			referencedColumnNames: 'move_event_id',
			referencedTableName: 'move_event',
			referencesUniqueColumn: 'false'
		)

		createIndex(indexName: 'UK_tag_event_event_tag', tableName: 'tag_event', unique: true) {
			column(name: 'event_id')
			column(name: 'tag_id')
		}
	}
}