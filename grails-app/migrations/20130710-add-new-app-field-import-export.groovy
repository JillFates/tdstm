
databaseChangeLog = {

	/* this changeSet will add 'shutdown_duration', 'startup_duration', 'testing_duration', 'shutdown_by', 'startup_by', 'testing_by', 'shutdown_fixed',
	   'startup_fixed', 'testing_fixed' in eav_attribute and associated table for import and export.  */
	changeSet(author: "lokanada", id: "20130710 TM-1981-3") {
		comment(""" Inserting 'shutdown_duration', 'startup_duration', 'testing_duration', 'shutdown_by', 'startup_by', 'testing_by', 'shutdown_fixed',
					'startup_fixed', 'testing_fixed' in  data_transfer_attribute_map  """)
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', """select count(*) from eav_attribute where attribute_code in
						("shutdownDuration","startupDuration","testingDuration","shutdownBy","startupBy",
							"testingBy","shutdownFixed","startupFixed","testingFixed") 
						and entity_type_id = (select entity_type_id from eav_entity_type where domain_name ="Application"  )""")
		}
		
		grailsChange {
			change {
				def mDataTransferSet = sql.firstRow("SELECT data_transfer_id as dataTransferId from data_transfer_set where set_code = 'MASTER'")
				def wDataTransferSet =sql.firstRow("SELECT data_transfer_id as dataTransferId from data_transfer_set where set_code = 'WALKTHROUGH'")
						
				def entityType = sql.firstRow("SELECT entity_type_id AS id FROM eav_entity_type WHERE entity_type_code='Application'")
				def columnField=['shutdownDuration':['ShutdownDuration', 'int'], 'startupDuration':['StartupDuration', 'int'], 'testingDuration':['TestingDuration', 'int'],
								 'shutdownBy':['ShutdownBy', 'String'], 'startupBy':['StartupBy', 'String'], 'testingBy':['TestingBy', 'String'],
								 'shutdownFixed':['ShutdownFixed', 'int'], 'startupFixed':['StartupFixed', 'int'], 'testingFixed':['TestingFixed', 'int']]
				
				columnField.each{ key ,value->
					sql.execute("""INSERT INTO eav_attribute (attribute_code, backend_type, default_value, entity_type_id, frontend_input,
			                    	 frontend_label, is_required, is_unique, note, sort_order, validation)
			                         VALUES (${key}, ${value[1]}, '1', ${entityType.id}, 'text', ${value[0]},
			                                    '0', '0','this field is used for just import','10','No validation');""")
		
				
					def attributeId = sql.firstRow("SELECT attribute_id as aId from eav_attribute WHERE entity_type_id = ${entityType.id} \
								 AND attribute_code=${key} ")

					def eavAttrSet = sql.firstRow("SELECT attribute_set_id as asId from eav_attribute_set \
								where entity_type_id = ${entityType.id} ")
				
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
											sheet_name, validation) VALUES (${value[0]},${mDataTransferSet.dataTransferId}, ${attributeId.aId},'0',
											'Applications','NO Validation')""")
					}
					
					def walkThruDTAId = sql.firstRow("SELECT id as dtaId from data_transfer_attribute_map \
						where data_transfer_set_id = ${wDataTransferSet.dataTransferId} AND eav_attribute_id = ${attributeId.aId}")
				
					if( !walkThruDTAId ){
						sql.execute("""INSERT INTO data_transfer_attribute_map (column_name, data_transfer_set_id, eav_attribute_id, is_required,
										sheet_name, validation) VALUES (${value[0]}, ${wDataTransferSet.dataTransferId},
										${attributeId.aId}, '0', 'Applications', 'NO Validation')""")
					}
				}
							
			}
		}
	}
	
	//this changeSet will add 'external_ref_id' column in data_transfer_attribute_map and related tables for import and export.
	changeSet(author: "lokanada", id: "20130710 TM-1981-4") {
		comment(" inserting 'external_ref_id' in  data_transfer_attribute_map ")
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', """select count(*) from eav_attribute where attribute_code="externalRefId"
						and entity_type_id = (select entity_type_id from eav_entity_type where domain_name ="AssetEntity ")""")
		}
		
		grailsChange {
			change {
				def mDataTransferSet = sql.firstRow("SELECT data_transfer_id as dataTransferId from data_transfer_set where set_code = 'MASTER'")
				def wDataTransferSet =sql.firstRow("SELECT data_transfer_id as dataTransferId from data_transfer_set where set_code = 'WALKTHROUGH'")
						
				def entityType = sql.rows("select entity_type_id as id,domain_name as name from eav_entity_type")
				
				entityType.each{type ->
					sql.execute("""INSERT INTO eav_attribute (attribute_code, backend_type, default_value, entity_type_id, frontend_input,
			                    	 frontend_label, is_required, is_unique, note, sort_order, validation)
			                         VALUES ('externalRefId', 'String', '1', ${type.id}, 'text', 'ExternalRefId',
			                                    '0', '0','this field is used for just import','10','No validation');""")
		
				
					def attributeId = sql.firstRow("SELECT attribute_id as aId from eav_attribute \
								WHERE entity_type_id = ${type.id} AND attribute_code='externalRefId' ")
				
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
									sheet_name, validation) VALUES ('ExternalRefId', ${mDataTransferSet.dataTransferId}, ${attributeId.aId},'0',
									${type.id==1 ? 'Servers' : (type.id==2 ? 'Applications' : (type.id==3 ? 'Databases' : 'Files'))},'NO Validation')""")
}
					
					def walkThruDTAId = sql.firstRow("SELECT id as dtaId from data_transfer_attribute_map \
						where data_transfer_set_id = ${wDataTransferSet.dataTransferId} AND eav_attribute_id = ${attributeId.aId}")
				
					if( !walkThruDTAId ){
						sql.execute("""INSERT INTO data_transfer_attribute_map (column_name, data_transfer_set_id, eav_attribute_id, is_required,
										sheet_name, validation) VALUES ('ExternalRefId', ${wDataTransferSet.dataTransferId},
										${attributeId.aId}, '0', ${type.id==1 ? 'Servers' : (type.id==2 ? 'Applications' : (type.id==3 ? 'Databases' : 'Files'))},
										'NO Validation')""")
					}
				}
			}
		}
	}
	
}
