package version.v4_6_0

import com.tdsops.tm.enums.domain.SettingType
import net.transitionmanager.common.Setting
import net.transitionmanager.service.DatabaseMigrationService

/**
 * Replacing move bundle actions with just remove, it shouldn't have replace.
 */
databaseChangeLog = {

	changeSet(author: "tpelletier", id: "20181106 TM-12280") {
		comment("Replacing actions for bundle, it should only have replace.")

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

						//update actions for moveBundle
						def fieldsToUpdate = settingsJson.fields.find { it.field == "moveBundle" }

						if (fieldsToUpdate) {
							fieldsToUpdate.control = 'Reference'
							fieldsToUpdate.bulkChangeActions = [
								'replace'
							]
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
