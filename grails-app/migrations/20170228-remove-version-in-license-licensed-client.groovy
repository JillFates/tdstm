import net.transitionmanager.domain.License

/**
 * @author oluna
 * TM-6063
 */
databaseChangeLog = {
	changeSet(author: "oluna", id: "20170228 TM-6085") {
		comment('Remove version Field, not needed for locking and causing problems')

		sql("ALTER TABLE `license` DROP `version`")
		sql("ALTER TABLE `licensed_client` DROP `version`")
	}
}
