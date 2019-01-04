
/**
 * This Changelog will add "key_value" table with columns "category","key","value","project_id" and "id".
 */

databaseChangeLog = {
	changeSet(author: "lokanada", id: "20130723 TM-2022-1") {
		comment('Create "key_value" table in "tdstm" schema')
		
		preConditions(onFail:'MARK_RAN') {
			not {
				tableExists(schemaName:'tdstm', tableName:'key_value')
			}
		}
	    //Changing  'key' column name as 'fi_key' cause 'key' is a reserved keyword in MYSQL
		sql("""CREATE TABLE  tdstm.key_value ( project_id bigint(20) NOT NULL, category varchar(255) NOT NULL,
			  fi_key varchar(255) NOT NULL, value varchar(255) NOT NULL, PRIMARY KEY (project_id, category, fi_key),
			  KEY FK_PROJECT_KEY_VALUE (project_id), CONSTRAINT FK_PROJECT_KEY_VALUE FOREIGN KEY (project_id) 
			  REFERENCES project (project_id))
			""")
	}
}
