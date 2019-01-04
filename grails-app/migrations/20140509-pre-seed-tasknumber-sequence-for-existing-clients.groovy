/**
 * Created migration to pre-seed TaskNumber sequence for existing clients that have tasks 
 * 
 */
databaseChangeLog = {
	
	changeSet(author: "lokanada", id: "20140509 TM-2686-1") {
		comment('pre-seed TaskNumber sequence for existing clients that have tasks ')
		grailsChange {
			change {
				// Delete any test sequence number that may exist
				sql.execute('DELETE FROM sequence_number')

				// Find the highest task numbers that exist per client and create a sequence record for them
				def sequenceList = sql.rows("""select p.client_id as id, max(task_number) as last_task_number 
					from project p join asset_comment ac on ac.project_id = p.project_id 
					group by p.client_id having last_task_number > 0""")
				sequenceList.each{
					sql.execute(" INSERT INTO sequence_number(context_id,name,last) VALUES (${it.id},'TaskNumber',${it.last_task_number}) ")
				}
			}
		}
	
	}
	
}
