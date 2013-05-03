/**
 * This set of Database change to delete all orphan records from mobelConnector table.
 */

databaseChangeLog = {
	changeSet(author: "lokanada", id: "20130503 TM-1880") {
		comment('Delete models from the model_connector table where model_id not in model table')
			preConditions(onFail:'MARK_RAN') {
				not{
					sqlCheck(expectedResult:'0', 'SELECT COUNT(*) FROM model_connector WHERE model_id NOT IN (SELECT model_id FROM model)')
				}
			}
			sql("DELETE FROM model_connector WHERE model_id NOT IN ( (SELECT model_id FROM model) )")
		}
}
