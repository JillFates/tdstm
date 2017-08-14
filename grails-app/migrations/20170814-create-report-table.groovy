/**
 * @author slopez
 * TM-6443
 */
databaseChangeLog = {
	changeSet(author: "dontiveros", id: "20170530 TM-6526: Asset Explorer endpoints") {
		comment('Create new Report table for endpoint support')

		preConditions(onFail:'MARK_RAN') {
			not {
				tableExists(tableName:'report')
			}
		}
		sql("""
			CREATE TABLE report (
				id      		BIGINT(20) NOT NULL AUTO_INCREMENT,
				project_id		BIGINT(20) NOT NULL,
    			person_id		BIGINT(20),
    			name			VARCHAR(32) NOT NULL,
    			is_system		TINYINT(1) DEFAULT 0,
    			is_shared		TINYINT(1) DEFAULT 0,
    			report_schema	TEXT,
    			date_created	DATETIME NOT NULL,
    			last_modified	DATETIME,
    			version			INT,
    			     
				PRIMARY KEY (id),
				FOREIGN KEY report(project_id) REFERENCES project(project_id),
    			FOREIGN KEY (person_id) REFERENCES person(person_id),
    			INDEX (id, project_id)
			);
		""")
	}
}