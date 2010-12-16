
import com.tdssrc.eav.*
import com.tdssrc.grails.GormUtil

def jdbcTemplate = ctx.getBean("jdbcTemplate")

println"**************UPDATE MANUFACTURER***************"
// Create manufactures if not exist
def manufacturerResultMap = jdbcTemplate.queryForList("select manufacturer from asset_entity where manufacturer_id is null && manufacturer != '' && manufacturer is not null group by manufacturer")
	manufacturerResultMap.each{ result->
		def manufacturerInstance = Manufacturer.findByName( result.manufacturer )
		if( !manufacturerInstance ){
			manufacturerInstance = new Manufacturer( name : result.manufacturer )
			if ( !manufacturerInstance.validate() || !manufacturerInstance.save() ) {
				def etext = "Unable to create manufacturerInstance" +
				GormUtil.allErrorsString( manufacturerInstance )
				println etext
			}
		}
		def manufacturer = result.manufacturer
		manufacturer = manufacturer.replace("'","\\'")
		def updateQuery = "update asset_entity set manufacturer_id = ${manufacturerInstance.id} where manufacturer='${manufacturer}'"
		def updated = jdbcTemplate.update(updateQuery)
		println "Updated '${result.manufacturer}' Manufacturer id ${manufacturerInstance.id} for ${updated} assets"
	}
println"**************UPDATE MODEL***************"
//Create model if not exist
def modelResultMap = jdbcTemplate.queryForList("select model, manufacturer_id as manufacturer from asset_entity where model_id is null && model != '' && model is not null group by model")
	modelResultMap.each{ result->
		def manufacturerInstance = result.manufacturer ? Manufacturer.findById( result.manufacturer ) : ""
		if( manufacturerInstance ){
			def modelInstance = Model.findByModelNameAndManufacturer( result.model, manufacturerInstance  )
			if(!modelInstance){
				modelInstance = new Model( modelName : result.model, manufacturer : manufacturerInstance )
				if ( !modelInstance.validate() || !modelInstance.save() ) {
					def etext = "Unable to create modelInstance" +
					GormUtil.allErrorsString( modelInstance )
					println etext
				}
			}
			def model = result.model
			model = model.replace("'","\\'")
			def updateQuery = "update asset_entity set model_id = ${modelInstance.id} where model='${model}'"
			def updated = jdbcTemplate.update(updateQuery)
			println "Updated '${result.model}' Model id ${modelInstance.id} for ${updated} assets"
		}
	}
println"**************DELETE POWER_TYPE***************"
def powerTypeAttribute = EavAttribute.findByAttributeCode('powerType')

	if(powerTypeAttribute) {
		
		EavAttributeOption.executeUpdate("Delete from EavAttributeOption where attribute = ?",[powerTypeAttribute])
		EavEntityAttribute.executeUpdate("Delete from EavEntityAttribute where attribute = ?",[powerTypeAttribute])
		EavAttribute.executeUpdate("Delete from EavAttribute where id = ?",[powerTypeAttribute.id])
	}
