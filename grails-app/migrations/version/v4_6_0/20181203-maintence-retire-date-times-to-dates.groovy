package version.v4_6_0

import com.tdsops.tm.enums.domain.SettingType
import net.transitionmanager.domain.Setting
import net.transitionmanager.service.DatabaseMigrationService

/**
 * Replacing validation actions with just remove, it shouldn't have replace.
 * Updating Size to have a number control
 * Setting validation to Unknown for blank or null.
 */
databaseChangeLog = {

	changeSet(author: "tpelletier", id: "20181203 TM-13370") {
		comment("Change the Maintenance and Retire dates from DateTime to Date")

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
						//Updating size field to have a date control.
						fieldsToUpdate = settingsJson.fields.findAll { it.field == 'retireDate' ||  it.field == "maintExpDate" }

						fieldsToUpdate.each { Map map ->
							map.control = "Date"
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

}
