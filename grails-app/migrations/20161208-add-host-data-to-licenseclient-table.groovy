/**
 * @author oluna
 * TM-3776
 */
databaseChangeLog = {
	changeSet(author: "oluna", id: "20161208.2 TM-3776 A") {
		comment('Adding "host_name" columns')

		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(tableName: 'licensed_client', columnName: 'host_name')
			}
		}

		sql("""
			ALTER TABLE `licensed_client`
			ADD COLUMN `host_name` varchar(255);
		""")

	}

	changeSet(author: "oluna", id: "20161208.2 TM-3776 B") {
		comment('Adding "websitename" columns ')

		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(tableName: 'licensed_client', columnName: 'websitename')
			}
		}

		sql("""
			ALTER TABLE `licensed_client`
			ADD COLUMN `websitename` varchar(255);
		""")

	}

}
