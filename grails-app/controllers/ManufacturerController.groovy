import grails.converters.JSON

import com.tds.asset.AssetEntity
import com.tdssrc.grails.WebUtil



class ManufacturerController {
	
	// Initialize services
    def jdbcTemplate
	def sessionFactory
	def securityService
	def userPreferenceService
	
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
		return 
    }
  
	/**
	 * This method is used by JQgrid to load manufacturerList
	 */
	def listJson={
		def sortIndex = params.sidx ?: 'modelName'
		def sortOrder  = params.sord ?: 'asc'
		def maxRows = Integer.valueOf(params.rows)
		def currentPage = Integer.valueOf(params.page) ?: 1
		def rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows
		
		session.MAN = [:]
		def manufacturers = Manufacturer.createCriteria().list(max: maxRows, offset: rowOffset) {
			if (params.name)
				ilike('name', "%${params.name}%")
			if (params.description)
				ilike('description', "%${params.description}%")
			
			order(sortIndex, sortOrder).ignoreCase()
		}

		def totalRows = manufacturers.totalCount
		def numberOfPages = Math.ceil(totalRows / maxRows)

		def results = manufacturers?.collect { [ cell: [ it.name, ManufacturerAlias.findAllByManufacturer( it )?.name,it.description, it.modelsCount,
					AssetEntity.countByManufacturer(it)], id: it.id,
			]}

		def jsonData = [rows: results, page: currentPage, records: totalRows, total: numberOfPages]

		render jsonData as JSON
		
	}

    def show = {
        def manufacturerInstance = Manufacturer.get( params.id )

        if(!manufacturerInstance) {
            flash.message = "Manufacturer not found with id ${params.id}"
            redirect(action:list)
        }
		else {
			 def manuAlias = WebUtil.listAsMultiValueString(manufacturerInstance.getAliases()?.name)
			 return [ manufacturerInstance : manufacturerInstance, manuAlias:manuAlias ] }
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
			def manuAlias = ManufacturerAlias.findAllByManufacturer( manufacturerInstance )
            return [ manufacturerInstance : manufacturerInstance, manuAlias:manuAlias ]
        }
    }

    def update = {
		
        def manufacturerInstance = Manufacturer.get( params.id )
        if(manufacturerInstance) {
            manufacturerInstance.properties = params
            def deletedAka = params.deletedAka
            def akaToSave = params.list('aka')
			if(deletedAka){
				ManufacturerAlias.executeUpdate("delete from ManufacturerAlias ma where ma.id in (${deletedAka})")
			}
			def manufacturerAliasList = ManufacturerAlias.findAllByManufacturer( manufacturerInstance )
			manufacturerAliasList.each{ manufacturerAlias->
				manufacturerAlias.name = params["aka_"+manufacturerAlias.id]
				manufacturerAlias.save(flush:true)
			}
			akaToSave.each{aka->
				manufacturerInstance.findOrCreateAliasByName(aka, true)
			}
			
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
		def loggedUser = securityService.getUserLogin()
        def manufacturerInstance = new Manufacturer(params)
        if(!manufacturerInstance.hasErrors() && manufacturerInstance.save()) {
			def akaNames = params.list('aka')
			if(akaNames.size() > 0){
				akaNames.each{aka->
					manufacturerInstance.findOrCreateAliasByName(aka, true)
				}
			}
            flash.message = "Manufacturer ${manufacturerInstance.name} created"
            redirect(action:list,id:manufacturerInstance.id)
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
		def toManufacturerAlias = ManufacturerAlias.findAllByManufacturer(toManufacturer).name
		
		// Add to the AKA field list in the target record
		if(!toManufacturerAlias?.contains(fromManufacturer.name)){
			def fromManufacturerAlias = ManufacturerAlias.findAllByManufacturer(fromManufacturer)
			ManufacturerAlias.executeUpdate("delete from ManufacturerAlias ma where ma.manufacturer = ${fromManufacturer.id}")
			fromManufacturerAlias.each{
				toManufacturer.findOrCreateAliasByName(it.name, true)
			}
			//merging fromManufacturer as AKA of toManufacturer
			toManufacturer.findOrCreateAliasByName(fromManufacturer.name, true)
			
			// Delete manufacturer record.
			fromManufacturer.delete()
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
		def jsonMap = [:]
		jsonMap.put("manufacturer", manufacturer)
		jsonMap.put("aliases", WebUtil.listAsMultiValueString(manufacturer.getAliases()?.name))
		
    	render jsonMap as JSON
    }
    
	/**
	 *  Validate whether requested AKA already exist in DB or not
	 *  @param: aka, name of aka
     *  @param: id, id of model
     *  @return : return aka if exists
	 */
	def validateAKA = {
		def duplicateAka = ""
		def aka = params.name
		def manuId = params.id
		def akaExist = Manufacturer.findByName(aka)
        
		if( akaExist ){
            duplicateAka = aka
		} else if(manuId) {
			def manufacturer = Manufacturer.get(manuId)
			def akaInAlias = ManufacturerAlias.findByNameAndManufacturer(aka, manufacturer)
			if( akaInAlias ){
				duplicateAka = aka
			}
		}
		render duplicateAka
	}
	
	/**
	 * render a list of suggestions for manufacturer's initial.
	 * @param : value is initial for which user wants suggestions .
	 */
	def autoCompleteManufacturer ={
		def initials = params.value
		def manufacturers = initials ? Manufacturer.findAllByNameIlike(initials+"%") : []
		[manufacturers:manufacturers]
	}
}
