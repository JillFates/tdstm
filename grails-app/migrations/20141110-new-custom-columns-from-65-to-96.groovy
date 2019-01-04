databaseChangeLog = {
	
	//This changeset will add customs 49 to 64 fileds in project table .
	changeSet(author: "dscarpa", id: "20141110 TM-3541-1") {
		comment('Add "Customs (65 - 96)" column to project table')
		
			preConditions(onFail:'MARK_RAN') {
				not {
					columnExists(schemaName:'tdstm', tableName:'project', columnName:'custom65' )
				}
			}
			sql("""
				    ALTER TABLE `tdstm`.`project`
						ADD (   `custom65` varchar(255),
								`custom66` varchar(255), 
								`custom67` varchar(255),
								`custom68` varchar(255), 
								`custom69` varchar(255),

								`custom70` varchar(255),
								`custom71` varchar(255), 
								`custom72` varchar(255),
								`custom73` varchar(255),
								`custom74` varchar(255),
						  		`custom75` varchar(255),
					      		`custom76` varchar(255),
						  		`custom77` varchar(255),
						  		`custom78` varchar(255),
						 		`custom79` varchar(255),

								`custom80` varchar(255), 
								`custom81` varchar(255),
								`custom82` varchar(255), 
								`custom83` varchar(255),
								`custom84` varchar(255),
								`custom85` varchar(255), 
								`custom86` varchar(255),
								`custom87` varchar(255),
								`custom88` varchar(255),
						  		`custom89` varchar(255),

					      		`custom90` varchar(255),
						  		`custom91` varchar(255),
						  		`custom92` varchar(255),
						 		`custom93` varchar(255),
								`custom94` varchar(255), 
								`custom95` varchar(255),
						  		`custom96` varchar(255) );
				""")
	}
	//This changeset will add customs 49 to 64 fileds in asset_entity table .
	changeSet(author: "dscarpa", id: "20141110 TM-3541-2") {
		comment('Add "Customs (65 - 96)" column to asset_entity table')
		
			preConditions(onFail:'MARK_RAN') {
				not {
					columnExists(schemaName:'tdstm', tableName:'asset_entity', columnName:'custom65' )
				}
			}
			sql("""
					ALTER TABLE `tdstm`.`asset_entity`
						ADD (   `custom65` varchar(255),
								`custom66` varchar(255), 
								`custom67` varchar(255),
								`custom68` varchar(255), 
								`custom69` varchar(255),

								`custom70` varchar(255),
								`custom71` varchar(255), 
								`custom72` varchar(255),
								`custom73` varchar(255),
								`custom74` varchar(255),
						  		`custom75` varchar(255),
					      		`custom76` varchar(255),
						  		`custom77` varchar(255),
						  		`custom78` varchar(255),
						 		`custom79` varchar(255),

								`custom80` varchar(255), 
								`custom81` varchar(255),
								`custom82` varchar(255), 
								`custom83` varchar(255),
								`custom84` varchar(255),
								`custom85` varchar(255), 
								`custom86` varchar(255),
								`custom87` varchar(255),
								`custom88` varchar(255),
						  		`custom89` varchar(255),

					      		`custom90` varchar(255),
						  		`custom91` varchar(255),
						  		`custom92` varchar(255),
						 		`custom93` varchar(255),
								`custom94` varchar(255), 
								`custom95` varchar(255),
						  		`custom96` varchar(255) );
			""")
	}
	//This changeset will add customs 65 to 96 fileds in eav Attribute and its associated table which is used for import and export .
	changeSet(author: "dscarpa", id: "20141110 TM-3541-3") {
		comment('Add customs (65..96) to asset, apps, db , files entity')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 'select count(*) from eav_attribute where attribute_code="custom65" and entity_type_id = (select entity_type_id from eav_entity_type where domain_name ="AssetEntity")')
		}
		grailsChange {
			change {
				
				def mDataTransferSet = sql.firstRow("SELECT data_transfer_id as dataTransferId from data_transfer_set where set_code = 'MASTER'")
				def wDataTransferSet = sql.firstRow("SELECT data_transfer_id as dataTransferId from data_transfer_set where set_code = 'WALKTHROUGH'")
				
				def entityTypes = sql.rows("select entity_type_id as id from eav_entity_type")
				
				entityTypes.each{ typeId ->

					def eavAttrSet = sql.firstRow("SELECT attribute_set_id as asId from eav_attribute_set \
								where entity_type_id = ${typeId.id} ")

					sql.execute("""INSERT INTO eav_attribute (attribute_code,backend_type,default_value,entity_type_id,frontend_input,
                    				frontend_label,is_required,is_unique,note,sort_order,validation)
                        VALUES 
                        ('custom65','String','1',${typeId.id}, 'text', 'Custom65','0', '0','this field is used for just import','10','No validation'),
                        ('custom66','String','1',${typeId.id}, 'text', 'Custom66','0', '0','this field is used for just import','10','No validation'),
                        ('custom67','String','1',${typeId.id}, 'text', 'Custom67','0', '0','this field is used for just import','10','No validation'),
                        ('custom68','String','1',${typeId.id}, 'text', 'Custom68','0', '0','this field is used for just import','10','No validation'),
                        ('custom69','String','1',${typeId.id}, 'text', 'Custom69','0', '0','this field is used for just import','10','No validation'),

                        ('custom70','String','1',${typeId.id}, 'text', 'Custom70','0', '0','this field is used for just import','10','No validation'),
                        ('custom71','String','1',${typeId.id}, 'text', 'Custom71','0', '0','this field is used for just import','10','No validation'),
                        ('custom72','String','1',${typeId.id}, 'text', 'Custom72','0', '0','this field is used for just import','10','No validation'),
                        ('custom73','String','1',${typeId.id}, 'text', 'Custom73','0', '0','this field is used for just import','10','No validation'),
                        ('custom74','String','1',${typeId.id}, 'text', 'Custom74','0', '0','this field is used for just import','10','No validation'),
                        ('custom75','String','1',${typeId.id}, 'text', 'Custom75','0', '0','this field is used for just import','10','No validation'),
                        ('custom76','String','1',${typeId.id}, 'text', 'Custom76','0', '0','this field is used for just import','10','No validation'),
                        ('custom77','String','1',${typeId.id}, 'text', 'Custom77','0', '0','this field is used for just import','10','No validation'),
                        ('custom78','String','1',${typeId.id}, 'text', 'Custom78','0', '0','this field is used for just import','10','No validation'),
                        ('custom79','String','1',${typeId.id}, 'text', 'Custom79','0', '0','this field is used for just import','10','No validation'),

                        ('custom80','String','1',${typeId.id}, 'text', 'Custom80','0', '0','this field is used for just import','10','No validation'),
                        ('custom81','String','1',${typeId.id}, 'text', 'Custom81','0', '0','this field is used for just import','10','No validation'),
                        ('custom82','String','1',${typeId.id}, 'text', 'Custom82','0', '0','this field is used for just import','10','No validation'),
                        ('custom83','String','1',${typeId.id}, 'text', 'Custom83','0', '0','this field is used for just import','10','No validation'),
                        ('custom84','String','1',${typeId.id}, 'text', 'Custom84','0', '0','this field is used for just import','10','No validation'),
                        ('custom85','String','1',${typeId.id}, 'text', 'Custom85','0', '0','this field is used for just import','10','No validation'),
                        ('custom86','String','1',${typeId.id}, 'text', 'Custom86','0', '0','this field is used for just import','10','No validation'),
                        ('custom87','String','1',${typeId.id}, 'text', 'Custom87','0', '0','this field is used for just import','10','No validation'),
                        ('custom88','String','1',${typeId.id}, 'text', 'Custom88','0', '0','this field is used for just import','10','No validation'),
                        ('custom89','String','1',${typeId.id}, 'text', 'Custom89','0', '0','this field is used for just import','10','No validation'),

                        ('custom90','String','1',${typeId.id}, 'text', 'Custom90','0', '0','this field is used for just import','10','No validation'),
                        ('custom91','String','1',${typeId.id}, 'text', 'Custom91','0', '0','this field is used for just import','10','No validation'),
                        ('custom92','String','1',${typeId.id}, 'text', 'Custom92','0', '0','this field is used for just import','10','No validation'),
                        ('custom93','String','1',${typeId.id}, 'text', 'Custom93','0', '0','this field is used for just import','10','No validation'),
                        ('custom94','String','1',${typeId.id}, 'text', 'Custom94','0', '0','this field is used for just import','10','No validation'),
                        ('custom95','String','1',${typeId.id}, 'text', 'Custom95','0', '0','this field is used for just import','10','No validation'),
                        ('custom96','String','1',${typeId.id}, 'text', 'Custom96','0', '0','this field is used for just import','10','No validation')
                        ;
                    """)

					sql.execute("""INSERT INTO eav_entity_attribute (attribute_id, eav_attribute_set_id, sort_order)
		                VALUES 
		                ((SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom65'), ${eavAttrSet.asId}, 10),
		                ((SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom66'), ${eavAttrSet.asId}, 10),
		                ((SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom67'), ${eavAttrSet.asId}, 10),
		                ((SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom68'), ${eavAttrSet.asId}, 10),
		                ((SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom69'), ${eavAttrSet.asId}, 10),

		                ((SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom70'), ${eavAttrSet.asId}, 10),
		                ((SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom71'), ${eavAttrSet.asId}, 10),
		                ((SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom72'), ${eavAttrSet.asId}, 10),
		                ((SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom73'), ${eavAttrSet.asId}, 10),
		                ((SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom74'), ${eavAttrSet.asId}, 10),
		                ((SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom75'), ${eavAttrSet.asId}, 10),
		                ((SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom76'), ${eavAttrSet.asId}, 10),
		                ((SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom77'), ${eavAttrSet.asId}, 10),
		                ((SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom78'), ${eavAttrSet.asId}, 10),
		                ((SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom79'), ${eavAttrSet.asId}, 10),

		                ((SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom80'), ${eavAttrSet.asId}, 10),
		                ((SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom81'), ${eavAttrSet.asId}, 10),
		                ((SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom82'), ${eavAttrSet.asId}, 10),
		                ((SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom83'), ${eavAttrSet.asId}, 10),
		                ((SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom84'), ${eavAttrSet.asId}, 10),
		                ((SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom85'), ${eavAttrSet.asId}, 10),
		                ((SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom86'), ${eavAttrSet.asId}, 10),
		                ((SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom87'), ${eavAttrSet.asId}, 10),
		                ((SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom88'), ${eavAttrSet.asId}, 10),
		                ((SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom89'), ${eavAttrSet.asId}, 10),

		                ((SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom90'), ${eavAttrSet.asId}, 10),
		                ((SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom91'), ${eavAttrSet.asId}, 10),
		                ((SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom92'), ${eavAttrSet.asId}, 10),
		                ((SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom93'), ${eavAttrSet.asId}, 10),
		                ((SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom94'), ${eavAttrSet.asId}, 10),
		                ((SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom95'), ${eavAttrSet.asId}, 10),
		                ((SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom96'), ${eavAttrSet.asId}, 10)
		                ;
		            """)

					sql.execute("""INSERT INTO data_transfer_attribute_map (column_name, data_transfer_set_id, eav_attribute_id, is_required,
												sheet_name, validation) 
						VALUES 
						('Custom65',${mDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom65'), '0',
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom66',${mDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom66'), '0',
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom67',${mDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom67'), '0',
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom68',${mDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom68'), '0',
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom69',${mDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom69'), '0',
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),

						('Custom70',${mDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom70'), '0',
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom71',${mDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom71'), '0',
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom72',${mDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom72'), '0',
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom73',${mDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom73'), '0',
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom74',${mDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom74'), '0',
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom75',${mDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom75'), '0',
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom76',${mDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom76'), '0',
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom77',${mDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom77'), '0',
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom78',${mDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom78'), '0',
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom79',${mDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom79'), '0',
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),

						('Custom80',${mDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom80'), '0',
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom81',${mDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom81'), '0',
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom82',${mDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom82'), '0',
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom83',${mDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom83'), '0',
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom84',${mDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom84'), '0',
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom85',${mDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom85'), '0',
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom86',${mDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom86'), '0',
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom87',${mDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom87'), '0',
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom88',${mDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom88'), '0',
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom89',${mDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom89'), '0',
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),

						('Custom90',${mDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom90'), '0',
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom91',${mDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom91'), '0',
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom92',${mDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom92'), '0',
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom93',${mDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom93'), '0',
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom94',${mDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom94'), '0',
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom95',${mDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom95'), '0',
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom96',${mDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom96'), '0',
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation')
						;
					""")

					sql.execute("""
						INSERT INTO data_transfer_attribute_map (column_name, data_transfer_set_id, eav_attribute_id, is_required,
								sheet_name, validation) 
						VALUES 
						('Custom65', ${wDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom65'),'0', 
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom66', ${wDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom66'),'0', 
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom67', ${wDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom67'),'0', 
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom68', ${wDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom68'),'0', 
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom69', ${wDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom69'),'0', 
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),

						('Custom70', ${wDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom70'),'0', 
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom71', ${wDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom71'),'0', 
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom72', ${wDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom72'),'0', 
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom73', ${wDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom73'),'0', 
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom74', ${wDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom74'),'0', 
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom75', ${wDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom75'),'0', 
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom76', ${wDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom76'),'0', 
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom77', ${wDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom77'),'0', 
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom78', ${wDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom78'),'0', 
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom79', ${wDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom79'),'0', 
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),

						('Custom80', ${wDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom80'),'0', 
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom81', ${wDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom81'),'0', 
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom82', ${wDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom82'),'0', 
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom83', ${wDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom83'),'0', 
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom84', ${wDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom84'),'0', 
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom85', ${wDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom85'),'0', 
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom86', ${wDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom86'),'0', 
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom87', ${wDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom87'),'0', 
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom88', ${wDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom88'),'0', 
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom89', ${wDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom89'),'0', 
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),

						('Custom90', ${wDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom90'),'0', 
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom91', ${wDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom91'),'0', 
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom92', ${wDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom92'),'0', 
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom93', ${wDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom93'),'0', 
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom94', ${wDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom94'),'0', 
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom95', ${wDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom95'),'0', 
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation'),
						('Custom96', ${wDataTransferSet.dataTransferId}, (SELECT attribute_id from eav_attribute WHERE entity_type_id = ${typeId.id} AND attribute_code='custom96'),'0', 
							${typeId.id==1 ? 'Devices' : (typeId.id== 2 ? 'Applications' : (typeId.id==3 ? 'Databases' : 'Files'))}, 'NO Validation')
						;
					""")

			   }
			}
		}
	}
}
