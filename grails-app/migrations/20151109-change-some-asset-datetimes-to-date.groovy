/**
* Database migration script for the TransitionManager application
*/
databaseChangeLog = {
	changeSet(author: "jmartin", id: "20151109 TM-4179") {
		comment('Alter the asset_entity schema to change the retire and maint date columns from DATETIME to DATE')
		sql('ALTER TABLE asset_entity ' +
			'MODIFY COLUMN maint_exp_date DATE, ' + 
			'MODIFY COLUMN retire_date DATE AFTER maint_exp_date, ' + 
			'MODIFY COLUMN purchase_date DATE AFTER retire_date')
	}
}
