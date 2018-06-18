databaseChangeLog = {
	changeSet(author: "slopez", id: "TM-10655-01") {
		comment('Add bypassMode column to the MoveEvent domain')
		sql ("""
                ALTER TABLE `move_event` ADD `api_action_bypass` TINYINT(1) DEFAULT 1 NOT NULL AFTER `runbook_recipe`;
 		""")
	}
}
