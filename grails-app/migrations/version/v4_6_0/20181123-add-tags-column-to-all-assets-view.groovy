package version.v4_6_0

import net.transitionmanager.domain.Dataview
import net.transitionmanager.service.DatabaseMigrationService

databaseChangeLog = {
	changeSet(author: 'slopez', id: '20181123-TM-13152-1') {
		comment('Add the Tags column to the All Assets List View.')
		grailsChange {
			change {
				// List system dataviews that requires updates
				// 1 - All Assets
				List<Dataview> dataViewList = Dataview.where {
					id == 1l
				}.list()

				// Dataview reportSchema modification closure
				Closure changeScript = { settingsJson ->
					// Iterate columns to see if "tags" column was already added, then do nothing
					def hasTagsColumns = settingsJson.columns.find { col -> col.property == 'tagAssets' }
					if (hasTagsColumns) {
						println 'All Assets view already has "Tags" column added.'
						return settingsJson
					}

					// If "Tags" columns is not already present in the view definition, then
					// iterate columns to find where "validation" column is and add new "tags" column after
					def columns = []
					settingsJson.columns.each { col ->
						columns << col
						if (col.property == 'validation') {
							// add tags column
							columns << [domain: 'common', 'edit': false, filter: '', label: 'Tags', locked: false, property: 'tagAssets', width: 200]
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