import com.tdsops.tm.enums.domain.SettingType
import net.transitionmanager.domain.Setting
import net.transitionmanager.service.DatabaseMigrationService


/**
 * Modify the field specs to have mapping for bulk actions and service.
 */
databaseChangeLog = {

	changeSet(author: "tpelletier", id: "20180919 TM-12280-2") {
		comment("Update field specs to have bulk actions for string fields.")

		grailsChange {
			change {
				// List all the field settings for the different domains.
				List<Setting> fieldSettings = Setting.where {
					type == SettingType.CUSTOM_DOMAIN_FIELD_SPEC
				}.list()

				// This closure adds the service and actions to moveBundle.
				Closure changeScript = { settingsJson ->
					if (settingsJson.fields) {
						List<String> lookUpFields = [
							'appSource',
							'appFunction',
							'appTech',
							'appVendor',
							'appVersion',
							'businessUnit',
							'cart',
							'dbFormat',
							'description',
							'drRpoDesc',
							'drRtoDesc',
							'fileFormat',
							'license',
							'LUN',
							'os',
							'shelf',
							'supportType',
							'truck',
							'url',
							'useFrequency',
							'userCount',
							'userLocations'
						]
						//Adds the actions for string fields.
						def fields = settingsJson.fields.findAll { lookUpFields.contains(it.field) }

						if (fields) {
							fields.each { field ->
								field.bulkChangeActions = [
									'replace',
									'clear'
								]
							}
						}

						//Adds the clear action for string fields.
						fields = settingsJson.fields.findAll { ['shortName', 'ipAddress', 'serialNumber', 'externalRefId', 'assetTag'].contains(it.field) }

						if (fields) {
							fields.each { field ->
								field.bulkChangeActions = [
									'clear'
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