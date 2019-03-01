package net.transitionmanager.domain

import com.tds.asset.AssetEntity
import com.tdsops.commons.lang.exception.PersistenceException
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
import groovy.util.logging.Slf4j

@Slf4j()
class Model {

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

	static String alternateKey = 'modelName'

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
		description nullable: true, size: 0..255
		endOfLifeDate nullable: true
		endOfLifeStatus nullable: true, size: 0..255
		height nullable: true
		layoutStyle nullable: true, size: 0..255
		modelFamily nullable: true, size: 0..255
		modelName blank: false, unique: ['manufacturer'], size: 0..255
		modelStatus nullable: true, inList: ['new', 'full', 'valid']
		powerDesign nullable: true
		powerNameplate nullable: true
		powerUse nullable: true
		productLine nullable: true, size: 0..255
		roomObject nullable: true
		sourceURL nullable: true, size: 0..255
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
	                     'noOfConnectors', 'source', 'valid']

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
		// <SL> moved to ModelService
		/*withNewSession {
			executeUpdate('update AssetEntity set model=null where model=?', [this])
			executeUpdate('delete ModelAlias where model=?', [this])
		}*/
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
	 * Create a model for a given model name and manufacturer.
	 *
	 * @param modelName  name of model
	 * @param manufacturer  the manufacturer
	 * @param assetType  (optional) asset type of model
	 * @param usize - (optional)  usize of model
	 *
	 * @deprecated <SL> is this being used?
	 */
	@Deprecated
	static Model createModelByModelName(String modelName, Manufacturer manufacturer, String assetType = 'Server',
	                                    Integer usize = 1, Person createdBy = null) {

		Model model = new Model(modelName: modelName, manufacturer: manufacturer, assetType: assetType,
				sourceTDS: 0, usize: usize, createdBy: createdBy)
		if (!model.save(flush: true)) {
			String error = 'Unable to create model: ' + GormUtil.allErrorsString(model)
			log.error('Call to createModelByModelName("{}", "{}",...) by {} - {}',
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
	 * Seartch for the first Alias Model
	 * @param aliasName String representing the alias
	 * @param manufactr Manufaturer of the Model
	 * @return
	 */
	static Model lookupFirstAlias(String aliasName, Manufacturer manufactr) {
		ModelAlias.where {
			name == aliasName && manufacturer == manufactr
		}.find()?.model
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
