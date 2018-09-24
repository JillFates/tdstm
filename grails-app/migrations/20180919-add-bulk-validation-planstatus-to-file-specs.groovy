import com.tdsops.tm.enums.domain.SettingType
import net.transitionmanager.domain.Setting
import net.transitionmanager.service.DatabaseMigrationService


/**
 * Modify the field specs to have mapping for bulk actions and service.
 */
databaseChangeLog = {

	changeSet(author: "tpelletier", id: "20180919 TM-12280-2") {
		comment("Update field specs to have bulk service and actions for validation and plans tatus.")

		grailsChange {
			change {
				// List all the field settings for the different domains.
				List<Setting> fieldSettings = Setting.where {
					type == SettingType.CUSTOM_DOMAIN_FIELD_SPEC
				}.list()

				// This closure adds the service and actions to moveBundle.
				Closure changeScript = { settingsJson ->
					if (settingsJson.fields) {
						//Adds the service and actions for validation and planStatus
						def fields = settingsJson.fields.findAll { it.field == "validation" || it.field == "planStatus" }

						if (fields) {
							fields.each { field ->
								field.bulkChangeService = "bulkChangeListService"
								field.bulkChangeActions = [
									replace: 'bulkReplace',
									clear  : 'bulkClear'
								]
							}
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