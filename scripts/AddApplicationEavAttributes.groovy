
import com.tds.asset.AssetEntity;
import com.tdssrc.eav.*
import com.tdssrc.grails.GormUtil

def jdbcTemplate = ctx.getBean("jdbcTemplate")
def masterDataTransferSet = DataTransferSet.findBySetCode("MASTER")
def walkThruDataTransferSet = DataTransferSet.findBySetCode("WALKTHROUGH")

def appEntityType = EavEntityType.findByEntityTypeCode("Application")
if(!appEntityType){
	appAttributeSet = new EavEntityType( entityTypeCode:'Application', domainName:'Application', isAuditable:1  ).save(flush:true)
}
def appAttributeSet = EavAttributeSet.findByAttributeSetName("Application")
if(!appAttributeSet){
	appAttributeSet = new EavAttributeSet( attributeSetName:'Application', entityType:appEntityType, sortOrder:20 ).save(flush:true)
}

/**
 *  Create Name
 */
def nameAttribute = EavAttribute.findByAttributeCodeAndEntityType('name',appEntityType)
if(nameAttribute){
	EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'name', frontendLabel='Name' where id = ?",[nameAttribute.id])
} else {
	nameAttribute = new EavAttribute( attributeCode : "name",
			backendType : 'String',
			frontendInput : 'text',
			frontendLabel : 'Name',
			note : 'this field is used for just import',
			sortOrder : 10,
			entityType:appEntityType,
			isRequired:0,
			isUnique:0,
			defaultValue:"1",
			validation:'No validation'
			)
	if ( !nameAttribute.validate() || !nameAttribute.save(flush:true) ) {
		println"Unable to create nameAttribute : "
		nameAttribute.errors.allErrors.each() {println"\n"+it }
	}
}

def nameEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(nameAttribute,appAttributeSet)
if(nameEavEntityAttribute){
	EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 346, attributeCode = 'name' where attributeCode = 'name'")
} else {
	nameEavEntityAttribute = new EavEntityAttribute(sortOrder:346,attribute:nameAttribute,eavAttributeSet:appAttributeSet)
	if ( !nameEavEntityAttribute.validate() || !nameEavEntityAttribute.save(flush:true) ) {
		println"Unable to create nameEavEntityAttribute : " +
				nameEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
	}
}

def nameDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,nameAttribute)
if( !nameDataTransferMapMaster ){
	nameDataTransferMapMaster = new DataTransferAttributeMap(columnName:"name",
			sheetName:"Applications",
			dataTransferSet : masterDataTransferSet,
			eavAttribute:nameAttribute,
			validation:"NO Validation",
			isRequired:0
			)
	if ( !nameDataTransferMapMaster.validate() || !nameDataTransferMapMaster.save(flush:true) ) {
		println"Unable to create nameDataTransferMapMaster : " +
				nameDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
	}
} else {
	DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'name',sheetName='Applications' where eavAttribute = ?",[nameAttribute])
}

def nameDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,nameAttribute)
if(!nameDataTransferMapWalkThru){
	nameDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"name",
			sheetName:"Applications",
			dataTransferSet : walkThruDataTransferSet,
			eavAttribute:nameAttribute,
			validation:"NO Validation",
			isRequired:0
			)
	if ( !nameDataTransferMapWalkThru.validate() || !nameDataTransferMapWalkThru.save(flush:true) ) {
		println"Unable to create nameDataTransferMapWalkThru : " +
				nameDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
	}
} else {
	DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'name',sheetName='Applications' where eavAttribute = ?",[nameAttribute])
}
/**
*  Create Vendor
*/
def vendorAttribute = EavAttribute.findByAttributeCodeAndEntityType('vendor',appEntityType)
if(vendorAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'vendor', frontendLabel='Vendor' where id = ?",[vendorAttribute.id])
} else {
   vendorAttribute = new EavAttribute( attributeCode : "vendor",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Vendor',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !vendorAttribute.validate() || !vendorAttribute.save(flush:true) ) {
	   println"Unable to create vendorAttribute : "
	   vendorAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def vendorEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(vendorAttribute,appAttributeSet)
if(vendorEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 346, attributeCode = 'vendor' where attributeCode = 'vendor'")
} else {
   vendorEavEntityAttribute = new EavEntityAttribute(sortOrder:346,attribute:vendorAttribute,eavAttributeSet:appAttributeSet)
   if ( !vendorEavEntityAttribute.validate() || !vendorEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create vendorEavEntityAttribute : " +
			   vendorEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def vendorDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,vendorAttribute)
if( !vendorDataTransferMapMaster ){
   vendorDataTransferMapMaster = new DataTransferAttributeMap(columnName:"vendor",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:vendorAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !vendorDataTransferMapMaster.validate() || !vendorDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create vendorDataTransferMapMaster : " +
			   vendorDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'vendor',sheetName='Applications' where eavAttribute = ?",[vendorAttribute])
}

def vendorDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,vendorAttribute)
if(!vendorDataTransferMapWalkThru){
   vendorDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"vendor",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:vendorAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !vendorDataTransferMapWalkThru.validate() || !vendorDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create vendorDataTransferMapWalkThru : " +
			   vendorDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'vendor',sheetName='Applications' where eavAttribute = ?",[vendorAttribute])
}
/**
*  Create Version
*/
def versionAttribute = EavAttribute.findByAttributeCodeAndEntityType('version',appEntityType)
if(versionAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'version', frontendLabel='Version' where id = ?",[versionAttribute.id])
} else {
   versionAttribute = new EavAttribute( attributeCode : "version",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Version',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !versionAttribute.validate() || !versionAttribute.save(flush:true) ) {
	   println"Unable to create versionAttribute : "
	   versionAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def versionEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(versionAttribute,appAttributeSet)
if(versionEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 346, attributeCode = 'version' where attributeCode = 'version'")
} else {
   versionEavEntityAttribute = new EavEntityAttribute(sortOrder:346,attribute:versionAttribute,eavAttributeSet:appAttributeSet)
   if ( !versionEavEntityAttribute.validate() || !versionEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create versionEavEntityAttribute : " +
			   versionEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def versionDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,versionAttribute)
if( !versionDataTransferMapMaster ){
   versionDataTransferMapMaster = new DataTransferAttributeMap(columnName:"version",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:versionAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !versionDataTransferMapMaster.validate() || !versionDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create versionDataTransferMapMaster : " +
			   versionDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'version',sheetName='Applications' where eavAttribute = ?",[versionAttribute])
}

def versionDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,versionAttribute)
if(!versionDataTransferMapWalkThru){
   versionDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"version",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:versionAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !versionDataTransferMapWalkThru.validate() || !versionDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create versionDataTransferMapWalkThru : " +
			   versionDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'version',sheetName='Applications' where eavAttribute = ?",[versionAttribute])
}
/**
*  Create Technology
*/
def technologyAttribute = EavAttribute.findByAttributeCodeAndEntityType('technology',appEntityType)
if(technologyAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'technology', frontendLabel='Technology' where id = ?",[technologyAttribute.id])
} else {
   technologyAttribute = new EavAttribute( attributeCode : "technology",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Technology',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !technologyAttribute.validate() || !technologyAttribute.save(flush:true) ) {
	   println"Unable to create technologyAttribute : "
	   technologyAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def technologyEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(technologyAttribute,appAttributeSet)
if(technologyEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 346, attributeCode = 'technology' where attributeCode = 'technology'")
} else {
   technologyEavEntityAttribute = new EavEntityAttribute(sortOrder:346,attribute:technologyAttribute,eavAttributeSet:appAttributeSet)
   if ( !technologyEavEntityAttribute.validate() || !technologyEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create technologyEavEntityAttribute : " +
			   technologyEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def technologyDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,technologyAttribute)
if( !technologyDataTransferMapMaster ){
   technologyDataTransferMapMaster = new DataTransferAttributeMap(columnName:"technology",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:technologyAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !technologyDataTransferMapMaster.validate() || !technologyDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create technologyDataTransferMapMaster : " +
			   technologyDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'technology',sheetName='Applications' where eavAttribute = ?",[technologyAttribute])
}

def technologyDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,technologyAttribute)
if(!technologyDataTransferMapWalkThru){
   technologyDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"technology",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:technologyAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !technologyDataTransferMapWalkThru.validate() || !technologyDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create technologyDataTransferMapWalkThru : " +
			   technologyDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'technology',sheetName='Applications' where eavAttribute = ?",[technologyAttribute])
}
/**
*  Create AccessType
*/
def accessTypeAttribute = EavAttribute.findByAttributeCodeAndEntityType('accessType',appEntityType)
if(accessTypeAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'accessType', frontendLabel='AccessType' where id = ?",[accessTypeAttribute.id])
} else {
   accessTypeAttribute = new EavAttribute( attributeCode : "accessType",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'AccessType',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !accessTypeAttribute.validate() || !accessTypeAttribute.save(flush:true) ) {
	   println"Unable to create accessTypeAttribute : "
	   accessTypeAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def accessTypeEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(accessTypeAttribute,appAttributeSet)
if(accessTypeEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 346, attributeCode = 'accessType' where attributeCode = 'accessType'")
} else {
   accessTypeEavEntityAttribute = new EavEntityAttribute(sortOrder:346,attribute:accessTypeAttribute,eavAttributeSet:appAttributeSet)
   if ( !accessTypeEavEntityAttribute.validate() || !accessTypeEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create accessTypeEavEntityAttribute : " +
			   accessTypeEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def accessTypeDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,accessTypeAttribute)
if( !accessTypeDataTransferMapMaster ){
   accessTypeDataTransferMapMaster = new DataTransferAttributeMap(columnName:"accessType",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:accessTypeAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !accessTypeDataTransferMapMaster.validate() || !accessTypeDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create accessTypeDataTransferMapMaster : " +
			   accessTypeDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'accessType',sheetName='Applications' where eavAttribute = ?",[accessTypeAttribute])
}

def accessTypeDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,accessTypeAttribute)
if(!accessTypeDataTransferMapWalkThru){
   accessTypeDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"accessType",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:accessTypeAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !accessTypeDataTransferMapWalkThru.validate() || !accessTypeDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create accessTypeDataTransferMapWalkThru : " +
			   accessTypeDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'accessType',sheetName='Applications' where eavAttribute = ?",[accessTypeAttribute])
}
/**
*  Create Source
*/
def sourceAttribute = EavAttribute.findByAttributeCodeAndEntityType('source',appEntityType)
if(sourceAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'source', frontendLabel='Source' where id = ?",[sourceAttribute.id])
} else {
   sourceAttribute = new EavAttribute( attributeCode : "source",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Source',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !sourceAttribute.validate() || !sourceAttribute.save(flush:true) ) {
	   println"Unable to create sourceAttribute : "
	   sourceAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def sourceEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(sourceAttribute,appAttributeSet)
if(sourceEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 346, attributeCode = 'source' where attributeCode = 'source'")
} else {
   sourceEavEntityAttribute = new EavEntityAttribute(sortOrder:346,attribute:sourceAttribute,eavAttributeSet:appAttributeSet)
   if ( !sourceEavEntityAttribute.validate() || !sourceEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create sourceEavEntityAttribute : " +
			   sourceEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def sourceDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,sourceAttribute)
if( !sourceDataTransferMapMaster ){
   sourceDataTransferMapMaster = new DataTransferAttributeMap(columnName:"source",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:sourceAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !sourceDataTransferMapMaster.validate() || !sourceDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create sourceDataTransferMapMaster : " +
			   sourceDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'source',sheetName='Applications' where eavAttribute = ?",[sourceAttribute])
}

def sourceDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,sourceAttribute)
if(!sourceDataTransferMapWalkThru){
   sourceDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"source",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:sourceAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !sourceDataTransferMapWalkThru.validate() || !sourceDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create sourceDataTransferMapWalkThru : " +
			   sourceDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'source',sheetName='Applications' where eavAttribute = ?",[sourceAttribute])
}
/**
*  Create License
*/
def licenseAttribute = EavAttribute.findByAttributeCodeAndEntityType('license',appEntityType)
if(licenseAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'license', frontendLabel='License' where id = ?",[licenseAttribute.id])
} else {
   licenseAttribute = new EavAttribute( attributeCode : "license",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'License',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !licenseAttribute.validate() || !licenseAttribute.save(flush:true) ) {
	   println"Unable to create licenseAttribute : "
	   licenseAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def licenseEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(licenseAttribute,appAttributeSet)
if(licenseEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 346, attributeCode = 'license' where attributeCode = 'license'")
} else {
   licenseEavEntityAttribute = new EavEntityAttribute(sortOrder:346,attribute:licenseAttribute,eavAttributeSet:appAttributeSet)
   if ( !licenseEavEntityAttribute.validate() || !licenseEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create licenseEavEntityAttribute : " +
			   licenseEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def licenseDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,licenseAttribute)
if( !licenseDataTransferMapMaster ){
   licenseDataTransferMapMaster = new DataTransferAttributeMap(columnName:"license",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:licenseAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !licenseDataTransferMapMaster.validate() || !licenseDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create licenseDataTransferMapMaster : " +
			   licenseDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'license',sheetName='Applications' where eavAttribute = ?",[licenseAttribute])
}

def licenseDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,licenseAttribute)
if(!licenseDataTransferMapWalkThru){
   licenseDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"license",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:licenseAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !licenseDataTransferMapWalkThru.validate() || !licenseDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create licenseDataTransferMapWalkThru : " +
			   licenseDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'license',sheetName='Applications' where eavAttribute = ?",[licenseAttribute])
}
/**
*  Create Description
*/
def descriptionAttribute = EavAttribute.findByAttributeCodeAndEntityType('description',appEntityType)
if(descriptionAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'description', frontendLabel='Description' where id = ?",[descriptionAttribute.id])
} else {
   descriptionAttribute = new EavAttribute( attributeCode : "description",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Description',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !descriptionAttribute.validate() || !descriptionAttribute.save(flush:true) ) {
	   println"Unable to create descriptionAttribute : "
	   descriptionAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def descriptionEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(descriptionAttribute,appAttributeSet)
if(descriptionEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 346, attributeCode = 'description' where attributeCode = 'description'")
} else {
   descriptionEavEntityAttribute = new EavEntityAttribute(sortOrder:346,attribute:descriptionAttribute,eavAttributeSet:appAttributeSet)
   if ( !descriptionEavEntityAttribute.validate() || !descriptionEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create descriptionEavEntityAttribute : " +
			   descriptionEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def descriptionDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,descriptionAttribute)
if( !descriptionDataTransferMapMaster ){
   descriptionDataTransferMapMaster = new DataTransferAttributeMap(columnName:"description",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:descriptionAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !descriptionDataTransferMapMaster.validate() || !descriptionDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create descriptionDataTransferMapMaster : " +
			   descriptionDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'description',sheetName='Applications' where eavAttribute = ?",[descriptionAttribute])
}

def descriptionDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,descriptionAttribute)
if(!descriptionDataTransferMapWalkThru){
   descriptionDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"description",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:descriptionAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !descriptionDataTransferMapWalkThru.validate() || !descriptionDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create descriptionDataTransferMapWalkThru : " +
			   descriptionDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'description',sheetName='Applications' where eavAttribute = ?",[descriptionAttribute])
}
/**
*  Create SupportType
*/
def supportTypeAttribute = EavAttribute.findByAttributeCodeAndEntityType('supportType',appEntityType)
if(supportTypeAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'supportType', frontendLabel='SupportType' where id = ?",[supportTypeAttribute.id])
} else {
   supportTypeAttribute = new EavAttribute( attributeCode : "supportType",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'SupportType',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !supportTypeAttribute.validate() || !supportTypeAttribute.save(flush:true) ) {
	   println"Unable to create supportTypeAttribute : "
	   supportTypeAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def supportTypeEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(supportTypeAttribute,appAttributeSet)
if(supportTypeEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 346, attributeCode = 'supportType' where attributeCode = 'supportType'")
} else {
   supportTypeEavEntityAttribute = new EavEntityAttribute(sortOrder:346,attribute:supportTypeAttribute,eavAttributeSet:appAttributeSet)
   if ( !supportTypeEavEntityAttribute.validate() || !supportTypeEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create supportTypeEavEntityAttribute : " +
			   supportTypeEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def supportTypeDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,supportTypeAttribute)
if( !supportTypeDataTransferMapMaster ){
   supportTypeDataTransferMapMaster = new DataTransferAttributeMap(columnName:"supportType",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:supportTypeAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !supportTypeDataTransferMapMaster.validate() || !supportTypeDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create supportTypeDataTransferMapMaster : " +
			   supportTypeDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'supportType',sheetName='Applications' where eavAttribute = ?",[supportTypeAttribute])
}

def supportTypeDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,supportTypeAttribute)
if(!supportTypeDataTransferMapWalkThru){
   supportTypeDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"supportType",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:supportTypeAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !supportTypeDataTransferMapWalkThru.validate() || !supportTypeDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create supportTypeDataTransferMapWalkThru : " +
			   supportTypeDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'supportType',sheetName='Applications' where eavAttribute = ?",[supportTypeAttribute])
}
/**
*  Create SME
*/
def smeAttribute = EavAttribute.findByAttributeCodeAndEntityType('sme',appEntityType)
if(smeAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'sme', frontendLabel='SME' where id = ?",[smeAttribute.id])
} else {
   smeAttribute = new EavAttribute( attributeCode : "sme",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'SME',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !smeAttribute.validate() || !smeAttribute.save(flush:true) ) {
	   println"Unable to create smeAttribute : "
	   smeAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def smeEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(smeAttribute,appAttributeSet)
if(smeEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 346, attributeCode = 'sme' where attributeCode = 'sme'")
} else {
   smeEavEntityAttribute = new EavEntityAttribute(sortOrder:346,attribute:smeAttribute,eavAttributeSet:appAttributeSet)
   if ( !smeEavEntityAttribute.validate() || !smeEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create smeEavEntityAttribute : " +
			   smeEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def smeDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,smeAttribute)
if( !smeDataTransferMapMaster ){
   smeDataTransferMapMaster = new DataTransferAttributeMap(columnName:"sme",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:smeAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !smeDataTransferMapMaster.validate() || !smeDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create smeDataTransferMapMaster : " +
			   smeDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'sme',sheetName='Applications' where eavAttribute = ?",[smeAttribute])
}

def smeDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,smeAttribute)
if(!smeDataTransferMapWalkThru){
   smeDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"sme",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:smeAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !smeDataTransferMapWalkThru.validate() || !smeDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create smeDataTransferMapWalkThru : " +
			   smeDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'sme',sheetName='Applications' where eavAttribute = ?",[smeAttribute])
}
/**
*  Create SME2
*/
def sme2Attribute = EavAttribute.findByAttributeCodeAndEntityType('sme2',appEntityType)
if(sme2Attribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'sme2', frontendLabel='SME2' where id = ?",[sme2Attribute.id])
} else {
   sme2Attribute = new EavAttribute( attributeCode : "sme2",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'SME2',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !sme2Attribute.validate() || !sme2Attribute.save(flush:true) ) {
	   println"Unable to create sme2Attribute : "
	   sme2Attribute.errors.allErrors.each() {println"\n"+it }
   }
}

def sme2EavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(sme2Attribute,appAttributeSet)
if(sme2EavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 346, attributeCode = 'sme2' where attributeCode = 'sme2'")
} else {
   sme2EavEntityAttribute = new EavEntityAttribute(sortOrder:346,attribute:sme2Attribute,eavAttributeSet:appAttributeSet)
   if ( !sme2EavEntityAttribute.validate() || !sme2EavEntityAttribute.save(flush:true) ) {
	   println"Unable to create sme2EavEntityAttribute : " +
			   sme2EavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def sme2DataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,sme2Attribute)
if( !sme2DataTransferMapMaster ){
   sme2DataTransferMapMaster = new DataTransferAttributeMap(columnName:"sme2",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:sme2Attribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !sme2DataTransferMapMaster.validate() || !sme2DataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create sme2DataTransferMapMaster : " +
			   sme2DataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'sme2',sheetName='Applications' where eavAttribute = ?",[sme2Attribute])
}

def sme2DataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,sme2Attribute)
if(!sme2DataTransferMapWalkThru){
   sme2DataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"sme2",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:sme2Attribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !sme2DataTransferMapWalkThru.validate() || !sme2DataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create sme2DataTransferMapWalkThru : " +
			   sme2DataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'sme2',sheetName='Applications' where eavAttribute = ?",[sme2Attribute])
}
/**
*  Create BusinessUnit
*/
def businessUnitAttribute = EavAttribute.findByAttributeCodeAndEntityType('businessUnit',appEntityType)
if(businessUnitAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'businessUnit', frontendLabel='BusinessUnit' where id = ?",[businessUnitAttribute.id])
} else {
   businessUnitAttribute = new EavAttribute( attributeCode : "businessUnit",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'BusinessUnit',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !businessUnitAttribute.validate() || !businessUnitAttribute.save(flush:true) ) {
	   println"Unable to create businessUnitAttribute : "
	   businessUnitAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def businessUnitEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(businessUnitAttribute,appAttributeSet)
if(businessUnitEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 346, attributeCode = 'businessUnit' where attributeCode = 'businessUnit'")
} else {
   businessUnitEavEntityAttribute = new EavEntityAttribute(sortOrder:346,attribute:businessUnitAttribute,eavAttributeSet:appAttributeSet)
   if ( !businessUnitEavEntityAttribute.validate() || !businessUnitEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create businessUnitEavEntityAttribute : " +
			   businessUnitEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def businessUnitDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,businessUnitAttribute)
if( !businessUnitDataTransferMapMaster ){
   businessUnitDataTransferMapMaster = new DataTransferAttributeMap(columnName:"businessUnit",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:businessUnitAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !businessUnitDataTransferMapMaster.validate() || !businessUnitDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create businessUnitDataTransferMapMaster : " +
			   businessUnitDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'businessUnit',sheetName='Applications' where eavAttribute = ?",[businessUnitAttribute])
}

def businessUnitDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,businessUnitAttribute)
if(!businessUnitDataTransferMapWalkThru){
   businessUnitDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"businessUnit",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:businessUnitAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !businessUnitDataTransferMapWalkThru.validate() || !businessUnitDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create businessUnitDataTransferMapWalkThru : " +
			   businessUnitDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'businessUnit',sheetName='Applications' where eavAttribute = ?",[businessUnitAttribute])
}
/**
*  Create Owner
*/
def ownerAttribute = EavAttribute.findByAttributeCodeAndEntityType('owner',appEntityType)
if(ownerAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'owner', frontendLabel='Owner' where id = ?",[ownerAttribute.id])
} else {
   ownerAttribute = new EavAttribute( attributeCode : "owner",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Owner',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !ownerAttribute.validate() || !ownerAttribute.save(flush:true) ) {
	   println"Unable to create ownerAttribute : "
	   ownerAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def ownerEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(ownerAttribute,appAttributeSet)
if(ownerEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 346, attributeCode = 'owner' where attributeCode = 'owner'")
} else {
   ownerEavEntityAttribute = new EavEntityAttribute(sortOrder:346,attribute:ownerAttribute,eavAttributeSet:appAttributeSet)
   if ( !ownerEavEntityAttribute.validate() || !ownerEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create ownerEavEntityAttribute : " +
			   ownerEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def ownerDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,ownerAttribute)
if( !ownerDataTransferMapMaster ){
   ownerDataTransferMapMaster = new DataTransferAttributeMap(columnName:"owner",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:ownerAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !ownerDataTransferMapMaster.validate() || !ownerDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create ownerDataTransferMapMaster : " +
			   ownerDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'owner',sheetName='Applications' where eavAttribute = ?",[ownerAttribute])
}

def ownerDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,ownerAttribute)
if(!ownerDataTransferMapWalkThru){
   ownerDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"owner",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:ownerAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !ownerDataTransferMapWalkThru.validate() || !ownerDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create ownerDataTransferMapWalkThru : " +
			   ownerDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'owner',sheetName='Applications' where eavAttribute = ?",[ownerAttribute])
}
/**
*  Create Retire
*/
def retireAttribute = EavAttribute.findByAttributeCodeAndEntityType('retire',appEntityType)
if(retireAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'retire', frontendLabel='Retire' where id = ?",[retireAttribute.id])
} else {
   retireAttribute = new EavAttribute( attributeCode : "retire",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Retire',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !retireAttribute.validate() || !retireAttribute.save(flush:true) ) {
	   println"Unable to create retireAttribute : "
	   retireAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def retireEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(retireAttribute,appAttributeSet)
if(retireEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 346, attributeCode = 'retire' where attributeCode = 'retire'")
} else {
   retireEavEntityAttribute = new EavEntityAttribute(sortOrder:346,attribute:retireAttribute,eavAttributeSet:appAttributeSet)
   if ( !retireEavEntityAttribute.validate() || !retireEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create retireEavEntityAttribute : " +
			   retireEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def retireDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,retireAttribute)
if( !retireDataTransferMapMaster ){
   retireDataTransferMapMaster = new DataTransferAttributeMap(columnName:"retire",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:retireAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !retireDataTransferMapMaster.validate() || !retireDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create retireDataTransferMapMaster : " +
			   retireDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'retire',sheetName='Applications' where eavAttribute = ?",[retireAttribute])
}

def retireDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,retireAttribute)
if(!retireDataTransferMapWalkThru){
   retireDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"retire",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:retireAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !retireDataTransferMapWalkThru.validate() || !retireDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create retireDataTransferMapWalkThru : " +
			   retireDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'retire',sheetName='Applications' where eavAttribute = ?",[retireAttribute])
}
/**
*  Create MaintExp
*/
def maintExpAttribute = EavAttribute.findByAttributeCodeAndEntityType('maintExp',appEntityType)
if(maintExpAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'maintExp', frontendLabel='MaintExp' where id = ?",[maintExpAttribute.id])
} else {
   maintExpAttribute = new EavAttribute( attributeCode : "maintExp",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'MaintExp',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !maintExpAttribute.validate() || !maintExpAttribute.save(flush:true) ) {
	   println"Unable to create maintExpAttribute : "
	   maintExpAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def maintExpEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(maintExpAttribute,appAttributeSet)
if(maintExpEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 346, attributeCode = 'maintExp' where attributeCode = 'maintExp'")
} else {
   maintExpEavEntityAttribute = new EavEntityAttribute(sortOrder:346,attribute:maintExpAttribute,eavAttributeSet:appAttributeSet)
   if ( !maintExpEavEntityAttribute.validate() || !maintExpEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create maintExpEavEntityAttribute : " +
			   maintExpEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def maintExpDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,maintExpAttribute)
if( !maintExpDataTransferMapMaster ){
   maintExpDataTransferMapMaster = new DataTransferAttributeMap(columnName:"maintExp",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:maintExpAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !maintExpDataTransferMapMaster.validate() || !maintExpDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create maintExpDataTransferMapMaster : " +
			   maintExpDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'maintExp',sheetName='Applications' where eavAttribute = ?",[maintExpAttribute])
}

def maintExpDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,maintExpAttribute)
if(!maintExpDataTransferMapWalkThru){
   maintExpDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"maintExp",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:maintExpAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !maintExpDataTransferMapWalkThru.validate() || !maintExpDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create maintExpDataTransferMapWalkThru : " +
			   maintExpDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'maintExp',sheetName='Applications' where eavAttribute = ?",[maintExpAttribute])
}
/**
*  Create Function
*/
def functionAttribute = EavAttribute.findByAttributeCodeAndEntityType('function',appEntityType)
if(functionAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'function', frontendLabel='Function' where id = ?",[functionAttribute.id])
} else {
   functionAttribute = new EavAttribute( attributeCode : "function",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Function',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !functionAttribute.validate() || !functionAttribute.save(flush:true) ) {
	   println"Unable to create functionAttribute : "
	   functionAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def functionEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(functionAttribute,appAttributeSet)
if(functionEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 346, attributeCode = 'function' where attributeCode = 'function'")
} else {
   functionEavEntityAttribute = new EavEntityAttribute(sortOrder:346,attribute:functionAttribute,eavAttributeSet:appAttributeSet)
   if ( !functionEavEntityAttribute.validate() || !functionEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create functionEavEntityAttribute : " +
			   functionEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def functionDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,functionAttribute)
if( !functionDataTransferMapMaster ){
   functionDataTransferMapMaster = new DataTransferAttributeMap(columnName:"function",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:functionAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !functionDataTransferMapMaster.validate() || !functionDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create functionDataTransferMapMaster : " +
			   functionDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'function',sheetName='Applications' where eavAttribute = ?",[functionAttribute])
}

def functionDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,functionAttribute)
if(!functionDataTransferMapWalkThru){
   functionDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"function",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:functionAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !functionDataTransferMapWalkThru.validate() || !functionDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create functionDataTransferMapWalkThru : " +
			   functionDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'function',sheetName='Applications' where eavAttribute = ?",[functionAttribute])
}
/**
*  Create Environment
*/
def environmentAttribute = EavAttribute.findByAttributeCodeAndEntityType('environment',appEntityType)
if(environmentAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'environment', frontendLabel='Environment' where id = ?",[environmentAttribute.id])
} else {
   environmentAttribute = new EavAttribute( attributeCode : "environment",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Environment',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !environmentAttribute.validate() || !environmentAttribute.save(flush:true) ) {
	   println"Unable to create environmentAttribute : "
	   environmentAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def environmentEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(environmentAttribute,appAttributeSet)
if(environmentEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 346, attributeCode = 'environment' where attributeCode = 'environment'")
} else {
   environmentEavEntityAttribute = new EavEntityAttribute(sortOrder:346,attribute:environmentAttribute,eavAttributeSet:appAttributeSet)
   if ( !environmentEavEntityAttribute.validate() || !environmentEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create environmentEavEntityAttribute : " +
			   environmentEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def environmentDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,environmentAttribute)
if( !environmentDataTransferMapMaster ){
   environmentDataTransferMapMaster = new DataTransferAttributeMap(columnName:"environment",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:environmentAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !environmentDataTransferMapMaster.validate() || !environmentDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create environmentDataTransferMapMaster : " +
			   environmentDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'environment',sheetName='Applications' where eavAttribute = ?",[environmentAttribute])
}

def environmentDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,environmentAttribute)
if(!environmentDataTransferMapWalkThru){
   environmentDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"environment",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:environmentAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !environmentDataTransferMapWalkThru.validate() || !environmentDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create environmentDataTransferMapWalkThru : " +
			   environmentDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'environment',sheetName='Applications' where eavAttribute = ?",[environmentAttribute])
}
/**
*  Create Criticality
*/
def criticalityAttribute = EavAttribute.findByAttributeCodeAndEntityType('criticality',appEntityType)
if(criticalityAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'criticality', frontendLabel='Criticality' where id = ?",[criticalityAttribute.id])
} else {
   criticalityAttribute = new EavAttribute( attributeCode : "criticality",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Criticality',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !criticalityAttribute.validate() || !criticalityAttribute.save(flush:true) ) {
	   println"Unable to create criticalityAttribute : "
	   criticalityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def criticalityEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(criticalityAttribute,appAttributeSet)
if(criticalityEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 346, attributeCode = 'criticality' where attributeCode = 'criticality'")
} else {
   criticalityEavEntityAttribute = new EavEntityAttribute(sortOrder:346,attribute:criticalityAttribute,eavAttributeSet:appAttributeSet)
   if ( !criticalityEavEntityAttribute.validate() || !criticalityEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create criticalityEavEntityAttribute : " +
			   criticalityEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def criticalityDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,criticalityAttribute)
if( !criticalityDataTransferMapMaster ){
   criticalityDataTransferMapMaster = new DataTransferAttributeMap(columnName:"criticality",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:criticalityAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !criticalityDataTransferMapMaster.validate() || !criticalityDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create criticalityDataTransferMapMaster : " +
			   criticalityDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'criticality',sheetName='Applications' where eavAttribute = ?",[criticalityAttribute])
}

def criticalityDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,criticalityAttribute)
if(!criticalityDataTransferMapWalkThru){
   criticalityDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"criticality",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:criticalityAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !criticalityDataTransferMapWalkThru.validate() || !criticalityDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create criticalityDataTransferMapWalkThru : " +
			   criticalityDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'criticality',sheetName='Applications' where eavAttribute = ?",[criticalityAttribute])
}
/**
*  Create MoveBundle
*/
def moveBundleAttribute = EavAttribute.findByAttributeCodeAndEntityType('moveBundle',appEntityType)
if(moveBundleAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'moveBundle', frontendLabel='MoveBundle' where id = ?",[moveBundleAttribute.id])
} else {
   moveBundleAttribute = new EavAttribute( attributeCode : "moveBundle",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'MoveBundle',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !moveBundleAttribute.validate() || !moveBundleAttribute.save(flush:true) ) {
	   println"Unable to create moveBundleAttribute : "
	   moveBundleAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def moveBundleEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(moveBundleAttribute,appAttributeSet)
if(moveBundleEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 346, attributeCode = 'moveBundle' where attributeCode = 'moveBundle'")
} else {
   moveBundleEavEntityAttribute = new EavEntityAttribute(sortOrder:346,attribute:moveBundleAttribute,eavAttributeSet:appAttributeSet)
   if ( !moveBundleEavEntityAttribute.validate() || !moveBundleEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create moveBundleEavEntityAttribute : " +
			   moveBundleEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def moveBundleDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,moveBundleAttribute)
if( !moveBundleDataTransferMapMaster ){
   moveBundleDataTransferMapMaster = new DataTransferAttributeMap(columnName:"moveBundle",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:moveBundleAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !moveBundleDataTransferMapMaster.validate() || !moveBundleDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create moveBundleDataTransferMapMaster : " +
			   moveBundleDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'moveBundle',sheetName='Applications' where eavAttribute = ?",[moveBundleAttribute])
}

def moveBundleDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,moveBundleAttribute)
if(!moveBundleDataTransferMapWalkThru){
   moveBundleDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"moveBundle",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:moveBundleAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !moveBundleDataTransferMapWalkThru.validate() || !moveBundleDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create moveBundleDataTransferMapWalkThru : " +
			   moveBundleDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'moveBundle',sheetName='Applications' where eavAttribute = ?",[moveBundleAttribute])
}
/**
*  Create PlanStatus
*/
def planStatusAttribute = EavAttribute.findByAttributeCodeAndEntityType('planStatus',appEntityType)
if(planStatusAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'planStatus', frontendLabel='PlanStatus' where id = ?",[planStatusAttribute.id])
} else {
   planStatusAttribute = new EavAttribute( attributeCode : "planStatus",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'PlanStatus',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !planStatusAttribute.validate() || !planStatusAttribute.save(flush:true) ) {
	   println"Unable to create planStatusAttribute : "
	   planStatusAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def planStatusEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(planStatusAttribute,appAttributeSet)
if(planStatusEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 346, attributeCode = 'planStatus' where attributeCode = 'planStatus'")
} else {
   planStatusEavEntityAttribute = new EavEntityAttribute(sortOrder:346,attribute:planStatusAttribute,eavAttributeSet:appAttributeSet)
   if ( !planStatusEavEntityAttribute.validate() || !planStatusEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create planStatusEavEntityAttribute : " +
			   planStatusEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def planStatusDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,planStatusAttribute)
if( !planStatusDataTransferMapMaster ){
   planStatusDataTransferMapMaster = new DataTransferAttributeMap(columnName:"planStatus",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:planStatusAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !planStatusDataTransferMapMaster.validate() || !planStatusDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create planStatusDataTransferMapMaster : " +
			   planStatusDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'planStatus',sheetName='Applications' where eavAttribute = ?",[planStatusAttribute])
}

def planStatusDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,planStatusAttribute)
if(!planStatusDataTransferMapWalkThru){
   planStatusDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"planStatus",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:planStatusAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !planStatusDataTransferMapWalkThru.validate() || !planStatusDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create planStatusDataTransferMapWalkThru : " +
			   planStatusDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'planStatus',sheetName='Applications' where eavAttribute = ?",[planStatusAttribute])
}
/**
*  Create TotalUsers
*/
def totalUsersAttribute = EavAttribute.findByAttributeCodeAndEntityType('totalUsers',appEntityType)
if(totalUsersAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'totalUsers', frontendLabel='TotalUsers' where id = ?",[totalUsersAttribute.id])
} else {
   totalUsersAttribute = new EavAttribute( attributeCode : "totalUsers",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'TotalUsers',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !totalUsersAttribute.validate() || !totalUsersAttribute.save(flush:true) ) {
	   println"Unable to create totalUsersAttribute : "
	   totalUsersAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def totalUsersEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(totalUsersAttribute,appAttributeSet)
if(totalUsersEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 346, attributeCode = 'totalUsers' where attributeCode = 'totalUsers'")
} else {
   totalUsersEavEntityAttribute = new EavEntityAttribute(sortOrder:346,attribute:totalUsersAttribute,eavAttributeSet:appAttributeSet)
   if ( !totalUsersEavEntityAttribute.validate() || !totalUsersEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create totalUsersEavEntityAttribute : " +
			   totalUsersEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def totalUsersDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,totalUsersAttribute)
if( !totalUsersDataTransferMapMaster ){
   totalUsersDataTransferMapMaster = new DataTransferAttributeMap(columnName:"totalUsers",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:totalUsersAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !totalUsersDataTransferMapMaster.validate() || !totalUsersDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create totalUsersDataTransferMapMaster : " +
			   totalUsersDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'totalUsers',sheetName='Applications' where eavAttribute = ?",[totalUsersAttribute])
}

def totalUsersDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,totalUsersAttribute)
if(!totalUsersDataTransferMapWalkThru){
   totalUsersDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"totalUsers",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:totalUsersAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !totalUsersDataTransferMapWalkThru.validate() || !totalUsersDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create totalUsersDataTransferMapWalkThru : " +
			   totalUsersDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'totalUsers',sheetName='Applications' where eavAttribute = ?",[totalUsersAttribute])
}
/**
*  Create UserLocations
*/
def userLocationsAttribute = EavAttribute.findByAttributeCodeAndEntityType('userLocations',appEntityType)
if(userLocationsAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'userLocations', frontendLabel='UserLocations' where id = ?",[userLocationsAttribute.id])
} else {
   userLocationsAttribute = new EavAttribute( attributeCode : "userLocations",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'UserLocations',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !userLocationsAttribute.validate() || !userLocationsAttribute.save(flush:true) ) {
	   println"Unable to create userLocationsAttribute : "
	   userLocationsAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def userLocationsEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(userLocationsAttribute,appAttributeSet)
if(userLocationsEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 346, attributeCode = 'userLocations' where attributeCode = 'userLocations'")
} else {
   userLocationsEavEntityAttribute = new EavEntityAttribute(sortOrder:346,attribute:userLocationsAttribute,eavAttributeSet:appAttributeSet)
   if ( !userLocationsEavEntityAttribute.validate() || !userLocationsEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create userLocationsEavEntityAttribute : " +
			   userLocationsEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def userLocationsDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,userLocationsAttribute)
if( !userLocationsDataTransferMapMaster ){
   userLocationsDataTransferMapMaster = new DataTransferAttributeMap(columnName:"userLocations",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:userLocationsAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !userLocationsDataTransferMapMaster.validate() || !userLocationsDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create userLocationsDataTransferMapMaster : " +
			   userLocationsDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'userLocations',sheetName='Applications' where eavAttribute = ?",[userLocationsAttribute])
}

def userLocationsDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,userLocationsAttribute)
if(!userLocationsDataTransferMapWalkThru){
   userLocationsDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"userLocations",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:userLocationsAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !userLocationsDataTransferMapWalkThru.validate() || !userLocationsDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create userLocationsDataTransferMapWalkThru : " +
			   userLocationsDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'userLocations',sheetName='Applications' where eavAttribute = ?",[userLocationsAttribute])
}
/**
*  Create ConcurrentUsers
*/
def concurrentUsersAttribute = EavAttribute.findByAttributeCodeAndEntityType('concurrentUsers',appEntityType)
if(concurrentUsersAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'concurrentUsers', frontendLabel='ConcurrentUsers' where id = ?",[concurrentUsersAttribute.id])
} else {
   concurrentUsersAttribute = new EavAttribute( attributeCode : "concurrentUsers",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'ConcurrentUsers',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !concurrentUsersAttribute.validate() || !concurrentUsersAttribute.save(flush:true) ) {
	   println"Unable to create concurrentUsersAttribute : "
	   concurrentUsersAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def concurrentUsersEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(concurrentUsersAttribute,appAttributeSet)
if(concurrentUsersEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 346, attributeCode = 'concurrentUsers' where attributeCode = 'concurrentUsers'")
} else {
   concurrentUsersEavEntityAttribute = new EavEntityAttribute(sortOrder:346,attribute:concurrentUsersAttribute,eavAttributeSet:appAttributeSet)
   if ( !concurrentUsersEavEntityAttribute.validate() || !concurrentUsersEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create concurrentUsersEavEntityAttribute : " +
			   concurrentUsersEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def concurrentUsersDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,concurrentUsersAttribute)
if( !concurrentUsersDataTransferMapMaster ){
   concurrentUsersDataTransferMapMaster = new DataTransferAttributeMap(columnName:"concurrentUsers",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:concurrentUsersAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !concurrentUsersDataTransferMapMaster.validate() || !concurrentUsersDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create concurrentUsersDataTransferMapMaster : " +
			   concurrentUsersDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'concurrentUsers',sheetName='Applications' where eavAttribute = ?",[concurrentUsersAttribute])
}

def concurrentUsersDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,concurrentUsersAttribute)
if(!concurrentUsersDataTransferMapWalkThru){
   concurrentUsersDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"concurrentUsers",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:concurrentUsersAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !concurrentUsersDataTransferMapWalkThru.validate() || !concurrentUsersDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create concurrentUsersDataTransferMapWalkThru : " +
			   concurrentUsersDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'concurrentUsers',sheetName='Applications' where eavAttribute = ?",[concurrentUsersAttribute])
}
/**
*  Create Frequency
*/
def frequencyAttribute = EavAttribute.findByAttributeCodeAndEntityType('frequency',appEntityType)
if(frequencyAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'frequency', frontendLabel='Frequency' where id = ?",[frequencyAttribute.id])
} else {
   frequencyAttribute = new EavAttribute( attributeCode : "frequency",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Frequency',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !frequencyAttribute.validate() || !frequencyAttribute.save(flush:true) ) {
	   println"Unable to create frequencyAttribute : "
	   frequencyAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def frequencyEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(frequencyAttribute,appAttributeSet)
if(frequencyEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 346, attributeCode = 'frequency' where attributeCode = 'frequency'")
} else {
   frequencyEavEntityAttribute = new EavEntityAttribute(sortOrder:346,attribute:frequencyAttribute,eavAttributeSet:appAttributeSet)
   if ( !frequencyEavEntityAttribute.validate() || !frequencyEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create frequencyEavEntityAttribute : " +
			   frequencyEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def frequencyDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,frequencyAttribute)
if( !frequencyDataTransferMapMaster ){
   frequencyDataTransferMapMaster = new DataTransferAttributeMap(columnName:"frequency",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:frequencyAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !frequencyDataTransferMapMaster.validate() || !frequencyDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create frequencyDataTransferMapMaster : " +
			   frequencyDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'frequency',sheetName='Applications' where eavAttribute = ?",[frequencyAttribute])
}

def frequencyDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,frequencyAttribute)
if(!frequencyDataTransferMapWalkThru){
   frequencyDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"frequency",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:frequencyAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !frequencyDataTransferMapWalkThru.validate() || !frequencyDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create frequencyDataTransferMapWalkThru : " +
			   frequencyDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'frequency',sheetName='Applications' where eavAttribute = ?",[frequencyAttribute])
}
/**
*  Create RPO
*/
def rpoAttribute = EavAttribute.findByAttributeCodeAndEntityType('rpo',appEntityType)
if(rpoAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'rpo', frontendLabel='RPO' where id = ?",[rpoAttribute.id])
} else {
   rpoAttribute = new EavAttribute( attributeCode : "rpo",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'RPO',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !rpoAttribute.validate() || !rpoAttribute.save(flush:true) ) {
	   println"Unable to create rpoAttribute : "
	   rpoAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def rpoEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(rpoAttribute,appAttributeSet)
if(rpoEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 346, attributeCode = 'rpo' where attributeCode = 'rpo'")
} else {
   rpoEavEntityAttribute = new EavEntityAttribute(sortOrder:346,attribute:rpoAttribute,eavAttributeSet:appAttributeSet)
   if ( !rpoEavEntityAttribute.validate() || !rpoEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create rpoEavEntityAttribute : " +
			   rpoEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def rpoDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,rpoAttribute)
if( !rpoDataTransferMapMaster ){
   rpoDataTransferMapMaster = new DataTransferAttributeMap(columnName:"rpo",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:rpoAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !rpoDataTransferMapMaster.validate() || !rpoDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create rpoDataTransferMapMaster : " +
			   rpoDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'rpo',sheetName='Applications' where eavAttribute = ?",[rpoAttribute])
}

def rpoDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,rpoAttribute)
if(!rpoDataTransferMapWalkThru){
   rpoDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"rpo",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:rpoAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !rpoDataTransferMapWalkThru.validate() || !rpoDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create rpoDataTransferMapWalkThru : " +
			   rpoDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'rpo',sheetName='Applications' where eavAttribute = ?",[rpoAttribute])
}
/**
*  Create RTO
*/
def rtoAttribute = EavAttribute.findByAttributeCodeAndEntityType('rto',appEntityType)
if(rtoAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'rto', frontendLabel='RTO' where id = ?",[rtoAttribute.id])
} else {
   rtoAttribute = new EavAttribute( attributeCode : "rto",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'RTO',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !rtoAttribute.validate() || !rtoAttribute.save(flush:true) ) {
	   println"Unable to create rtoAttribute : "
	   rtoAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def rtoEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(rtoAttribute,appAttributeSet)
if(rtoEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 346, attributeCode = 'rto' where attributeCode = 'rto'")
} else {
   rtoEavEntityAttribute = new EavEntityAttribute(sortOrder:346,attribute:rtoAttribute,eavAttributeSet:appAttributeSet)
   if ( !rtoEavEntityAttribute.validate() || !rtoEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create rtoEavEntityAttribute : " +
			   rtoEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def rtoDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,rtoAttribute)
if( !rtoDataTransferMapMaster ){
   rtoDataTransferMapMaster = new DataTransferAttributeMap(columnName:"rto",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:rtoAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !rtoDataTransferMapMaster.validate() || !rtoDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create rtoDataTransferMapMaster : " +
			   rtoDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'rto',sheetName='Applications' where eavAttribute = ?",[rtoAttribute])
}

def rtoDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,rtoAttribute)
if(!rtoDataTransferMapWalkThru){
   rtoDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"rto",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:rtoAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !rtoDataTransferMapWalkThru.validate() || !rtoDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create rtoDataTransferMapWalkThru : " +
			   rtoDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'rto',sheetName='Applications' where eavAttribute = ?",[rtoAttribute])
}

/**
*  Create DowntimeTolerance
*/
def downtimeToleranceAttribute = EavAttribute.findByAttributeCodeAndEntityType('downtimeTolerance',appEntityType)
if(downtimeToleranceAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'downtimeTolerance', frontendLabel='DowntimeTolerance' where id = ?",[downtimeToleranceAttribute.id])
} else {
   downtimeToleranceAttribute = new EavAttribute( attributeCode : "downtimeTolerance",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'DowntimeTolerance',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !downtimeToleranceAttribute.validate() || !downtimeToleranceAttribute.save(flush:true) ) {
	   println"Unable to create downtimeToleranceAttribute : "
	   downtimeToleranceAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def downtimeToleranceEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(downtimeToleranceAttribute,appAttributeSet)
if(downtimeToleranceEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 346, attributeCode = 'downtimeTolerance' where attributeCode = 'downtimeTolerance'")
} else {
   downtimeToleranceEavEntityAttribute = new EavEntityAttribute(sortOrder:346,attribute:downtimeToleranceAttribute,eavAttributeSet:appAttributeSet)
   if ( !downtimeToleranceEavEntityAttribute.validate() || !downtimeToleranceEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create downtimeToleranceEavEntityAttribute : " +
			   downtimeToleranceEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def downtimeToleranceDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,downtimeToleranceAttribute)
if( !downtimeToleranceDataTransferMapMaster ){
   downtimeToleranceDataTransferMapMaster = new DataTransferAttributeMap(columnName:"downtimeTolerance",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:downtimeToleranceAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !downtimeToleranceDataTransferMapMaster.validate() || !downtimeToleranceDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create downtimeToleranceDataTransferMapMaster : " +
			   downtimeToleranceDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'downtimeTolerance',sheetName='Applications' where eavAttribute = ?",[downtimeToleranceAttribute])
}

def downtimeToleranceDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,downtimeToleranceAttribute)
if(!downtimeToleranceDataTransferMapWalkThru){
   downtimeToleranceDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"downtimeTolerance",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:downtimeToleranceAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !downtimeToleranceDataTransferMapWalkThru.validate() || !downtimeToleranceDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create downtimeToleranceDataTransferMapWalkThru : " +
			   downtimeToleranceDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'downtimeTolerance',sheetName='Applications' where eavAttribute = ?",[downtimeToleranceAttribute])
}