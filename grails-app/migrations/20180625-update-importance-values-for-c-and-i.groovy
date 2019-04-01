import net.transitionmanager.common.Setting
import net.transitionmanager.service.DatabaseMigrationService
import org.grails.web.json.JSONObject

databaseChangeLog = {
	changeSet(author: 'arecordon', id: 'TM-8090-1') {
		comment("Change the importance values C and I to Y and G respectively.")
		grailsChange {
			change {
				// Fetch all the existing settings
				List<Setting> settingList = Setting.list()
				// Map with the replacements.
				Map<String, String> replacementsMap = [
				    "C": "Y",
					"I": "G"
				]
				// Old values that need to be replaced.
				Set<String> replacementKeys = replacementsMap.keySet()

				// Script that will iterate over the list of fields of a Setting and replace the importance if needed.
				Closure updateSettingScript = { JSONObject settingJson ->
					for (field in settingJson.fields) {
						String importance = field.imp
						if (replacementKeys.contains(importance)) {
							field.imp = replacementsMap[importance]
						}
					}
					return settingJson
				}

				DatabaseMigrationService databaseMigrationService = ctx.getBean("databaseMigrationService")

				databaseMigrationService.updateJsonObjects(settingList, "json", updateSettingScript)
			}
		}

	}
}

