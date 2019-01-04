
databaseChangeLog = {
	changeSet(author: "erobles", id: "20140812 TM-3128") {
		comment('Remove legacy columns that are no longer needed')
		sql("""ALTER TABLE asset_entity 
			DROP COLUMN target_team_sa_id,
			DROP COLUMN target_team_log_id,
			DROP COLUMN target_team_dba_id,
			DROP COLUMN source_team_sa_id,
			DROP COLUMN source_team_log_id,
			DROP COLUMN source_team_dba_id,
			DROP COLUMN current_status,
			DROP COLUMN target_team_id,
			DROP COLUMN target_rack,
			DROP COLUMN target_location,
			DROP COLUMN source_team_id,
			DROP COLUMN source_rack,
			DROP COLUMN source_location,
			DROP COLUMN model,
			DROP COLUMN manufacturer,
			DROP COLUMN app_owner,
			DROP INDEX FK6F1FC812A6E128AE,
			DROP INDEX FK6F1FC8126E66DA2B,
			DROP INDEX FK6F1FC8127CC78AEA,
			DROP INDEX FK6F1FC8121DDC97E4,
			DROP INDEX FK6F1FC8125F462A1,
			DROP INDEX FK6F1FC81214551360,
			DROP INDEX FK6F1FC812D0A6F0F,
			DROP INDEX FK6F1FC812FF6AA285""")
	}
}
