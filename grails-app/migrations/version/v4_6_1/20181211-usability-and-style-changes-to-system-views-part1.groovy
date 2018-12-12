package version.v4_6_1

import net.transitionmanager.domain.Dataview
import net.transitionmanager.service.DatabaseMigrationService

databaseChangeLog = {
	changeSet(author: 'slopez', id: '20181211-TM-13084-1') {
		comment('Make usability and style changes to the current set of new System Views - part 1')
		grailsChange {
			change {
				// get DatabaseMigrationService bean from context
				DatabaseMigrationService databaseMigrationService = ctx.getBean("databaseMigrationService")

				// List system dataviews that requires updates
				// 1 - All Assets
				// 2 - Database List
				// 3 - Device List
				// 4 - Server List
				// 5 - Storage Device List
				// 6 - Logical Storage List
				// 7 - Application List

				// step 1 - decrease all system views columns widths
				List<Dataview> dataViewList = Dataview.createCriteria().list {
					inList('id', [1l,2l,3l,4l,5l,6l,7l])
				}

				// Dataview reportSchema modification closure to set all columns widths except tagAssets columns
				Closure changeScript = { reportSchema ->
					def columns = []
					reportSchema.columns.each { col ->
						if (col && col.property) {
							if (col.property != 'tagAssets') {
								col.width = 140
							}
							columns << col
						}
					}
					reportSchema.columns = columns
					return reportSchema
				}

				databaseMigrationService.updateJsonObjects(dataViewList, 'reportSchema', changeScript)

				// step 2 - reorder all assets view columns [Plan Status, Bundle, Tags and Validation]
				Dataview allAssetsView = Dataview.get(1l)

				// Dataview reportSchema modification closure to reorder all assets view columns
				Closure changeScript2 = { reportSchema ->
					def planStatusCol
					def moveBundleCol
					def tagAssetsCol
					def validationCol
					reportSchema.columns.each { col ->
						if (col && col.property) {
							if (col.property == 'planStatus') {
								planStatusCol = col
							} else if (col.property == 'validation') {
								validationCol = col
							} else if (col.property == 'tagAssets') {
								tagAssetsCol = col
							} else if (col.property == 'moveBundle') {
								moveBundleCol = col
							}
						}
					}

					def columns = []
					reportSchema.columns.each { col ->
						if (col && col.property) {
							if (!(col.property in ['planStatus', 'validation', 'tagAssets', 'moveBundle'])) {
								columns << col
								if (col.property == 'environment') {
									// add columns in desired order [Bundle, Tags, Validation, Plan Status]
									columns << moveBundleCol
									columns << tagAssetsCol
									columns << validationCol
									columns << planStatusCol
								}
							}
						}
					}
					reportSchema.columns = columns
					return reportSchema
				}

				databaseMigrationService.updateJsonObjects([allAssetsView], 'reportSchema', changeScript2)

				// step 3 - add environment column to device, server, physical storage views
				List<Dataview> missingEnvironmentColumnDataViewList = Dataview.createCriteria().list {
					inList('id', [3l,4l,5l])
				}

				// Dataview reportSchema modification closure to add missing environment column
				Closure changeScript3 = { reportSchema ->
					def columns = []
					reportSchema.columns.each { col ->
						if (col && col.property) {
							if (col.property == 'description') {
								columns << col
								columns << [domain: 'common', 'edit': false, filter: '', label: 'Environment', locked: false, property: 'environment', width: 140]
							} else {
								columns << col
							}
						}
					}
					reportSchema.columns = columns
					return reportSchema
				}

				databaseMigrationService.updateJsonObjects(missingEnvironmentColumnDataViewList, 'reportSchema', changeScript3)

				// step 4 - in device list change the order of [Manufacturer, Model, Device Type]
				Dataview deviceListView = Dataview.get(3l)

				// Dataview reportSchema modification closure to reorder device list columns
				Closure changeScript4 = { reportSchema ->
					def manufacturerCol
					def modelCol
					def assetTypeCol
					reportSchema.columns.each { col ->
						if (col && col.property) {
							if (col.property == 'manufacturer') {
								manufacturerCol = col
							} else if (col.property == 'model') {
								modelCol = col
							} else if (col.property == 'assetType') {
								assetTypeCol = col
							}
						}
					}

					def columns = []
					reportSchema.columns.each { col ->
						if (col && col.property) {
							if (!(col.property in ['manufacturer', 'model', 'assetType'])) {
								columns  << col
								if (col.property == 'assetTag') {
									// add columns in desired order [Device Type, Manufacturer, Model]
									columns << assetTypeCol
									columns << manufacturerCol
									columns << modelCol
								}
							}
						}
					}
					reportSchema.columns = columns
					return reportSchema
				}

				databaseMigrationService.updateJsonObjects([deviceListView], 'reportSchema', changeScript4)
			}
		}
	}

}