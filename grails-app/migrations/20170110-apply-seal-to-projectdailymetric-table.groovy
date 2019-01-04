import net.transitionmanager.ProjectDailyMetric

/**
 * @author oluna
 * TM-3776
 */
databaseChangeLog = {
	changeSet(author: "oluna", id: "20170110 TM-5812 compute seals") {
		comment('compute all seals')

		grailsChange {
			change {
				ProjectDailyMetric.findAll().each {
					it.seal = it.computeSeal()
					it.save(flush: true)
				}
			}
		}
	}

}
