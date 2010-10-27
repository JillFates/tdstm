
// insert to production database
import com.tdssrc.eav.*

def jdbcTemplate = ctx.getBean("jdbcTemplate")
def eavEntityType  = EavEntityType.get(1)
def eavAttributeSet = EavAttributeSet.get(1)
/*=========================================
 * Change project cleaning team to Logistics team
 *========================================*/
def projectTeams = ProjectTeam.findAllWhere(teamCode:"Cleaning")
projectTeams.each{ team->
	team.teamCode = "Logistics"
	if(!team.save(flush:true)){
		println "Unable to update Cleaning team: ${team}"
	}
}
/*=========================================
 * Change project cleaning party group to Logistics team
 *========================================*/
def partyGroups = PartyGroup.findAllWhere(name:"Cleaning")
partyGroups.each{ partyGroup ->
	partyGroup.name = "Logistics"
	if(!partyGroup.save(flush:true)){
		println "Unable to update Cleaning Party Group: ${partyGroup}"
	}
}

/*=========================================
 * Insert Custom 1-8 attribute to EavAttribute table
 *========================================*/
if(!EavAttribute.findWhere(attributeCode:'custom1')) {
	attribute = new EavAttribute(attributeCode:'custom1', backendType:'String', defaultValue:'null', entityType:eavEntityType,
					 frontendInput:'text', frontendLabel:'Custom1', isRequired:0, isUnique:0, note:'Custom field 1',
					 sortOrder:321, validation:'')
	if(!attribute.save()) {
		println "Unable to save attribute: ${attribute.errors}"
	} else {
		new EavEntityAttribute(attribute:attribute, eavAttributeSet:eavAttributeSet, sortOrder:attribute.sortOrder).save()
	}
}
if(!EavAttribute.findWhere(attributeCode:'custom2')) {
	attribute = new EavAttribute(attributeCode:'custom2', backendType:'String', defaultValue:'null', entityType:eavEntityType,
					 frontendInput:'text', frontendLabel:'Custom2', isRequired:0, isUnique:0, note:'Custom field 2',
					 sortOrder:322, validation:'')
	if(!attribute.save()) {
		println "Unable to save attribute: ${attribute.errors}"
	} else {
		new EavEntityAttribute(attribute:attribute, eavAttributeSet:eavAttributeSet, sortOrder:attribute.sortOrder).save()
	}
}
if(!EavAttribute.findWhere(attributeCode:'custom3')) {
	attribute = new EavAttribute(attributeCode:'custom3', backendType:'String', defaultValue:'null', entityType:eavEntityType,
					 frontendInput:'text', frontendLabel:'Custom3', isRequired:0, isUnique:0, note:'Custom field 3',
					 sortOrder:323, validation:'')
	if(!attribute.save()) {
		println "Unable to save attribute: ${attribute.errors}"
	} else {
		new EavEntityAttribute(attribute:attribute, eavAttributeSet:eavAttributeSet, sortOrder:attribute.sortOrder).save()
	}
}
if(!EavAttribute.findWhere(attributeCode:'custom4')) {
	attribute = new EavAttribute(attributeCode:'custom4', backendType:'String', defaultValue:'null', entityType:eavEntityType,
					 frontendInput:'text', frontendLabel:'Custom4', isRequired:0, isUnique:0, note:'Custom field 4',
					 sortOrder:324, validation:'')
	if(!attribute.save()) {
		println "Unable to save attribute: ${attribute.errors}"
	} else {
		new EavEntityAttribute(attribute:attribute, eavAttributeSet:eavAttributeSet, sortOrder:attribute.sortOrder).save()
	}
}
if(!EavAttribute.findWhere(attributeCode:'custom5')) {
	attribute = new EavAttribute(attributeCode:'custom5', backendType:'String', defaultValue:'null', entityType:eavEntityType,
					 frontendInput:'text', frontendLabel:'Custom5', isRequired:0, isUnique:0, note:'Custom field 5',
					 sortOrder:325, validation:'')
	if(!attribute.save()) {
		println "Unable to save attribute: ${attribute.errors}"
	} else {
		new EavEntityAttribute(attribute:attribute, eavAttributeSet:eavAttributeSet, sortOrder:attribute.sortOrder).save()
	}
}
if(!EavAttribute.findWhere(attributeCode:'custom6')) {
	attribute = new EavAttribute(attributeCode:'custom6', backendType:'String', defaultValue:'null', entityType:eavEntityType,
					 frontendInput:'text', frontendLabel:'Custom6', isRequired:0, isUnique:0, note:'Custom field 6',
					 sortOrder:326, validation:'')
	if(!attribute.save()) {
		println "Unable to save attribute: ${attribute.errors}"
	} else {
		new EavEntityAttribute(attribute:attribute, eavAttributeSet:eavAttributeSet, sortOrder:attribute.sortOrder).save()
	}
}
if(!EavAttribute.findWhere(attributeCode:'custom7')) {
	attribute = new EavAttribute(attributeCode:'custom7', backendType:'String', defaultValue:'null', entityType:eavEntityType,
					 frontendInput:'text', frontendLabel:'Custom7', isRequired:0, isUnique:0, note:'Custom field 7',
					 sortOrder:327, validation:'')
	if(!attribute.save()) {
		println "Unable to save attribute: ${attribute.errors}"
	} else {
		new EavEntityAttribute(attribute:attribute, eavAttributeSet:eavAttributeSet, sortOrder:attribute.sortOrder).save()
	}
}
if(!EavAttribute.findWhere(attributeCode:'custom8')) {
	attribute = new EavAttribute(attributeCode:'custom8', backendType:'String', defaultValue:'null', entityType:eavEntityType,
					 frontendInput:'text', frontendLabel:'Custom8', isRequired:0, isUnique:0, note:'Custom field 8',
					 sortOrder:328, validation:'')
	if(!attribute.save()) {
		println "Unable to save attribute: ${attribute.errors}"
	} else {
		new EavEntityAttribute(attribute:attribute, eavAttributeSet:eavAttributeSet, sortOrder:attribute.sortOrder).save()
	}
}
/*=========================================
 * Insert CurrentStatus attribute to EavAttribute table
 *========================================*/
