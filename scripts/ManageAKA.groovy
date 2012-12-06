
/*
 *  Fetching AKA data from manufacturer table and saving it into  manufacturer_alias table.
 * 
 */

def manufactureres = Manufacturer.list()
	
manufactureres.each { manufacturer->
	def akas = manufacturer.aka
	def akaList = akas?.split(",")
	if(akas){
		akaList.each{ aka->
			def manuExist = ManufacturerAlias.findByNameAndManufacturer(aka, manufacturer)
			if(!manuExist){
				def manufacturerAlias = new ManufacturerAlias(
															manufacturer:manufacturer, 
															name:aka.trim()
															)
				if(!manufacturerAlias.save(insert:true)){
					manufacturerAlias.errors.allErrors.each {
						println "manufacturerAlias:::::::::::::"+it
					}
				}
			}
		}
	}
}
println "AKA saved in manufacturer_alias table"

/*
 *  Fetching AKA data from model table and saving it into  model_alias table.
 *
 */

def models = Model.list()
models.each { model->
	def akas = model.aka
	def akaList = akas?.split(",")
	if(akas){
		akaList.each{ aka->
			def manuExist = ModelAlias.findByNameAndModel(aka, model)
			if(!manuExist){
				def modelAlias = new ModelAlias(
												model:model, 
												manufacturer:model.manufacturer,
												name:aka.trim()
												)
				if(!modelAlias.save(insert:true)){
					modelAlias.errors.allErrors.each {
						println "modelAlias:::::::::::::"+it
					}
				}
			}
		}
	}
}
println "AKA saved in model_alias table"