package version.v4_6_2

databaseChangeLog = {

	changeSet(author: 'slopez', id: '20190204 TM-14060-1') {
		comment("Add percent done columns to asset comment table")

		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'asset_comment', columnName: 'api_action_percent_done')
				columnExists(tableName: 'asset_comment', columnName: 'task_percent_done')
			}
		}

		// add new columns to asset_comment table
		sql (""" 
				ALTER TABLE `asset_comment` ADD COLUMN `api_action_percent_done` TINYINT DEFAULT 0; 
				ALTER TABLE `asset_comment` ADD COLUMN `task_percent_done` TINYINT DEFAULT 0;
		""")

		// taskPercentDone - set to 100 for all tasks with status = AssetCommentStatus.COMPLETED
		// apiActionPercentDone - set to 100 for all tasks with status = AssetCommentStatus.COMPLETED and apiAction is set
		sql ("""
				UPDATE `asset_comment` SET `api_action_percent_done` = 100 
				WHERE `api_action_id` IS NOT NULL AND `status` = 'Completed';
				
				UPDATE `asset_comment` SET `task_percent_done` = 100 
				WHERE `status` = 'Completed';
		""")

		// set not null constraint on task_percent_done column
		sql ("""
				ALTER TABLE `asset_comment` MODIFY `task_percent_done` TINYINT DEFAULT 0 NOT NULL; 
		""")

	}

	changeSet(author: 'slopez', id: '20190204 TM-14060-2') {
		comment("Add modifications required for TMR to api action table")

		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'api_action', columnName: 'action_type')
				columnExists(tableName: 'api_action', columnName: 'script')
				columnExists(tableName: 'api_action', columnName: 'command_line')
				columnExists(tableName: 'api_action', columnName: 'is_remote')
				columnExists(tableName: 'api_action', columnName: 'remote_credential_method')
			}
		}

		// add new columns to api_action table
		sql (""" 
				ALTER TABLE `api_action` ADD COLUMN `action_type` VARCHAR(15) DEFAULT 'WebAPI'; 
				ALTER TABLE `api_action` ADD COLUMN `script` MEDIUMTEXT;
				ALTER TABLE `api_action` ADD COLUMN `command_line` VARCHAR(1024);
				ALTER TABLE `api_action` ADD COLUMN `is_remote` BOOLEAN DEFAULT FALSE NOT NULL;
				ALTER TABLE `api_action` ADD COLUMN `remote_credential_method` VARCHAR(15);
		""")

		// enhance current fields on api_action table
		sql (""" 
				ALTER TABLE `api_action` MODIFY `endpoint_url` VARCHAR(1024); 
				ALTER TABLE `api_action` MODIFY `doc_url` VARCHAR(1024); 
		""")

	}

	changeSet(author: 'slopez', id: '20190204 TM-14060-3') {
		comment('add new permissions for TMR support')

		grailsChange {
			change {
				def perms = [
						'ActionRemoteAllowed'		: [
								group      : 'NONE',
								description: 'Can invoke API Actions from remote system',
								roles      : []
						],
						'ActionViewScript'			: [
								group      : 'NONE',
								description: 'Can view API Action script to be executed',
								roles      : []
						],
						'EarlyAccessTMR'			: [
								group      : 'NONE',
								description: 'EarlyAccessTMR',
								roles      : []
						],

				]

				def databaseMigrationService = ctx.getBean('databaseMigrationService')
				databaseMigrationService.addPermissions(sql, perms)
			}
		}
	}
}