import com.tdsops.tm.enums.domain.SettingType
import net.transitionmanager.domain.Dataview
import net.transitionmanager.domain.Setting
import net.transitionmanager.service.DatabaseMigrationService


databaseChangeLog = {
	changeSet(author: 'arecordon', id: 'TM-9770-1') {
		comment('Correct the name in the field specs for location, rack and room')
		grailsChange {
			change {
				List<Setting> deviceSettings = Setting.where {
					type == SettingType.CUSTOM_DOMAIN_FIELD_SPEC
					key == 'DEVICE'
				}.list()

				// Map where the keys are the incorrect field names and the values are the corrected names.
				Map<String, String> renameMap = [
					sourceLocation:	'locationSource',
					sourceRack: 'rackSource',
					sourceRoom: 'roomSource',
					targetLocation:	'locationTarget',
					targetRack: 'rackTarget',
					targetRoom: 'roomTarget'
				]
				//
				Set<String> incorrectFieldNames = renameMap.keySet()


				// Iterate over the fields replacing the field name where needed.
				Closure updateSettingScript = { fieldSettingsJson ->
					for (field in fieldSettingsJson.fields) {
						String fieldName = field.field
						if (fieldName in incorrectFieldNames) {
							field.field = renameMap[fieldName]
						}
					}
					return fieldSettingsJson

				}

				DatabaseMigrationService databaseMigrationService = ctx.getBean("databaseMigrationService")
				// Update the different settings with the correct names.
				databaseMigrationService.updateJsonObjects(deviceSettings, "json", updateSettingScript)
			}
		}

	}

	changeSet(author: 'arecordon', id: 'TM-9770-2') {
		comment('Default the version column to zero')
		sql('UPDATE dataview SET version = 0 WHERE version IS NULL')
		sql('ALTER TABLE dataview MODIFY COLUMN version INT NOT NULL DEFAULT 0')
	}

	changeSet(author: 'arecordon', id: 'TM-9770-3') {
		comment("Correct the name for location, rack and room in the report schema for existing dataviews.")
		grailsChange {
			change {
				List<Dataview> dataviews = Dataview.where {}.list()

				// Map where the keys are the incorrect field names and the values are the corrected names.
				Map<String, String> renameMap = [
					sourceLocation:	'locationSource',
					sourceRack: 'rackSource',
					sourceRoom: 'roomSource',
					targetLocation:	'locationTarget',
					targetRack: 'rackTarget',
					targetRoom: 'roomTarget'
				]
				//
				Set<String> incorrectFieldNames = renameMap.keySet()


				// Iterate over the fields replacing the field name where needed.
				Closure updateReportSchemaScript = { reportSchemaJson ->
					for (column in reportSchemaJson.columns) {
						String fieldName = column.property
						if (fieldName in incorrectFieldNames) {
							column.property = renameMap[fieldName]
						}
					}
					return reportSchemaJson

				}

				DatabaseMigrationService databaseMigrationService = ctx.getBean("databaseMigrationService")
				// Update the different dataviews with the new names.
				databaseMigrationService.updateJsonObjects(dataviews, "reportSchema", updateReportSchemaScript)
			}
		}

	}
}

