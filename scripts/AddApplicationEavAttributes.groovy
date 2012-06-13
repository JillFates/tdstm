
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
def nameAttribute = EavAttribute.findByAttributeCodeAndEntityType('assetName',appEntityType)
if(nameAttribute){
	EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'assetName', frontendLabel='Name' where id = ?",[nameAttribute.id])
} else {
	nameAttribute = new EavAttribute( attributeCode : "assetName",
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
	EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 10, attributeCode = 'assetName' where attributeCode = 'assetName'")
} else {
	nameEavEntityAttribute = new EavEntityAttribute(sortOrder:10,attribute:nameAttribute,eavAttributeSet:appAttributeSet)
	if ( !nameEavEntityAttribute.validate() || !nameEavEntityAttribute.save(flush:true) ) {
		println"Unable to create nameEavEntityAttribute : " +
				nameEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
	}
}

def nameDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,nameAttribute)
if( !nameDataTransferMapMaster ){
	nameDataTransferMapMaster = new DataTransferAttributeMap(columnName:"Name",
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
	DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'assetName',sheetName='Applications' where eavAttribute = ?",[nameAttribute])
}

def nameDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,nameAttribute)
if(!nameDataTransferMapWalkThru){
	nameDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Name",
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
	DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Name',sheetName='Applications' where eavAttribute = ?",[nameAttribute])
}
/**
*  Create Vendor
*/
def vendorAttribute = EavAttribute.findByAttributeCodeAndEntityType('appVendor',appEntityType)
if(vendorAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'appVendor', frontendLabel='Vendor' where id = ?",[vendorAttribute.id])
} else {
   vendorAttribute = new EavAttribute( attributeCode : "appVendor",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Vendor',
		   note : 'this field is used for just import',
		   sortOrder : 20,
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
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 20, attributeCode = 'appVendor' where attributeCode = 'appVendor'")
} else {
   vendorEavEntityAttribute = new EavEntityAttribute(sortOrder:20,attribute:vendorAttribute,eavAttributeSet:appAttributeSet)
   if ( !vendorEavEntityAttribute.validate() || !vendorEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create vendorEavEntityAttribute : " +
			   vendorEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def vendorDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,vendorAttribute)
if( !vendorDataTransferMapMaster ){
   vendorDataTransferMapMaster = new DataTransferAttributeMap(columnName:"Vendor",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Vendor',sheetName='Applications' where eavAttribute = ?",[vendorAttribute])
}

def vendorDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,vendorAttribute)
if(!vendorDataTransferMapWalkThru){
   vendorDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Vendor",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Vendor',sheetName='Applications' where eavAttribute = ?",[vendorAttribute])
}
/**
*  Create Version
*/
def versionAttribute = EavAttribute.findByAttributeCodeAndEntityType('appVersion',appEntityType)
if(versionAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'appVersion', frontendLabel='Version' where id = ?",[versionAttribute.id])
} else {
   versionAttribute = new EavAttribute( attributeCode : "appVersion",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Version',
		   note : 'this field is used for just import',
		   sortOrder : 30,
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
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 30, attributeCode = 'appVersion' where attributeCode = 'appVersion'")
} else {
   versionEavEntityAttribute = new EavEntityAttribute(sortOrder:30,attribute:versionAttribute,eavAttributeSet:appAttributeSet)
   if ( !versionEavEntityAttribute.validate() || !versionEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create versionEavEntityAttribute : " +
			   versionEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def versionDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,versionAttribute)
if( !versionDataTransferMapMaster ){
   versionDataTransferMapMaster = new DataTransferAttributeMap(columnName:"Version",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Version',sheetName='Applications' where eavAttribute = ?",[versionAttribute])
}

def versionDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,versionAttribute)
if(!versionDataTransferMapWalkThru){
   versionDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Version",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Version',sheetName='Applications' where eavAttribute = ?",[versionAttribute])
}
/**
*  Create Technology
*/
def technologyAttribute = EavAttribute.findByAttributeCodeAndEntityType('appTech',appEntityType)
if(technologyAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'appTech', frontendLabel='Technology' where id = ?",[technologyAttribute.id])
} else {
   technologyAttribute = new EavAttribute( attributeCode : "appTech",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Technology',
		   note : 'this field is used for just import',
		   sortOrder : 40,
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
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 40, attributeCode = 'appTech' where attributeCode = 'appTech'")
} else {
   technologyEavEntityAttribute = new EavEntityAttribute(sortOrder:40,attribute:technologyAttribute,eavAttributeSet:appAttributeSet)
   if ( !technologyEavEntityAttribute.validate() || !technologyEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create technologyEavEntityAttribute : " +
			   technologyEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def technologyDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,technologyAttribute)
if( !technologyDataTransferMapMaster ){
   technologyDataTransferMapMaster = new DataTransferAttributeMap(columnName:"Technology",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Technology',sheetName='Applications' where eavAttribute = ?",[technologyAttribute])
}

def technologyDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,technologyAttribute)
if(!technologyDataTransferMapWalkThru){
   technologyDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Technology",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Technology',sheetName='Applications' where eavAttribute = ?",[technologyAttribute])
}
/**
*  Create AccessType
*/
def accessTypeAttribute = EavAttribute.findByAttributeCodeAndEntityType('appAccess',appEntityType)
if(accessTypeAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'appAccess', frontendLabel='AccessType' where id = ?",[accessTypeAttribute.id])
} else {
   accessTypeAttribute = new EavAttribute( attributeCode : "appAccess",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'AccessType',
		   note : 'this field is used for just import',
		   sortOrder : 50,
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
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 50, attributeCode = 'appAccess' where attributeCode = 'appAccess'")
} else {
   accessTypeEavEntityAttribute = new EavEntityAttribute(sortOrder:50,attribute:accessTypeAttribute,eavAttributeSet:appAttributeSet)
   if ( !accessTypeEavEntityAttribute.validate() || !accessTypeEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create accessTypeEavEntityAttribute : " +
			   accessTypeEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def accessTypeDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,accessTypeAttribute)
if( !accessTypeDataTransferMapMaster ){
   accessTypeDataTransferMapMaster = new DataTransferAttributeMap(columnName:"AccessType",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'AccessType',sheetName='Applications' where eavAttribute = ?",[accessTypeAttribute])
}

def accessTypeDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,accessTypeAttribute)
if(!accessTypeDataTransferMapWalkThru){
   accessTypeDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"AccessType",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'AccessType',sheetName='Applications' where eavAttribute = ?",[accessTypeAttribute])
}
/**
*  Create Source
*/
def sourceAttribute = EavAttribute.findByAttributeCodeAndEntityType('appSource',appEntityType)
if(sourceAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'appSource', frontendLabel='Source' where id = ?",[sourceAttribute.id])
} else {
   sourceAttribute = new EavAttribute( attributeCode : "appSource",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Source',
		   note : 'this field is used for just import',
		   sortOrder : 60,
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
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 60, attributeCode = 'appSource' where attributeCode = 'appSource'")
} else {
   sourceEavEntityAttribute = new EavEntityAttribute(sortOrder:60,attribute:sourceAttribute,eavAttributeSet:appAttributeSet)
   if ( !sourceEavEntityAttribute.validate() || !sourceEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create sourceEavEntityAttribute : " +
			   sourceEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def sourceDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,sourceAttribute)
if( !sourceDataTransferMapMaster ){
   sourceDataTransferMapMaster = new DataTransferAttributeMap(columnName:"Source",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Source',sheetName='Applications' where eavAttribute = ?",[sourceAttribute])
}

def sourceDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,sourceAttribute)
if(!sourceDataTransferMapWalkThru){
   sourceDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Source",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Source',sheetName='Applications' where eavAttribute = ?",[sourceAttribute])
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
		   sortOrder : 70,
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
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 70, attributeCode = 'license' where attributeCode = 'license'")
} else {
   licenseEavEntityAttribute = new EavEntityAttribute(sortOrder:70,attribute:licenseAttribute,eavAttributeSet:appAttributeSet)
   if ( !licenseEavEntityAttribute.validate() || !licenseEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create licenseEavEntityAttribute : " +
			   licenseEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def licenseDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,licenseAttribute)
if( !licenseDataTransferMapMaster ){
   licenseDataTransferMapMaster = new DataTransferAttributeMap(columnName:"License",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'License',sheetName='Applications' where eavAttribute = ?",[licenseAttribute])
}

def licenseDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,licenseAttribute)
if(!licenseDataTransferMapWalkThru){
   licenseDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"License",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'License',sheetName='Applications' where eavAttribute = ?",[licenseAttribute])
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
		   sortOrder : 80,
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
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 80, attributeCode = 'description' where attributeCode = 'description'")
} else {
   descriptionEavEntityAttribute = new EavEntityAttribute(sortOrder:80,attribute:descriptionAttribute,eavAttributeSet:appAttributeSet)
   if ( !descriptionEavEntityAttribute.validate() || !descriptionEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create descriptionEavEntityAttribute : " +
			   descriptionEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def descriptionDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,descriptionAttribute)
if( !descriptionDataTransferMapMaster ){
   descriptionDataTransferMapMaster = new DataTransferAttributeMap(columnName:"Description",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Description',sheetName='Applications' where eavAttribute = ?",[descriptionAttribute])
}

def descriptionDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,descriptionAttribute)
if(!descriptionDataTransferMapWalkThru){
   descriptionDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Description",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Description',sheetName='Applications' where eavAttribute = ?",[descriptionAttribute])
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
		   sortOrder : 90,
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
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 90, attributeCode = 'supportType' where attributeCode = 'supportType'")
} else {
   supportTypeEavEntityAttribute = new EavEntityAttribute(sortOrder:90,attribute:supportTypeAttribute,eavAttributeSet:appAttributeSet)
   if ( !supportTypeEavEntityAttribute.validate() || !supportTypeEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create supportTypeEavEntityAttribute : " +
			   supportTypeEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def supportTypeDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,supportTypeAttribute)
if( !supportTypeDataTransferMapMaster ){
   supportTypeDataTransferMapMaster = new DataTransferAttributeMap(columnName:"SupportType",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'SupportType',sheetName='Applications' where eavAttribute = ?",[supportTypeAttribute])
}

def supportTypeDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,supportTypeAttribute)
if(!supportTypeDataTransferMapWalkThru){
   supportTypeDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"SupportType",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'SupportType',sheetName='Applications' where eavAttribute = ?",[supportTypeAttribute])
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
		   sortOrder : 100,
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
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 100, attributeCode = 'sme' where attributeCode = 'sme'")
} else {
   smeEavEntityAttribute = new EavEntityAttribute(sortOrder:100,attribute:smeAttribute,eavAttributeSet:appAttributeSet)
   if ( !smeEavEntityAttribute.validate() || !smeEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create smeEavEntityAttribute : " +
			   smeEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def smeDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,smeAttribute)
if( !smeDataTransferMapMaster ){
   smeDataTransferMapMaster = new DataTransferAttributeMap(columnName:"SME",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'SME',sheetName='Applications' where eavAttribute = ?",[smeAttribute])
}

def smeDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,smeAttribute)
if(!smeDataTransferMapWalkThru){
   smeDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"SME",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'SME',sheetName='Applications' where eavAttribute = ?",[smeAttribute])
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
		   sortOrder : 110,
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
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 120, attributeCode = 'sme2' where attributeCode = 'sme2'")
} else {
   sme2EavEntityAttribute = new EavEntityAttribute(sortOrder:120,attribute:sme2Attribute,eavAttributeSet:appAttributeSet)
   if ( !sme2EavEntityAttribute.validate() || !sme2EavEntityAttribute.save(flush:true) ) {
	   println"Unable to create sme2EavEntityAttribute : " +
			   sme2EavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def sme2DataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,sme2Attribute)
if( !sme2DataTransferMapMaster ){
   sme2DataTransferMapMaster = new DataTransferAttributeMap(columnName:"SME2",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'SME2',sheetName='Applications' where eavAttribute = ?",[sme2Attribute])
}

def sme2DataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,sme2Attribute)
if(!sme2DataTransferMapWalkThru){
   sme2DataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"SME2",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'SME2',sheetName='Applications' where eavAttribute = ?",[sme2Attribute])
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
		   sortOrder : 130,
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
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 130, attributeCode = 'businessUnit' where attributeCode = 'businessUnit'")
} else {
   businessUnitEavEntityAttribute = new EavEntityAttribute(sortOrder:130,attribute:businessUnitAttribute,eavAttributeSet:appAttributeSet)
   if ( !businessUnitEavEntityAttribute.validate() || !businessUnitEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create businessUnitEavEntityAttribute : " +
			   businessUnitEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def businessUnitDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,businessUnitAttribute)
if( !businessUnitDataTransferMapMaster ){
   businessUnitDataTransferMapMaster = new DataTransferAttributeMap(columnName:"BusinessUnit",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'BusinessUnit',sheetName='Applications' where eavAttribute = ?",[businessUnitAttribute])
}

def businessUnitDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,businessUnitAttribute)
if(!businessUnitDataTransferMapWalkThru){
   businessUnitDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"BusinessUnit",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'BusinessUnit',sheetName='Applications' where eavAttribute = ?",[businessUnitAttribute])
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
		   sortOrder : 140,
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
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 140, attributeCode = 'owner' where attributeCode = 'owner'")
} else {
   ownerEavEntityAttribute = new EavEntityAttribute(sortOrder:140,attribute:ownerAttribute,eavAttributeSet:appAttributeSet)
   if ( !ownerEavEntityAttribute.validate() || !ownerEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create ownerEavEntityAttribute : " +
			   ownerEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def ownerDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,ownerAttribute)
if( !ownerDataTransferMapMaster ){
   ownerDataTransferMapMaster = new DataTransferAttributeMap(columnName:"Owner",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Owner',sheetName='Applications' where eavAttribute = ?",[ownerAttribute])
}

def ownerDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,ownerAttribute)
if(!ownerDataTransferMapWalkThru){
   ownerDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Owner",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Owner',sheetName='Applications' where eavAttribute = ?",[ownerAttribute])
}
/**
*  Create Retire
*/
def retireAttribute = EavAttribute.findByAttributeCodeAndEntityType('retireDate',appEntityType)
if(retireAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'retireDate', frontendLabel='Retire' where id = ?",[retireAttribute.id])
} else {
   retireAttribute = new EavAttribute( attributeCode : "retireDate",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Retire',
		   note : 'this field is used for just import',
		   sortOrder : 150,
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
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 150, attributeCode = 'retireDate' where attributeCode = 'retireDate'")
} else {
   retireEavEntityAttribute = new EavEntityAttribute(sortOrder:150,attribute:retireAttribute,eavAttributeSet:appAttributeSet)
   if ( !retireEavEntityAttribute.validate() || !retireEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create retireEavEntityAttribute : " +
			   retireEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def retireDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,retireAttribute)
if( !retireDataTransferMapMaster ){
   retireDataTransferMapMaster = new DataTransferAttributeMap(columnName:"Retire",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Retire',sheetName='Applications' where eavAttribute = ?",[retireAttribute])
}

def retireDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,retireAttribute)
if(!retireDataTransferMapWalkThru){
   retireDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Retire",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Retire',sheetName='Applications' where eavAttribute = ?",[retireAttribute])
}
/**
*  Create MaintExp
*/
def maintExpAttribute = EavAttribute.findByAttributeCodeAndEntityType('maintExpDate',appEntityType)
if(maintExpAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'maintExpDate', frontendLabel='MaintExp' where id = ?",[maintExpAttribute.id])
} else {
   maintExpAttribute = new EavAttribute( attributeCode : "maintExpDate",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'MaintExp',
		   note : 'this field is used for just import',
		   sortOrder : 160,
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
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 160, attributeCode = 'maintExpDate' where attributeCode = 'maintExpDate'")
} else {
   maintExpEavEntityAttribute = new EavEntityAttribute(sortOrder:160,attribute:maintExpAttribute,eavAttributeSet:appAttributeSet)
   if ( !maintExpEavEntityAttribute.validate() || !maintExpEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create maintExpEavEntityAttribute : " +
			   maintExpEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def maintExpDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,maintExpAttribute)
if( !maintExpDataTransferMapMaster ){
   maintExpDataTransferMapMaster = new DataTransferAttributeMap(columnName:"MaintExp",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'MaintExp',sheetName='Applications' where eavAttribute = ?",[maintExpAttribute])
}

def maintExpDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,maintExpAttribute)
if(!maintExpDataTransferMapWalkThru){
   maintExpDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"MaintExp",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'MaintExp',sheetName='Applications' where eavAttribute = ?",[maintExpAttribute])
}
/**
*  Create Function
*/
def functionAttribute = EavAttribute.findByAttributeCodeAndEntityType('appFunction',appEntityType)
if(functionAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'appFunction', frontendLabel='Function' where id = ?",[functionAttribute.id])
} else {
   functionAttribute = new EavAttribute( attributeCode : "appFunction",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Function',
		   note : 'this field is used for just import',
		   sortOrder : 170,
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
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 170, attributeCode = 'appFunction' where attributeCode = 'appFunction'")
} else {
   functionEavEntityAttribute = new EavEntityAttribute(sortOrder:170,attribute:functionAttribute,eavAttributeSet:appAttributeSet)
   if ( !functionEavEntityAttribute.validate() || !functionEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create functionEavEntityAttribute : " +
			   functionEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def functionDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,functionAttribute)
if( !functionDataTransferMapMaster ){
   functionDataTransferMapMaster = new DataTransferAttributeMap(columnName:"Function",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Function',sheetName='Applications' where eavAttribute = ?",[functionAttribute])
}

def functionDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,functionAttribute)
if(!functionDataTransferMapWalkThru){
   functionDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Function",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Function',sheetName='Applications' where eavAttribute = ?",[functionAttribute])
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
		   sortOrder : 180,
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
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 180, attributeCode = 'environment' where attributeCode = 'environment'")
} else {
   environmentEavEntityAttribute = new EavEntityAttribute(sortOrder:180,attribute:environmentAttribute,eavAttributeSet:appAttributeSet)
   if ( !environmentEavEntityAttribute.validate() || !environmentEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create environmentEavEntityAttribute : " +
			   environmentEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def environmentDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,environmentAttribute)
if( !environmentDataTransferMapMaster ){
   environmentDataTransferMapMaster = new DataTransferAttributeMap(columnName:"Environment",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Environment',sheetName='Applications' where eavAttribute = ?",[environmentAttribute])
}

def environmentDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,environmentAttribute)
if(!environmentDataTransferMapWalkThru){
   environmentDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Environment",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Environment',sheetName='Applications' where eavAttribute = ?",[environmentAttribute])
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
		   sortOrder : 190,
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
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 190, attributeCode = 'criticality' where attributeCode = 'criticality'")
} else {
   criticalityEavEntityAttribute = new EavEntityAttribute(sortOrder:190,attribute:criticalityAttribute,eavAttributeSet:appAttributeSet)
   if ( !criticalityEavEntityAttribute.validate() || !criticalityEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create criticalityEavEntityAttribute : " +
			   criticalityEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def criticalityDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,criticalityAttribute)
if( !criticalityDataTransferMapMaster ){
   criticalityDataTransferMapMaster = new DataTransferAttributeMap(columnName:"Criticality",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Criticality',sheetName='Applications' where eavAttribute = ?",[criticalityAttribute])
}

def criticalityDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,criticalityAttribute)
if(!criticalityDataTransferMapWalkThru){
   criticalityDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Criticality",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Criticality',sheetName='Applications' where eavAttribute = ?",[criticalityAttribute])
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
		   sortOrder : 200,
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
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 200, attributeCode = 'moveBundle' where attributeCode = 'moveBundle'")
} else {
   moveBundleEavEntityAttribute = new EavEntityAttribute(sortOrder:200,attribute:moveBundleAttribute,eavAttributeSet:appAttributeSet)
   if ( !moveBundleEavEntityAttribute.validate() || !moveBundleEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create moveBundleEavEntityAttribute : " +
			   moveBundleEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def moveBundleDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,moveBundleAttribute)
if( !moveBundleDataTransferMapMaster ){
   moveBundleDataTransferMapMaster = new DataTransferAttributeMap(columnName:"MoveBundle",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'MoveBundle',sheetName='Applications' where eavAttribute = ?",[moveBundleAttribute])
}

def moveBundleDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,moveBundleAttribute)
if(!moveBundleDataTransferMapWalkThru){
   moveBundleDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"MoveBundle",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'MoveBundle',sheetName='Applications' where eavAttribute = ?",[moveBundleAttribute])
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
		   sortOrder : 210,
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
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 210, attributeCode = 'planStatus' where attributeCode = 'planStatus'")
} else {
   planStatusEavEntityAttribute = new EavEntityAttribute(sortOrder:210,attribute:planStatusAttribute,eavAttributeSet:appAttributeSet)
   if ( !planStatusEavEntityAttribute.validate() || !planStatusEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create planStatusEavEntityAttribute : " +
			   planStatusEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def planStatusDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,planStatusAttribute)
if( !planStatusDataTransferMapMaster ){
   planStatusDataTransferMapMaster = new DataTransferAttributeMap(columnName:"PlanStatus",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'PlanStatus',sheetName='Applications' where eavAttribute = ?",[planStatusAttribute])
}

def planStatusDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,planStatusAttribute)
if(!planStatusDataTransferMapWalkThru){
   planStatusDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"PlanStatus",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'PlanStatus',sheetName='Applications' where eavAttribute = ?",[planStatusAttribute])
}
/**
*  Create TotalUsers
*/
def totalUsersAttribute = EavAttribute.findByAttributeCodeAndEntityType('userCount',appEntityType)
if(totalUsersAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'userCount', frontendLabel='TotalUsers' where id = ?",[totalUsersAttribute.id])
} else {
   totalUsersAttribute = new EavAttribute( attributeCode : "userCount",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'TotalUsers',
		   note : 'this field is used for just import',
		   sortOrder : 220,
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
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 220, attributeCode = 'userCount' where attributeCode = 'userCount'")
} else {
   totalUsersEavEntityAttribute = new EavEntityAttribute(sortOrder:220,attribute:totalUsersAttribute,eavAttributeSet:appAttributeSet)
   if ( !totalUsersEavEntityAttribute.validate() || !totalUsersEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create totalUsersEavEntityAttribute : " +
			   totalUsersEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def totalUsersDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,totalUsersAttribute)
if( !totalUsersDataTransferMapMaster ){
   totalUsersDataTransferMapMaster = new DataTransferAttributeMap(columnName:"TotalUsers",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'TotalUsers',sheetName='Applications' where eavAttribute = ?",[totalUsersAttribute])
}

def totalUsersDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,totalUsersAttribute)
if(!totalUsersDataTransferMapWalkThru){
   totalUsersDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"TotalUsers",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'TotalUsers',sheetName='Applications' where eavAttribute = ?",[totalUsersAttribute])
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
		   sortOrder : 230,
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
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 230, attributeCode = 'userLocations' where attributeCode = 'userLocations'")
} else {
   userLocationsEavEntityAttribute = new EavEntityAttribute(sortOrder:230,attribute:userLocationsAttribute,eavAttributeSet:appAttributeSet)
   if ( !userLocationsEavEntityAttribute.validate() || !userLocationsEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create userLocationsEavEntityAttribute : " +
			   userLocationsEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def userLocationsDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,userLocationsAttribute)
if( !userLocationsDataTransferMapMaster ){
   userLocationsDataTransferMapMaster = new DataTransferAttributeMap(columnName:"UserLocations",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'UserLocations',sheetName='Applications' where eavAttribute = ?",[userLocationsAttribute])
}

def userLocationsDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,userLocationsAttribute)
if(!userLocationsDataTransferMapWalkThru){
   userLocationsDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"UserLocations",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'UserLocations',sheetName='Applications' where eavAttribute = ?",[userLocationsAttribute])
}
/**
*  Create Frequency
*/
def frequencyAttribute = EavAttribute.findByAttributeCodeAndEntityType('useFrequency',appEntityType)
if(frequencyAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'useFrequency', frontendLabel='Frequency' where id = ?",[frequencyAttribute.id])
} else {
   frequencyAttribute = new EavAttribute( attributeCode : "useFrequency",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Frequency',
		   note : 'this field is used for just import',
		   sortOrder : 250,
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
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 250, attributeCode = 'useFrequency' where attributeCode = 'useFrequency'")
} else {
   frequencyEavEntityAttribute = new EavEntityAttribute(sortOrder:250,attribute:frequencyAttribute,eavAttributeSet:appAttributeSet)
   if ( !frequencyEavEntityAttribute.validate() || !frequencyEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create frequencyEavEntityAttribute : " +
			   frequencyEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def frequencyDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,frequencyAttribute)
if( !frequencyDataTransferMapMaster ){
   frequencyDataTransferMapMaster = new DataTransferAttributeMap(columnName:"Frequency",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Frequency',sheetName='Applications' where eavAttribute = ?",[frequencyAttribute])
}

def frequencyDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,frequencyAttribute)
if(!frequencyDataTransferMapWalkThru){
   frequencyDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Frequency",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Frequency',sheetName='Applications' where eavAttribute = ?",[frequencyAttribute])
}
/**
*  Create RPO
*/
def rpoAttribute = EavAttribute.findByAttributeCodeAndEntityType('drRpoDesc',appEntityType)
if(rpoAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'drRpoDesc', frontendLabel='RPO' where id = ?",[rpoAttribute.id])
} else {
   rpoAttribute = new EavAttribute( attributeCode : "drRpoDesc",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'RPO',
		   note : 'this field is used for just import',
		   sortOrder : 260,
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
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 260, attributeCode = 'drRpoDesc' where attributeCode = 'drRpoDesc'")
} else {
   rpoEavEntityAttribute = new EavEntityAttribute(sortOrder:260,attribute:rpoAttribute,eavAttributeSet:appAttributeSet)
   if ( !rpoEavEntityAttribute.validate() || !rpoEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create rpoEavEntityAttribute : " +
			   rpoEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def rpoDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,rpoAttribute)
if( !rpoDataTransferMapMaster ){
   rpoDataTransferMapMaster = new DataTransferAttributeMap(columnName:"RPO",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'RPO',sheetName='Applications' where eavAttribute = ?",[rpoAttribute])
}

def rpoDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,rpoAttribute)
if(!rpoDataTransferMapWalkThru){
   rpoDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"RPO",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'RPO',sheetName='Applications' where eavAttribute = ?",[rpoAttribute])
}
/**
*  Create RTO
*/
def rtoAttribute = EavAttribute.findByAttributeCodeAndEntityType('drRtoDesc',appEntityType)
if(rtoAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'drRtoDesc', frontendLabel='RTO' where id = ?",[rtoAttribute.id])
} else {
   rtoAttribute = new EavAttribute( attributeCode : "drRtoDesc",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'RTO',
		   note : 'this field is used for just import',
		   sortOrder : 270,
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
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 270, attributeCode = 'drRtoDesc' where attributeCode = 'drRtoDesc'")
} else {
   rtoEavEntityAttribute = new EavEntityAttribute(sortOrder:270,attribute:rtoAttribute,eavAttributeSet:appAttributeSet)
   if ( !rtoEavEntityAttribute.validate() || !rtoEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create rtoEavEntityAttribute : " +
			   rtoEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def rtoDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,rtoAttribute)
if( !rtoDataTransferMapMaster ){
   rtoDataTransferMapMaster = new DataTransferAttributeMap(columnName:"RTO",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'RTO',sheetName='Applications' where eavAttribute = ?",[rtoAttribute])
}

def rtoDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,rtoAttribute)
if(!rtoDataTransferMapWalkThru){
   rtoDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"RTO",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'RTO',sheetName='Applications' where eavAttribute = ?",[rtoAttribute])
}

/**
*  Create DowntimeTolerance
*/
def downtimeToleranceAttribute = EavAttribute.findByAttributeCodeAndEntityType('moveDowntimeTolerance',appEntityType)
if(downtimeToleranceAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'moveDowntimeTolerance', frontendLabel='DowntimeTolerance' where id = ?",[downtimeToleranceAttribute.id])
} else {
   downtimeToleranceAttribute = new EavAttribute( attributeCode : "moveDowntimeTolerance",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'DowntimeTolerance',
		   note : 'this field is used for just import',
		   sortOrder : 280,
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
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 280, attributeCode = 'moveDowntimeTolerance' where attributeCode = 'moveDowntimeTolerance'")
} else {
   downtimeToleranceEavEntityAttribute = new EavEntityAttribute(sortOrder:280,attribute:downtimeToleranceAttribute,eavAttributeSet:appAttributeSet)
   if ( !downtimeToleranceEavEntityAttribute.validate() || !downtimeToleranceEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create downtimeToleranceEavEntityAttribute : " +
			   downtimeToleranceEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def downtimeToleranceDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,downtimeToleranceAttribute)
if( !downtimeToleranceDataTransferMapMaster ){
   downtimeToleranceDataTransferMapMaster = new DataTransferAttributeMap(columnName:"DowntimeTolerance",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'DowntimeTolerance',sheetName='Applications' where eavAttribute = ?",[downtimeToleranceAttribute])
}

def downtimeToleranceDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,downtimeToleranceAttribute)
if(!downtimeToleranceDataTransferMapWalkThru){
   downtimeToleranceDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"DowntimeTolerance",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'DowntimeTolerance',sheetName='Applications' where eavAttribute = ?",[downtimeToleranceAttribute])
}
/**
*  Create validation
*/
def validationAttribute = EavAttribute.findByAttributeCodeAndEntityType('validation',appEntityType)
if(validationAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'validation', frontendLabel='Validation' where id = ?",[validationAttribute.id])
} else {
   validationAttribute = new EavAttribute( attributeCode : "validation",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Validation',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !validationAttribute.validate() || validationAttribute.save(flush:true) ) {
	   println"Unable to create validationAttribute : "
	   validationAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def validationEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(validationAttribute,appAttributeSet)
if(validationEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 10, attributeCode = 'validation' where attributeCode = 'validation'")
} else {
   validationEavEntityAttribute = new EavEntityAttribute(sortOrder:10,attribute:validationAttribute,eavAttributeSet:appAttributeSet)
   if ( !validationEavEntityAttribute.validate() || !validationEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create validationEavEntityAttribute : " +
			   validationEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def validationDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,validationAttribute)
if( !validationDataTransferMapMaster ){
   validationDataTransferMapMaster = new DataTransferAttributeMap(columnName:"Validation",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:validationAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !validationDataTransferMapMaster.validate() || !validationDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create validationDataTransferMapMaster : " +
			   validationDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'validation',sheetName='Applications' where eavAttribute = ?",[validationAttribute])
}

def validationDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,validationAttribute)
if(!validationDataTransferMapWalkThru){
   validationDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Validation",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:validationAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !validationDataTransferMapWalkThru.validate() || !validationDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create validationDataTransferMapWalkThru : " +
			   validationDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Validation',sheetName='Applications' where eavAttribute = ?",[validationAttribute])
}
/**
*  Create Latency
*/
def latencyAttribute = EavAttribute.findByAttributeCodeAndEntityType('latency',appEntityType)
if(latencyAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'latency', frontendLabel='Latency' where id = ?",[latencyAttribute.id])
} else {
   latencyAttribute = new EavAttribute( attributeCode : "latency",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Latency',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !latencyAttribute.validate() || !latencyAttribute.save(flush:true) ) {
	   println"Unable to create latencyAttribute : "
	   latencyAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def latencyEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(latencyAttribute,appAttributeSet)
if(latencyEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 10, attributeCode = 'latency' where attributeCode = 'latency'")
} else {
   latencyEavEntityAttribute = new EavEntityAttribute(sortOrder:10,attribute:latencyAttribute,eavAttributeSet:appAttributeSet)
   if ( !latencyEavEntityAttribute.validate() || !latencyEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create latencyEavEntityAttribute : " +
			   latencyEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}
def latencyDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,latencyAttribute)
if( !latencyDataTransferMapMaster ){
   latencyDataTransferMapMaster = new DataTransferAttributeMap(columnName:"Latency",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:latencyAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !latencyDataTransferMapMaster.validate() || !latencyDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create latencyDataTransferMapMaster : " +
			   latencyDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'latency',sheetName='Applications' where eavAttribute = ?",[latencyAttribute])
}

def latencyDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,latencyAttribute)
if(!latencyDataTransferMapWalkThru){
   latencyDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Latency",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:latencyAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !latencyDataTransferMapWalkThru.validate() || !latencyDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create latencyDataTransferMapWalkThru : " +
			   latencyDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Latency',sheetName='Applications' where eavAttribute = ?",[latencyAttribute])
}
/**
*  Create TestProc
*/
def testProcAttribute = EavAttribute.findByAttributeCodeAndEntityType('testProc',appEntityType)
if(testProcAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'testProc', frontendLabel='TestProc' where id = ?",[testProcAttribute.id])
} else {
   testProcAttribute = new EavAttribute( attributeCode : "testProc",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'TestProc',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !testProcAttribute.validate() || !testProcAttribute.save(flush:true) ) {
	   println"Unable to create testProcAttribute : "
	   testProcAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def testProcEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(testProcAttribute,appAttributeSet)
if(testProcEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 10, attributeCode = 'testProc' where attributeCode = 'testProc'")
} else {
   testProcEavEntityAttribute = new EavEntityAttribute(sortOrder:10,attribute:testProcAttribute,eavAttributeSet:appAttributeSet)
   if ( !testProcEavEntityAttribute.validate() || !testProcEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create testProcEavEntityAttribute : " +
			   testProcEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def testProcDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,testProcAttribute)
if( !testProcDataTransferMapMaster ){
   testProcDataTransferMapMaster = new DataTransferAttributeMap(columnName:"TestProc",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:testProcAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !testProcDataTransferMapMaster.validate() || !testProcDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create testProcDataTransferMapMaster : " +
			   testProcDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'testProc',sheetName='Applications' where eavAttribute = ?",[testProcAttribute])
}

def testProcDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,testProcAttribute)
if(!testProcDataTransferMapWalkThru){
   testProcDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"TestProc",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:testProcAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !testProcDataTransferMapWalkThru.validate() || !testProcDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create testProcDataTransferMapWalkThru : " +
			   testProcDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'TestProc',sheetName='Applications' where eavAttribute = ?",[testProcAttribute])
}
/**
*  Create StartupProc
*/
def startupProcAttribute = EavAttribute.findByAttributeCodeAndEntityType('startupProc',appEntityType)
if(startupProcAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'startupProc', frontendLabel='StartupProc' where id = ?",[startupProcAttribute.id])
} else {
   startupProcAttribute = new EavAttribute( attributeCode : "startupProc",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'StartupProc',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !startupProcAttribute.validate() || !startupProcAttribute.save(flush:true) ) {
	   println"Unable to create startupProcAttribute : "
	   startupProcAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def startupProcEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(startupProcAttribute,appAttributeSet)
if(startupProcEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 10, attributeCode = 'startupProc' where attributeCode = 'startupProc'")
} else {
   startupProcEavEntityAttribute = new EavEntityAttribute(sortOrder:10,attribute:startupProcAttribute,eavAttributeSet:appAttributeSet)
   if ( !startupProcEavEntityAttribute.validate() || !startupProcEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create startupProcEavEntityAttribute : " +
			   startupProcEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def startupProcDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,startupProcAttribute)
if( !startupProcDataTransferMapMaster ){
   startupProcDataTransferMapMaster = new DataTransferAttributeMap(columnName:"StartupProc",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:startupProcAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !startupProcDataTransferMapMaster.validate() || !startupProcDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create startupProcDataTransferMapMaster : " +
			   startupProcDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'startupProc',sheetName='Applications' where eavAttribute = ?",[startupProcAttribute])
}

def startupProcDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,startupProcAttribute)
if(!startupProcDataTransferMapWalkThru){
   startupProcDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"StartupProc",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:startupProcAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !startupProcDataTransferMapWalkThru.validate() || !startupProcDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create startupProcDataTransferMapWalkThru : " +
			   startupProcDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'StartupProc',sheetName='Applications' where eavAttribute = ?",[startupProcAttribute])
}
/**
*  Create custom1
*/
def custom1Attribute = EavAttribute.findByAttributeCodeAndEntityType('custom1',appEntityType)
if(custom1Attribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'custom1', frontendLabel='Custom1' where id = ?",[custom1Attribute.id])
} else {
   custom1Attribute = new EavAttribute( attributeCode : "custom1",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Custom1',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !custom1Attribute.validate() || !custom1Attribute.save(flush:true) ) {
	   println"Unable to create custom1Attribute : "
	   custom1Attribute.errors.allErrors.each() {println"\n"+it }
   }
}

def custom1EavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(custom1Attribute,appAttributeSet)
if(custom1EavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 10, attributeCode = 'custom1' where attributeCode = 'custom1'")
} else {
   custom1EavEntityAttribute = new EavEntityAttribute(sortOrder:10,attribute:custom1Attribute,eavAttributeSet:appAttributeSet)
   if ( !custom1EavEntityAttribute.validate() || !custom1EavEntityAttribute.save(flush:true) ) {
	   println"Unable to create custom1EavEntityAttribute : " +
			   custom1EavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def custom1DataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,custom1Attribute)
if( !custom1DataTransferMapMaster ){
   custom1DataTransferMapMaster = new DataTransferAttributeMap(columnName:"Custom1",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:custom1Attribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !custom1DataTransferMapMaster.validate() || !custom1DataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create custom1DataTransferMapMaster : " +
			   custom1DataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'custom1',sheetName='Applications' where eavAttribute = ?",[custom1Attribute])
}

def custom1DataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,custom1Attribute)
if(!custom1DataTransferMapWalkThru){
   custom1DataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Custom1",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:custom1Attribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !custom1DataTransferMapWalkThru.validate() || !custom1DataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create custom1DataTransferMapWalkThru : " +
			   custom1DataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Custom1',sheetName='Applications' where eavAttribute = ?",[custom1Attribute])
}
/**
*  Create custom2
*/
def custom2Attribute = EavAttribute.findByAttributeCodeAndEntityType('custom2',appEntityType)
if(custom2Attribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'custom2', frontendLabel='Custom2' where id = ?",[custom2Attribute.id])
} else {
   custom2Attribute = new EavAttribute( attributeCode : "custom2",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Custom2',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !custom2Attribute.validate() || !custom2Attribute.save(flush:true) ) {
	   println"Unable to create custom2Attribute : "
	   custom2Attribute.errors.allErrors.each() {println"\n"+it }
   }
}

def custom2EavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(custom2Attribute,appAttributeSet)
if(custom2EavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 20, attributeCode = 'custom2' where attributeCode = 'custom2'")
} else {
   custom2EavEntityAttribute = new EavEntityAttribute(sortOrder:20,attribute:custom2Attribute,eavAttributeSet:appAttributeSet)
   if ( !custom2EavEntityAttribute.validate() || !custom2EavEntityAttribute.save(flush:true) ) {
	   println"Unable to create custom2EavEntityAttribute : " +
			   custom2EavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def custom2DataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,custom2Attribute)
if( !custom2DataTransferMapMaster ){
   custom2DataTransferMapMaster = new DataTransferAttributeMap(columnName:"Custom2",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:custom2Attribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !custom2DataTransferMapMaster.validate() || !custom2DataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create custom2DataTransferMapMaster : " +
			   custom2DataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'custom2',sheetName='Applications' where eavAttribute = ?",[custom2Attribute])
}

def custom2DataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,custom2Attribute)
if(!custom2DataTransferMapWalkThru){
   custom2DataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Custom2",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:custom2Attribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !custom2DataTransferMapWalkThru.validate() || !custom2DataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create custom2DataTransferMapWalkThru : " +
			   custom2DataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Custom2',sheetName='Applications' where eavAttribute = ?",[custom2Attribute])
}
/**
*  Create custom3
*/
def custom3Attribute = EavAttribute.findByAttributeCodeAndEntityType('custom3',appEntityType)
if(custom3Attribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'custom3', frontendLabel='Custom3' where id = ?",[custom3Attribute.id])
} else {
   custom3Attribute = new EavAttribute( attributeCode : "custom3",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Custom3',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !custom3Attribute.validate() || !custom3Attribute.save(flush:true) ) {
	   println"Unable to create custom3Attribute : "
	   custom3Attribute.errors.allErrors.each() {println"\n"+it }
   }
}

def custom3EavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(custom3Attribute,appAttributeSet)
if(custom3EavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 30, attributeCode = 'custom3' where attributeCode = 'custom3'")
} else {
   custom3EavEntityAttribute = new EavEntityAttribute(sortOrder:30,attribute:custom3Attribute,eavAttributeSet:appAttributeSet)
   if ( !custom3EavEntityAttribute.validate() || !custom3EavEntityAttribute.save(flush:true) ) {
	   println"Unable to create custom3EavEntityAttribute : " +
			   custom3EavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def custom3DataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,custom3Attribute)
if( !custom3DataTransferMapMaster ){
   custom3DataTransferMapMaster = new DataTransferAttributeMap(columnName:"Custom3",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:custom3Attribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !custom3DataTransferMapMaster.validate() || !custom3DataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create custom3DataTransferMapMaster : " +
			   custom3DataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'custom3',sheetName='Applications' where eavAttribute = ?",[custom3Attribute])
}

def custom3DataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,custom3Attribute)
if(!custom3DataTransferMapWalkThru){
   custom3DataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Custom3",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:custom3Attribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !custom3DataTransferMapWalkThru.validate() || !custom3DataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create custom3DataTransferMapWalkThru : " +
			   custom3DataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Custom3',sheetName='Applications' where eavAttribute = ?",[custom3Attribute])
}
/**
*  Create custom4
*/
def custom4Attribute = EavAttribute.findByAttributeCodeAndEntityType('custom4',appEntityType)
if(custom4Attribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'custom4', frontendLabel='Custom4' where id = ?",[custom4Attribute.id])
} else {
   custom4Attribute = new EavAttribute( attributeCode : "custom4",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Custom4',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !custom4Attribute.validate() || !custom4Attribute.save(flush:true) ) {
	   println"Unable to create custom4Attribute : "
	   custom4Attribute.errors.allErrors.each() {println"\n"+it }
   }
}

def custom4EavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(custom4Attribute,appAttributeSet)
if(custom4EavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 40, attributeCode = 'custom4' where attributeCode = 'custom4'")
} else {
   custom4EavEntityAttribute = new EavEntityAttribute(sortOrder:40,attribute:custom4Attribute,eavAttributeSet:appAttributeSet)
   if ( !custom4EavEntityAttribute.validate() || !custom4EavEntityAttribute.save(flush:true) ) {
	   println"Unable to create custom4EavEntityAttribute : " +
			   custom4EavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def custom4DataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,custom4Attribute)
if( !custom4DataTransferMapMaster ){
   custom4DataTransferMapMaster = new DataTransferAttributeMap(columnName:"Custom4",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:custom4Attribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !custom4DataTransferMapMaster.validate() || !custom4DataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create custom4DataTransferMapMaster : " +
			   custom4DataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'custom4',sheetName='Applications' where eavAttribute = ?",[custom4Attribute])
}

def custom4DataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,custom4Attribute)
if(!custom4DataTransferMapWalkThru){
   custom4DataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Custom4",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:custom4Attribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !custom4DataTransferMapWalkThru.validate() || !custom4DataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create custom4DataTransferMapWalkThru : " +
			   custom4DataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Custom4',sheetName='Applications' where eavAttribute = ?",[custom4Attribute])
}
/**
*  Create custom5
*/
def custom5Attribute = EavAttribute.findByAttributeCodeAndEntityType('custom5',appEntityType)
if(custom5Attribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'custom5', frontendLabel='Custom5' where id = ?",[custom5Attribute.id])
} else {
   custom5Attribute = new EavAttribute( attributeCode : "custom5",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Custom5',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !custom5Attribute.validate() || !custom5Attribute.save(flush:true) ) {
	   println"Unable to create custom5Attribute : "
	   custom5Attribute.errors.allErrors.each() {println"\n"+it }
   }
}

def custom5EavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(custom5Attribute,appAttributeSet)
if(custom5EavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 50, attributeCode = 'custom5' where attributeCode = 'custom5'")
} else {
   custom5EavEntityAttribute = new EavEntityAttribute(sortOrder:50,attribute:custom5Attribute,eavAttributeSet:appAttributeSet)
   if ( !custom5EavEntityAttribute.validate() || !custom5EavEntityAttribute.save(flush:true) ) {
	   println"Unable to create custom5EavEntityAttribute : " +
			   custom5EavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def custom5DataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,custom5Attribute)
if( !custom5DataTransferMapMaster ){
   custom5DataTransferMapMaster = new DataTransferAttributeMap(columnName:"Custom5",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:custom5Attribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !custom5DataTransferMapMaster.validate() || !custom5DataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create custom5DataTransferMapMaster : " +
			   custom5DataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'custom5',sheetName='Applications' where eavAttribute = ?",[custom5Attribute])
}

def custom5DataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,custom5Attribute)
if(!custom5DataTransferMapWalkThru){
   custom5DataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Custom5",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:custom5Attribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !custom5DataTransferMapWalkThru.validate() || !custom5DataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create custom5DataTransferMapWalkThru : " +
			   custom5DataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Custom5',sheetName='Applications' where eavAttribute = ?",[custom5Attribute])
}
/**
*  Create custom6
*/
def custom6Attribute = EavAttribute.findByAttributeCodeAndEntityType('custom6',appEntityType)
if(custom6Attribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'custom6', frontendLabel='Custom6' where id = ?",[custom6Attribute.id])
} else {
   custom6Attribute = new EavAttribute( attributeCode : "custom6",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Custom6',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !custom6Attribute.validate() || !custom6Attribute.save(flush:true) ) {
	   println"Unable to create custom6Attribute : "
	   custom6Attribute.errors.allErrors.each() {println"\n"+it }
   }
}

def custom6EavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(custom6Attribute,appAttributeSet)
if(custom6EavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 60, attributeCode = 'custom6' where attributeCode = 'custom6'")
} else {
   custom6EavEntityAttribute = new EavEntityAttribute(sortOrder:60,attribute:custom6Attribute,eavAttributeSet:appAttributeSet)
   if ( !custom6EavEntityAttribute.validate() || !custom6EavEntityAttribute.save(flush:true) ) {
	   println"Unable to create custom6EavEntityAttribute : " +
			   custom6EavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def custom6DataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,custom6Attribute)
if( !custom6DataTransferMapMaster ){
   custom6DataTransferMapMaster = new DataTransferAttributeMap(columnName:"Custom6",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:custom6Attribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !custom6DataTransferMapMaster.validate() || !custom6DataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create custom6DataTransferMapMaster : " +
			   custom6DataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'custom6',sheetName='Applications' where eavAttribute = ?",[custom6Attribute])
}

def custom6DataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,custom6Attribute)
if(!custom6DataTransferMapWalkThru){
   custom6DataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Custom6",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:custom6Attribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !custom6DataTransferMapWalkThru.validate() || !custom6DataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create custom6DataTransferMapWalkThru : " +
			   custom6DataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Custom6',sheetName='Applications' where eavAttribute = ?",[custom6Attribute])
}
/**
*  Create custom7
*/
def custom7Attribute = EavAttribute.findByAttributeCodeAndEntityType('custom7',appEntityType)
if(custom7Attribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'custom7', frontendLabel='Custom7' where id = ?",[custom7Attribute.id])
} else {
   custom7Attribute = new EavAttribute( attributeCode : "custom7",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Custom7',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !custom7Attribute.validate() || !custom7Attribute.save(flush:true) ) {
	   println"Unable to create custom7Attribute : "
	   custom7Attribute.errors.allErrors.each() {println"\n"+it }
   }
}

def custom7EavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(custom7Attribute,appAttributeSet)
if(custom7EavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 70, attributeCode = 'custom7' where attributeCode = 'custom7'")
} else {
   custom7EavEntityAttribute = new EavEntityAttribute(sortOrder:70,attribute:custom7Attribute,eavAttributeSet:appAttributeSet)
   if ( !custom7EavEntityAttribute.validate() || !custom7EavEntityAttribute.save(flush:true) ) {
	   println"Unable to create custom7EavEntityAttribute : " +
			   custom7EavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def custom7DataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,custom7Attribute)
if( !custom7DataTransferMapMaster ){
   custom7DataTransferMapMaster = new DataTransferAttributeMap(columnName:"Custom7",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:custom7Attribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !custom7DataTransferMapMaster.validate() || !custom7DataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create custom7DataTransferMapMaster : " +
			   custom7DataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'custom7',sheetName='Applications' where eavAttribute = ?",[custom7Attribute])
}

def custom7DataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,custom7Attribute)
if(!custom7DataTransferMapWalkThru){
   custom7DataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Custom7",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:custom7Attribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !custom7DataTransferMapWalkThru.validate() || !custom7DataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create custom7DataTransferMapWalkThru : " +
			   custom7DataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Custom7',sheetName='Applications' where eavAttribute = ?",[custom7Attribute])
}
/**
*  Create custom8
*/
def custom8Attribute = EavAttribute.findByAttributeCodeAndEntityType('custom8',appEntityType)
if(custom8Attribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'custom8', frontendLabel='Custom8' where id = ?",[custom8Attribute.id])
} else {
   custom8Attribute = new EavAttribute( attributeCode : "custom8",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Custom8',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !custom8Attribute.validate() || !custom8Attribute.save(flush:true) ) {
	   println"Unable to create custom8Attribute : "
	   custom8Attribute.errors.allErrors.each() {println"\n"+it }
   }
}

def custom8EavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(custom8Attribute,appAttributeSet)
if(custom8EavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 80, attributeCode = 'custom8' where attributeCode = 'custom8'")
} else {
   custom8EavEntityAttribute = new EavEntityAttribute(sortOrder:80,attribute:custom8Attribute,eavAttributeSet:appAttributeSet)
   if ( !custom8EavEntityAttribute.validate() || !custom8EavEntityAttribute.save(flush:true) ) {
	   println"Unable to create custom8EavEntityAttribute : " +
			   custom8EavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def custom8DataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,custom8Attribute)
if( !custom8DataTransferMapMaster ){
   custom8DataTransferMapMaster = new DataTransferAttributeMap(columnName:"Custom8",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:custom8Attribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !custom8DataTransferMapMaster.validate() || !custom8DataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create custom8DataTransferMapMaster : " +
			   custom8DataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'custom8',sheetName='Applications' where eavAttribute = ?",[custom8Attribute])
}

def custom8DataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,custom8Attribute)
if(!custom8DataTransferMapWalkThru){
   custom8DataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Custom8",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:custom8Attribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !custom8DataTransferMapWalkThru.validate() || !custom8DataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create custom8DataTransferMapWalkThru : " +
			   custom8DataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Custom8',sheetName='Applications' where eavAttribute = ?",[custom8Attribute])
}
/**
*  Create url
*/
def urlAttribute = EavAttribute.findByAttributeCodeAndEntityType('url',appEntityType)
if(urlAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'url', frontendLabel='Url' where id = ?",[urlAttribute.id])
} else {
   urlAttribute = new EavAttribute( attributeCode : "url",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Url',
		   note : 'this field is used for just import',
		   sortOrder : 10,
		   entityType:appEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !urlAttribute.validate() || !urlAttribute.save(flush:true) ) {
	   println"Unable to create urlAttribute : "
	   urlAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def urlEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(urlAttribute,appAttributeSet)
if(urlEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 10, attributeCode = 'url' where attributeCode = 'url'")
} else {
   urlEavEntityAttribute = new EavEntityAttribute(sortOrder:10,attribute:urlAttribute,eavAttributeSet:appAttributeSet)
   if ( !urlEavEntityAttribute.validate() || !urlEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create urlEavEntityAttribute : " +
			   urlEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def urlDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,urlAttribute)
if( !urlDataTransferMapMaster ){
   urlDataTransferMapMaster = new DataTransferAttributeMap(columnName:"Url",
		   sheetName:"Applications",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:urlAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !urlDataTransferMapMaster.validate() || !urlDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create urlDataTransferMapMaster : " +
			   urlDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'url',sheetName='Applications' where eavAttribute = ?",[urlAttribute])
}

def urlDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,urlAttribute)
if(!urlDataTransferMapWalkThru){
   urlDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Url",
		   sheetName:"Applications",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:urlAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !urlDataTransferMapWalkThru.validate() || !urlDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create urlDataTransferMapWalkThru : " +
			   urlDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Url',sheetName='Applications' where eavAttribute = ?",[urlAttribute])
}