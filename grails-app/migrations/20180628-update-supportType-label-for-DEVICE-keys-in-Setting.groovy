import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.SettingType
import net.transitionmanager.common.Setting
import net.transitionmanager.service.DatabaseMigrationService
import org.grails.web.json.JSONObject

databaseChangeLog = {
	changeSet(author: 'dcorrea', id: 'TM-11229') {
		comment('Change Device.SupportType label for Device.Support label')
		grailsChange {
			change {
				// Fetch all the existing settings
				List<Setting> settingList = Setting.findAllByTypeAndKey(SettingType.CUSTOM_DOMAIN_FIELD_SPEC, AssetClass.DEVICE)

				// Script that will iterate over the list of fields of a Setting and replace 'SupportType' for 'Support' in field 'supportType'
				Closure updateSettingScript = { JSONObject settingJson ->
					for (field in settingJson.fields) {
						if(field.field == 'supportType'){
							field.label = 'Support'
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

