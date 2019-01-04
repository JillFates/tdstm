import net.transitionmanager.domain.License

/**
 * @author oluna
 * TM-6063
 */
databaseChangeLog = {
	changeSet(author: "oluna", id: "20170307 TM-6063.2.li.v2") {
		comment('Fix "license.instalation_num" misspelled column')

		preConditions(onFail:'MARK_RAN') {
			columnExists(tableName:"license", columnName:"instalation_num")
		}

		sql("""
			ALTER TABLE `license`
				CHANGE COLUMN `instalation_num` `installation_num` VARCHAR(255) NOT NULL default '';
		""")
	}

	changeSet(author: "oluna", id: "20170307 TM-6063.2.lc.v2") {
		comment('Fix "licensed_client.instalation_num" misspelled column')

		preConditions(onFail:'MARK_RAN') {
			columnExists(tableName:"licensed_client", columnName:"instalation_num")
		}

		sql("""
			ALTER TABLE `licensed_client`
				CHANGE COLUMN `instalation_num` `installation_num` VARCHAR(255) NOT NULL default '';
		""")
	}

	changeSet(author: "oluna", id: "20170309 TM-6151.a") {
		comment('Fix dumb environment erase in license')

		preConditions(onFail:'MARK_RAN') {
			not{
				columnExists(tableName:"license", columnName:"environment")
			}
		}

		sql("""
			ALTER TABLE `license`
				ADD COLUMN `environment` VARCHAR(255) NOT NULL AFTER `email`;
		""")

		sql("""
			update `license` set `environment`='${License.Environment.DEMO.name()}' where `environment`='';
		""")
	}

	changeSet(author: "oluna", id: "20170309 TM-6151.b") {
		comment('Fix dumb environment erase in licensed_client')

		preConditions(onFail:'MARK_RAN') {
			not{
				columnExists(tableName:"licensed_client", columnName:"environment")
			}
		}

		sql("""
			ALTER TABLE `licensed_client`
				ADD COLUMN `environment` VARCHAR(255) NOT NULL AFTER `email`;
		""")

		sql("""
			update `licensed_client` set `environment`='${License.Environment.DEMO.name()}' where `environment`='';
		""")
	}
}
