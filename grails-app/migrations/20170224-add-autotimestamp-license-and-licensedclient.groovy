import net.transitionmanager.domain.License

/**
 * @author oluna
 * TM-6063
 */
databaseChangeLog = {
	changeSet(author: "oluna", id: "20170224 TM-6063.1.a") {
		comment('add autotimestamp and optimistic locking field for License Domain Objects')

		sql("""
			ALTER TABLE `license`
				ADD COLUMN `date_created` DATETIME NOT NULL,
				ADD COLUMN `last_updated` DATETIME NOT NULL,
				ADD COLUMN `version` int(11) NOT NULL
		""")
	}
	changeSet(author: "oluna", id: "20170224 TM-6063.1.b") {
		comment('add autotimestamp and optimistic locking field for LicensedClient Domain Objects')

		sql("""
			ALTER TABLE `licensed_client`
				ADD COLUMN `date_created` DATETIME NOT NULL,
				ADD COLUMN `last_updated` DATETIME NOT NULL,
				ADD COLUMN `version` int(11) NOT NULL
		""")
	}
}
