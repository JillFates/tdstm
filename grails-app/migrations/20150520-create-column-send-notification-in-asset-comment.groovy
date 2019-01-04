/**
 * Add default values for is published
 */
databaseChangeLog = {
	changeSet(author: "jdanahy", id: "20150520 TM-3902-1") {
		comment('Create column send notification in asset comment')
		sql("""
				ALTER TABLE asset_comment
				ADD send_notification tinyint(1)
			""")
	}
	
	changeSet(author: "jdanahy", id: "20150520 TM-3902-2") {
		comment('Create column send notification in asset comment')
		
		preConditions(onFail:'MARK_RAN') {
			columnExists(schemaName:'tdstm', tableName:'asset_comment', columnName:'send_notification')
		}
		sql("""
				UPDATE tdstm.asset_comment SET send_notification = 0;
				ALTER TABLE `tdstm`.`asset_comment` CHANGE COLUMN `send_notification` `send_notification` BIT(1) NOT NULL DEFAULT 0;
			""")
	}
}
