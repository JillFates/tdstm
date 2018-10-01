import com.tdsops.tm.enums.domain.SettingType
import net.transitionmanager.domain.Setting
import net.transitionmanager.service.DatabaseMigrationService


/**
 * Modify the field specs to have mapping for bulk actions and service.
 */
databaseChangeLog = {

	changeSet(author: "tpelletier", id: "20180917 TM-12359") {
		comment("Update field specs to have bulk service and actions.")

		grailsChange {
			change {
				// List all the field settings for the different domains.
				List<Setting> fieldSettings = Setting.where {
					type == SettingType.CUSTOM_DOMAIN_FIELD_SPEC
				}.list()

				// This closure adds default service and actions to each field, and the actions/service for tags.
				Closure changeScript = { settingsJson ->

					// Add the default actions of [:] and the default service of null.
					if (settingsJson.fields) {
						settingsJson.fields.each { field ->
							field.bulkChangeActions = []
						}

						//Adds the service and actions for tags
						def mapForTags = settingsJson.fields.find { it.field == "tagAssets" }

						if (mapForTags) {
							mapForTags.bulkChangeActions = [
								add,
								clear,
								replace,
								remove
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