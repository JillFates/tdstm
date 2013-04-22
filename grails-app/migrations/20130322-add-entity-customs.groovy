/**
 * Inserting custom(9..24) fields in eav_attribute and associated table for import and export .
 */
databaseChangeLog = {
	changeSet(author: "lokanada", id: "20130322 TM-1263-5") {
		comment('Add customs (9..24) to asset, apps, db , files entity')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 'select count(*) from eav_attribute where attribute_code="custom9" and entity_type_id = (select entity_type_id from eav_entity_type where domain_name ="AssetEntity")')
		}
		grailsChange {
			change {
				
				def mDataTransferSet = sql.firstRow("SELECT data_transfer_id as dataTransferId from data_transfer_set where set_code = 'MASTER'")
				def wDataTransferSet =sql.firstRow("SELECT data_transfer_id as dataTransferId from data_transfer_set where set_code = 'WALKTHROUGH'")
				
				def entityTypes = sql.rows("select entity_type_id as id from eav_entity_type")
				
				entityTypes.each{ typeId->
					 (9..24).each{i->
							def customField = "custom"+i
							def customLabel = "Custom"+i
							sql.execute("""INSERT INTO eav_attribute (attribute_code,backend_type,default_value,entity_type_id,frontend_input,
		                    				frontend_label,is_required,is_unique,note,sort_order,validation)
		                                    VALUES (${customField},'String','1',${typeId.id}, 'text', ${customLabel},
		                                    '0', '0','this field is used for just import','10','No validation');""")
							
							
							def attributeId = sql.firstRow("SELECT attribute_id as aId from eav_attribute WHERE entity_type_id = ${typeId.id} \
										 AND attribute_code=${customField} ")
		
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
												sheet_name, validation) VALUES (${customLabel},${mDataTransferSet.dataTransferId}, ${attributeId.aId},'0',
												${typeId.id==1 ? 'Servers' : (typeId.id== 2 ? 'Application' : (typeId.id==3 ? 'Databases' : 'Files'))},
												'NO Validation');""")
							}
							
							def walkThruDTAId = sql.firstRow("SELECT id as dtaId from data_transfer_attribute_map \
								where data_transfer_set_id = ${wDataTransferSet.dataTransferId} AND eav_attribute_id = ${attributeId.aId}")
		
							if( !walkThruDTAId ){
								sql.execute("""INSERT INTO data_transfer_attribute_map (column_name, data_transfer_set_id, eav_attribute_id, is_required,
											sheet_name, validation) VALUES (${customLabel}, ${wDataTransferSet.dataTransferId},
											${attributeId.aId},'0', ${typeId.id==1 ? 'Servers' : (typeId.id== 2 ? 'Application' : (typeId.id==3 ? 'Databases' : 'Files'))},
											'NO Validation');""")
							}
					 }
			   }
			}
		}
	}
    /*
     *While inserting custom (9..24) sheet_name updated as 'Application' instead of 'Applications'
     */
    changeSet(author: "lokanada", id: "20130325 TM-1263-6") {
        comment("Set the sheet_name='Applications'  where it is 'Application'")
        sql("UPDATE data_transfer_attribute_map SET sheet_name='Applications' WHERE sheet_name = 'Application'")
    }
	
	/**
	 * Inserting custom(1..8) fields in eav_attribute and associated table for import and export .
	 */
	changeSet(author: "lokanada", id: "20130422 TM-1867") {
		comment('Add customs (1..8) columns to apps, db , files entity')
		// Skipped pre-condition as we need to do multiple cheks .
		grailsChange {
			change {
				
				def entityTypes = sql.rows("select entity_type_id as id , entity_type_code as type from eav_entity_type where domain_name != 'AssetEntity'")
				def mDataTransferSet = sql.firstRow("SELECT data_transfer_id as dataTransferId from data_transfer_set where set_code = 'MASTER'")
				def wDataTransferSet =sql.firstRow("SELECT data_transfer_id as dataTransferId from data_transfer_set where set_code = 'WALKTHROUGH'")
				
				entityTypes.each{ typeId->
					 (1..8).each{i->
							def customField = "custom"+i
							def customLabel = "Custom"+i
							def fieldExist = sql.firstRow("""select count(*) as counts from eav_attribute where attribute_code= ${customField}
								and entity_type_id = (select entity_type_id from eav_entity_type where entity_type_code = ${typeId.type})""")
							if(fieldExist.counts == 0){
								sql.execute("""INSERT INTO eav_attribute (attribute_code,backend_type,default_value,entity_type_id,frontend_input,
			                    				frontend_label,is_required,is_unique,note,sort_order,validation)
			                                    VALUES (${customField},'String','1',${typeId.id}, 'text', ${customLabel},
			                                    '0', '0','this field is used for just import','10','No validation');""")
								
								
								def attributeId = sql.firstRow("SELECT attribute_id as aId from eav_attribute WHERE entity_type_id = ${typeId.id} \
											 AND attribute_code=${customField} ")
			
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
													sheet_name, validation) VALUES (${customLabel},${mDataTransferSet.dataTransferId}, ${attributeId.aId},'0',
													${typeId.type== 'Application' ? 'Applications' : (typeId.type=='Database' ? 'Databases' : 'Files')},
													'NO Validation');""")
								}
								
								def walkThruDTAId = sql.firstRow("SELECT id as dtaId from data_transfer_attribute_map \
									where data_transfer_set_id = ${wDataTransferSet.dataTransferId} AND eav_attribute_id = ${attributeId.aId}")
			
								if( !walkThruDTAId ){
									sql.execute("""INSERT INTO data_transfer_attribute_map (column_name, data_transfer_set_id, eav_attribute_id, is_required,
												sheet_name, validation) VALUES (${customLabel}, ${wDataTransferSet.dataTransferId},
												${attributeId.aId},'0', ${typeId.type== 'Application' ? 'Applications' : (typeId.type=='Database' ? 'Databases' : 'Files')},
												'NO Validation');""")
								}
							}
					 	}
					}
				}
			}
		}
	
	/**
	 * Inserting url field in eav_attribute and associated table for import and export .
	 */
	changeSet(author: "lokanada", id: "20130422 TM-1867-1") {
		comment('Add url column to apps')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 'select count(*) from eav_attribute where attribute_code="url" and entity_type_id = (select entity_type_id from eav_entity_type where domain_name ="Application")')
		}
		grailsChange {
			change {
				
				def mDataTransferSet = sql.firstRow("SELECT data_transfer_id as dataTransferId from data_transfer_set where set_code = 'MASTER'")
				def wDataTransferSet =sql.firstRow("SELECT data_transfer_id as dataTransferId from data_transfer_set where set_code = 'WALKTHROUGH'")
				
				def entityType = sql.firstRow("select entity_type_id as id , entity_type_code as type from eav_entity_type where domain_name = 'Application'")
				
				sql.execute("""INSERT INTO eav_attribute (attribute_code,backend_type,default_value,entity_type_id,frontend_input,
		                    				frontend_label,is_required,is_unique,note,sort_order,validation)
		                                    VALUES ('url','String','1',${entityType.id}, 'text', 'Url',
		                                    '0', '0','this field is used for just import','10','No validation');""")
				
				def attributeId = sql.firstRow("SELECT attribute_id as aId from eav_attribute WHERE entity_type_id = ${entityType.id} \
					AND attribute_code='url' ")

			    def eavAttrSet = sql.firstRow("SELECT attribute_set_id as asId from eav_attribute_set \
						   where entity_type_id = ${entityType.id} ")
		
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
														sheet_name, validation) VALUES ('Url',${mDataTransferSet.dataTransferId}, ${attributeId.aId},'0',
														'Applications','NO Validation');""")
				}
				
				def walkThruDTAId = sql.firstRow("SELECT id as dtaId from data_transfer_attribute_map \
					where data_transfer_set_id = ${wDataTransferSet.dataTransferId} AND eav_attribute_id = ${attributeId.aId}")
		
				if( !walkThruDTAId ){
					sql.execute("""INSERT INTO data_transfer_attribute_map (column_name, data_transfer_set_id, eav_attribute_id, is_required,
													sheet_name, validation) VALUES ('Url', ${wDataTransferSet.dataTransferId},
													${attributeId.aId},'0', 'Applications','NO Validation');""")
				}
			}
		}
	}
}
