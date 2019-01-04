/**
 * Created migration to pre-seed AssetTag sequence for existing clients that have tasks
 *
 */
databaseChangeLog = {
	
	changeSet(author: "lokanada", id: "20140704 TM-2948-1") {
		comment('pre-seed AssetTag sequence for existing clients')
		grailsChange {
			change {
				// Find the highest task numbers that exist per client and create a sequence record for them
				def sequenceList = sql.rows("""select p.client_id as id, max(asset_tag) as last_asset_tag
					from project p join asset_entity ae on ae.project_id = p.project_id
					 where asset_tag like 'TDS-%' group by p.client_id""")
				sequenceList.each{
					sql.execute(" INSERT INTO sequence_number(context_id,name,last) VALUES (${it.id},'AssetTag',${it.last_asset_tag.split('-')[1]}) ")
				}
			}
		}
	}
}
