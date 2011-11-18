
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
*  Create DBSize
*/
def dbSizeAttribute = EavAttribute.findByAttributeCodeAndEntityType('dbSize',dbEntityType)
if(dbSizeAttribute){
   EavAttribute.executeUpdate("UPDATE EavAttribute SET attributeCode = 'dbSize', frontendLabel='DBSize' where id = ?",[dbSizeAttribute.id])
} else {
   dbSizeAttribute = new EavAttribute( attributeCode : "dbSize",
		   backendType : 'String',
		   frontendInput : 'text',
		   frontendLabel : 'DBSize',
		   note : 'this field is used for just import',
		   sortOrder : 30,
		   entityType:dbEntityType,
		   isRequired:0,
		   isUnique:0,
		   defaultValue:"1",
		   validation:'No validation'
		   )
   if ( !dbSizeAttribute.validate() || !dbSizeAttribute.save(flush:true) ) {
	   println"Unable to create dbSizeAttribute : "
	   dbSizeAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def dbSizeEavEntityAttribute = EavEntityAttribute.findByAttributeAndEavAttributeSet(dbSizeAttribute,dbAttributeSet)
if(dbSizeEavEntityAttribute){
   EavAttribute.executeUpdate("UPDATE from EavAttribute set sortOrder= 30, attributeCode = 'dbSize' where attributeCode = 'dbSize'")
} else {
   dbSizeEavEntityAttribute = new EavEntityAttribute(sortOrder:30,attribute:dbSizeAttribute,eavAttributeSet:dbAttributeSet)
   if ( !dbSizeEavEntityAttribute.validate() || !dbSizeEavEntityAttribute.save(flush:true) ) {
	   println"Unable to create dbSizeEavEntityAttribute : " +
			   dbSizeEavEntityAttribute.errors.allErrors.each() {println"\n"+it }
   }
}

def dbSizeDataTransferMapMaster = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(masterDataTransferSet,dbSizeAttribute)
if( !dbSizeDataTransferMapMaster ){
   dbSizeDataTransferMapMaster = new DataTransferAttributeMap(columnName:"DBSize",
		   sheetName:"Databases",
		   dataTransferSet : masterDataTransferSet,
		   eavAttribute:dbSizeAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !dbSizeDataTransferMapMaster.validate() || !dbSizeDataTransferMapMaster.save(flush:true) ) {
	   println"Unable to create dbSizeDataTransferMapMaster : " +
			   dbSizeDataTransferMapMaster.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'DBSize',sheetName='Databases' where eavAttribute = ?",[dbSizeAttribute])
}

def dbSizeDataTransferMapWalkThru = DataTransferAttributeMap.findByDataTransferSetAndEavAttribute(walkThruDataTransferSet,dbSizeAttribute)
if(!dbSizeDataTransferMapWalkThru){
   dbSizeDataTransferMapWalkThru = new DataTransferAttributeMap(columnName:"DBSize",
		   sheetName:"Databases",
		   dataTransferSet : walkThruDataTransferSet,
		   eavAttribute:dbSizeAttribute,
		   validation:"NO Validation",
		   isRequired:0
		   )
   if ( !dbSizeDataTransferMapWalkThru.validate() || !dbSizeDataTransferMapWalkThru.save(flush:true) ) {
	   println"Unable to create dbSizeDataTransferMapWalkThru : " +
			   dbSizeDataTransferMapWalkThru.errors.allErrors.each() {println"\n"+it }
   }
} else {
   DataTransferAttributeMap.executeUpdate("UPDATE DataTransferAttributeMap SET columnName = 'DBSize',sheetName='Databases' where eavAttribute = ?",[dbSizeAttribute])
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