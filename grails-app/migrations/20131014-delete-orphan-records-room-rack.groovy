/**
 *This change set is used to remove orphan records from room and rack table where 
 *associated project does not exist
 */
databaseChangeLog = {
	/*
	 * While deleting the project we were missing to remove the Rooms and Racks so  records became orphan ,
	 * Removing all orphan records in Room and Rack table for which project is not there
	 */
	changeSet(author: "lokanada", id: "20131014 TM-2309-2") {
		sql(" DELETE FROM room  WHERE project_id NOT IN ( SELECT p.project_id FROM project p ) ");
		sql(" DELETE FROM rack  WHERE project_id NOT IN ( SELECT p.project_id FROM project p ) ");
		sql(" update model m set m.model_scope_id = null where m.model_scope_id not in (select p.project_id from project p) ");
		sql(" update model_sync m set m.model_scope_id = null where m.model_scope_id not in (select p.project_id from project p) ");
	}
}