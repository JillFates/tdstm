/**
 * This changelog will add the last_modified column to the user_login table and give them default values.
 */
databaseChangeLog = {
	// This Changeset is used to add column last_modified to table user_login
	changeSet(author: "dinesh", id: "20130918 TM-2252-1") {
		comment('Add "last_modified" column in user_login table')
		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(schemaName:'tdstm', tableName:'user_login', columnName:'last_modified' )
			}
		}
		sql("ALTER TABLE tdstm.user_login ADD column last_modified DateTime NOT null")
		sql("UPDATE tdstm.user_login SET last_modified = CURRENT_TIMESTAMP")
	}
}
