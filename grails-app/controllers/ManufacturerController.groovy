import grails.converters.JSON
import net.tds.util.jmesa.AssetEntityBean
import org.jmesa.facade.TableFacade
import org.jmesa.facade.TableFacadeImpl
import org.jmesa.limit.Limit

import com.tds.asset.AssetEntity
import com.tdssrc.grails.WebUtil



class ManufacturerController {
	
	// Initialize services
    def jdbcTemplate
	def sessionFactory
	def securityService
	
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
		def manufacturerList =  new ArrayList()
        def manufacturersList = Manufacturer.list( params )
		manufacturersList.each{manufacturer->
			AssetEntityBean assetBeanInstance = new AssetEntityBean();
			assetBeanInstance.setId(manufacturer.id)
			assetBeanInstance.setName(manufacturer.name)
			assetBeanInstance.setDescription(manufacturer.description)
			assetBeanInstance.setAka(WebUtil.listAsMultiValueString(ManufacturerAlias.findAllByManufacturer( manufacturer )?.name))
			assetBeanInstance.setModelCount(manufacturer.modelsCount)
			assetBeanInstance.setCount(AssetEntity.countByManufacturer(manufacturer))
			
			manufacturerList.add(assetBeanInstance)
		}
		
		
        TableFacade tableFacade = new TableFacadeImpl("tag",request)
        tableFacade.items = manufacturerList
        Limit limit = tableFacade.limit
		if(limit.isExported()){
            tableFacade.setExportTypes(response,limit.getExportType())
            tableFacade.setColumnProperties("name","aka","description")
            tableFacade.render()
        }else
            return [manufacturersList : manufacturerList]
    }

    def show = {
        def manufacturerInstance = Manufacturer.get( params.id )

        if(!manufacturerInstance) {
            flash.message = "Manufacturer not found with id ${params.id}"
            redirect(action:list)
        }
		else {
			 def manuAlias = WebUtil.listAsMultiValueString(ManufacturerAlias.findAllByManufacturer( manufacturerInstance )?.name)
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
				manufacturerInstance.findOrCreateByName(aka, true)
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
					manufacturerInstance.findOrCreateByName(aka, true)
				}
			}
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
		def toManufacturerAlias = ManufacturerAlias.findAllByManufacturer(toManufacturer).name
		
		// Add to the AKA field list in the target record
		if(!toManufacturerAlias?.contains(fromManufacturer.name)){
			def fromManufacturerAlias = ManufacturerAlias.findAllByManufacturer(fromManufacturer)
			ManufacturerAlias.executeUpdate("delete from ManufacturerAlias ma where ma.manufacturer = ${fromManufacturer.id}")
			fromManufacturerAlias.each{
				toManufacturer.findOrCreateByName(it.name, true)
			}
			
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
    	render manufacturer as JSON
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
}
