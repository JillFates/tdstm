databaseChangeLog = {

	changeSet(author: "dscarpa", id: "2015026 TM-3906-2") {
		comment('Add column timezone_id to project table')
		
		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(tableName:'project', columnName:'timezone_id' )
			}
		}
		sql("ALTER TABLE `project` ADD `timezone_id` BIGINT(20);")
		sql("ALTER TABLE `project` ADD CONSTRAINT FK_TIMEZONE_ID FOREIGN KEY (`timezone_id`) REFERENCES `timezone` (`id`);")
	}
}
