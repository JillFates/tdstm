import net.transitionmanager.domain.Dataview
import net.transitionmanager.service.DatabaseMigrationService

databaseChangeLog = {
	changeSet(author: 'slopez', id: '20181115-TM-13024-1') {
		comment('Change the column ordering of the new System Views.')
		grailsChange {
			change {
				// List system dataviews that requires updates
				// 2 - All Databases
				// 3 - All Devices
				// 4 - All Servers
				// 5 - All Storage Physical
				// 6 - All Storage Virtual
				// 7 - All Applications
				List<Dataview> dataViewList = Dataview.where {
					id in [2l,3l,4l,5l,6l,7l]
				}.list()

				// Dataview reportSchema modification closure
				Closure changeScript = { settingsJson ->
					// Find id column to remove and set assetName as locked
					def columns = []
					settingsJson.columns.each { col ->
						if (col.property != 'id') {
							if (col.property == 'assetName') {
								col.locked = true
							}
							columns << col
						}
					}
					settingsJson.columns = columns
					return settingsJson
				}

				// get DatabaseMigrationService bean from context
				DatabaseMigrationService databaseMigrationService = ctx.getBean("databaseMigrationService")
				databaseMigrationService.updateJsonObjects(dataViewList, 'reportSchema', changeScript)
			}
		}
	}

}