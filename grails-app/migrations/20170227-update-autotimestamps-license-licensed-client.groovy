import net.transitionmanager.domain.License

/**
 * @author oluna
 * TM-6063
 */
databaseChangeLog = {
	changeSet(author: "oluna", id: "20170224 TM-6063 delete lic activities") {
		comment('Delete license current Activies')

		sql("update license set last_updated=now()")
		sql("update license set date_created=now()")
		sql("update licensed_client set last_updated=now()")
		sql("update licensed_client set date_created=now()")
	}
}
