import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.grails.commons.ApplicationHolder as AH

/**
 * This changelog will add the password_changed_date and force_password_change columns to the user_login table and give them default values
 */
databaseChangeLog = {
	// This Changeset is used to add column password_changed_date to table user_login
	changeSet(author: "Ross", id: "20130614 TM-1930-1") {
		comment('Add "password_changed_date" column in user_login table')
		sql("ALTER TABLE tdstm.user_login ADD column password_changed_date DateTime DEFAULT CURRENT_TIMESTAMP")
	}
	
	// This Changeset is used to add column force_password_change to table user_login
	changeSet(author: "Ross", id: "20130614 TM-1930-2") {
		comment('Add "force_password_change" column in user_login table')
		sql("ALTER TABLE tdstm.user_login ADD column force_password_change boolean DEFAULT false")
	}
}