/**
 * Drop not null constraint on files table file_format column.
 */
databaseChangeLog = {
    changeSet(author: "slopez", id: "20170125 TM-5911-2") {
        comment('Drop not null constraint on files table file_format column.')
        sql("""
			ALTER TABLE files MODIFY COLUMN file_format VARCHAR(255) NULL;
		""")
    }
}
