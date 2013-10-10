/**
 * This changelog will refactor the properties into the base domain.
 * Presently the Database and Files domains contain separate size and unit properties. 
 * Since size is going to be common across Devices, Storage, Databases and Network classes
 */
databaseChangeLog = {
	// This Changeset is used to add column size to table asset_entity
	changeSet(author: "lokanada", id: "20131004 TM-2301-1") {
		comment("""
                    Update AssetEntity:
                        add column size (INT 10) Nullable
                    Update AssetEntity.size and scale for Files domain size=fileSize
                    Update AssetEntity.size and scale for Database domain size=dbSize
        """)
		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(schemaName:'tdstm', tableName:'asset_entity', columnName:'size' )
			}
		}
		sql("ALTER TABLE tdstm.asset_entity ADD COLUMN size INT(10);")
		sql("UPDATE tdstm.asset_entity a RIGHT JOIN files f ON a.asset_entity_id = f.files_id SET a.size = f.file_size;")
        sql("UPDATE asset_entity a RIGHT JOIN data_base d ON a.asset_entity_id = d.db_id SET a.size = d.db_size;")
		// Below columns should be dropped later once everything works fine.
		sql("ALTER TABLE tdstm.data_base MODIFY COLUMN db_size INT(11) DEFAULT NULL")
		sql("ALTER TABLE tdstm.files MODIFY COLUMN file_size INT(11) DEFAULT NULL")
	}
    
    // This Changeset is used to add column scale to table asset_entity
    changeSet(author: "lokanada", id: "20131004 TM-2301-2") {
        comment("""
                    Update AssetEntity:
                        add column scale CHAR(2) Nullable
                    Update AssetEntity.size and scale for Files domain scale=sizeUnit
        """)
        preConditions(onFail:'MARK_RAN') {
            not {
                columnExists(schemaName:'tdstm', tableName:'asset_entity', columnName:'scale' )
            }
        }
        sql("ALTER TABLE tdstm.asset_entity ADD COLUMN scale CHAR(2)")
        sql("UPDATE tdstm.asset_entity a RIGHT JOIN files f ON a.asset_entity_id = f.files_id SET a.scale = f.size_unit;")
    }
    
    // This Changeset is used to add column rate_of_change to table asset_entity
    changeSet(author: "lokanada", id: "20131004 TM-2301-3") {
        comment(" Update AssetEntity: add column rateOfChange TINYINT(3) Nullable")
        preConditions(onFail:'MARK_RAN') {
            not {
                columnExists(schemaName:'tdstm', tableName:'asset_entity', columnName:'rate_of_change' )
            }
        }
        sql("ALTER TABLE tdstm.asset_entity ADD COLUMN rate_of_change TINYINT(3);")
    }
    
    // This Changeset is used to update data_transfer_attribute_map column column_name with FileSize with Size, DBSize with Size
    // Update eav_attribute.attribute_code with fileSize to size and bdSize to size
    changeSet(author: "lokanada", id: "20131004 TM-2301-4") {
        comment(""" Update eav_attribute.attribute_code with fileSize to size and bdSize to size and 
                data_transfer_attribute_map column column_name with Filesize with Size, DBSize with Size
            """)
        sql("UPDATE data_transfer_attribute_map SET column_name='Size' where column_name in ('DBSize', 'FileSize');")
        sql("UPDATE eav_attribute SET attribute_code='size' where attribute_code in ('dbSize', 'fileSize');")
    }
	
	/**
	 * Inserting scale,rateOFChange fields in eav_attribute and associated table for import and export .
	 */
	changeSet(author: "lokanada", id: "20131008 TM-2301-5") {
		comment('Add scale,rateOFChange to asset, db , files entity')
		// Skipped pre-condition as we need to do multiple cheks .
		grailsChange {
			change {
				def entityTypes = sql.rows(
									"""select entity_type_id as id , entity_type_code as type 
										from eav_entity_type where domain_name != 'Application';
									""")
				def mDataTransferSet = sql.firstRow(
										"""SELECT data_transfer_id as dataTransferId from data_transfer_set 
											where set_code = 'MASTER';
										""")
				def wDataTransferSet =sql.firstRow(
										"""SELECT data_transfer_id as dataTransferId from data_transfer_set 
											where set_code = 'WALKTHROUGH';
										""")
				def columnField=['size':'Size', 'scale':'Scale', 'rateOfChange':'Rate Of Change']
				entityTypes.each{ entityType->
					columnField.each{ key ,value ->
						def fieldExist = sql.firstRow(
											"""select count(*) as counts from eav_attribute where attribute_code= ${key}
												and entity_type_id = (select entity_type_id from eav_entity_type 
												where entity_type_code = ${entityType.type})
											""")
						if(fieldExist.counts == 0){
							sql.execute(
								"""INSERT INTO eav_attribute (attribute_code,backend_type,default_value,entity_type_id,
									frontend_input, frontend_label,is_required,is_unique,note,sort_order,validation)
                                    VALUES (${key},${key!= 'scale' ? 'int' : 'String'},'1',${entityType.id}, 
									'text', ${value},'0', '0','this field is used for just import','10','No validation');
								""")


							def attributeId = sql.firstRow(
												"""SELECT attribute_id as aId from eav_attribute 
													WHERE entity_type_id = ${entityType.id} AND attribute_code=${key};
												""")

							def eavAttrSet = sql.firstRow("SELECT attribute_set_id as asId from eav_attribute_set \
											where entity_type_id = ${entityType.id} ")

							def eavEntityAttribute = sql.firstRow(
														"""SELECT entity_attribute_id as eaId from eav_entity_attribute \
															WHERE attribute_id = ${attributeId.aId} 
															AND eav_attribute_set_id = ${eavAttrSet.asId};
														""")

							if(!eavEntityAttribute){
								sql.execute(
									"""INSERT INTO eav_entity_attribute (attribute_id, eav_attribute_set_id, sort_order)
										VALUES (${attributeId.aId}, ${eavAttrSet.asId}, 10);
									""")
							}

							def masterDTAId = sql.firstRow("SELECT id as dtaId from data_transfer_attribute_map \
											where data_transfer_set_id = ${mDataTransferSet.dataTransferId} AND eav_attribute_id = ${attributeId.aId}")

							if( !masterDTAId ){
								sql.execute(
									"""INSERT INTO data_transfer_attribute_map (column_name, data_transfer_set_id, eav_attribute_id, is_required,
										sheet_name, validation) VALUES (${value},${mDataTransferSet.dataTransferId}, ${attributeId.aId},'0',
										${entityType.id==1 ? 'Servers' : (entityType.id==3 ? 'Databases' : 'Files')},
										'NO Validation');
								""")
							}

							def walkThruDTAId = sql.firstRow("SELECT id as dtaId from data_transfer_attribute_map \
									where data_transfer_set_id = ${wDataTransferSet.dataTransferId} AND eav_attribute_id = ${attributeId.aId}")

							if( !walkThruDTAId ){
								sql.execute(
									"""INSERT INTO data_transfer_attribute_map (column_name, data_transfer_set_id, eav_attribute_id, is_required,
										sheet_name, validation) VALUES (${value}, ${wDataTransferSet.dataTransferId},
										${attributeId.aId},'0', ${entityType.id==1 ? 'Servers' : (entityType.id==3 ? 'Databases' : 'Files')},
										'NO Validation');
									""")
							}
						}
					}
				}
			}
		}
	}
    
    // Update eav_attribute.frontend_label with fileSize to size and bdSize to Size
    changeSet(author: "lokanada", id: "20131004 TM-2301-6") {
        comment("Update eav_attribute frontend_label to Size")
        sql("UPDATE eav_attribute set frontend_label = 'Size' where frontend_label in ('DBSize', 'FileSize');")
    }
	
	//TODO :@John, enable below changeSets after ensuring the data transferred to new columns.
	/*// This set of Database change to drop dbSize column from database table.
	changeSet(author: "lokanada", id: "20131010 TM-2301-7") {
		comment('Drop "dbSize" column from the data_base table')
		preConditions(onFail:'MARK_RAN') {
			columnExists(schemaName:'tdstm', tableName:'data_base', columnName:'db_size' )
		}
		dropColumn(tableName:'data_base', columnName:'db_size')
	}
	
	// This set of Database change to drop fileSize column from files table.
	changeSet(author: "lokanada", id: "20131010 TM-2301-8") {
		comment('Drop "fileSize" column from the files table')
		preConditions(onFail:'MARK_RAN') {
			columnExists(schemaName:'tdstm', tableName:'files', columnName:'file_size' )
		}
		dropColumn(tableName:'files', columnName:'file_size')
	}*/
}