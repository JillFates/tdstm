/**
 * @author oluna
 * TM-3776
 */
databaseChangeLog = {
	changeSet(author: "oluna", id: "20161207 TM-3776 A") {
		comment('Fix "license" columns for Admin')

		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(tableName: 'license', columnName: 'owner_id')
			}
		}

		sql("""
			ALTER TABLE `license`
			ADD COLUMN `owner_id` bigint(20);
		""")

	}

	changeSet(author: "oluna", id: "20161207 TM-3776 B") {
		comment('Fix "license" columns for Manager')

		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(tableName: 'licensed_client', columnName: 'owner')
			}
		}

		sql("""
			ALTER TABLE `licensed_client`
			ADD COLUMN `owner` varchar(255);
		""")

	}
}
