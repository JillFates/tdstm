import net.transitionmanager.domain.License

/**
 * @author oluna
 * TM-6063
 */
databaseChangeLog = {
	changeSet(author: "oluna", id: "20170224 TM-6063 delete lic activities") {
		comment('Delete license current Activies')

		sql("delete from `license_activity_track`")
	}
}
