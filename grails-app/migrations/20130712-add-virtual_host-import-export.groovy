
/**
 * This changeset is used to Insert 'virtualHost' in  eav-attribute and associated tables for Import and Export .
 */
databaseChangeLog = {
	//Inserting 'virtualHost' in  eav-attribute and associated tables for Import and Export
	changeSet(author: "lokanada", id: "20130710 TM-1989-1") {
		comment(" Inserting 'virtualHost' in  eav-attribute and associated tables for Import and Export")
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', """select count(*) from eav_attribute where attribute_code="virtualHost"
						and entity_type_id = (select entity_type_id from eav_entity_type where domain_name ="AssetEntity ")""")
		}
		
		grailsChange {
			change {
				def mDataTransferSet = sql.firstRow("SELECT data_transfer_id as dataTransferId from data_transfer_set where set_code = 'MASTER'")
				def wDataTransferSet =sql.firstRow("SELECT data_transfer_id as dataTransferId from data_transfer_set where set_code = 'WALKTHROUGH'")
						
				def type = sql.firstRow("SELECT entity_type_id AS id,domain_name AS name FROM eav_entity_type WHERE domain_name ='AssetEntity' ")
				
				sql.execute("""INSERT INTO eav_attribute (attribute_code, backend_type, default_value, entity_type_id, frontend_input,
		                    	 frontend_label, is_required, is_unique, note, sort_order, validation)
		                         VALUES ('virtualHost', 'String', '1', ${type.id}, 'text', 'VirtualHost',
		                                    '0', '0','this field is used for just import','10','No validation');""")
	
			
				def attributeId = sql.firstRow("SELECT attribute_id as aId from eav_attribute \
							WHERE entity_type_id = ${type.id} AND attribute_code='virtualHost' ")
			
				def eavAttrSet = sql.firstRow("SELECT attribute_set_id as asId from eav_attribute_set \
							where entity_type_id = ${type.id} ")
			
				def eavEntityAttribute = sql.firstRow("SELECT entity_attribute_id as eaId from eav_entity_attribute \
					where attribute_id = ${attributeId.aId} AND eav_attribute_set_id = ${eavAttrSet.asId}")
					
				if(!eavEntityAttribute){
					sql.execute("""INSERT INTO eav_entity_attribute (attribute_id, eav_attribute_set_id, sort_order)
					                                    VALUES (${attributeId.aId}, ${eavAttrSet.asId}, 10)""")
				}
				
				def masterDTAId = sql.firstRow("SELECT id as dtaId from data_transfer_attribute_map \
							where data_transfer_set_id = ${mDataTransferSet.dataTransferId} AND eav_attribute_id = ${attributeId.aId}")
			
				if( !masterDTAId ){
					sql.execute("""INSERT INTO data_transfer_attribute_map (column_name, data_transfer_set_id, eav_attribute_id, is_required,
								sheet_name, validation) VALUES ('VirtualHost', ${mDataTransferSet.dataTransferId}, ${attributeId.aId},'0',
								'Servers' ,'NO Validation')""")
				}
					
				def walkThruDTAId = sql.firstRow("SELECT id as dtaId from data_transfer_attribute_map \
					where data_transfer_set_id = ${wDataTransferSet.dataTransferId} AND eav_attribute_id = ${attributeId.aId}")
			
				if( !walkThruDTAId ){
					sql.execute("""INSERT INTO data_transfer_attribute_map (column_name, data_transfer_set_id, eav_attribute_id, is_required,
									sheet_name, validation) VALUES ('VirtualHost', ${wDataTransferSet.dataTransferId},
									${attributeId.aId}, '0', 'Servers', 'NO Validation')""")
				}
			}
		}
	}
}
