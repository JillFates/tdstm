import net.transitionmanager.domain.License

/**
 * @author oluna
 * TM-3776
 */
databaseChangeLog = {
	changeSet(author: "oluna", id: "20161129 TM-3776A") {
		comment('Fix "license" columns for client')

		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(schemaName: 'tdstm', tableName: 'license', columnName: 'status')
			}
		}

		sql("""
			ALTER TABLE `license`
			ADD COLUMN `status` INT NOT NULL DEFAULT ${License.Status.PENDING.id};
		""")

	}

	changeSet(author: "oluna", id: "20161129 TM-3776B") {
		comment('Fix "license" columns for client')

		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(schemaName: 'tdstm', tableName: 'license', columnName: 'type')
			}
		}

		sql("""
			ALTER TABLE `license`
			ADD COLUMN `type` INT NOT NULL DEFAULT 0;
		""")

	}
}