import com.tdssrc.grails.GormUtil;

def jdbcTemplate = ctx.getBean("jdbcTemplate")
println"**************UPDATE MANUFACTURER***************"
// Create manufactures if not exist
def manufacturerResultMap = jdbcTemplate.queryForList("select manufacturer from asset_entity where manufacturer != '' and manufacturer is not null group by manufacturer")
	manufacturerResultMap.each{ result->
		def manufacturerName = result.manufacturer.replaceAll("\\s+\$", "").replaceAll("^\\s+", "")
		def manufacturerInstance = Manufacturer.findByName( manufacturerName )
		if( !manufacturerInstance ){
			def manufacturers = Manufacturer.findAllByAkaIsNotNull()
			manufacturers.each{manufacturer->
				if(manufacturer.aka.toLowerCase().contains( manufacturerName.toLowerCase() )){
					manufacturerInstance = manufacturer
				}
			}
			if(!manufacturerInstance){
				manufacturerInstance = new Manufacturer( name : manufacturerName )
				if ( !manufacturerInstance.validate() || !manufacturerInstance.save() ) {
					def etext = "Unable to create manufacturerInstance" +
					GormUtil.allErrorsString( manufacturerInstance )
					println etext
				}
			}
		}
		manufacturerName = manufacturerName.replace("'","\\'")
		def updateQuery = "update asset_entity set manufacturer_id = ${manufacturerInstance.id} where manufacturer='${manufacturerName}'"
		def updated = jdbcTemplate.update(updateQuery)
		println "Updated '${manufacturerName}' Manufacturer id ${manufacturerInstance.id} for ${updated} assets"
	}
println"**************UPDATE MODEL***************"
//Create model if not exist
def modelResultMap = jdbcTemplate.queryForList("select distinct model, manufacturer as manufacturer, asset_type as assetType from asset_entity where model != '' and model is not null and manufacturer != '' and manufacturer is not null and asset_type != '' and asset_type is not null order by model")
	modelResultMap.each{ result->
		def manufacturerInstance = result.manufacturer ? Manufacturer.findByName( result.manufacturer ) : ""
		def model = result.model.replaceAll("\\s+\$", "").replaceAll("^\\s+", "")
		def assetType = result.assetType
		if( !manufacturerInstance ){
			def manufacuturers = Manufacturer.findAllByAkaIsNotNull()
			manufacuturers.each{manufacuturer->
				if(manufacuturer.aka.toLowerCase().contains( result.manufacturer.toLowerCase() )){
					manufacturerInstance = manufacuturer
				}
			}
		}
		if( manufacturerInstance && assetType ){
			def modelInstance = Model.findWhere(modelName:model,assetType : assetType,manufacturer: manufacturerInstance  )
			if(!modelInstance){
				def models = Model.findAllByManufacturerAndAkaIsNotNull( manufacturerInstance ).findAll{it.assetType == assetType}
				models.each{ 
					if(it.aka.toLowerCase().contains( model.toLowerCase() )){
						modelInstance = it
					}
				}
				if(!modelInstance){
					modelInstance = new Model( modelName : model, assetType:assetType, manufacturer : manufacturerInstance )
					if ( !modelInstance.validate() || !modelInstance.save() ) {
						def etext = "Unable to create modelInstance" +
						GormUtil.allErrorsString( modelInstance )
						println etext
					}
				}
			}
			
			model = model.replace("'","\\'")
			def updateQuery = "update asset_entity set model_id = ${modelInstance.id} where model='${model}' and manufacturer='${manufacturerInstance.name}' and asset_type = '${assetType}'"
			def updated = jdbcTemplate.update(updateQuery)
			println "Updated '${model}' Model id ${modelInstance.id} for ${updated} assets"
		}
	}