import com.tdsops.tm.enums.domain.SettingType
import net.transitionmanager.domain.Setting
import net.transitionmanager.service.DatabaseMigrationService


/**
 * Modify scale fields control type to inList.
 */
databaseChangeLog = {

    changeSet(author: "ecantu", id: "20190327 TM-13502") {
        comment("Update scale field to have a control of inList type.")

        grailsChange {
            change {
                // List all the field settings for the different domains.
                List<Setting> fieldSettings = Setting.where {
                    type == SettingType.CUSTOM_DOMAIN_FIELD_SPEC
                }.list()

                Closure changeScript = { settingsJson ->
                    // Get all 'scale' field specs
                    if (settingsJson.fields) {
                        def mapForScaleField = settingsJson.fields.findAll { it.field == 'scale' }

                        // For the scale fields, change control type to 'inList'
                        mapForScaleField.each { Map map ->
                            map.control = "inList"
                        }
                    }
                    return settingsJson
                }
                DatabaseMigrationService databaseMigrationService = ctx.getBean("databaseMigrationService")
                // Update settings
                databaseMigrationService.updateJsonObjects(fieldSettings, "json", changeScript)

            }
        }
    }
}
