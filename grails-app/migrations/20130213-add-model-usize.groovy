/**
 * Inserting usize fields in eav_attribute and associated table for import and export .
 */
databaseChangeLog = {
	changeSet(author: "lokanada", id: "20130213 TM-1204") {
		comment('Add usize to eav_attribute')
		grailsChange {
			change {
				
				def mDataTransferSet = sql.firstRow("SELECT data_transfer_id as dataTransferId from data_transfer_set where set_code = 'MASTER'")
				def wDataTransferSet =sql.firstRow("SELECT data_transfer_id as dataTransferId from data_transfer_set where set_code = 'WALKTHROUGH'")
				
				def entityType = sql.firstRow("select entity_type_id as id from eav_entity_type where domain_name ='AssetEntity'")
				// find eav_attribute id for usize
				def attributeId = sql.firstRow("SELECT attribute_id as aId from eav_attribute WHERE entity_type_id = ${entityType.id} \
							 AND attribute_code='usize' ")
				
                // Create eav_attribute is not exists
				if( !attributeId ){
					sql.execute("""INSERT INTO eav_attribute (attribute_code,backend_type,default_value,entity_type_id,frontend_input,
                    				frontend_label,is_required,is_unique,note,sort_order,validation)
                                    VALUES ('usize','String','1',${entityType.id }, 'text', 'Usize',
                                    '0', '0','this field is used for just import','347','No validation');""")
					
					attributeId = sql.firstRow("SELECT attribute_id as aId from eav_attribute WHERE entity_type_id = ${entityType.id} \
									AND attribute_code='usize' ")
				}


				def eavAttrSet = sql.firstRow("SELECT attribute_set_id as asId from eav_attribute_set \
							where entity_type_id = ${entityType.id} ")

				def eavEntityAttribute = sql.firstRow("SELECT entity_attribute_id as eaId from eav_entity_attribute \
					where attribute_id = ${attributeId.aId} AND eav_attribute_set_id = ${eavAttrSet.asId}")
				
                // Insert into eav_entity_attribute
				if(!eavEntityAttribute){
					sql.execute("""INSERT INTO eav_entity_attribute (attribute_id, eav_attribute_set_id, sort_order)
                                VALUES (${attributeId.aId}, ${eavAttrSet.asId}, 10);""")
				}
				
                // Add data_transfer_attribute_map record for eav_attribute 
				def masterDTAId = sql.firstRow("SELECT id as dtaId from data_transfer_attribute_map \
							where data_transfer_set_id = ${mDataTransferSet.dataTransferId} AND eav_attribute_id = ${attributeId.aId}")

				if( !masterDTAId ){
					sql.execute("""INSERT INTO data_transfer_attribute_map (column_name, data_transfer_set_id, eav_attribute_id, is_required,
									sheet_name, validation) VALUES ('usize',${mDataTransferSet.dataTransferId}, ${attributeId.aId},'0',
									'Servers', 'NO Validation');""")
				}
				
				def walkThruDTAId = sql.firstRow("SELECT id as dtaId from data_transfer_attribute_map \
					where data_transfer_set_id = ${wDataTransferSet.dataTransferId} AND eav_attribute_id = ${attributeId.aId}")

				if( !walkThruDTAId ){
					sql.execute("""INSERT INTO data_transfer_attribute_map (column_name, data_transfer_set_id, eav_attribute_id, is_required,
											sheet_name, validation) VALUES ('usize', ${wDataTransferSet.dataTransferId},
											${attributeId.aId},'0' ,'Servers', 'NO Validation');""")
				}
			}
		}
	}
}
