databaseChangeLog = {
	// This Changeset is used to Modify "owner" column in eav_attribute table
	changeSet(author: "lokanada", id: "20140327 TM-2562-1") {
		comment('Modify "owner" column in eav_attribute table to "AppOwner"')
		preConditions(onFail:'MARK_RAN') {
				sqlCheck(expectedResult:'1', """select count(*) from eav_attribute where attribute_code="owner" and
												entity_type_id = (select entity_type_id from eav_entity_type where domain_name ="Application")""")
		}
		grailsChange {
			change {
					def typeId = sql.firstRow(" SELECT entity_type_id AS id FROM eav_entity_type WHERE domain_name ='Application' ")
					
					sql.execute("""update eav_attribute set frontend_label= 'App Owner',attribute_code='appOwner' where attribute_code='owner' and entity_type_id = ${typeId.id}""")
					
					sql.execute(""" update data_transfer_attribute_map set column_name= 'AppOwner' where eav_attribute_id in
									(select attribute_id from eav_attribute where attribute_code ='appOwner' and entity_type_id = ${typeId.id}) """)
			}
		}
	}
	
}