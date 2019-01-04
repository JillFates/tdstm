/**
 * @author oluna
 * TM-3776
 */
databaseChangeLog = {
	changeSet(author: "oluna", id: "20170110 TM-3776") {
		comment('Adding "banner_message" column')

		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(tableName: 'license', columnName: 'banner_message')
			}
		}

		sql("""
			ALTER TABLE `license`
			ADD COLUMN `banner_message` TEXT;
		""")

	}

}
