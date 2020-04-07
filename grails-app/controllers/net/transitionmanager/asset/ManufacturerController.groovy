package net.transitionmanager.asset

import com.tdssrc.grails.NumberUtil
import net.transitionmanager.controller.PaginationMethods
import net.transitionmanager.exception.ServiceException
import com.tdsops.common.security.spring.HasPermission
import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.WebUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.manufacturer.Manufacturer
import net.transitionmanager.manufacturer.ManufacturerAlias
import net.transitionmanager.model.Model
import net.transitionmanager.security.Permission
import org.hibernate.criterion.Order
import org.springframework.jdbc.core.JdbcTemplate

@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class ManufacturerController implements ControllerMethods, PaginationMethods {

	static allowedMethods = [delete: 'POST', save: 'POST', update: 'PUT']
	static defaultAction = 'list'
    private final static DELETE_OK_MESSAGE = "Manufacturer deleted successfully.";
    private final static DELETE_ERROR_MESSAGE = "Manufacturer not found.";

    JdbcTemplate jdbcTemplate
	ManufacturerService manufacturerService

	@HasPermission(Permission.ManufacturerList)
	def list() {}

	/**
	 * Used by JQgrid to load manufacturerList.
	 */
	@HasPermission(Permission.ManufacturerList)
	def listJson() {
		String sortIndex = paginationOrderBy(Manufacturer, 'sidx', 'name')
		String sortOrder  = paginationSortOrder('sord')

		session.MAN = [:]
		List<Manufacturer> manufacturers = Manufacturer.createCriteria().list() {
			if (params.name) {
				ilike('name', "%$params.name%")
			}
			if (params.description) {
				ilike('description', "%$params.description%")
			}
			if (params.corporateName) {
				ilike('corporateName', "%$params.corporateName%")
			}
			if (params.corporateLocation) {
				ilike('corporateLocation', "%$params.corporateLocation%")
			}
			if (params.website) {
				ilike('website', "%$params.website%")
			}

			order((sortOrder == 'ASC' ? Order.asc(sortIndex) : Order.desc(sortIndex)).ignoreCase())
		}

		def results = manufacturers.collect {
            Map<String, Object> data = [id: it.id, name: it.name, description: it.description, corporateName: it.corporateName, corporateLocation: it.corporateLocation,
                                                                           website: it.website, modelsCount: it.modelsCount, assetCount: AssetEntity.countByManufacturer(it)]

            data.alias = WebUtil.listAsMultiValueString(ManufacturerAlias.findAllByManufacturer(it).name)
            data.aliases = (it.getAliases()) ? it.getAliases() : null
            data
        }

        renderSuccessJson([rows: results])
	}

	@HasPermission(Permission.ManufacturerView)
	def show() {
		def manufacturer = Manufacturer.get(params.id)
		if (!manufacturer) {
			flash.message = "Manufacturer not found with id $params.id"
			redirect(action: 'list')
		}
		else {
			 def manuAlias = WebUtil.listAsMultiValueString(manufacturer.aliases*.name)
			 [manufacturerInstance : manufacturer, manuAlias: manuAlias]
		}
	}

	@HasPermission(Permission.ManufacturerDelete)
	def delete() {
		def manufacturer = Manufacturer.get(params.id)
		if (manufacturer) {
			manufacturerService.delete(manufacturer)
			flash.message = "Manufacturer ${manufacturer.name} deleted"
            renderSuccessJson([status: DELETE_OK_MESSAGE] )
		} else {
			flash.message = "Manufacturer not found with id ${params.id}"
            renderSuccessJson([status: DELETE_ERROR_MESSAGE] )
		}
	}

	@HasPermission(Permission.ManufacturerEdit)
	def edit() {
		def manufacturer = Manufacturer.get(params.id)

		if(!manufacturer) {
			flash.message = "Manufacturer not found with id $params.id"
			redirect(action: 'list')
		} else {
			def manuAlias = ManufacturerAlias.findAllByManufacturer(manufacturer)
			[ manufacturerInstance : manufacturer, manuAlias:manuAlias ]
		}
	}

	@HasPermission(Permission.ManufacturerEdit)
	def update() {
		def manufacturer = Manufacturer.get(params.id)
		if (manufacturer) {
			try {
                Map manufacturerRequest = request.JSON
                List<String> aliasUpdated = (manufacturerRequest.aliasUpdated) ? manufacturerRequest.aliasUpdated : []
				manufacturer.properties = request.JSON
                String deletedAka = manufacturerRequest.aliasDeleted
                List<String> akaToSave = (manufacturerRequest.aliasAdded) ? manufacturerRequest.aliasAdded.toString().split(',').toList() : []
                Map<String, String> akaToUpdate = aliasUpdated.collectEntries {[it.id, it.name]}
				if (manufacturerService.update(manufacturer, deletedAka, akaToSave, akaToUpdate)) {
					flash.message = "Manufacturer ${params.id} updated"
                    renderSuccessJson([manufacturer: manufacturer])
				} else {
					def manuAlias = ManufacturerAlias.findAllByManufacturer(manufacturer)
					render(view: 'edit', model: [manufacturerInstance: manufacturer, manuAlias: manuAlias])
				}
			} catch (ServiceException e) {
				//log.error(e.message, e)
				flash.message = e.message
				manufacturer.clearErrors()
				def manuAlias = ManufacturerAlias.findAllByManufacturer(manufacturer)
				render(view: 'edit', model: [manufacturerInstance: manufacturer, manuAlias: manuAlias])
			}
		} else {
			flash.message = "Manufacturer not found with id $params.id"
			redirect(action:"edit", id:params.id)
		}
	}

	@HasPermission(Permission.ManufacturerCreate)
	def create() {
		[manufacturerInstance: new Manufacturer(params)]
	}

	@HasPermission(Permission.ManufacturerCreate)
	def save() {
        Map manufacturerRequest = request.JSON
        def aliasList = manufacturerRequest.aliasAdded.toString().split(',').toList()
		def manufacturer = new Manufacturer(request.JSON)

		try {
			if (manufacturerService.save(manufacturer, aliasList)) {
                renderSuccessJson([manufacturer: manufacturer])
			} else {
                renderSuccessJson([manufacturer: manufacturer])
			}
		} catch (ServiceException e) {
			render(text: HtmlUtil.escape(e.message))
		}
	}

	/*
	 *  Send List of Manufacturer as JSON object
	 */
	@HasPermission(Permission.ManufacturerView)
	def retrieveManufacturersListAsJSON() {
		String assetType = params.assetType
		boolean includeAlias = params.includeAlias == 'true'
		List<Manufacturer> manufacturers
		if (assetType == 'all') {
			manufacturers = Model.executeQuery('select m.manufacturer From Model m group by m.manufacturer order by m.manufacturer.name')
		} else {
			manufacturers = Model.executeQuery(
					'select m.manufacturer From Model m where m.assetType = ?0 ' +
					'group by m.manufacturer order by m.manufacturer.name',
					[assetType])
		}

		def manufacturersList = manufacturers.collect {
			Map<String, Object> data = [id: it.id, name: it.name]
			if (includeAlias) {
				data.alias = WebUtil.listAsMultiValueString(ManufacturerAlias.findAllByManufacturer(it).name)
			}
			data
		}
		render manufacturersList as JSON
	}

	/*
 	 *  Send List of Manufacturer to be able to select one to merge with the current manufacturer, as JSON object
 	 *  @param fromId:Current manufacturer id
	 *  @return : List of Manufacturers to merge as JSON
 	 */
	def retrieveManufacturersListToMergeAsJSON() {

		// Gets current manufacturer
		def manufacturer = Manufacturer.get(params.fromId)

		List<Manufacturer> manufacturers
		manufacturers = Manufacturer.list()

		// Remove current manufacturer from the list
		manufacturers.removeElement(manufacturer)

		manufacturers = manufacturers.sort { it.name }

		def manufacturersList = manufacturers.collect {
			Map<String, Object> data = [id: it.id, name: it.name]

			data.alias = WebUtil.listAsMultiValueString(ManufacturerAlias.findAllByManufacturer(it).name)

			data
		}
		render manufacturersList as JSON
	}

	/**
	 *  Send Manufacturer details as JSON object
	 */
	@HasPermission(Permission.ManufacturerView)
	def retrieveManufacturerAsJSON() {
		def manufacturer = Manufacturer.get(params.id)
		def jsonMap = [manufacturer: manufacturer,
					   akaCollection: manufacturer.getAliases(),
		               aliases: WebUtil.listAsMultiValueString(manufacturer.getAliases()?.name)]
		render jsonMap as JSON
	}

	/**
	* Validate whether requested manufactuer alias already exists in the database or not
	* @param: alias, the new alias to be validated
	* @param: id, id of manufacturer
	* @param: parentName, name of the manufacturer to validate the alias with (not needed if the manufacturer's name hasn't changed)
	* @return: "valid" if the alias is valid, "invalid" otherwise
	*/
	@HasPermission(Permission.ManufacturerEdit)
	def validateAliasForForm() {
		def alias = params.alias
		def manufacturerId = params.id
		def newManufacturerName = params.parentName
		
		// get the manufacturer if specified and call the service method for alias validation
		def manufacturer = manufacturerId ? Manufacturer.read(manufacturerId) : null
		def isValid = manufacturerService.isValidAlias(alias, manufacturer, true, newManufacturerName)
		if (isValid)
			render 'valid'
		else
			render 'invalid'
		
	}

	/**
	 * render a list of suggestions for manufacturer's initial.
	 * @param : value is initial for which user wants suggestions .
	 */
	@HasPermission(Permission.ManufacturerView)
	def autoCompleteManufacturer() {
		[manufacturers: params.value ? Manufacturer.findAllByNameIlike(params.value + "%") : []]
	}

    /**
     * Determine if a provider name is unique across projects
     *
     * @param name - the name to lookup.
     */
    @HasPermission(Permission.ManufacturerView)
    def validateUniqueName(String name) {
        Long id = NumberUtil.toLong(request.JSON.id)
        boolean isUnique = manufacturerService.isValidName(request.JSON.name, id)
        renderSuccessJson([isUnique: isUnique])
    }

    /**
     * Validate whether requested manufactuer alias already exists in the database or not
     * @param: alias, the new alias to be validated
     * @param: id, id of manufacturer
     * @param: parentName, name of the manufacturer to validate the alias with (not needed if the manufacturer's name hasn't changed)
     * @return: "valid" if the alias is valid, "invalid" otherwise
     */
    @HasPermission(Permission.ManufacturerEdit)
    def validateUniqueAlias() {
        def alias = request.JSON.alias
        Long manufacturerId = NumberUtil.toLong(request.JSON.id)
        def newManufacturerName = request.JSON.name

        // get the manufacturer if specified and call the service method for alias validation
        def manufacturer = manufacturerId ? Manufacturer.read(manufacturerId) : null
        def isValid = manufacturerService.isValidAlias(alias, manufacturer, true, newManufacturerName)
        renderSuccessJson([isValid: isValid])

    }
}
