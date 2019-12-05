package version.v4_7_1

import com.tdsops.tm.enums.domain.SettingType
import net.transitionmanager.common.Setting
import net.transitionmanager.common.DatabaseMigrationService

databaseChangeLog = {
	changeSet(author: "arecordon", id: "20191030 TM-16274-1") {
		comment('Remove Plan Methodology from the Field Specs for Applications.')

		grailsChange {
		    change {
		        List<Setting> fieldSettings = Setting.where {
		            type == SettingType.CUSTOM_DOMAIN_FIELD_SPEC
		            key == 'APPLICATION'
                }.list()

                Closure changeScript = { settingsJson ->
                    settingsJson.remove('planMethodology')
                    return settingsJson
                }

                DatabaseMigrationService databaseMigrationService = ctx.getBean("databaseMigrationService")
                databaseMigrationService.updateJsonObjects(fieldSettings, "json", changeScript)
		    }
		}
	}

}
