/**
 * @author oluna
 * TM-3776
 */
databaseChangeLog = {
	changeSet(author: "oluna", id: "20160920 TM-3776-Manager.v3") {
		comment('Create "licensed_client" table in "tdstm" schema')

		preConditions(onFail:'MARK_RAN') {
			not {
				tableExists(tableName:'licensed_client')
			}
		}
		//Changing  'key' column name as 'fi_key' cause 'key' is a reserved keyword in MYSQL
		sql("""
			CREATE TABLE licensed_client(
				id 	   			varchar(255) NOT NULL,
				installation_num varchar(255) NOT NULL,
				email		   	varchar(255) NOT NULL,
                project        	varchar(255) NOT NULL,
                client        	varchar(255) NOT NULL,
				environment    	INT NOT NULL,
				method		   	INT NOT NULL,
				status			INT NOT NULL,
				type			INT NOT NULL,
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
