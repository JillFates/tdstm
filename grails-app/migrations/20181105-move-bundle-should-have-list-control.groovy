import com.tdsops.tm.enums.domain.SettingType
import net.transitionmanager.common.Setting
import net.transitionmanager.common.DatabaseMigrationService


/**
 * Modify moveBundle field to have the control of List.
 */
databaseChangeLog = {

	changeSet(author: "tpelletier", id: "20181105 TM-12848") {
		comment("Move bundle should have a list control.")

		grailsChange {
			change {
				// List all the field settings for the different domains.
				List<Setting> fieldSettings = Setting.where {
					type == SettingType.CUSTOM_DOMAIN_FIELD_SPEC
				}.list()

				// This closure replace changes the 'tags' field (if exists) to 'tagAssets'.
				Closure changeScript = { settingsJson ->
					// Add the field to the list of fields for this domain
					if (settingsJson.fields) {
						def mapForTags = settingsJson.fields.findAll { it.field == 'moveBundle' }

						mapForTags.each { Map map ->
							map.control = "InList"
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
