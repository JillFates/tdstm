/**
 * Drop user login columns from the model_sync_batch table
 */
databaseChangeLog = {
	
	changeSet(author: "dscarpa", id: "20150417 TM-3813-2") {
		comment('Drop userlogin_id and user_login_id columns from the model_sync_batch table')
		sql("ALTER TABLE model_sync_batch DROP foreign key FK6632AB6CD191A8BA;")
		sql("ALTER TABLE model_sync_batch DROP COLUMN user_login_id;")
		sql("ALTER TABLE model_sync_batch DROP COLUMN userlogin_id;")
	}
}
