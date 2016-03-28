/**
 * @author Augusto Recordon
 * This script drops the Project -> Custom Fields Shown column.
 */

databaseChangeLog = {
	// Drops the Project -> Custom Fields Shown column.
	changeSet(author: "arecordon", id: "20160328 TM-4725-1") {
		comment('Drops the project custom fields shown column.')
	sql("ALTER TABLE project DROP COLUMN custom_fields_shown")
	}
}