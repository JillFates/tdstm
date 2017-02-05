databaseChangeLog = {

	changeSet(author: 'jmartin', id: '20170203 TM-5958-01') {
		comment('Create the ApiAction table')

		sql("""
			CREATE TABLE api_action (
				id bigint(20) NOT NULL AUTO_INCREMENT,
				project_id bigint(20) NOT NULL,
				name varchar(64) NOT NULL,
				version bigint(20) NOT NULL DEFAULT 0,
				description varchar(255) NOT NULL DEFAULT '',
				agent_class varchar(64) NOT NULL,
				agent_method varchar(64) NOT NULL COMMENT 'The name of the method on the agent class to invoke',
				method_params JSON NULL COMMENT 'The definition of the method parameters in JSON format',
				async_queue varchar(64) NOT NULL DEFAULT '' COMMENT 'The name of the queue that async actions use to invoke methods',
				callback_mode varchar(64) NOT NULL COMMENT 'Indicates how the response is returned for async methods',
				timeout int(8) NOT NULL DEFAULT 0 COMMENT 'The time (sec) after which an async call is considered failed',
				date_created datetime DEFAULT NULL,
				last_modified datetime DEFAULT NULL,
				PRIMARY KEY (id),
				KEY (project_id),
				UNIQUE KEY (project_id, name),
				CONSTRAINT `FK_API_ACTION_TO_PROJECT`
					FOREIGN KEY (project_id )
					REFERENCES project (project_id )
					ON DELETE CASCADE
					ON UPDATE CASCADE
			) ENGINE=InnoDB CHARSET=latin1;
		""")
	}

	changeSet(author: 'jmartin', id: '20170203 TM-5958-02') {
		comment('Add apiAction column to AssetComment table')
		sql("""
			ALTER TABLE `asset_comment`
			ADD COLUMN `api_action_id` bigint(20) NULL COMMENT 'The API Action to be invoked',
			ADD COLUMN `api_action_settings` JSON NULL COMMENT 'Settings for the API Action that override the default settings',
			ADD COLUMN `api_action_invoked_at` DATE NULL COMMENT 'The datetime that the API Action was invoked',
			ADD COLUMN `api_action_completed_at` DATE NULL COMMENT 'The datetime that the API Action invocation completed',
			ADD INDEX `IDX_ASSET_COMMENT_API_ACTION` (`api_action_id`),
			ADD CONSTRAINT `FK_ASSET_COMMENT_TO_API_ACTION`
				FOREIGN KEY (`api_action_id`)
				REFERENCES api_action (`id`)
				ON DELETE NO ACTION
				ON UPDATE CASCADE;
		""")
	}

}