package version.v4_6_0

import com.tdsops.tm.enums.domain.SettingType
import net.transitionmanager.common.Setting
import net.transitionmanager.common.DatabaseMigrationService

/**
 * Replacing validation actions with just remove, it shouldn't have replace.
 * Updating Size to have a number control
 * Setting validation to Unknown for blank or null.
 */
databaseChangeLog = {

	changeSet(author: "tpelletier", id: "20181130 TM-13368-1") {
		comment("Replacing actions for validated, it should only have replace and updating size to have a number control")

		grailsChange {
			change {
				// List all the field settings for the different domains.
				List<Setting> fieldSettings = Setting.where {
					type == SettingType.CUSTOM_DOMAIN_FIELD_SPEC
				}.list()

				// This closure adds an empty list of actions to each field, and the actions for tags.
				Closure changeScript = { settingsJson ->

					// Add the default actions of [:] and the default service of null.
					if (settingsJson.fields) {

						//update actions for validated
						def fieldsToUpdate = settingsJson.fields.find { it.field == "validated" }

						if (fieldsToUpdate) {
							fieldsToUpdate.bulkChangeActions = [
								'replace'
							]
						}

						//Updating size field to have a number control.
						fieldsToUpdate = settingsJson.fields.findAll { it.field == 'size' }

						fieldsToUpdate.each { Map map ->
							map.control = "Number"
						}
					}
					return settingsJson
				}


				DatabaseMigrationService databaseMigrationService = ctx.getBean("databaseMigrationService")
				// Update the different settings with the Asset Class fields.
				databaseMigrationService.updateJsonObjects(fieldSettings, "json", changeScript)

			}
		}
	}

	changeSet(author: "tpelletier", id: "20181130 TM-13368-2") {
		comment("Set the validation='Unknown' black or null")
		sql("UPDATE asset_entity SET validation='Unknown' WHERE (validation = '' OR validation IS NULL)")
	}
}
