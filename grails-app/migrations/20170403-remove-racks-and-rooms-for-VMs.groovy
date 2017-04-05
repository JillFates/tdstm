import com.tds.asset.AssetType
import com.tdssrc.grails.WebUtil

/**
 * Deletes references to TBD racks and rooms for VMs and Blades.
 */
databaseChangeLog = {

    changeSet(author: "arecordon", id: "20170403 TM-5656 1") {
        comment('Deletes references to TBD racks and rooms for VMs and Blades.')

        grailsChange {
            change {
                List<String> assetTypes = AssetType.virtualServerTypes
                String assetTypesAsString = WebUtil.listOfStringsAsMultiValueString(assetTypes)
                String sqlStatement =
                        """UPDATE asset_entity
                            SET
                                room_source_id = NULL,
                                rack_source_id = NULL,
                                source_rack_position = NULL,
                                room_target_id = NULL,
                                rack_target_id = NULL,
                                target_rack_position = NULL
                            WHERE
                                asset_type IN (${assetTypesAsString});"""
                sql.executeUpdate(sqlStatement)
            }
        }
    }
}