if(!EavAttribute.findWhere(attributeCode:'currentStatus')) {
	attribute = new EavAttribute(attributeCode:'currentStatus', backendType:'int', defaultValue:'null', entityType:eavEntityType,
					 frontendInput:'text', frontendLabel:'Current Status', isRequired:0, isUnique:0, note:'The current status of that asset',
					 sortOrder:329, validation:'')
	if(!attribute.save()) {
		println "Unable to save attribute: ${attribute.errors}"
	} else {
		new EavEntityAttribute(attribute:attribute, eavAttributeSet:eavAttributeSet, sortOrder:attribute.sortOrder).save()
	}
}
/*============================================================
* Update the data_transfer_attribute_map for Custom attributes
*============================================================*/
def masteDataTransferSet = DataTransferSet.findByTitle( "TDS Master Spreadsheet" )
def walkthruDataTransferSet = DataTransferSet.findByTitle( "TDS Walkthru" )
def customAttributes = EavAttribute.findAll("FROM EavAttribute e where e.attributeCode in ('custom1','custom2','custom3','custom4','custom5','custom6','custom7','custom8')")
jdbcTemplate.update("update data_transfer_attribute_map set sheet_name = 'Servers' where sheet_name = 'Server'")
customAttributes.each{
	def masterDataAttribMap = DataTransferAttributeMap.findWhere( columnName: it.frontendLabel, sheetName: "Servers", dataTransferSet: masteDataTransferSet,eavAttribute: it)
	if(!masterDataAttribMap){
		new DataTransferAttributeMap( columnName: it.frontendLabel,	sheetName: "Servers",
				dataTransferSet: masteDataTransferSet,eavAttribute: it,
				validation: 'Not required',	isRequired: 0 ).save()
	}
	
	def walkthruDataAttribMap = DataTransferAttributeMap.findWhere( columnName: it.frontendLabel, sheetName: "Servers", dataTransferSet: walkthruDataTransferSet,eavAttribute: it)
	if(!walkthruDataAttribMap ){
	new DataTransferAttributeMap( columnName: it.frontendLabel,	sheetName: "Servers",
			dataTransferSet: walkthruDataTransferSet,eavAttribute: it,
			validation: 'Not required',	isRequired: 0 ).save()
	}
}

/*=========================================
 * DELETE duplicate CurrentStatus from ProjectAssetMap
 *========================================*/

def dupProjectAssetMap = jdbcTemplate.queryForList("select asset_id as assetId from project_asset_map p group by asset_id having count(asset_id) > 1")
dupProjectAssetMap.each{ map->
	/* remove one Project asset map when it mapped with different status*/
	jdbcTemplate.update("""delete from project_asset_map where asset_id = ${map.assetId} and current_state_id != (select cast(t.state_to as UNSIGNED INTEGER) as stateTo from asset_transition t
							where t.asset_entity_id = ${map.assetId} and t.voided = 0 and ( t.type = 'process' or t.state_To = 10 )
							order by date_created desc, stateTo desc limit 1)""")
	/* remove one Project asset map when it mapped with same status*/
	def projectMap = ProjectAssetMap.findAll("from ProjectAssetMap p where p.asset = ${map.assetId} order by p.createdDate asc ")
	if(projectMap.size() > 1){
		jdbcTemplate.update("delete from project_asset_map where id = ${projectMap[0].id}")
	}
}
/*=========================================
 * Update CurrentStatus of assetEntity
 *========================================*/
def updateQuery = "Update asset_entity set current_status = ( select p.current_state_id from project_asset_map p where  asset_entity.asset_entity_id = p.asset_id )"
def result = jdbcTemplate.update(updateQuery)
if(result)
	println " $result assets are updated with current status"




