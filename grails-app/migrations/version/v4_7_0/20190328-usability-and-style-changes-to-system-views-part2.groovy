package version.v4_7_0

import net.transitionmanager.domain.Dataview
import net.transitionmanager.service.DatabaseMigrationService

databaseChangeLog = {
	changeSet(author: 'slopez', id: '20190328-TM-13566-1') {
		comment('Make usability and style changes to the current set of new System Views - part 1')
		grailsChange {
			change {
				// get DatabaseMigrationService bean from context
				DatabaseMigrationService databaseMigrationService = ctx.getBean("databaseMigrationService")

				// List system dataviews that requires updates
				// 3 - Device List
				// 4 - Server List
				// 5 - Storage Device List

				// step 1 - in device list change the order of [Device Type, Manufacturer, Model]
				Dataview deviceListView = Dataview.get(3l)

				// Dataview reportSchema modification closure to reorder device list columns
				Closure changeScript1 = { reportSchema ->
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

					// set assetType col to be locked
					assetTypeCol['locked'] = true

					def columns = []
					reportSchema.columns.each { col ->
						if (col && col.property) {
							if (!(col.property in ['manufacturer', 'model', 'assetType'])) {
								columns  << col
								if (col.property == 'assetName') {
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

				databaseMigrationService.updateJsonObjects([deviceListView], 'reportSchema', changeScript1)


				// step 2 - in server list change the order of [Device Type, Manufacturer, Model]
				Dataview serverListView = Dataview.get(4l)

				// Dataview reportSchema modification closure to reorder device list columns
				Closure changeScript2 = { reportSchema ->
					def manufacturerCol
					def modelCol
					def assetTypeCol
					def foundAssetTypeCol = false
					reportSchema.columns.each { col ->
						if (col && col.property) {
							if (col.property == 'manufacturer') {
								manufacturerCol = col
							} else if (col.property == 'model') {
								modelCol = col
							} else if (col.property == 'assetType' && !foundAssetTypeCol) {
								assetTypeCol = col
								// doing this to remove duplicated assetType column in server list view
								foundAssetTypeCol = true
							}
						}
					}

					// set assetType col to be locked and add filter
					assetTypeCol['locked'] = true
					assetTypeCol['filter'] = 'Server|Appliance|Blade|VM|Virtual'

					def columns = []
					reportSchema.columns.each { col ->
						if (col && col.property) {
							if (!(col.property in ['manufacturer', 'model', 'assetType'])) {
								columns  << col
								if (col.property == 'assetName') {
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

				databaseMigrationService.updateJsonObjects([serverListView], 'reportSchema', changeScript2)


				// step 3 - in physical storage list change the order of [Device Type, Manufacturer, Model]
				Dataview physicalStorageListView = Dataview.get(5l)

				// Dataview reportSchema modification closure to reorder device list columns
				Closure changeScript3 = { reportSchema ->
					def manufacturerCol
					def modelCol
					def assetTypeCol
					def foundAssetTypeCol = false
					reportSchema.columns.each { col ->
						if (col && col.property) {
							if (col.property == 'manufacturer') {
								manufacturerCol = col
							} else if (col.property == 'model') {
								modelCol = col
							} else if (col.property == 'assetType' && !foundAssetTypeCol) {
								assetTypeCol = col
								// doing this to remove duplicated assetType column in server list view
								foundAssetTypeCol = true
							}
						}
					}

					// set assetType col to be locked and add filter
					assetTypeCol['locked'] = true
					assetTypeCol['filter'] = 'Array|Disk|NAS|SAN|SAN Switch|Storage|Tape|Tape Library|Virtual Tape Library'

					def columns = []
					reportSchema.columns.each { col ->
						if (col && col.property) {
							if (!(col.property in ['manufacturer', 'model', 'assetType'])) {
								columns  << col
								if (col.property == 'assetName') {
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

				databaseMigrationService.updateJsonObjects([physicalStorageListView], 'reportSchema', changeScript3)

			}
		}
	}

}