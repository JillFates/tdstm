package version.v4_7_2

import net.transitionmanager.common.DatabaseMigrationService
import net.transitionmanager.imports.Dataview


/**
 * Add the AssetClass field to the field settings for every project.
 */

databaseChangeLog = {

	changeSet(author: "tpelletier", id: "20200129 TM-10953-1") {
		comment("remove null columns from data views.")
		grailsChange {
			change {
				// List dataviews that requires updates
				List<Dataview> dataViewList = Dataview.findAllByIdGreaterThan(7l)

				// Dataview reportSchema modification closure
				Closure changeScript = { settingsJson ->
					settingsJson.columns.remove(null)
					return settingsJson
				}

				// get DatabaseMigrationService bean from context
				DatabaseMigrationService databaseMigrationService = ctx.getBean("databaseMigrationService")
				databaseMigrationService.updateJsonObjects(dataViewList, 'reportSchema', changeScript)
			}
		}
	}
}
