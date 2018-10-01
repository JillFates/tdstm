import com.tdsops.tm.enums.domain.SettingType
import net.transitionmanager.domain.Setting
import net.transitionmanager.service.DatabaseMigrationService


/**
 * Modify the field specs to have mapping for bulk actions and service.
 */
databaseChangeLog = {

	changeSet(author: "tpelletier", id: "20180920 TM-12254") {
		comment("Update field specs to have bulk actions for custom fields.")

		grailsChange {
			change {
				// List all the field settings for the different domains.
				List<Setting> fieldSettings = Setting.where {
					type == SettingType.CUSTOM_DOMAIN_FIELD_SPEC
				}.list()

				// This closure adds the bulk actions to custom fields.
				Closure changeScript = { settingsJson ->
					if (settingsJson.fields) {
						//Adds the actions for custom fields
						def fields = settingsJson.fields.findAll { it.field.startsWith('custom') }

						if (fields) {
							fields.each { field ->
								field.bulkChangeActions = [
									'replace',
									'clear  '
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