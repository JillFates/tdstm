import net.transitionmanager.domain.License

/**
 * @author oluna
 * TM-3776
 */
databaseChangeLog = {
	changeSet(author: "oluna", id: "20161129 TM-3776A.v2") {
		comment('Fix "license" columns for client')

		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(tableName: 'license', columnName: 'status')
			}
		}

		def pendingStatus = License.Status.PENDING
		def PENDING_ID = pendingStatus.hasProperty("id")? pendingStatus["id"] : 4

		sql("""
			ALTER TABLE `license`
			ADD COLUMN `status` INT NOT NULL DEFAULT ${PENDING_ID}
		""")

	}

	changeSet(author: "oluna", id: "20161129 TM-3776B.v2") {
		comment('Fix "license" columns for client')

		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(tableName: 'license', columnName: 'type')
			}
		}

		sql("""
			ALTER TABLE `license`
			ADD COLUMN `type` INT NOT NULL DEFAULT 0;
		""")

	}
}
