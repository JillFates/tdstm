/**
 * @author slopez
 * TM-6443
 */
databaseChangeLog = {
    changeSet(author: "slopez", id: "20170530 TM-6443: Create new Setting domain and service class") {
        comment('Create new Setting domain and service class')

        preConditions(onFail:'MARK_RAN') {
            not {
                tableExists(tableName:'setting')
            }
        }
        //Changing  'key' column name as 'setting_key' cause 'key' is a reserved keyword in MYSQL

        sql("""
			CREATE TABLE setting (
				setting_id      BIGINT(20) NOT NULL AUTO_INCREMENT,
				project_id      BIGINT(20),
				type            VARCHAR(32) NOT NULL,
				setting_key     VARCHAR(100) NOT NULL,
				json            TEXT NOT NULL,
				date_created	DATETIME NOT NULL,
				last_modified	DATETIME,
				version         INT,
				PRIMARY KEY (setting_id),
				UNIQUE KEY setting_key (type, setting_key, project_id)
			);
		""")
    }
}
