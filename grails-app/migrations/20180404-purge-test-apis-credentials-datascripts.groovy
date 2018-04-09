databaseChangeLog = {
	changeSet(author: 'arecordon', id: 'TM-10098-1') {
		comment('Purge the API Actions, Credentials and DataScripts and clear references.')
		sql("UPDATE asset_comment SET api_action_id = NULL WHERE api_action_id IS NOT NULL")
		sql("UPDATE import_batch SET data_script_id = NULL WHERE data_script_id IS NOT NULL")
		sql("DELETE FROM api_action")
		sql("DELETE FROM credential")
		sql("DELETE FROM data_script")
	}
}
