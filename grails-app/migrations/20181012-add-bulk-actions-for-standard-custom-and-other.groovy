import com.tdsops.tm.enums.domain.SettingType
import net.transitionmanager.domain.Setting
import net.transitionmanager.service.DatabaseMigrationService


/**
 * Modify the field specs to have mapping for bulk actions.
 */
databaseChangeLog = {

	changeSet(author: "tpelletier", id: "20180919 TM-12280-2") {
		comment("Update field specs to have bulk actions for validation, plans status, moveBundle, standard fields, string fields, and custom fields.")

		grailsChange {
			change {
				List<String> lookUpFields = [
					//string fileds
					'criticality',
					'latency',
					'maintExpDate',
					'railType',
					'rateOfChange',
					'retireDate',
					'shutdownDuration',
					'shutdownFixed',
					'size',
					'sourceBladePosition',
					'sourceRackPosition',
					'startupDuration',
					'startupFixed',
					'startupProc',
					'targetBladePosition',
					'targetRackPosition',
					'testingDuration',
					'testingFixed',
					'testProc',

					//standard fields
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

				// List all the field settings for the different domains.
				List<Setting> fieldSettings = Setting.where {
					type == SettingType.CUSTOM_DOMAIN_FIELD_SPEC
				}.list()

				// This closure adds the service and actions to moveBundle.
				Closure changeScript = { settingsJson ->
					if (settingsJson.fields) {
						//Adds the service and actions for validation and planStatus
						def fields = settingsJson.fields.findAll {
							it.field == "validation" ||
							it.field == "planStatus" ||
							it.field == "moveBundle" ||
							it.field.startsWith('custom') ||
							lookUpFields.contains(it.field)
						}

						if (fields) {
							fields.each { field ->
								field.bulkChangeActions = [
									'replace',
									'clear'
								]
							}
						}

						//Adds the clear action for string fields.
						fields = settingsJson.fields.findAll {
							['shortName', 'ipAddress', 'serialNumber', 'externalRefId', 'assetTag'].contains(it.field)
						}

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
