/**
 * Inserting usize fields in eav_attribute and associated table for import and export .
 */
databaseChangeLog = {
	changeSet(author: "jmartin", id: "20130315 TM-1253") {
		comment('Change the UserLogin.username column to varchar(50)')
		modifyDataType(tableName: 'user_login', columnName: 'username', newDataType: 'varchar(50)')
	}
}
