package version.v4_6_3

databaseChangeLog = {

	changeSet(author: 'slopez', id: '20190619 TM-15353-1') {
		comment('Remove api action percent done column from asset comment table')

		preConditions(onFail: 'MARK_RAN') {
			columnExists(tableName: 'asset_comment', columnName: 'api_action_percent_done')
		}

		// handle modifications to asset_comment table
		sql (""" 
				ALTER TABLE `asset_comment` DROP COLUMN `api_action_percent_done`; 
				ALTER TABLE `asset_comment` CHANGE `task_percent_done` `percentage_complete` TINYINT DEFAULT 0 NOT NULL; 
		""")
	}

}