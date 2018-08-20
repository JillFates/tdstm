import com.tdsops.tm.enums.domain.SettingType
import net.transitionmanager.domain.Setting
import net.transitionmanager.service.DatabaseMigrationService


/**
 * Modify all date fields to have the control of dateTime.
 */
databaseChangeLog = {

	changeSet(author: "tpelletier", id: "20180815 TM-11726") {
		comment("Update date related fields to have a control of dateTime.")

		grailsChange {
			change {
				List dateColumns = ['maintExpDate', 'retireDate', 'lastUpdated',]
				// List all the field settings for the different domains.
				List<Setting> fieldSettings = Setting.where {
					type == SettingType.CUSTOM_DOMAIN_FIELD_SPEC
				}.list()

				// This closure replace changes the 'tags' field (if exists) to 'tagAssets'.
				Closure changeScript = { settingsJson ->
					// Add the field to the list of fields for this domain
					if (settingsJson.fields) {
						def mapForTags = settingsJson.fields.findAll { it.field in dateColumns }

						mapForTags.each { Map map ->
							map.control = "DataTime"
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