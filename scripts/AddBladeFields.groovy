// Adds blade fields to the EAV tables
import com.tdssrc.eav.*

if(!EavAttribute.findWhere(attributeCode:'bladeSize')) {
	attribute = new EavAttribute(attributeCode:'bladeSize', backendType:'String', defaultValue:'null', entityType:EavEntityType.get(1),
					 frontendInput:'select', frontendLabel:'Blade Size', isRequired:0, isUnique:0, note:'Blade size (full or half)',
					 sortOrder:330, validation:'')
	if(!attribute.save()) {
		println "Unable to save attribute: ${attribute.errors}"
	} else {
		new EavAttributeOption(attribute:attribute, sortOrder:330, value:'Full').save()
		new EavAttributeOption(attribute:attribute, sortOrder:330, value:'Half').save()
		new EavEntityAttribute(attribute:attribute, eavAttributeSet:EavAttributeSet.get(1), sortOrder:attribute.sortOrder).save()
	}
}

if(!EavAttribute.findWhere(attributeCode:'sourceBladeChassis')) {
	attribute = new EavAttribute(attributeCode:'sourceBladeChassis', backendType:'String', defaultValue:'null', entityType:EavEntityType.get(1),
					 frontendInput:'autocomplete', frontendLabel:'Source Blade', isRequired:0, isUnique:0, note:'Source blade chassis that the blade is installed in before the move.',
					 sortOrder:340, validation:'')
	if(!attribute.save()) {
		println "Unable to save attribute: ${attribute.errors}"
	} else {
		new EavEntityAttribute(attribute:attribute, eavAttributeSet:EavAttributeSet.get(1), sortOrder:attribute.sortOrder).save()
	}
}

if(!EavAttribute.findWhere(attributeCode:'sourceBladePosition')) {
	attribute = new EavAttribute(attributeCode:'sourceBladePosition', backendType:'int', defaultValue:'null', entityType:EavEntityType.get(1),
					 frontendInput:'text', frontendLabel:'Source Blade Position', isRequired:0, isUnique:0, note:'The position on the chassis that the blade is located in before the move',
					 sortOrder:350, validation:'')
	if(!attribute.save()) {
		println "Unable to save attribute: ${attribute.errors}"
	} else {
		new EavEntityAttribute(attribute:attribute, eavAttributeSet:EavAttributeSet.get(1), sortOrder:attribute.sortOrder).save()
	}
}

if(!EavAttribute.findWhere(attributeCode:'targetBladeChassis')) {
	attribute = new EavAttribute(attributeCode:'targetBladeChassis', backendType:'String', defaultValue:'null', entityType:EavEntityType.get(1),
					 frontendInput:'autocomplete', frontendLabel:'Target Blade', isRequired:0, isUnique:0, note:'Target blade chassis that the blade is installed in after the move.',
					 sortOrder:360, validation:'')
	if(!attribute.save()) {
		println "Unable to save attribute: ${attribute.errors}"
	} else {
		new EavEntityAttribute(attribute:attribute, eavAttributeSet:EavAttributeSet.get(1), sortOrder:attribute.sortOrder).save()
	}
}

if(!EavAttribute.findWhere(attributeCode:'targetBladePosition')) {
	attribute = new EavAttribute(attributeCode:'targetBladePosition', backendType:'int', defaultValue:'null', entityType:EavEntityType.get(1),
					 frontendInput:'text', frontendLabel:'Target Blade Position', isRequired:0, isUnique:0, note:'The position on the chassis that the blade is located in after the move',
					 sortOrder:370, validation:'')
	if(!attribute.save()) {
		println "Unable to save attribute: ${attribute.errors}"
	} else {
		new EavEntityAttribute(attribute:attribute, eavAttributeSet:EavAttributeSet.get(1), sortOrder:attribute.sortOrder).save()
	}
}
