/**
 * This changelog will set staff_type to Salary where staff_type is null
 */
databaseChangeLog = {
	// This Changeset is used to set staff_type to 'Salary' where staff_type is null
	changeSet(author: "lokanada", id: "20130626 TM-1904-20") {
		comment('Set staff_type to "Salary" where staff_type is null')
		sql("UPDATE person SET staff_type='Salary' WHERE staff_type IS NULL")
	}
}