/**
 * @author oluna
 * TM-3776
 */
databaseChangeLog = {
	changeSet(author: "oluna", id: "20160920 TM-3776-Manager") {
		comment('Create "licensed_client" table in "tdstm" schema')

		preConditions(onFail:'MARK_RAN') {
			not {
				tableExists(tableName:'licensed_client')
			}
		}
		//Changing  'key' column name as 'fi_key' cause 'key' is a reserved keyword in MYSQL
		sql("""
			CREATE TABLE tdstm.licensed_client(
				id 	   			varchar(255) NOT NULL,
				instalation_num varchar(255) NOT NULL,
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

	changeSet(author: "oluna", id: "20160920 TM-3776-B2") {
		sql("""
			ALTER TABLE `tdstm`.`license`
			CHANGE COLUMN `environment` `environment` INT NOT NULL COMMENT '' ;
		""")
	}
}