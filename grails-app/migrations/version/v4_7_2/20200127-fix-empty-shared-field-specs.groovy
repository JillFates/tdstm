package version.v4_7_2

import com.tdsops.tm.enums.domain.SettingType
import net.transitionmanager.common.DatabaseMigrationService
import net.transitionmanager.common.Setting

databaseChangeLog = {
    changeSet(author: "arecorrdon", id: "20200127 TM-16835-1") {
        comment('Update the Field Settings, setting the shared flag to 0 when null.')

        grailsChange {
            change {

                List<Setting> fieldSettings = Setting.where {
                    type == SettingType.CUSTOM_DOMAIN_FIELD_SPEC
                }.list()

                Closure changeScript = { settingsJson ->
                    for (Map field in settingsJson.fields) {
                        if (field.shared == null) {
                            field.shared = 0
                        }
                    }

                    return settingsJson
                }
                DatabaseMigrationService databaseMigrationService = ctx.getBean("databaseMigrationService")

                databaseMigrationService.updateJsonObjects(fieldSettings, "json", changeScript)

            }
        }
    }
}