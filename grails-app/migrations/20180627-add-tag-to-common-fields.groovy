import com.tdsops.tm.enums.domain.SettingType
import net.transitionmanager.common.Setting
import net.transitionmanager.service.DatabaseMigrationService


/**
 * Add the AssetClass field to the field settings for every project.
 */

databaseChangeLog = {

    changeSet(author: "tpelletier", id: "20180627 TM-10953-1") {
        comment("Add the Tags field to the field specs.")
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
                        'control': 'asset-tag-selector',
                        'default': '',
                        'field': 'tags',
                        'imp': 'N',
                        'label': 'Tags',
                        'order': 0,
                        'shared': 0,
                        'show': 1,
                        'tip': 'Tags',
                        'udf': 0
                ]

                // This closure adds the definition of the tags field to the settings for a given domain.
                Closure changeScript = { settingsJson ->
                    // Add the field to the list of fields for this domain
                    if(settingsJson.fields) {
                        settingsJson.fields << assetClassMap
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
