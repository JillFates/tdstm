/**
 * This Changeset is used to set  model_status to "new" where model_status is null
 */
databaseChangeLog = {
	// This Changeset is used to set  model_status to "new" where model_status is null
	changeSet(author: "lokanada", id: "20130808 TM-2077-1") {
		comment('Set model_status to "new" where model_status is null')
		sql("UPDATE model SET model_status='new' WHERE model_status IS NULL OR model_status = ''")
	}
}