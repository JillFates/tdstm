/**
 * This changelog is used to drop the hack column tempForUpdate and add in the version property
 */
databaseChangeLog = {

	changeSet(author: "John", id: "20131018 TM-2326-1") {
		comment("Add column version")
		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(schemaName:'tdstm', tableName:'party', columnName:'version' )
			}
		}
		addColumn(tableName: 'party') {
			column(name: 'version', type: 'bigint(11)', defaultValue: 0) {
				constraints(nullable: 'true')
			}
		}
	}

	changeSet(author: "John", id: "20131018 TM-2326-2") {
		comment("Drop column tempForUpdate")
		preConditions(onFail:'MARK_RAN') {
			columnExists(schemaName:'tdstm', tableName:'party', columnName:'temp_for_update' )
		}
		dropColumn(tableName: 'party', columnName: 'temp_for_update')
	}
}