/**
 * @author oluna
 * TM-3776
 */
databaseChangeLog = {
	changeSet(author: "oluna", id: "20161220 TM-3776") {
		comment('Adding "banner_message" columns')

		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(tableName: 'licensed_client', columnName: 'banner_message')
			}
		}

		sql("""
			ALTER TABLE `licensed_client`
			ADD COLUMN `banner_message` TEXT;
		""")

	}

}
