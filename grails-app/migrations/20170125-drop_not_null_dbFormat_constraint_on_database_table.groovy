/**
 * Drop not null constraint on database table dbFormat column.
 */
databaseChangeLog = {
    changeSet(author: "slopez", id: "20170125 TM-5911-1") {
        comment('Drop not null constraint on database table dbFormat column.')
        sql("""
			ALTER TABLE data_base MODIFY COLUMN db_format VARCHAR(255) NULL;
		""")
    }
}
