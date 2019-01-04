import com.tdsops.tm.enums.domain.SettingType
import net.transitionmanager.domain.Setting
import net.transitionmanager.service.DatabaseMigrationService


/**
 * Modify the 'tags' field name to 'tagAssets' on the field settings table for every project.
 */

databaseChangeLog = {

    changeSet(author: "ecantu", id: "20180627 TM-11518") {
        comment("Update the field name from 'tags' to 'tagAssets'.")

        grailsChange {
            change {
                // List all the field settings for the different domains.
                List<Setting> fieldSettings = Setting.where {
                    type == SettingType.CUSTOM_DOMAIN_FIELD_SPEC
                }.list()

                // This closure replace changes the 'tags' field (if exists) to 'tagAssets'.
                Closure changeScript = { settingsJson ->
                    // Add the field to the list of fields for this domain
                    if(settingsJson.fields) {
                        def mapForTags = settingsJson.fields.find { it.field == "tags"}
                        if (mapForTags) {
                            mapForTags.field = "tagAssets"
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
