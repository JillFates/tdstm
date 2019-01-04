/**
 *This change set is used to update person's last name and middle name from null to blank
 */
databaseChangeLog = {
	/*This change set is used to update person's last name and middle name from null 
	   to blank and modifying columns as not null.*/	
	changeSet(author: "lokanada", id: "20130910 TM-2144-1") {
		sql("UPDATE person SET last_name = '' WHERE last_name IS NULL")
		sql("UPDATE person SET middle_name = '' WHERE middle_name IS NULL")
		sql("ALTER TABLE person MODIFY first_name VARCHAR(34) NOT NULL")
		sql("ALTER TABLE person MODIFY last_name VARCHAR(34) NOT NULL")
		sql("ALTER TABLE person MODIFY middle_name VARCHAR(20) NOT NULL")
	}
}
