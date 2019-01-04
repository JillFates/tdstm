/**
 * This set of Database change to delete eav_attribute and associated tables data to cleanup excel template
 *  for import and export .
 */

databaseChangeLog = {	
	changeSet(author: "lokanada", id: "20121224 TM-1154.01") {
		comment("""delete eav_attribute and associated tables data where attribute_code in 
					('SourceTeamMt, TargetTeamMt, SourceTeamLog, TargetTeamLog, SourceTeamSa, TargetTeamSa, SourceTeamDba, TargetTeamDba, App Owner, App SME')""")
		sql("""DELETE FROM data_transfer_attribute_map 
							WHERE eav_attribute_id IN ( SELECT attribute_id FROM eav_attribute
												WHERE attribute_code IN ('sourceTeamMt','targetTeamMt','sourceTeamLog','targetTeamLog',
																		 'sourceTeamSa','targetTeamSa','sourceTeamDba','targetTeamDba',
				 														 'appOwner','appSme')
											)"""
			)
		sql("""DELETE FROM data_transfer_value 
							WHERE eav_attribute_id IN ( SELECT attribute_id FROM eav_attribute 
													WHERE attribute_code IN ('sourceTeamMt','targetTeamMt','sourceTeamLog','targetTeamLog',
																			 'sourceTeamSa','targetTeamSa','sourceTeamDba','targetTeamDba',
																			 'appOwner','appSme')
													)"""
			)
	}
	
	changeSet(author: "lokanada", id: "20121226 TM-1154.2") {
		comment("""delete eav_attribute associated tables data where attribute_id not in eav_attribute(orphan data)""")
		sql("DELETE FROM eav_entity_attribute WHERE attribute_id NOT IN ( SELECT attribute_id FROM eav_attribute)")
		sql("DELETE FROM eav_attribute_option WHERE attribute_id NOT IN ( SELECT attribute_id FROM eav_attribute)")
		sql("DELETE FROM eav_entity_datatype WHERE attribute_id NOT IN ( SELECT attribute_id FROM eav_attribute)")
	}
    
    changeSet(author: "lokanada", id: "20121228 TM-1154.3") {
        comment("""Add ('SourceTeamMt, TargetTeamMt, SourceTeamLog, TargetTeamLog, SourceTeamSa, TargetTeamSa, SourceTeamDba, TargetTeamDba, App Owner, App SME') to eav_attribute""")
        grailsChange {
            change {
                [sourceTeamMt:['String','',1,'text','Source Team Mt',0,0,'The Team that device is associated as Source',330,'Must match existing Team (hard) if Not create new Team '],
                 targetTeamMt:['String','',1,'text','Target Team Mt',0,0,'The Team that device is associated as Target',340,'Must match existing Team (hard) if Not create new Team '],
                 sourceTeamLog:['String','Log',1,'text','Source Team Log',0,0,'The Team that device is associated as Source',341,'No validation'],
                 targetTeamLog:['String','Log',1,'text','Target Team Log',0,0,'The Team that device is associated as Target',342,'No validation'],
                 sourceTeamSa:['String','Sa',1,'text','Source Team Sa',0,0,'The Team that device is associated as Source',343,'No validation'],
                 targetTeamSa:['String','Sa',1,'text','Target Team Sa',0,0,'The Team that device is associated as Target',344,'No validation'],
                 sourceTeamDba:['String','Dba',1,'text','Source Team Dba',0,0,'The Team that device is associated as Source',345,'No validation'],
                 targetTeamDba:['String','Dba',1,'text','Target Team Dba',0,0,'The Team that device is associated as Target',346,'No validation'],
                 appOwner:['String','',1,'autocomplete','AppOwner',0,0,'Application Owner',390,''],
                 appSme:['String','',1,'autocomplete','SME',0,0,'Application Subject Matter Expert (SME)',400,'']
                 ].each{ attribute, data->
                    // See if the eav_attribute already exists
                    def attrSearch = sql.firstRow("SELECT attribute_id AS id FROM eav_attribute WHERE attribute_code = ${attribute}")
                    if (attrSearch) {
                        println "EavAttribute code: '${attribute}' already exists"
                    } else {
                        sql.execute("""INSERT INTO eav_attribute (attribute_code,backend_type,default_value,entity_type_id,frontend_input,
                                                                    frontend_label,is_required,is_unique,note,sort_order,validation)
                                            VALUES (${attribute},${data[0]},${data[1]},${data[2]},${data[3]},${data[4]},
                                                ${data[5]},${data[6]},${data[7]},${data[8]},${data[9]});""")
                    }
                    [1,2].each{ attributeSet->
                        def attrEntitySearch2 = sql.firstRow("""SELECT attribute_id AS id FROM eav_entity_attribute WHERE eav_attribute_set_id = ${attributeSet} AND
                                                                    attribute_id = (select attribute_id from eav_attribute where attribute_code = ${attribute})""")
                        if (!attrEntitySearch2) {
                            sql.execute("""INSERT INTO eav_entity_attribute (attribute_id, eav_attribute_set_id, sort_order)
                                                VALUES ((select attribute_id from eav_attribute where attribute_code = ${attribute}),${attributeSet},${data[8]});""")
                        }
                    }
                }
            }
        }
    }
    
}
