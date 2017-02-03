databaseChangeLog = {
	changeSet(author: "jmartin", id: "20170203 TM-5958-01") {
		comment('Create the ApiAction table')

		sql("""
			CREATE TABLE api_action (
				id bigint(20) NOT NULL AUTO_INCREMENT,
				version bigint(20) NOT NULL DEFAULT 1,
				name varchar(64) NOT NULL,
				project_id bigint(20) NOT NULL,
				agent_class int(3) NOT NULL,
				agent_method varchar(64) NOT NULL COMMENT 'The name of the method on the agent class to invoke',
				callback_mode int(3) NOT NULL COMMENT 'Indicates how the response is returned for async methods',
				callback_queue varchar(64) NOT NULL DEFAULT '' COMMENT 'The name of the queue if the callback is to message',
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

	changeSet(author: "jmartin", id: "20170203 TM-5958-02") {
		comment('Add apiAction column to AssetComment table')
		sql("""
			ALTER TABLE `asset_comment`
			ADD COLUMN `api_action_id` bigint(20) NULL,
			ADD INDEX `IDX_ASSET_COMMENT_API_ACTION` (`api_action_id`),
			ADD CONSTRAINT `FK_ASSET_COMMENT_TO_API_ACTION`
					FOREIGN KEY (`api_action_id`)
					REFERENCES api_action (`id`)
					ON DELETE NO ACTION
					ON UPDATE CASCADE;
		""")
	}
}