import com.tdsops.tm.enums.domain.SettingType
import net.transitionmanager.domain.Setting
import net.transitionmanager.service.DatabaseMigrationService


/**
 * Add the AssetClass field to the field settings for every project.
 */

databaseChangeLog = {

    changeSet(author: "arecordon", id: "20171026 TM-7397-1") {
        comment("Add the Asset Class field to the field specs.")
        grailsChange {
            change {
                // List all the field settings for the different domains.
                List<Setting> fieldSettings = Setting.where {
                    type == SettingType.CUSTOM_DOMAIN_FIELD_SPEC
                }.list()

                // Asset Class field definition
                Map assetClassMap = [
                        'constraints': [
                                'required': 1
                        ],
                        'control': 'String',
                        'default': '',
                        'field': 'assetClass',
                        'imp': 'N',
                        'label': 'Asset Class',
                        'order': 0,
                        'shared': 0,
                        'show': 0,
                        'tip': '',
                        'udf': 0
                ]

                // This closure adds the definition of the Asset Class field to the settings for a given domain.
                Closure changeScript = { settingsJson ->
                    // Add the field to the list of fields for this domain
                    settingsJson.fields << assetClassMap
                    return settingsJson
                }


                DatabaseMigrationService databaseMigrationService = ctx.getBean("databaseMigrationService")
                // Update the different settings with the Asset Class fields.
                databaseMigrationService.updateJsonObjects(fieldSettings, "json", changeScript)

            }
        }
    }

}
