import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.grails.commons.ApplicationHolder as AH

/**
 * This changelog will add the password_changed_date and force_password_change columns to the user_login table and give them default values
 */
databaseChangeLog = {
	// This Changeset is used to add column password_changed_date to table user_login
	changeSet(author: "Ross", id: "20130614 TM-1930-1") {
		comment('Add "password_changed_date" column in user_login table')
		sql("ALTER TABLE tdstm.user_login ADD column password_changed_date DateTime")
		sql("UPDATE tdstm.user_login SET password_changed_date = CURRENT_TIMESTAMP")
		sql("ALTER TABLE tdstm.user_login MODIFY column password_changed_date DateTime NOT null")
	}
	
	// This Changeset is used to add column force_password_change to table user_login
	changeSet(author: "Ross", id: "20130614 TM-1930-2") {
		comment('Add "force_password_change" column in user_login table')
		sql("ALTER TABLE tdstm.user_login ADD column force_password_change char(1) NOT null DEFAULT 'N'")
	}
}