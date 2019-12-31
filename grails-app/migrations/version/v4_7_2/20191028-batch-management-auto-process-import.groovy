package version.v4_7_2
/**
 * Add new not nullable column isAutoProcess on DataScript Domain class (data_script table).
 * Add new nullable column groupGuid on ImportBatch Domain class (import_batch table).
 * Add new not nullable column sendNotification on ImportBatch Domain class (import_batch table).
 * All these fields are used in TM-16284, and TM-12671 with its sub-tickets.
 *
 * @See TM-12671, TM-16284
 * @author dcorrea
 */
databaseChangeLog = {

	changeSet(author: 'dcorrea', id: '20191028 TM-16284-1') {
		comment('Add column is_auto_process to the data_script table')

		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'data_script', columnName: 'is_auto_process')
			}
		}

		addColumn(tableName: 'data_script') {
			column(name: 'is_auto_process', type: 'TINYINT(1)', defaultValue: '0') {
				constraints(nullable: 'false')
			}
		}
	}

	changeSet(author: 'dcorrea', id: '20191028 TM-16284-2') {
		comment('Add columns group_guid and send_notification to the import_batch table')

		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'import_batch', columnName: 'group_guid')
				columnExists(tableName: 'import_batch', columnName: 'send_notification')
			}
		}

		addColumn(tableName: 'import_batch') {
			column(name: 'group_guid', type: 'varchar(36)') {
				constraints(nullable: 'true')
			}
		}

		addColumn(tableName: 'import_batch') {
			column(name: 'send_notification', type: 'TINYINT(1)', defaultValue: '0') {
				constraints(nullable: 'false')
			}
		}
	}
}
