import com.tdsops.tm.enums.domain.SettingType
import net.transitionmanager.domain.Setting
import net.transitionmanager.service.DatabaseMigrationService


/**
 * Modify the field specs to have mapping for bulk actions and service.
 */
databaseChangeLog = {

	changeSet(author: "tpelletier", id: "20180919 TM-12280") {
		comment("Update field specs to have bulk service and actions for move bundle.")

		grailsChange {
			change {
				// List all the field settings for the different domains.
				List<Setting> fieldSettings = Setting.where {
					type == SettingType.CUSTOM_DOMAIN_FIELD_SPEC
				}.list()

				// This closure adds the service and actions to moveBundle.
				Closure changeScript = { settingsJson ->
					if (settingsJson.fields) {
						//Adds the service and actions for moveBundle
						def mapForMoveBundle = settingsJson.fields.find { it.field == "moveBundle" }

						if (mapForMoveBundle) {
							mapForMoveBundle.bulkChangeActions = [
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