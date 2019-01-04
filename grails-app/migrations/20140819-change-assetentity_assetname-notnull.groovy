/**
 * This migration script changes the AssetEntity.assetName to be non-null
 */

databaseChangeLog = {
	changeSet(author: "jmartin", id: "20140819 TM-3155-1") {
		comment('Change the AssetEntity.assetName to be non-null')

		// Update any existing assets names to 'Unknown-ID#' if it is not set 
		sql("UPDATE asset_entity SET asset_name=concat('Unknown-',asset_entity_id) WHERE COALESCE(asset_name,'') = ''")

		sql('ALTER TABLE `asset_entity` CHANGE COLUMN `asset_name` `asset_name` VARCHAR(255) NOT NULL')
	}
}
