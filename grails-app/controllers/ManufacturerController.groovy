import com.tds.asset.AssetEntity
import com.tdssrc.grails.WebUtil
import grails.converters.JSON
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.Manufacturer
import net.transitionmanager.domain.ManufacturerAlias
import net.transitionmanager.domain.Model
import net.transitionmanager.domain.ModelAlias
import net.transitionmanager.service.SecurityService
import org.hibernate.criterion.Order
import org.springframework.jdbc.core.JdbcTemplate

import grails.plugin.springsecurity.annotation.Secured
@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class ManufacturerController implements ControllerMethods {

	static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
	static defaultAction = 'list'

	JdbcTemplate jdbcTemplate
	SecurityService securityService

	def list() {}

	/**
	 * Used by JQgrid to load manufacturerList.
	 */
	def listJson() {
		String sortIndex = params.sidx ?: 'modelName'
		String sortOrder  = params.sord ?: 'asc'
		int maxRows = params.int('rows')
		int currentPage = params.int('page') ?: 1
		int rowOffset = (currentPage - 1) * maxRows

		session.MAN = [:]
		List<Manufacturer> manufacturers = Manufacturer.createCriteria().list(max: maxRows, offset: rowOffset) {
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

			order((sortOrder == 'asc' ? Order.asc(sortIndex) : Order.desc(sortIndex)).ignoreCase())
		}

		int totalRows = manufacturers.totalCount
		def results = manufacturers.collect { [cell: [it.name, ManufacturerAlias.findAllByManufacturer(it)?.name,
		                                              it.description, it.corporateName, it.corporateLocation,
		                                              it.website, it.modelsCount, AssetEntity.countByManufacturer(it)],
		                                        id: it.id] }

		def jsonData = [rows: results, page: currentPage, records: totalRows, total: Math.ceil(totalRows / maxRows)]

		render jsonData as JSON
	}

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

	def delete() {
		def manufacturer = Manufacturer.get(params.id)
		if (manufacturer) {
			def manuAlias = ManufacturerAlias.findAllByManufacturer(manufacturer)
			manuAlias*.delete()
			ModelAlias.executeUpdate("delete from ModelAlias ma where ma.manufacturer.id = $manufacturer.id")
			manufacturer.delete(flush:true)
			flash.message = "Manufacturer $params.id deleted"
			redirect(action: 'list')
		} else {
			flash.message = "Manufacturer not found with id $params.id"
			redirect(action: 'list')
		}
	}

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

	def update() {

		def manufacturer = Manufacturer.get(params.id)
		if (manufacturer) {
			manufacturer.properties = params
			def deletedAka = params.deletedAka
			def akaToSave = params.list('aka')
			if (deletedAka) {
				ManufacturerAlias.executeUpdate("delete from ManufacturerAlias ma where ma.id in ($deletedAka)")
			}
			def manufacturerAliasList = ManufacturerAlias.findAllByManufacturer(manufacturer)
			manufacturerAliasList.each { manufacturerAlias ->
				manufacturerAlias.name = params["aka_"+manufacturerAlias.id]
				manufacturerAlias.save(flush:true)
			}
			akaToSave.each { aka ->
				manufacturer.findOrCreateAliasByName(aka, true)
			}

			if (!manufacturer.hasErrors() && manufacturer.save()) {
				flash.message = "Manufacturer $params.id updated"
				redirect(action: "show", id: manufacturer.id)
			} else {
				render(view: 'edit', model: [manufacturerInstance: manufacturer])
			}
		} else {
			flash.message = "Manufacturer not found with id $params.id"
			redirect(action:"edit",id:params.id)
		}
	}

	def create() {
		[manufacturerInstance: new Manufacturer(params)]
	}

	def save() {
		def manufacturer = new Manufacturer(params)
		if (!manufacturer.hasErrors() && manufacturer.save()) {
			def akaNames = params.list('aka')
			if ((akaNames.size() > 0) && ! (akaNames.size() == 1 && akaNames[0].equals(''))) {
				akaNames.each { aka ->
					manufacturer.findOrCreateAliasByName(aka, true)
				}
			}
			flash.message = "Manufacturer $manufacturer.name created"
			redirect(action: 'list',id:manufacturer.id)
		} else {
			render(view:'create',model:[manufacturerInstance:manufacturer])
		}
	}

	/*
	 *  Send List of Manufacturer as JSON object
	 */
	def retrieveManufacturersListAsJSON() {
		String assetType = params.assetType
		boolean includeAlias = params.includeAlias == 'true'
		List<Manufacturer> manufacturers
		if (assetType == 'all') {
			manufacturers = Model.executeQuery('select m.manufacturer From Model m group by m.manufacturer order by m.manufacturer.name')
		} else {
			manufacturers = Model.executeQuery(
					'select m.manufacturer From Model m where m.assetType = ? ' +
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

	/**
	 *  Send Manufacturer details as JSON object
	 */
	def retrieveManufacturerAsJSON() {
		def manufacturer = Manufacturer.get(params.id)
		def jsonMap = [manufacturer: manufacturer,
		               aliases: WebUtil.listAsMultiValueString(manufacturer.getAliases()?.name)]
		render jsonMap as JSON
	}

	/**
	 *  Validate whether requested AKA already exist in DB or not
	 *  @param: aka, name of aka
	 *  @param: id, id of model
	 *  @return : return aka if exists
	 */
	def validateAKA() {
		def aka = params.name

		def akaExist = Manufacturer.findByName(aka)
		if (akaExist) {
			render aka
		}

		def manuId = params.id
		if (manuId) {
			if (ManufacturerAlias.countByNameAndManufacturer(aka, Manufacturer.load(manuId))) {
				render aka
			}
		}

		render ''
	}

	/**
	 * render a list of suggestions for manufacturer's initial.
	 * @param : value is initial for which user wants suggestions .
	 */
	def autoCompleteManufacturer() {
		[manufacturers: params.value ? Manufacturer.findAllByNameIlike(params.value + "%") : []]
	}
}
