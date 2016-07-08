import grails.converters.JSON

import com.tds.asset.AssetEntity
import com.tdssrc.grails.WebUtil

import org.hibernate.criterion.Order

class ManufacturerController {
	
	// Initialize services
	def jdbcTemplate
	def sessionFactory
	def securityService
	
	def index() { redirect(action:"list",params:params) }
	
	// the delete, save and update actions only accept POST requests
	def allowedMethods = [delete:'POST', save:'POST', update:'POST']
	
	def list() {
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
			if (params.corporateName)
				ilike('corporateName', "%${params.corporateName}%")
			if (params.corporateLocation)
				ilike('corporateLocation', "%${params.corporateLocation}%")
			if (params.website)
				ilike('website', "%${params.website}%")
			
			order(new Order(sortIndex, sortOrder=='asc').ignoreCase())
		}
		
		def totalRows = manufacturers.totalCount
		def numberOfPages = Math.ceil(totalRows / maxRows)
		
		def results = manufacturers?.collect { [ cell: [ it.name, ManufacturerAlias.findAllByManufacturer( it )?.name, it.description, it.corporateName, it.corporateLocation, it.website, it.modelsCount,
					AssetEntity.countByManufacturer(it)], id: it.id,
			]}
		
		def jsonData = [rows: results, page: currentPage, records: totalRows, total: numberOfPages]
		
		render jsonData as JSON
		
	}
	
	def show() {
		def manufacturerInstance = Manufacturer.get( params.id )
		
		if(!manufacturerInstance) {
			flash.message = "Manufacturer not found with id ${params.id}"
			redirect(action:"list")
		}
		else {
			 def manuAlias = WebUtil.listAsMultiValueString(manufacturerInstance.getAliases()?.name)
			 return [ manufacturerInstance : manufacturerInstance, manuAlias:manuAlias ]
		}
	}
	
	def delete() {
		def manufacturerInstance = Manufacturer.get( params.id )
		if (manufacturerInstance) {
			def manuAlias = ManufacturerAlias.findAllByManufacturer( manufacturerInstance )
			manuAlias*.delete()
			ModelAlias.executeUpdate("delete from ModelAlias ma where ma.manufacturer.id = ${manufacturerInstance.id}")
			manufacturerInstance.delete(flush:true)
			flash.message = "Manufacturer ${params.id} deleted"
			redirect(action:"list")
		} else {
			flash.message = "Manufacturer not found with id ${params.id}"
			redirect(action:"list")
		}
	}
	
	def edit() {
		def manufacturerInstance = Manufacturer.get( params.id )
		
		if(!manufacturerInstance) {
			flash.message = "Manufacturer not found with id ${params.id}"
			redirect(action:"list")
		} else {
			def manuAlias = ManufacturerAlias.findAllByManufacturer( manufacturerInstance )
			return [ manufacturerInstance : manufacturerInstance, manuAlias:manuAlias ]
		}
	}
	
	def update() {
		
		def manufacturerInstance = Manufacturer.get( params.id )
		if (manufacturerInstance) {
			manufacturerInstance.properties = params
			def deletedAka = params.deletedAka
			def akaToSave = params.list('aka')
			if (deletedAka) {
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
			
			if (!manufacturerInstance.hasErrors() && manufacturerInstance.save()) {
				flash.message = "Manufacturer ${params.id} updated"
				redirect(action:"show",id:manufacturerInstance.id)
			} else {
				render(view:'edit',model:[manufacturerInstance:manufacturerInstance])
			}
		} else {
			flash.message = "Manufacturer not found with id ${params.id}"
			redirect(action:"edit",id:params.id)
		}
	}
	
	def create() {
		def manufacturerInstance = new Manufacturer()
		manufacturerInstance.properties = params
		return ['manufacturerInstance':manufacturerInstance]
	}
	
	def save() {
		def loggedUser = securityService.getUserLogin()
		def manufacturerInstance = new Manufacturer(params)
		if (!manufacturerInstance.hasErrors() && manufacturerInstance.save()) {
			def akaNames = params.list('aka')
			if ( (akaNames.size() > 0) && ! (akaNames.size() == 1 && akaNames[0].equals('')) ) {
				akaNames.each{ aka->
					manufacturerInstance.findOrCreateAliasByName(aka, true)
				}
			}
			flash.message = "Manufacturer ${manufacturerInstance.name} created"
			redirect(action:"list",id:manufacturerInstance.id)
		} else {
			render(view:'create',model:[manufacturerInstance:manufacturerInstance])
		}
	}
	
	/*
	 *  Send List of Manufacturer as JSON object
	 */
	def retrieveManufacturersListAsJSON() {
		def assetType = params.assetType
		def includeAlias = (params.includeAlias && (params.includeAlias == 'true'))
		def manufacturers = []
		if (assetType == 'all') {
			manufacturers = Model.findAll("From Model group by manufacturer order by manufacturer.name")?.manufacturer
		} else {
			manufacturers = Model.findAll("From Model where assetType = ? group by manufacturer order by manufacturer.name",[assetType])?.manufacturer
		}
		def manufacturersList = []
		manufacturers.each{
			if (includeAlias) {
				manufacturersList << [id:it.id,name:it.name, alias: WebUtil.listAsMultiValueString(ManufacturerAlias.findAllByManufacturer(it).name)]
			} else {
				manufacturersList << [id:it.id,name:it.name]
			}
		}
		render manufacturersList as JSON
	}

	/*
	 *  Send Manufacturer details as JSON object
	 */
	def retrieveManufacturerAsJSON() {
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
	def validateAKA() {
		def duplicateAka = ""
		def aka = params.name
		def manuId = params.id
		def akaExist = Manufacturer.findByName(aka)
		
		if ( akaExist ) {
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
	def autoCompleteManufacturer() {
		def initials = params.value
		def manufacturers = initials ? Manufacturer.findAllByNameIlike(initials+"%") : []
		[manufacturers:manufacturers]
	}

	def selectManufacturerToMerge() {

	}

}
