
import com.tdssrc.eav.*
import com.tdssrc.grails.GormUtil

def jdbcTemplate = ctx.getBean("jdbcTemplate")

println"**************UPDATE MANUFACTURER***************"
// Create manufactures if not exist
def manufacturerResultMap = jdbcTemplate.queryForList("select manufacturer from asset_entity where manufacturer_id is null && manufacturer != '' && manufacturer is not null group by manufacturer")
	manufacturerResultMap.each{ result->
		def manufacturer = result.manufacturer.replaceAll("\\s+\$", "").replaceAll("^\\s+", "")
		def manufacturerInstance = Manufacturer.findByName( manufacturer )
		if( !manufacturerInstance ){
			manufacturerInstance = new Manufacturer( name : manufacturer )
			if ( !manufacturerInstance.validate() || !manufacturerInstance.save() ) {
				def etext = "Unable to create manufacturerInstance" +
				GormUtil.allErrorsString( manufacturerInstance )
				println etext
			}
		}
		manufacturer = manufacturer.replace("'","\\'")
		def updateQuery = "update asset_entity set manufacturer_id = ${manufacturerInstance.id} where manufacturer='${manufacturer}'"
		def updated = jdbcTemplate.update(updateQuery)
		println "Updated '${manufacturer}' Manufacturer id ${manufacturerInstance.id} for ${updated} assets"
	}
println"**************UPDATE MODEL***************"
//Create model if not exist
def modelResultMap = jdbcTemplate.queryForList("select model, manufacturer_id as manufacturer from asset_entity where model_id is null && model != '' && model is not null group by model")
	modelResultMap.each{ result->
		def manufacturerInstance = result.manufacturer ? Manufacturer.findById( result.manufacturer ) : ""
		def model = result.model.replaceAll("\\s+\$", "").replaceAll("^\\s+", "")
		if( manufacturerInstance ){
			def modelInstance = Model.findByModelNameAndManufacturer( model, manufacturerInstance  )
			if(!modelInstance){
				modelInstance = new Model( modelName : model, manufacturer : manufacturerInstance )
				if ( !modelInstance.validate() || !modelInstance.save() ) {
					def etext = "Unable to create modelInstance" +
					GormUtil.allErrorsString( modelInstance )
					println etext
				}
			}
			
			model = model.replace("'","\\'")
			def updateQuery = "update asset_entity set model_id = ${modelInstance.id} where model='${model}'"
			def updated = jdbcTemplate.update(updateQuery)
			println "Updated '${model}' Model id ${modelInstance.id} for ${updated} assets"
		}
	}
println"**************Delete Asset properties ***************"
/*
 * Power
 */
def powerTypeAttribute = EavAttribute.findByAttributeCode('powerType')
if(powerTypeAttribute) {
	EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[powerTypeAttribute])
	EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[powerTypeAttribute])
	DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[powerTypeAttribute])
	EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[powerTypeAttribute.id])
}
/*
 * PDU
 */
def pduPortAttribute = EavAttribute.findByAttributeCode('pduPort')
if(pduPortAttribute) {
		EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[pduPortAttribute])
		EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[pduPortAttribute])
		DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[pduPortAttribute])
		EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[pduPortAttribute.id])
	}

def pduQuantityAttribute = EavAttribute.findByAttributeCode('pduQuantity')
if(pduQuantityAttribute) {
	EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[pduQuantityAttribute])
	EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[pduQuantityAttribute])
	DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[pduQuantityAttribute])
	EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[pduQuantityAttribute.id])
}

def pduTypeAttribute = EavAttribute.findByAttributeCode('pduType')
if(pduTypeAttribute) {
	EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[pduTypeAttribute])
	EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[pduTypeAttribute])
	DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[pduTypeAttribute])
	EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[pduTypeAttribute.id])
}
 /*
  * NIC
  */
def nicPortAttribute = EavAttribute.findByAttributeCode('nicPort')
if(nicPortAttribute) {
	EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[nicPortAttribute])
	EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[nicPortAttribute])
	DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[powerTypeAttribute])
	EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[nicPortAttribute.id])
}

/*
 * Fiber
 */
def fiberCabinetAttribute = EavAttribute.findByAttributeCode('fiberCabinet')
if(fiberCabinetAttribute) {
	EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[fiberCabinetAttribute])
	EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[fiberCabinetAttribute])
	DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[fiberCabinetAttribute])
	EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[fiberCabinetAttribute.id])
}

def fiberTypeAttribute = EavAttribute.findByAttributeCode('fiberType')
if(fiberTypeAttribute) {
	EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[fiberTypeAttribute])
	EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[fiberTypeAttribute])
	DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[fiberTypeAttribute])
	EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[fiberTypeAttribute.id])
}

def fiberQuantityAttribute = EavAttribute.findByAttributeCode('fiberQuantity')
if(fiberQuantityAttribute) {
	EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[fiberQuantityAttribute])
	EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[fiberQuantityAttribute])
	DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[fiberQuantityAttribute])
	EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[fiberQuantityAttribute.id])
}
/*
 *  HBA
 */
def hbaPortAttribute = EavAttribute.findByAttributeCode('hbaPort')
if(hbaPortAttribute) {
	EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[hbaPortAttribute])
	EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[hbaPortAttribute])
	DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[hbaPortAttribute])
	EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[hbaPortAttribute.id])
}
/*
 * KVM 
 */
def kvmDeviceAttribute = EavAttribute.findByAttributeCode('kvmDevice')
if(kvmDeviceAttribute) {
	EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[kvmDeviceAttribute])
 	EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[kvmDeviceAttribute])
	DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[kvmDeviceAttribute])
 	EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[kvmDeviceAttribute.id])
}

def kvmPortAttribute = EavAttribute.findByAttributeCode('kvmPort')
if(kvmPortAttribute) {
 	EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[kvmPortAttribute])
 	EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[kvmPortAttribute])
	DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[kvmPortAttribute])
 	EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[kvmPortAttribute.id])
}
/*
 * usize
 */
def usizeAttribute = EavAttribute.findByAttributeCode('usize')
if(usizeAttribute) {
	EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[usizeAttribute])
	EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[usizeAttribute])
	DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[usizeAttribute])
	EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[usizeAttribute.id])
}
/*
 * remoteMgmtPort
 */
def remoteMgmtPortAttribute = EavAttribute.findByAttributeCode('remoteMgmtPort')
if(remoteMgmtPortAttribute) {
	EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[remoteMgmtPortAttribute])
	EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[remoteMgmtPortAttribute])
	DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute = ?",[remoteMgmtPortAttribute])
	EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[remoteMgmtPortAttribute.id])
}

DataTransferAttributeMap.executeUpdate("Delete from DataTransferAttributeMap where eavAttribute.id not in ( select ea.id from EavAttribute ea) )")
