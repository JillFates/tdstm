import net.transitionmanager.domain.Dataview
import net.transitionmanager.service.DatabaseMigrationService
import org.grails.web.json.JSONObject


databaseChangeLog = {
	changeSet(author: 'arecordon', id: 'TM-10775-1') {
		comment("Correct the name in the dataviews' when the sorting field is location, rack or room.")
		grailsChange {
			change {
				List<Dataview> dataviews = Dataview.list()

				// Map where the keys are the incorrect field names and the values are the corrected names.
				Map<String, String> renameMap = [
					sourceLocation:	'locationSource',
					sourceRack: 'rackSource',
					sourceRoom: 'roomSource',
					targetLocation:	'locationTarget',
					targetRack: 'rackTarget',
					targetRoom: 'roomTarget'
				]

				Set<String> incorrectFieldNames = renameMap.keySet()

				// Replace the sorting property name where needed.
				Closure updateReportSchemaScript = { JSONObject reportSchemaJson ->
					String sortProperty = reportSchemaJson.sort?.property
					if (reportSchemaJson.containsKey('sort') && sortProperty in incorrectFieldNames) {
						reportSchemaJson['sort']['property'] = renameMap[sortProperty]
					}
					return reportSchemaJson

				}

				DatabaseMigrationService databaseMigrationService = ctx.getBean("databaseMigrationService")
				// Update the different dataviews sorting property with the new names.
				databaseMigrationService.updateJsonObjects(dataviews, "reportSchema", updateReportSchemaScript)
			}
		}

	}
}

