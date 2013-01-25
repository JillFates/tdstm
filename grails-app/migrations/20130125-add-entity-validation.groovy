/**
 * Inserting validation fields in eav_attribute and associated table for import and export .
 */
databaseChangeLog = {
	changeSet(author: "lokanada", id: "20130125 TM-1194") {
		comment('Add Validation to asset , db , files entity')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 'select count(*) from eav_attribute where attribute_code="validation" and entity_type_id = (select entity_type_id from eav_entity_type where domain_name ="AssetEntity")')
		}
		grailsChange {
			change {
				
				def mDataTransferSet = sql.firstRow("SELECT data_transfer_id as dataTransferId from data_transfer_set where set_code = 'MASTER'")
				def wDataTransferSet =sql.firstRow("SELECT data_transfer_id as dataTransferId from data_transfer_set where set_code = 'WALKTHROUGH'")
				
				def entityTypes = sql.rows("select entity_type_id as id from eav_entity_type where domain_name !='Application'")
				
				entityTypes.each{ typeId->
					sql.execute("""INSERT INTO eav_attribute (attribute_code,backend_type,default_value,entity_type_id,frontend_input,
                    				frontend_label,is_required,is_unique,note,sort_order,validation)
                                    VALUES ('validation','String','1',${typeId.id }, 'text', 'Validation',
                                    '0', '0','this field is used for just import','10','No validation');""")
					
					
					def attributeId = sql.firstRow("SELECT attribute_id as aId from eav_attribute WHERE entity_type_id = ${typeId.id} \
								 AND attribute_code='validation' ")

					def eavAttrSet = sql.firstRow("SELECT attribute_set_id as asId from eav_attribute_set \
								where entity_type_id = ${typeId.id} ")

					def eavEntityAttribute = sql.firstRow("SELECT entity_attribute_id as eaId from eav_entity_attribute \
						where attribute_id = ${attributeId.aId} AND eav_attribute_set_id = ${eavAttrSet.asId}")
						
					if(!eavEntityAttribute){
						sql.execute("""INSERT INTO eav_entity_attribute (attribute_id, eav_attribute_set_id, sort_order)
                                    VALUES (${attributeId.aId}, ${eavAttrSet.asId}, 10);""")
					}
					
					def masterDTAId = sql.firstRow("SELECT id as dtaId from data_transfer_attribute_map \
								where data_transfer_set_id = ${mDataTransferSet.dataTransferId} AND eav_attribute_id = ${attributeId.aId}")

					if( !masterDTAId ){
						sql.execute("""INSERT INTO data_transfer_attribute_map (column_name, data_transfer_set_id, eav_attribute_id, is_required,
										sheet_name, validation) VALUES ('Validation',${mDataTransferSet.dataTransferId}, ${attributeId.aId},'0',
										${typeId.id==1 ? 'Servers' : (typeId.id==3 ? 'Databases' : 'Files')}, 'NO Validation');""")
					}
					
					def walkThruDTAId = sql.firstRow("SELECT id as dtaId from data_transfer_attribute_map \
						where data_transfer_set_id = ${wDataTransferSet.dataTransferId} AND eav_attribute_id = ${attributeId.aId}")

					if( !walkThruDTAId ){
						sql.execute("""INSERT INTO data_transfer_attribute_map (column_name, data_transfer_set_id, eav_attribute_id, is_required,
												sheet_name, validation) VALUES ('Validation', ${wDataTransferSet.dataTransferId},
												${attributeId.aId},'0' ,${typeId.id==1 ? 'Servers' : (typeId.id==3 ? 'Databases' : 'Files')}, 'NO Validation');""")
					}
				 }
			}
		}
	}
}
