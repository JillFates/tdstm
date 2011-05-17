// Adds blade fields to the EAV tables
import com.tdssrc.eav.*
def jdbcTemplate = ctx.getBean("jdbcTemplate")
def bladeSizeAttribute = EavAttribute.findByAttributeCode('bladeSize')
if(bladeSizeAttribute) {
	jdbcTemplate.update("delete from data_transfer_value where eav_attribute_id = ${bladeSizeAttribute.id}")
	jdbcTemplate.update("delete from eav_attribute_option where attribute_id = ${bladeSizeAttribute.id}")
	jdbcTemplate.update("delete from eav_entity_attribute where attribute_id = ${bladeSizeAttribute.id}")
	jdbcTemplate.update("delete from data_transfer_attribute_map where eav_attribute_id = ${bladeSizeAttribute.id}")
	jdbcTemplate.update("delete from eav_attribute where attribute_id = ${bladeSizeAttribute.id}")
}

if(!EavAttribute.findWhere(attributeCode:'sourceBladeChassis')) {
	attribute = new EavAttribute(attributeCode:'sourceBladeChassis', backendType:'String', defaultValue:'null', entityType:EavEntityType.get(1),
					 frontendInput:'autocomplete', frontendLabel:'Source Blade', isRequired:0, isUnique:0, note:'Source blade chassis that the blade is installed in before the move.',
					 sortOrder:331, validation:'')
	if(!attribute.save()) {
		println "Unable to save attribute: ${attribute.errors}"
	} else {
		new EavEntityAttribute(attribute:attribute, eavAttributeSet:EavAttributeSet.get(1), sortOrder:attribute.sortOrder).save()
	}
}

if(!EavAttribute.findWhere(attributeCode:'sourceBladePosition')) {
	attribute = new EavAttribute(attributeCode:'sourceBladePosition', backendType:'int', defaultValue:'null', entityType:EavEntityType.get(1),
					 frontendInput:'text', frontendLabel:'Source Blade Position', isRequired:0, isUnique:0, note:'The position on the chassis that the blade is located in before the move',
					 sortOrder:332, validation:'')
	if(!attribute.save()) {
		println "Unable to save attribute: ${attribute.errors}"
	} else {
		new EavEntityAttribute(attribute:attribute, eavAttributeSet:EavAttributeSet.get(1), sortOrder:attribute.sortOrder).save()
	}
}

if(!EavAttribute.findWhere(attributeCode:'targetBladeChassis')) {
	attribute = new EavAttribute(attributeCode:'targetBladeChassis', backendType:'String', defaultValue:'null', entityType:EavEntityType.get(1),
					 frontendInput:'autocomplete', frontendLabel:'Target Blade', isRequired:0, isUnique:0, note:'Target blade chassis that the blade is installed in after the move.',
					 sortOrder:333, validation:'')
	if(!attribute.save()) {
		println "Unable to save attribute: ${attribute.errors}"
	} else {
		new EavEntityAttribute(attribute:attribute, eavAttributeSet:EavAttributeSet.get(1), sortOrder:attribute.sortOrder).save()
	}
}

if(!EavAttribute.findWhere(attributeCode:'targetBladePosition')) {
	attribute = new EavAttribute(attributeCode:'targetBladePosition', backendType:'int', defaultValue:'null', entityType:EavEntityType.get(1),
					 frontendInput:'text', frontendLabel:'Target Blade Position', isRequired:0, isUnique:0, note:'The position on the chassis that the blade is located in after the move',
					 sortOrder:334, validation:'')
	if(!attribute.save()) {
		println "Unable to save attribute: ${attribute.errors}"
	} else {
		new EavEntityAttribute(attribute:attribute, eavAttributeSet:EavAttributeSet.get(1), sortOrder:attribute.sortOrder).save()
	}
}

/*============================================================
* Update the data_transfer_attribute_map for Blade attributes
*============================================================*/
def masteDataTransferSet = DataTransferSet.findByTitle( "TDS Master Spreadsheet" )
def walkthruDataTransferSet = DataTransferSet.findByTitle( "TDS Walkthru" )
def bladeAttributes = EavAttribute.findAll("FROM EavAttribute e where e.attributeCode in ('sourceBladeChassis','sourceBladePosition','targetBladeChassis','targetBladePosition')")
	bladeAttributes.each{
		def masterDataAttribMap = DataTransferAttributeMap.findWhere( columnName: it.frontendLabel, sheetName: "Servers", dataTransferSet: masteDataTransferSet,eavAttribute: it)
		if(!masterDataAttribMap){
			new DataTransferAttributeMap( columnName: it.frontendLabel,	sheetName: "Servers",
					dataTransferSet: masteDataTransferSet,eavAttribute: it,
					validation: it.validation,	isRequired: it.isRequired ).save()
		}
		
		def walkthruDataAttribMap = DataTransferAttributeMap.findWhere( columnName: it.frontendLabel, sheetName: "Servers", dataTransferSet: walkthruDataTransferSet,eavAttribute: it)
		if(!walkthruDataAttribMap ){
		new DataTransferAttributeMap( columnName: it.frontendLabel,	sheetName: "Servers",
				dataTransferSet: walkthruDataTransferSet,eavAttribute: it,
				validation: it.validation,	isRequired: it.isRequired ).save()
		}
	}
