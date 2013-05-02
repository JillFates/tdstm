
/**
 *Add asset type in "eav_attribute_option" which exist in "asset_entity" table and "model" table 
 *but does not exist in  "eav_attribute_option" table
 */

databaseChangeLog = {
	
	changeSet(author: "lokanada", id: "20130502 TM-1881-1") {
		comment("""Add asset type in "eav_attribute_option" which exist in "asset_entity" table and "model" table 
					but does not exist in  "eav_attribute_option" table""")
		grailsChange {
			change {
				//Fetching list of assettypes from asset_entity and model tables .
				def entityAssetTypes = sql.rows("""SELECT asset_type AS assetType FROM model GROUP BY asset_type
												   UNION
												   SELECT asset_type AS assetType FROM asset_entity GROUP BY asset_type""")
				
				//Iterating over the list and inserting value in 'eav_attribute_option' table if asset type is not null and does not exist. .
				entityAssetTypes.assetType.each{ assetType->
					if(assetType){
						def attributeId =  sql.firstRow("SELECT attribute_id as aId FROM eav_attribute WHERE attribute_code='assetType'")
						def isTypeExist = sql.firstRow("""SELECT option_id AS optionId FROM eav_attribute_option 
											WHERE attribute_id = ${attributeId.aId}
											AND value=${assetType}""")
						
						if(!isTypeExist){
							sql.execute("INSERT INTO eav_attribute_option (attribute_id, sort_order, value) VALUES (${attributeId.aId}, '0', ${assetType})")
						}
					}
				}
			}
		}
	}
}