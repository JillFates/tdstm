/**
 * @author dontiveros
 * TM-6526
 */
databaseChangeLog = {
	changeSet(author: "dontiveros", id: "20170530 TM-6526-01") {
		comment('Create new Dataview table for endpoint support')

		sql('''
			CREATE TABLE IF NOT EXISTS dataview (
				id      		BIGINT(20) NOT NULL AUTO_INCREMENT,
				project_id		BIGINT(20) NOT NULL,
    			person_id		BIGINT(20),
    			name			VARCHAR(255) NOT NULL,
    			is_system		TINYINT(1) DEFAULT 0,
    			is_shared		TINYINT(1) DEFAULT 0,
    			report_schema	TEXT,
    			date_created	DATETIME NOT NULL,
    			last_modified	DATETIME,
    			version			INT,
    			     
				PRIMARY KEY (id),
				FOREIGN KEY FK1 (project_id) REFERENCES project(project_id),
    			FOREIGN KEY FK2 (person_id) REFERENCES person(person_id)
			);
		''')
	}

	changeSet(author: "dontiveros", id: "20170530 TM-6526-02") {
		comment('Drop report table')

		sql('DROP TABLE IF EXISTS report;')
	}
}
