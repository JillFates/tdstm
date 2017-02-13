package net.transitionmanager.domain

import com.tds.asset.AssetEntity
import com.tdsops.commons.lang.exception.PersistenceException
import com.tdssrc.eav.EavAttribute
import com.tdssrc.eav.EavAttributeOption
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
import groovy.util.logging.Slf4j
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.ModelService

@Slf4j(value='logger')
class Model {

	transient SecurityService securityService
	def modelService

	// TODO - modelName should be renamed to name (as it is in the db - confusing)
	String modelName
	String description
	String assetType = 'Server'
	String modelStatus = 'new'
	String layoutStyle

	// Blade chassis fields
	Integer bladeRows
	Integer bladeCount
	Integer bladeLabelCount
	String bladeHeight = 'Half'

	// Product information
	Integer usize = 1
	Integer useImage = 0
	Integer height
	Integer weight
	Integer depth
	Integer width
	Integer powerUse
	Integer powerNameplate
	Integer powerDesign
	String productLine
	String modelFamily
	Date endOfLifeDate
	String endOfLifeStatus
	String sourceURL        // URL of where model data was derived from

	// Room Associated properties (should be tinyint 0/1)
	Boolean roomObject

	Person createdBy
	Person updatedBy
	Person validatedBy
	Date dateCreated
	Date lastModified

	// TO BE DELETED
	byte[] frontImage
	byte[] rearImage
	Project modelScope
	Integer sourceTDS = 1
	Integer sourceTDSVersion = 1

	static hasMany = [modelConnectors: ModelConnector, racks: Rack]

	static belongsTo = [manufacturer: Manufacturer]

	static constraints = {
		assetType blank: false
		bladeCount nullable: true
		bladeHeight nullable: true, inList: ['Half', 'Full']
		bladeLabelCount nullable: true
		bladeRows nullable: true
		createdBy nullable: true
		depth nullable: true
		description nullable: true
		endOfLifeDate nullable: true
		endOfLifeStatus nullable: true
		height nullable: true
		layoutStyle nullable: true
		modelFamily nullable: true
		modelName blank: false, unique: ['manufacturer']
		modelStatus nullable: true, inList: ['new', 'full', 'valid']
		powerDesign nullable: true
		powerNameplate nullable: true
		powerUse nullable: true
		productLine nullable: true
		roomObject nullable: true
		sourceURL nullable: true
		updatedBy nullable: true
		usize nullable: true, inList: 1..52
		validatedBy nullable: true
		weight nullable: true
		width nullable: true

		// TODO - DELETE THIS
		dateCreated nullable: true
		frontImage nullable: true
		lastModified nullable: true
		modelScope nullable: true
		rearImage nullable: true
		sourceTDS nullable: true
		sourceTDSVersion nullable: true
	}

	static transients = ['aliases', 'assetsCount', 'manufacturerName',
	                     'noOfConnectors', 'securityService', 'source', 'valid']

	static mapping = {
		autoTimestamp false
		columns {
			id column: 'model_id'
			modelName column: 'name'
			// TODO : what is the point of the following three statements?
			createdBy column: 'created_by'
			updatedBy column: 'updated_by'
			validatedBy column: 'validated_by'
			// TODO : these are going to be deleted
			frontImage sqlType: 'LONGBLOB'
			rearImage sqlType: 'LONGBLOB'
			useImage sqltype: 'tinyint'
			sourceTDS sqltype: 'tinyint'
		}
	}

	String toString() { modelName }

	def beforeInsert = {
		dateCreated = TimeUtil.nowGMT()
		lastModified = TimeUtil.nowGMT()

		initializeBladeProperties()

		if (!createdBy) {
			Person person = securityService.userLoginPerson
			if (person) {
				createdBy = person
			}
			else {
				logger.info('No user found to associate to the model.')
			}
		}

		prependHttp()
	}

	def beforeUpdate = {
		lastModified = TimeUtil.nowGMT()

		initializeBladeProperties()

		prependHttp()
	}

	private void initializeBladeProperties() {
		if (assetType == 'Blade Chassis') {
			if (!bladeRows) {
				bladeRows = 2
			}
			if (!bladeCount) {
				bladeCount = 10
			}
			if (!bladeLabelCount) {
				bladeLabelCount = 5
			}
		}
		else {
			bladeRows = null
			bladeCount = null
			bladeLabelCount = null
		}
	}

	def beforeDelete = {
		withNewSession {
			executeUpdate('update AssetEntity set model=null where model=?', [this])
			executeUpdate('delete ModelAlias where model=?', [this])
		}
	}

