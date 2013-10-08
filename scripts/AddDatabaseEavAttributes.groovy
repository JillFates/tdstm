
import com.tdssrc.eav.*

def jdbcTemplate = ctx.getBean("jdbcTemplate")
def masterDataTransferSet = DataTransferSet.findBySetCode("MASTER")
def walkThruDataTransferSet = DataTransferSet.findBySetCode("WALKTHROUGH")

def dbEntityType = EavEntityType.findByEntityTypeCode("Database")
if(!dbEntityType){
	dbAttributeSet = new EavEntityType( entityTypeCode:'Database', domainName:'Database', isAuditable:1  ).save(flush:true)
}
def dbAttributeSet = EavAttributeSet.findByAttributeSetName("Database")
if(!dbAttributeSet){
	dbAttributeSet = new EavAttributeSet( attributeSetName:'Database', entityType:dbEntityType, sortOrder:20 ).save(flush:true)
}

/**
 *  Create Name
 */

def nameAttribute = EavAttribute.findByAttributeCodeAndEntityType('assetName',dbEntityType)
if(nameAttribute){
	EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'assetName', frontendLabel='Name' where id = ?",[nameAttribute.id])
} else {
	nameAttribute = new EavAttribute( attributeCode : "assetName",
			backendType : 'String',
			frontendInput : 'text',
			frontendLabel : 'Name',
			note : 'this field is used for just import',
			sortOrder : 10,
			entityType:dbEntityType,
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

def nameEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(nameAttribute,dbAttributeSet)
if(nameEavEntityAttribute){
	EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 10, attributeCode = 'assetName' where attributeCode = 'assetName'")
} else {
	nameEavEntityAttribute = new EavEntityAttribute(sortOrder:10,attribute:nameAttribute,eavAttributeSet:dbAttributeSet)
	if ( !nameEavEntityAttribute.validate() || !nameEavEntityAttribute.save(flush:true) ) {
		println"Unable to create nameEavEntityAttribute : " +
				nameEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
	}
}

def nameDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,nameAttribute)
if( !nameDataTransferMapMaster ){
	nameDataTransferMapMaster = new DataTransferAttributeMap(columnName:"Name",
			sheetName:"Databases",
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
	DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Name',sheetName='Databases' where eavAttribute = ?",[nameAttribute])
}

def nameDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,nameAttribute)
if(!nameDataTransferMapWalkThru){
	nameDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Name",
			sheetName:"Databases",
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
	DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Name',sheetName='Databases' where eavAttribute = ?",[nameAttribute])
}

/**
 *  Create Format
 */
def dbFormatAttribute = EavAttribute.findByAttributeCodeAndEntityType('dbFormat',dbEntityType)
if(dbFormatAttribute){
	EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'dbFormat', frontendLabel='Format' where id = ?",[dbFormatAttribute.id])
} else {
	dbFormatAttribute = new EavAttribute( attributeCode : "dbFormat",
			backendType : 'String',
			frontendInput : 'text',
			frontendLabel : 'Format',
			note : 'this field is used for just import',
			sortOrder : 20,
			entityType:dbEntityType,
			isRequired:0,
			isUnique:0,
			defaultValue:"1",
			validation:'No validation'
			)
	if ( !dbFormatAttribute.validate() || !dbFormatAttribute.save(flush:true) ) {
		println"Unable to create dbFormatAttribute : "
		dbFormatAttribute.errors.allErrors.each() {println"\n"+it }
	}
}

def dbFormatEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(dbFormatAttribute,dbAttributeSet)
if(dbFormatEavEntityAttribute){
	EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 20, attributeCode = 'dbFormat' where attributeCode = 'dbFormat'")
} else {
	dbFormatEavEntityAttribute = new EavEntityAttribute(sortOrder:20,attribute:dbFormatAttribute,eavAttributeSet:dbAttributeSet)
	if ( !dbFormatEavEntityAttribute.validate() || !dbFormatEavEntityAttribute.save(flush:true) ) {
		println"Unable to create dbFormatEavEntityAttribute : " +
				dbFormatEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
	}
}

def dbFormatDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,dbFormatAttribute)
if( !dbFormatDataTransferMapMaster ){
	dbFormatDataTransferMapMaster = new DataTransferAttributeMap(columnName:"Format",
			sheetName:"Databases",
			dataTransferSet : masterDataTransferSet,
			eavAttribute:dbFormatAttribute,
			validation:"NO Validation",
			isRequired:0
			)
	if ( !dbFormatDataTransferMapMaster.validate() || !dbFormatDataTransferMapMaster.save(flush:true) ) {
		println"Unable to create dbFormatDataTransferMapMaster : " +
				dbFormatDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
	}
} else {
	DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Format',sheetName='Databases' where eavAttribute = ?",[dbFormatAttribute])
}

def dbFormatDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,dbFormatAttribute)
if(!dbFormatDataTransferMapWalkThru){
	dbFormatDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Format",
			sheetName:"Databases",
			dataTransferSet : walkThruDataTransferSet,
			eavAttribute:dbFormatAttribute,
			validation:"NO Validation",
			isRequired:0
			)
	if ( !dbFormatDataTransferMapWalkThru.validate() || !dbFormatDataTransferMapWalkThru.save(flush:true) ) {
		println"Unable to create dbFormatDataTransferMapWalkThru : " +
				dbFormatDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
	}
} else {
	DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Format',sheetName='Databases' where eavAttribute = ?",[dbFormatAttribute])
}
/**
*  Create Size
*/
def sizeAttribute = EavAttribute.findByAttributeCodeAndEntityType('size',dbEntityType)
if(sizeAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'size', frontendLabel='Size' where id = ?",[sizeAttribute.id])
} else {
   sizeAttribute = new EavAttribute( attributeCode : "size",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Size',
		   note : 'this field is used for just import',
		   sortOrder : 30,
		   entityType:dbEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !sizeAttribute.validate() || !sizeAttribute.save(flush:true) ) {
	   println"Unable to create sizeAttribute : "
	   sizeAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def sizeEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(sizeAttribute,dbAttributeSet)
if(sizeEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 30, attributeCode = 'size' where attributeCode = 'size'")
} else {
   sizeEavEntityAttribute = new EavEntityAttribute(sortOrder:30,attribute:sizeAttribute,eavAttributeSet:dbAttributeSet)
   if ( !sizeEavEntityAttribute.validate() || !sizeEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create sizeEavEntityAttribute : " +
			   sizeEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def sizeDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,sizeAttribute)
if( !sizeDataTransferMapMaster ){
   sizeDataTransferMapMaster = new DataTransferAttributeMap(columnName:"Size",
		   sheetName:"Databases",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:sizeAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !sizeDataTransferMapMaster.validate() || !sizeDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create sizeDataTransferMapMaster : " +
			   sizeDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Size',sheetName='Databases' where eavAttribute = ?",[sizeAttribute])
}

def sizeDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,sizeAttribute)
if(!sizeDataTransferMapWalkThru){
   sizeDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Size",
		   sheetName:"Databases",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:sizeAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !sizeDataTransferMapWalkThru.validate() || !sizeDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create sizeDataTransferMapWalkThru : " +
			   sizeDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Size',sheetName='Databases' where eavAttribute = ?",[sizeAttribute])
}
/**
*  Create Description
*/
def descriptionAttribute = EavAttribute.findByAttributeCodeAndEntityType('description',dbEntityType)
if(descriptionAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'description', frontendLabel='Description' where id = ?",[descriptionAttribute.id])
} else {
   descriptionAttribute = new EavAttribute( attributeCode : "description",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Description',
		   note : 'this field is used for just import',
		   sortOrder : 40,
		   entityType:dbEntityType,
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

def descriptionEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(descriptionAttribute,dbAttributeSet)
if(descriptionEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 40, attributeCode = 'description' where attributeCode = 'description'")
} else {
   descriptionEavEntityAttribute = new EavEntityAttribute(sortOrder:40,attribute:descriptionAttribute,eavAttributeSet:dbAttributeSet)
   if ( !descriptionEavEntityAttribute.validate() || !descriptionEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create descriptionEavEntityAttribute : " +
			   descriptionEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def descriptionDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,descriptionAttribute)
if( !descriptionDataTransferMapMaster ){
   descriptionDataTransferMapMaster = new DataTransferAttributeMap(columnName:"Description",
		   sheetName:"Databases",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Description',sheetName='Databases' where eavAttribute = ?",[descriptionAttribute])
}

def descriptionDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,descriptionAttribute)
if(!descriptionDataTransferMapWalkThru){
   descriptionDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Description",
		   sheetName:"Databases",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Description',sheetName='Databases' where eavAttribute = ?",[descriptionAttribute])
}
/**
*  Create SupportType
*/
def supportTypeAttribute = EavAttribute.findByAttributeCodeAndEntityType('supportType',dbEntityType)
if(supportTypeAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'supportType', frontendLabel='SupportType' where id = ?",[supportTypeAttribute.id])
} else {
   supportTypeAttribute = new EavAttribute( attributeCode : "supportType",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'SupportType',
		   note : 'this field is used for just import',
		   sortOrder : 50,
		   entityType:dbEntityType,
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

def supportTypeEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(supportTypeAttribute,dbAttributeSet)
if(supportTypeEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 50, attributeCode = 'supportType' where attributeCode = 'supportType'")
} else {
   supportTypeEavEntityAttribute = new EavEntityAttribute(sortOrder:50,attribute:supportTypeAttribute,eavAttributeSet:dbAttributeSet)
   if ( !supportTypeEavEntityAttribute.validate() || !supportTypeEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create supportTypeEavEntityAttribute : " +
			   supportTypeEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def supportTypeDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,supportTypeAttribute)
if( !supportTypeDataTransferMapMaster ){
   supportTypeDataTransferMapMaster = new DataTransferAttributeMap(columnName:"SupportType",
		   sheetName:"Databases",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'SupportType',sheetName='Databases' where eavAttribute = ?",[supportTypeAttribute])
}

def supportTypeDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,supportTypeAttribute)
if(!supportTypeDataTransferMapWalkThru){
   supportTypeDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"SupportType",
		   sheetName:"Databases",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'SupportType',sheetName='Databases' where eavAttribute = ?",[supportTypeAttribute])
}
/**
*  Create Retire
*/
def retireDateAttribute = EavAttribute.findByAttributeCodeAndEntityType('retireDate',dbEntityType)
if(retireDateAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'retireDate', frontendLabel='Retire' where id = ?",[retireDateAttribute.id])
} else {
   retireDateAttribute = new EavAttribute( attributeCode : "retireDate",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Retire',
		   note : 'this field is used for just import',
		   sortOrder : 60,
		   entityType:dbEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !retireDateAttribute.validate() || !retireDateAttribute.save(flush:true) ) {
	   println"Unable to create retireDateAttribute : "
	   retireDateAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def retireDateEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(retireDateAttribute,dbAttributeSet)
if(retireDateEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 60, attributeCode = 'retireDate' where attributeCode = 'retireDate'")
} else {
   retireDateEavEntityAttribute = new EavEntityAttribute(sortOrder:60,attribute:retireDateAttribute,eavAttributeSet:dbAttributeSet)
   if ( !retireDateEavEntityAttribute.validate() || !retireDateEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create retireDateEavEntityAttribute : " +
			   retireDateEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def retireDateDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,retireDateAttribute)
if( !retireDateDataTransferMapMaster ){
   retireDateDataTransferMapMaster = new DataTransferAttributeMap(columnName:"Retire",
		   sheetName:"Databases",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:retireDateAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !retireDateDataTransferMapMaster.validate() || !retireDateDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create retireDateDataTransferMapMaster : " +
			   retireDateDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Retire',sheetName='Databases' where eavAttribute = ?",[retireDateAttribute])
}

def retireDateDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,retireDateAttribute)
if(!retireDateDataTransferMapWalkThru){
   retireDateDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Retire",
		   sheetName:"Databases",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:retireDateAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !retireDateDataTransferMapWalkThru.validate() || !retireDateDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create retireDateDataTransferMapWalkThru : " +
			   retireDateDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Retire',sheetName='Databases' where eavAttribute = ?",[retireDateAttribute])
}
/**
 *  Create MaintExp
 */
def maintExpDateAttribute = EavAttribute.findByAttributeCodeAndEntityType('maintExpDate',dbEntityType)
if(maintExpDateAttribute){
	EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'maintExpDate', frontendLabel='MaintExp' where id = ?",[maintExpDateAttribute.id])
} else {
	maintExpDateAttribute = new EavAttribute( attributeCode : "maintExpDate",
			backendType : 'String',
			frontendInput : 'text',
			frontendLabel : 'MaintExp',
			note : 'this field is used for just import',
			sortOrder : 70,
			entityType:dbEntityType,
			isRequired:0,
			isUnique:0,
			defaultValue:"1",
			validation:'No validation'
			)
	if ( !maintExpDateAttribute.validate() || !maintExpDateAttribute.save(flush:true) ) {
		println"Unable to create maintExpDateAttribute : "
		maintExpDateAttribute.errors.allErrors.each() {println"\n"+it }
	}
}

def maintExpDateEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(maintExpDateAttribute,dbAttributeSet)
if(maintExpDateEavEntityAttribute){
	EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 70, attributeCode = 'maintExpDate' where attributeCode = 'maintExpDate'")
} else {
	maintExpDateEavEntityAttribute = new EavEntityAttribute(sortOrder:70,attribute:maintExpDateAttribute,eavAttributeSet:dbAttributeSet)
	if ( !maintExpDateEavEntityAttribute.validate() || !maintExpDateEavEntityAttribute.save(flush:true) ) {
		println"Unable to create maintExpDateEavEntityAttribute : " +
				maintExpDateEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
	}
}

def maintExpDateDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,maintExpDateAttribute)
if( !maintExpDateDataTransferMapMaster ){
	maintExpDateDataTransferMapMaster = new DataTransferAttributeMap(columnName:"MaintExp",
			sheetName:"Databases",
			dataTransferSet : masterDataTransferSet,
			eavAttribute:maintExpDateAttribute,
			validation:"NO Validation",
			isRequired:0
			)
	if ( !maintExpDateDataTransferMapMaster.validate() || !maintExpDateDataTransferMapMaster.save(flush:true) ) {
		println"Unable to create maintExpDateDataTransferMapMaster : " +
				maintExpDateDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
	}
} else {
	DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'MaintExp',sheetName='Databases' where eavAttribute = ?",[maintExpDateAttribute])
}

def maintExpDateDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,maintExpDateAttribute)
if(!maintExpDateDataTransferMapWalkThru){
	maintExpDateDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"MaintExp",
			sheetName:"Databases",
			dataTransferSet : walkThruDataTransferSet,
			eavAttribute:maintExpDateAttribute,
			validation:"NO Validation",
			isRequired:0
			)
	if ( !maintExpDateDataTransferMapWalkThru.validate() || !maintExpDateDataTransferMapWalkThru.save(flush:true) ) {
		println"Unable to create maintExpDateDataTransferMapWalkThru : " +
				maintExpDateDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
	}
} else {
	DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'MaintExp',sheetName='Databases' where eavAttribute = ?",[maintExpDateAttribute])
}
/**
*  Create Environment
*/
def environmentAttribute = EavAttribute.findByAttributeCodeAndEntityType('environment',dbEntityType)
if(environmentAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'environment', frontendLabel='Environment' where id = ?",[environmentAttribute.id])
} else {
   environmentAttribute = new EavAttribute( attributeCode : "environment",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Environment',
		   note : 'this field is used for just import',
		   sortOrder : 80,
		   entityType:dbEntityType,
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

def environmentEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(environmentAttribute,dbAttributeSet)
if(environmentEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 80, attributeCode = 'environment' where attributeCode = 'environment'")
} else {
   environmentEavEntityAttribute = new EavEntityAttribute(sortOrder:80,attribute:environmentAttribute,eavAttributeSet:dbAttributeSet)
   if ( !environmentEavEntityAttribute.validate() || !environmentEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create environmentEavEntityAttribute : " +
			   environmentEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def environmentDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,environmentAttribute)
if( !environmentDataTransferMapMaster ){
   environmentDataTransferMapMaster = new DataTransferAttributeMap(columnName:"Environment",
		   sheetName:"Databases",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Environment',sheetName='Databases' where eavAttribute = ?",[environmentAttribute])
}

def environmentDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,environmentAttribute)
if(!environmentDataTransferMapWalkThru){
   environmentDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Environment",
		   sheetName:"Databases",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Environment',sheetName='Databases' where eavAttribute = ?",[environmentAttribute])
}
/**
*  Create MoveBundle
*/
def moveBundleAttribute = EavAttribute.findByAttributeCodeAndEntityType('moveBundle',dbEntityType)
if(moveBundleAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'moveBundle', frontendLabel='MoveBundle' where id = ?",[moveBundleAttribute.id])
} else {
   moveBundleAttribute = new EavAttribute( attributeCode : "moveBundle",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'MoveBundle',
		   note : 'this field is used for just import',
		   sortOrder : 90,
		   entityType:dbEntityType,
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

def moveBundleEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(moveBundleAttribute,dbAttributeSet)
if(moveBundleEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 90, attributeCode = 'moveBundle' where attributeCode = 'moveBundle'")
} else {
   moveBundleEavEntityAttribute = new EavEntityAttribute(sortOrder:90,attribute:moveBundleAttribute,eavAttributeSet:dbAttributeSet)
   if ( !moveBundleEavEntityAttribute.validate() || !moveBundleEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create moveBundleEavEntityAttribute : " +
			   moveBundleEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def moveBundleDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,moveBundleAttribute)
if( !moveBundleDataTransferMapMaster ){
   moveBundleDataTransferMapMaster = new DataTransferAttributeMap(columnName:"MoveBundle",
		   sheetName:"Databases",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'MoveBundle',sheetName='Databases' where eavAttribute = ?",[moveBundleAttribute])
}

def moveBundleDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,moveBundleAttribute)
if(!moveBundleDataTransferMapWalkThru){
   moveBundleDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"MoveBundle",
		   sheetName:"Databases",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'MoveBundle',sheetName='Databases' where eavAttribute = ?",[moveBundleAttribute])
}
/**
*  Create PlanStatus
*/
def planStatusAttribute = EavAttribute.findByAttributeCodeAndEntityType('planStatus',dbEntityType)
if(planStatusAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'planStatus', frontendLabel='PlanStatus' where id = ?",[planStatusAttribute.id])
} else {
   planStatusAttribute = new EavAttribute( attributeCode : "planStatus",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'PlanStatus',
		   note : 'this field is used for just import',
		   sortOrder : 100,
		   entityType:dbEntityType,
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

def planStatusEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(planStatusAttribute,dbAttributeSet)
if(planStatusEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 100, attributeCode = 'planStatus' where attributeCode = 'planStatus'")
} else {
   planStatusEavEntityAttribute = new EavEntityAttribute(sortOrder:100,attribute:planStatusAttribute,eavAttributeSet:dbAttributeSet)
   if ( !planStatusEavEntityAttribute.validate() || !planStatusEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create planStatusEavEntityAttribute : " +
			   planStatusEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def planStatusDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,planStatusAttribute)
if( !planStatusDataTransferMapMaster ){
   planStatusDataTransferMapMaster = new DataTransferAttributeMap(columnName:"PlanStatus",
		   sheetName:"Databases",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'PlanStatus',sheetName='Databases' where eavAttribute = ?",[planStatusAttribute])
}

def planStatusDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,planStatusAttribute)
if(!planStatusDataTransferMapWalkThru){
   planStatusDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"PlanStatus",
		   sheetName:"Databases",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'PlanStatus',sheetName='Databases' where eavAttribute = ?",[planStatusAttribute])
}
/**
*  Create Custom1
*/
def custom1Attribute = EavAttribute.findByAttributeCodeAndEntityType('custom1',dbEntityType)
if(custom1Attribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'custom1', frontendLabel='Custom1' where id = ?",[custom1Attribute.id])
} else {
   custom1Attribute = new EavAttribute( attributeCode : "custom1",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Custom1',
		   note : 'this field is used for just import',
		   sortOrder : 100,
		   entityType:dbEntityType,
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

def custom1EavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(custom1Attribute,dbAttributeSet)
if(custom1EavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 100, attributeCode = 'custom1' where attributeCode = 'custom1'")
} else {
   custom1EavEntityAttribute = new EavEntityAttribute(sortOrder:100,attribute:custom1Attribute,eavAttributeSet:dbAttributeSet)
   if ( !custom1EavEntityAttribute.validate() || !custom1EavEntityAttribute.save(flush:true) ) {
	   println"Unable to create custom1EavEntityAttribute : " +
			   custom1EavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def custom1DataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,custom1Attribute)
if( !custom1DataTransferMapMaster ){
   custom1DataTransferMapMaster = new DataTransferAttributeMap(columnName:"Custom1",
		   sheetName:"Databases",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Custom1',sheetName='Databases' where eavAttribute = ?",[custom1Attribute])
}

def custom1DataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,custom1Attribute)
if(!custom1DataTransferMapWalkThru){
   custom1DataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Custom1",
		   sheetName:"Databases",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Custom1',sheetName='Databases' where eavAttribute = ?",[custom1Attribute])
}
/**
*  Create Custom2
*/
def custom2Attribute = EavAttribute.findByAttributeCodeAndEntityType('custom2',dbEntityType)
if(custom2Attribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'custom2', frontendLabel='Custom2' where id = ?",[custom2Attribute.id])
} else {
   custom2Attribute = new EavAttribute( attributeCode : "custom2",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Custom2',
		   note : 'this field is used for just import',
		   sortOrder : 100,
		   entityType:dbEntityType,
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

def custom2EavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(custom2Attribute,dbAttributeSet)
if(custom2EavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 200, attributeCode = 'custom2' where attributeCode = 'custom2'")
} else {
   custom2EavEntityAttribute = new EavEntityAttribute(sortOrder:200,attribute:custom2Attribute,eavAttributeSet:dbAttributeSet)
   if ( !custom2EavEntityAttribute.validate() || !custom2EavEntityAttribute.save(flush:true) ) {
	   println"Unable to create custom2EavEntityAttribute : " +
			   custom2EavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def custom2DataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,custom2Attribute)
if( !custom2DataTransferMapMaster ){
   custom2DataTransferMapMaster = new DataTransferAttributeMap(columnName:"Custom2",
		   sheetName:"Databases",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Custom2',sheetName='Databases' where eavAttribute = ?",[custom2Attribute])
}

def custom2DataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,custom2Attribute)
if(!custom2DataTransferMapWalkThru){
   custom2DataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Custom2",
		   sheetName:"Databases",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Custom2',sheetName='Databases' where eavAttribute = ?",[custom2Attribute])
}
/**
*  Create Custom3
*/
def custom3Attribute = EavAttribute.findByAttributeCodeAndEntityType('custom3',dbEntityType)
if(custom3Attribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'custom3', frontendLabel='Custom3' where id = ?",[custom3Attribute.id])
} else {
   custom3Attribute = new EavAttribute( attributeCode : "custom3",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Custom3',
		   note : 'this field is used for just import',
		   sortOrder : 100,
		   entityType:dbEntityType,
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

def custom3EavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(custom3Attribute,dbAttributeSet)
if(custom3EavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 300, attributeCode = 'custom3' where attributeCode = 'custom3'")
} else {
   custom3EavEntityAttribute = new EavEntityAttribute(sortOrder:300,attribute:custom3Attribute,eavAttributeSet:dbAttributeSet)
   if ( !custom3EavEntityAttribute.validate() || !custom3EavEntityAttribute.save(flush:true) ) {
	   println"Unable to create custom3EavEntityAttribute : " +
			   custom3EavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def custom3DataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,custom3Attribute)
if( !custom3DataTransferMapMaster ){
   custom3DataTransferMapMaster = new DataTransferAttributeMap(columnName:"Custom3",
		   sheetName:"Databases",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Custom3',sheetName='Databases' where eavAttribute = ?",[custom3Attribute])
}

def custom3DataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,custom3Attribute)
if(!custom3DataTransferMapWalkThru){
   custom3DataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Custom3",
		   sheetName:"Databases",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Custom3',sheetName='Databases' where eavAttribute = ?",[custom3Attribute])
}
/**
*  Create Custom4
*/
def custom4Attribute = EavAttribute.findByAttributeCodeAndEntityType('custom4',dbEntityType)
if(custom4Attribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'custom4', frontendLabel='Custom4' where id = ?",[custom4Attribute.id])
} else {
   custom4Attribute = new EavAttribute( attributeCode : "custom4",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Custom4',
		   note : 'this field is used for just import',
		   sortOrder : 100,
		   entityType:dbEntityType,
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

def custom4EavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(custom4Attribute,dbAttributeSet)
if(custom4EavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 400, attributeCode = 'custom4' where attributeCode = 'custom4'")
} else {
   custom4EavEntityAttribute = new EavEntityAttribute(sortOrder:400,attribute:custom4Attribute,eavAttributeSet:dbAttributeSet)
   if ( !custom4EavEntityAttribute.validate() || !custom4EavEntityAttribute.save(flush:true) ) {
	   println"Unable to create custom4EavEntityAttribute : " +
			   custom4EavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def custom4DataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,custom4Attribute)
if( !custom4DataTransferMapMaster ){
   custom4DataTransferMapMaster = new DataTransferAttributeMap(columnName:"Custom4",
		   sheetName:"Databases",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Custom4',sheetName='Databases' where eavAttribute = ?",[custom4Attribute])
}

def custom4DataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,custom4Attribute)
if(!custom4DataTransferMapWalkThru){
   custom4DataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Custom4",
		   sheetName:"Databases",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Custom4',sheetName='Databases' where eavAttribute = ?",[custom4Attribute])
}
/**
*  Create Custom5
*/
def custom5Attribute = EavAttribute.findByAttributeCodeAndEntityType('custom5',dbEntityType)
if(custom5Attribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'custom5', frontendLabel='Custom5' where id = ?",[custom5Attribute.id])
} else {
   custom5Attribute = new EavAttribute( attributeCode : "custom5",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Custom5',
		   note : 'this field is used for just import',
		   sortOrder : 100,
		   entityType:dbEntityType,
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

def custom5EavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(custom5Attribute,dbAttributeSet)
if(custom5EavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 500, attributeCode = 'custom5' where attributeCode = 'custom5'")
} else {
   custom5EavEntityAttribute = new EavEntityAttribute(sortOrder:500,attribute:custom5Attribute,eavAttributeSet:dbAttributeSet)
   if ( !custom5EavEntityAttribute.validate() || !custom5EavEntityAttribute.save(flush:true) ) {
	   println"Unable to create custom5EavEntityAttribute : " +
			   custom5EavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def custom5DataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,custom5Attribute)
if( !custom5DataTransferMapMaster ){
   custom5DataTransferMapMaster = new DataTransferAttributeMap(columnName:"Custom5",
		   sheetName:"Databases",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Custom5',sheetName='Databases' where eavAttribute = ?",[custom5Attribute])
}

def custom5DataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,custom5Attribute)
if(!custom5DataTransferMapWalkThru){
   custom5DataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Custom5",
		   sheetName:"Databases",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Custom5',sheetName='Databases' where eavAttribute = ?",[custom5Attribute])
}
/**
*  Create Custom6
*/
def custom6Attribute = EavAttribute.findByAttributeCodeAndEntityType('custom6',dbEntityType)
if(custom6Attribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'custom6', frontendLabel='Custom6' where id = ?",[custom6Attribute.id])
} else {
   custom6Attribute = new EavAttribute( attributeCode : "custom6",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Custom6',
		   note : 'this field is used for just import',
		   sortOrder : 100,
		   entityType:dbEntityType,
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

def custom6EavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(custom6Attribute,dbAttributeSet)
if(custom6EavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 600, attributeCode = 'custom6' where attributeCode = 'custom6'")
} else {
   custom6EavEntityAttribute = new EavEntityAttribute(sortOrder:600,attribute:custom6Attribute,eavAttributeSet:dbAttributeSet)
   if ( !custom6EavEntityAttribute.validate() || !custom6EavEntityAttribute.save(flush:true) ) {
	   println"Unable to create custom6EavEntityAttribute : " +
			   custom6EavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def custom6DataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,custom6Attribute)
if( !custom6DataTransferMapMaster ){
   custom6DataTransferMapMaster = new DataTransferAttributeMap(columnName:"Custom6",
		   sheetName:"Databases",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Custom6',sheetName='Databases' where eavAttribute = ?",[custom6Attribute])
}

def custom6DataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,custom6Attribute)
if(!custom6DataTransferMapWalkThru){
   custom6DataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Custom6",
		   sheetName:"Databases",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Custom6',sheetName='Databases' where eavAttribute = ?",[custom6Attribute])
}
/**
*  Create Custom7
*/
def custom7Attribute = EavAttribute.findByAttributeCodeAndEntityType('custom7',dbEntityType)
if(custom7Attribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'custom7', frontendLabel='Custom7' where id = ?",[custom7Attribute.id])
} else {
   custom7Attribute = new EavAttribute( attributeCode : "custom7",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Custom7',
		   note : 'this field is used for just import',
		   sortOrder : 100,
		   entityType:dbEntityType,
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

def custom7EavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(custom7Attribute,dbAttributeSet)
if(custom7EavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 700, attributeCode = 'custom7' where attributeCode = 'custom7'")
} else {
   custom7EavEntityAttribute = new EavEntityAttribute(sortOrder:700,attribute:custom7Attribute,eavAttributeSet:dbAttributeSet)
   if ( !custom7EavEntityAttribute.validate() || !custom7EavEntityAttribute.save(flush:true) ) {
	   println"Unable to create custom7EavEntityAttribute : " +
			   custom7EavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def custom7DataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,custom7Attribute)
if( !custom7DataTransferMapMaster ){
   custom7DataTransferMapMaster = new DataTransferAttributeMap(columnName:"Custom7",
		   sheetName:"Databases",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Custom7',sheetName='Databases' where eavAttribute = ?",[custom7Attribute])
}

def custom7DataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,custom7Attribute)
if(!custom7DataTransferMapWalkThru){
   custom7DataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Custom7",
		   sheetName:"Databases",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Custom7',sheetName='Databases' where eavAttribute = ?",[custom7Attribute])
}
/**
*  Create Custom8
*/
def custom8Attribute = EavAttribute.findByAttributeCodeAndEntityType('custom8',dbEntityType)
if(custom8Attribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'custom8', frontendLabel='Custom8' where id = ?",[custom8Attribute.id])
} else {
   custom8Attribute = new EavAttribute( attributeCode : "custom8",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'Custom8',
		   note : 'this field is used for just import',
		   sortOrder : 100,
		   entityType:dbEntityType,
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

def custom8EavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(custom8Attribute,dbAttributeSet)
if(custom8EavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 800, attributeCode = 'custom8' where attributeCode = 'custom8'")
} else {
   custom8EavEntityAttribute = new EavEntityAttribute(sortOrder:800,attribute:custom8Attribute,eavAttributeSet:dbAttributeSet)
   if ( !custom8EavEntityAttribute.validate() || !custom8EavEntityAttribute.save(flush:true) ) {
	   println"Unable to create custom8EavEntityAttribute : " +
			   custom8EavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def custom8DataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,custom8Attribute)
if( !custom8DataTransferMapMaster ){
   custom8DataTransferMapMaster = new DataTransferAttributeMap(columnName:"Custom8",
		   sheetName:"Databases",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Custom8',sheetName='Databases' where eavAttribute = ?",[custom8Attribute])
}

def custom8DataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,custom8Attribute)
if(!custom8DataTransferMapWalkThru){
   custom8DataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"Custom8",
		   sheetName:"Databases",
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
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'Custom8',sheetName='Databases' where eavAttribute = ?",[custom8Attribute])
}