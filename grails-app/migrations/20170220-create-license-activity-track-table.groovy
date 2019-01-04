/**
 * @author oluna
 * TM-3776
 */
databaseChangeLog = {
	changeSet(author: "oluna", id: "20160920 TM-3776-Manager") {
		comment('Create "license_activity_track" table')

		preConditions(onFail:'MARK_RAN') {
			not {
				tableExists(tableName:'license_activity_track')
			}
		}
		//Changing  'key' column name as 'fi_key' cause 'key' is a reserved keyword in MYSQL

		sql("""
			CREATE TABLE license_activity_track(
				id	bigint(20) NOT NULL AUTO_INCREMENT,
				date_created	DATETIME,
				user_login_id	bigint(20),
				licensed_client_id	varchar(255),
				changes	TEXT,
				PRIMARY KEY (id),
				FOREIGN KEY (user_login_id) REFERENCES user_login(user_login_id)
					ON DELETE CASCADE
       				ON UPDATE CASCADE,
				FOREIGN KEY (licensed_client_id) REFERENCES licensed_client(id)
					ON DELETE CASCADE
					ON UPDATE CASCADE
			)
		""")
	}
}
