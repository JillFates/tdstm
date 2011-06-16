import grails.converters.JSON
import org.jmesa.facade.TableFacade
import org.jmesa.facade.TableFacadeImpl
import org.jmesa.limit.Limit


class ManufacturerController {
	
	// Initialize services
    def jdbcTemplate
	def sessionFactory
	
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        
        if(!params.sort){ 
        	params.sort = 'name'
			params.order = 'asc'
        }
		boolean filter = params.filter
		if(filter){
			session.modelFilters.each{
				if(it.key.contains("tag")){
					request.parameterMap[it.key] = [session.modelFilters[it.key]]
				}
			}
		} else {
			session.modelFilters = params
		}
        def manufacturersList = Manufacturer.list( params )
        TableFacade tableFacade = new TableFacadeImpl("tag",request)
        tableFacade.items = manufacturersList
        Limit limit = tableFacade.limit
		if(limit.isExported()){
            tableFacade.setExportTypes(response,limit.getExportType())
            tableFacade.setColumnProperties("name","aka","description")
            tableFacade.render()
        }else
            return [manufacturersList : manufacturersList]
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
    	def manufacturers = Model.findAll("From Model where assetType = ? group by manufacturer order by manufacturer.name",[assetType])?.manufacturer
		def manufacturersList = []
    	manufacturers.each{
    		manufacturersList << [id:it.id,name:it.name]
    	}
    	render manufacturersList as JSON
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
		
		// Revise Model, Asset, and any other records that may point to this manufacturer
		def updateAssetsQuery = "update asset_entity set manufacturer_id = ${toManufacturer.id} where manufacturer_id='${fromManufacturer.id}'"
		jdbcTemplate.update(updateAssetsQuery)
		
		def updateModelsQuery = "update model set manufacturer_id = ${toManufacturer.id} where manufacturer_id='${fromManufacturer.id}'"
		jdbcTemplate.update(updateModelsQuery)
		
		// Add to the AKA field list in the target record
		if(!toManufacturer.aka?.contains(fromManufacturer.name)){
			def aka = new StringBuffer(toManufacturer.aka ? toManufacturer.aka+"," : "")
			aka.append(fromManufacturer.name)
			aka.append(fromManufacturer.aka ? ","+fromManufacturer.aka : "")
			
			// Delete manufacturer record.
			fromManufacturer.delete()
			
			sessionFactory.getCurrentSession().flush();
						
			toManufacturer.aka = aka.toString() 
			if(toManufacturer.validate(true))
				toManufacturer.save(flush:true)
			else 
				toManufacturer.errors.allErrors.each() {println it }
			
		} else {
			//	Delete manufacturer record.
			fromManufacturer.delete()
			
			sessionFactory.getCurrentSession().flush();
		}
		
		// Return to manufacturer list view with the flash message "Merge completed."
    	flash.message = "Merge completed."
    	redirect(action:list)
    }
    /*
     *  Send Manufacturer details as JSON object
     */
	def getManufacturerAsJSON = {
    	def id = params.id
    	def manufacturer = Manufacturer.get(params.id)
    	render manufacturer as JSON
    }
}
