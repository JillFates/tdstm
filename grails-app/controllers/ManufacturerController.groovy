import grails.converters.JSON

class ManufacturerController {
	
	// Initialize services
    def jdbcTemplate
	
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        if(!params.max) params.max = 25
        if(!params.sort){ 
        	params.sort = 'name'
			params.order = 'asc'
        }
        [ manufacturerInstanceList: Manufacturer.list( params ) ]
    }

    def show = {
        def manufacturerInstance = Manufacturer.get( params.id )

        if(!manufacturerInstance) {
            flash.message = "Manufacturer not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ manufacturerInstance : manufacturerInstance ] }
    }

    def delete = {
        def manufacturerInstance = Manufacturer.get( params.id )
        if(manufacturerInstance) {
            manufacturerInstance.delete(flush:true)
            flash.message = "Manufacturer ${params.id} deleted"
            redirect(action:list)
        }
        else {
            flash.message = "Manufacturer not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def manufacturerInstance = Manufacturer.get( params.id )

        if(!manufacturerInstance) {
            flash.message = "Manufacturer not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ manufacturerInstance : manufacturerInstance ]
        }
    }

    def update = {
        def manufacturerInstance = Manufacturer.get( params.id )
        if(manufacturerInstance) {
            manufacturerInstance.properties = params
            if(!manufacturerInstance.hasErrors() && manufacturerInstance.save()) {
                flash.message = "Manufacturer ${params.id} updated"
                redirect(action:show,id:manufacturerInstance.id)
            }
            else {
                render(view:'edit',model:[manufacturerInstance:manufacturerInstance])
            }
        }
        else {
            flash.message = "Manufacturer not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }

    def create = {
        def manufacturerInstance = new Manufacturer()
        manufacturerInstance.properties = params
        return ['manufacturerInstance':manufacturerInstance]
    }

    def save = {
        def manufacturerInstance = new Manufacturer(params)
        if(!manufacturerInstance.hasErrors() && manufacturerInstance.save()) {
            flash.message = "Manufacturer ${manufacturerInstance.id} created"
            redirect(action:show,id:manufacturerInstance.id)
        }
        else {
            render(view:'create',model:[manufacturerInstance:manufacturerInstance])
        }
    }
    /*
     *  Send List of Manufacturer as JSON object
     */
	def getManufacturersListAsJSON = {
    	def assetType = params.assetType
    	def manufacturers = Model.findAll("From Model where assetType = ? group by manufacturer ",[assetType])?.manufacturer
		render manufacturers as JSON
    }
    /*
     * When the user clicks on an item do the following actions:
     *	1. Add to the AKA field list in the target record
	 *	2. Revise Model, Asset, and any other records that may point to this manufacturer
	 *	3. Delete manufacturer record.
	 *	4. Return to manufacturer list view with the flash message "Merge completed."
     */
	def merge = {
    	
		// Get the manufacturer instances for params ids
		def toManufacturer = Manufacturer.get(params.id)
		def fromManufacturer = Manufacturer.get(params.fromId)
		
		// Add to the AKA field list in the target record
		if(!toManufacturer.aka?.contains(fromManufacturer.name)){
			def aka = toManufacturer.aka ? toManufacturer.aka +","+fromManufacturer.name : fromManufacturer.name
			toManufacturer.aka = aka 
			if(!toManufacturer.hasErrors())
				toManufacturer.save(flush:true)
		}
		
		// Revise Model, Asset, and any other records that may point to this manufacturer
		def updateAssetsQuery = "update asset_entity set manufacturer_id = ${toManufacturer.id} where manufacturer_id='${fromManufacturer.id}'"
		jdbcTemplate.update(updateAssetsQuery)
		
		def updateModelsQuery = "update model set manufacturer_id = ${toManufacturer.id} where manufacturer_id='${fromManufacturer.id}'"
		jdbcTemplate.update(updateModelsQuery)
		
		// Delete manufacturer record.
		fromManufacturer.delete(flush:true)
		
		// Return to manufacturer list view with the flash message "Merge completed."
    	flash.message = "Merge completed."
    	redirect(action:list)
    }
}
