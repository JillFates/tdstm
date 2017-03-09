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
}