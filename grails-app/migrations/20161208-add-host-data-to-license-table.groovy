/**
 * @author oluna
 * TM-3776
 */
databaseChangeLog = {
	changeSet(author: "oluna", id: "20161208 TM-3776 A") {
		comment('Adding "host_name" columns')

		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(tableName: 'license', columnName: 'host_name')
			}
		}

		sql("""
			ALTER TABLE `license`
			ADD COLUMN `host_name` varchar(255);
		""")

	}

	changeSet(author: "oluna", id: "20161208 TM-3776 B") {
		comment('Adding "websitename" columns ')

		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(tableName: 'license', columnName: 'websitename')
			}
		}

		sql("""
			ALTER TABLE `license`
			ADD COLUMN `websitename` varchar(255);
		""")

	}

}
