/**
 * @author oluna
 * TM-3776
 */
databaseChangeLog = {
	changeSet(author: "oluna", id: "20160920 TM-3776-A.v3") {
		comment('Create "license" table in "tdstm" schema')

		preConditions(onFail:'MARK_RAN') {
			not {
				tableExists(tableName:'license')
			}
		}
		//Changing  'key' column name as 'fi_key' cause 'key' is a reserved keyword in MYSQL
		sql("""
			CREATE TABLE license(
				id 	   			varchar(255) NOT NULL,
				installation_num varchar(255) NOT NULL,
				email		   	varchar(255) NOT NULL,
				environment    	varchar(255) NOT NULL,
                project        	varchar(255) NOT NULL,
				method		   	varchar(255) NOT NULL,
				max			   	bigint,
				request_date	DATETIME,
				valid_start     DATE,
				valid_end	   	DATE,
				request_note    TEXT,
				hash		   	TEXT,
				PRIMARY KEY (id)
			)
		""")
	}
}