	private List<String> getAssetTypeList() {
		EavAttributeOption.findAllByAttribute(
				EavAttribute.findByAttributeCode('assetType'),
				[sort: 'value', order: 'asc'])*.value*.trim().findAll()
	}

	int getNoOfConnectors() {
		ModelConnector.countByModel(this)
	}

	int getAssetsCount() {
		AssetEntity.countByModel(this)
	}

	String getSource() {
		sourceTDS == 1 ? 'TDS' : ''
	}

	String getManufacturerName() {
		manufacturer?.name
	}

	// alias records for the manufacturer
	List<ModelAlias> getAliases() {
		ModelAlias.findAllByModel(this, [sort: 'name'])
	}

	/**
	 * Whether the current model has been validated
	 */
	boolean isValid() {
		modelStatus == 'valid'
	}

	/**
	 * Get a ModelAlias object by name and create one (optionally) if it doesn't exist
	 * @param name  name of the model alias
	 * @param createIfNotFound  optional flag to indicating if record should be created (default false)
	 */
	ModelAlias findOrCreateAliasByName(String name, boolean createIfNotFound = false) {
		name = name.trim()
		ModelAlias alias = ModelAlias.findByNameAndModel(name, this)
		if (!alias && createIfNotFound) {
			def isValid = modelService.isValidAlias(name, this)
			alias = new ModelAlias(name: name, model: this, manufacturer: manufacturer)
			if (!isValid || !alias.save()) {
				logger.error '{}', GormUtil.allErrorsString(alias)
				return null
			}
		}
		alias
	}

	/**
	 * Create a model for a given model name and manufacturer.
	 *
	 * @param modelName  name of model
	 * @param manufacturer  the manufacturer
	 * @param assetType  (optional) asset type of model
	 * @param usize - (optional)  usize of model
	 */
	static Model createModelByModelName(String modelName, Manufacturer manufacturer, String assetType = 'Server',
	                                    int usize = 1, Person createdBy = null) {

		Model model = new Model(modelName: modelName, manufacturer: manufacturer, assetType: assetType,
				sourceTDS: 0, usize: usize, createdBy: createdBy)
		if (!model.save(flush: true)) {
			String error = 'Unable to create model: ' + GormUtil.allErrorsString(model)
			logger.error('Call to createModelByModelName("{}", "{}",...) by {} - {}',
					modelName, manufacturer.name, createdBy, error)
			throw new RuntimeException(error)
		}

		ModelConnector powerConnector = new ModelConnector(model: model, connector: 1, label: 'Pwr1',
				type: 'Power', labelPosition: 'Right', connectorPosX: 0, connectorPosY: 0, status: 'missing')
		if (!powerConnector.save(flush: true)) {
			throw new PersistenceException("Unable to create Power Connectors for $model")
					.addContextValue('messageCode', 'model.create.connector.failure')
					.addContextValue('messageArgs', [model])
					.addContextValue('gorm', GormUtil.allErrorsString(powerConnector))
		}

		model
	}

	/**
	 * Prepend "http://" for sourceURL if http:// or https:// does not exist.
	 */
	private void prependHttp() {
		if (sourceURL?.size() > 10 && !(sourceURL ==~ /(?i)^https?:\/\/.*/)) {
			sourceURL = 'http://' + sourceURL
		}
	}

	// all fields with their labels which are used in model List jqgrid.
	static final Map<String, String> modelFieldsAndlabels = [
		description: 'Description', assetType: 'Asset Type', layoutStyle: 'Layout Style', bladeRows: 'Blade Rows',
		modelScope: 'Model Scope', bladeCount: 'Blade Count', bladeLabelCount: 'Blade Label Count',
		bladeHeight: 'Blade Height', usize: 'USize', useImage: 'Use Image', height: 'Height', weight: 'Weight',
		depth: 'Depth', width: 'Width', powerUse: 'Power', powerNameplate: 'Power Name Plate', powerDesign: 'Power Design',
		productLine: 'Product Line', modelFamily: 'Model Family', endOfLifeDate: 'End Of Life Date',
		endOfLifeStatus: 'endOfLifeStatus', modelConnectors: 'No Of Connectors', roomObject: 'roomObject',
		createdBy: 'Created By', updatedBy: 'Updated By', validatedBy: 'Validated By', dateCreated: 'Date Created',
		lastModified: 'Last Modified', sourceURL: 'Source URL'].asImmutable()
}
