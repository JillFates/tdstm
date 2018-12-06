package net.transitionmanager.service

import com.tds.asset.Application
import com.tds.asset.ApplicationAssetMap
import com.tds.asset.AssetCableMap
import com.tds.asset.AssetComment
import com.tds.asset.AssetDependency
import com.tds.asset.AssetDependencyBundle
import com.tds.asset.AssetEntity
import com.tds.asset.AssetOptions
import com.tds.asset.AssetType
import com.tds.asset.Database
import com.tds.asset.Files
import com.tdsops.common.exceptions.ConfigurationException
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.common.sql.SqlUtil
import com.tdsops.tm.domain.AssetEntityHelper
import com.tdsops.tm.enums.domain.AssetCableStatus
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.AssetDependencyStatus
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdsops.tm.enums.domain.ValidationType
import com.tdssrc.grails.ApplicationConstants
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WebUtil
import com.tdssrc.grails.WorkbookUtil
import grails.converters.JSON
import grails.transaction.Transactional
import net.transitionmanager.asset.DeviceUtils
import net.transitionmanager.command.AssetCommand
import net.transitionmanager.command.CloneAssetCommand
import net.transitionmanager.controller.ServiceResults
import net.transitionmanager.domain.AppMoveEvent
import net.transitionmanager.domain.Manufacturer
import net.transitionmanager.domain.Model
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.ProjectAssetMap
import net.transitionmanager.domain.ProjectTeam
import net.transitionmanager.domain.Rack
import net.transitionmanager.domain.Room
import net.transitionmanager.search.FieldSearchData
import net.transitionmanager.security.Permission
import net.transitionmanager.strategy.asset.AssetSaveUpdateStrategy
import org.apache.commons.lang.StringEscapeUtils as SEU
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.math.NumberUtils
import org.apache.poi.ss.usermodel.Cell
import org.hibernate.Criteria
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

import javax.servlet.http.HttpSession
import java.text.DateFormat
import java.util.regex.Matcher

@Transactional
class AssetEntityService implements ServiceMethods {

	// TODO : JPM 9/2014 : determine if customLabels is used as it does NOT have all of the values it should
	protected static final List<String> customLabels = (1..Project.CUSTOM_FIELD_COUNT).collect { 'Custom' + it }.asImmutable()

	// properties that should be excluded from the custom column select list
	private static final Map<String, List<String>> COLUMN_PROPS_TO_EXCLUDE = [
			(AssetClass.APPLICATION): [ 'assetName'],
			(AssetClass.DATABASE): [ 'assetName'],
			(AssetClass.DEVICE): [
				'assetName', 'assetType', 'manufacturer', 'model', 'planStatus', 'moveBundle', 'sourceLocationName'
			],
			(AssetClass.STORAGE): [ 'assetName' ]
	].asImmutable()

	// The follow define the various properties that can be used with bindData to assign domain.properties
	static final List<String> CUSTOM_PROPERTIES = (1..Project.CUSTOM_FIELD_COUNT).collect { 'custom' + it }.asImmutable()

	// Common properties for all asset classes (Application, Database, Files/Storate, Device)
	static final List<String> ASSET_PROPERTIES = [
		'assetName',  'shortName', 'priority', 'planStatus', 'department',
		'costCenter', 'maintContract', 'maintExpDate', 'retireDate',
		'description', 'supportType', 'environment', 'serialNumber',
		'validation', 'externalRefId', 'size', 'scale', 'rateOfChange'].asImmutable()

	// Properties strictly for DEVICES (a.k.a. AssetEntity)
	static final List<String> DEVICE_PROPERTIES = [
		'assetTag', 'assetType', 'ipAddress', 'os', 'usize', 'truck', 'cart', 'shelf', 'railType',
		'sourceBladePosition', 'targetRackPosition', 'sourceRackPosition', 'targetBladePosition'
	].asImmutable()

	// Properties strictly for ASSETS that are date (a.k.a. AssetEntity)
	static final List<String> ASSET_DATE_PROPERTIES = ['purchaseDate', 'maintExpDate', 'retireDate'].asImmutable()

	// List of all of the Integer properties for the potentially any of the asset classes
	static final List<String> ASSET_INTEGER_PROPERTIES = [
		'size', 'rateOfChange', 'priority', 'sourceBladePosition',
		'targetBladePosition', 'sourceRackPosition',
		'targetRackPosition'].asImmutable()

	static final Map<String, Map<String, String>> ASSET_TYPE_NAME_MAP = [
			(AssetType.APPLICATION.toString()): [
					internalName: "application", frontEndName: "Application", frontEndNamePlural: "Applications",
					labelPreferenceName: "appLbl", labelText: "Application", labelHandles: "application"],

			(AssetType.DATABASE.toString()): [
					internalName: "database", frontEndName: "Database", frontEndNamePlural: "Databases",
					labelPreferenceName: "dbLbl", labelText: "Database", labelHandles: "database"],

			(AssetType.SERVER.toString()): [
					internalName: "serverPhysical", frontEndName: "Physical Server", frontEndNamePlural: "Physical Servers",
					labelPreferenceName: "svrLbl", labelText: "Physical Server", labelHandles: "serverPhysical"],

			(AssetType.VM.toString()): [
					internalName: "serverVirtual", frontEndName: "Virtual Server", frontEndNamePlural: "Virtual Servers",
					labelPreferenceName: "svrLbl", labelText: "Virtual Server", labelHandles: "serverVirtual"],

			(AssetType.FILES.toString()): [
					internalName: "storageLogical", frontEndName: "Logical Storage", frontEndNamePlural: "Logical Storage",
					labelPreferenceName: "slLbl", labelText: "Logical Storage", labelHandles: "storageLogical"],

			(AssetType.STORAGE.toString()): [
					internalName: "storagePhysical", frontEndName: "Physical Storage", frontEndNamePlural: "Physical Storage",
					labelPreferenceName: "slpLbl", labelText: "Storage Device", labelHandles: "storagePhysical"],

			(AssetType.NETWORK.toString()): [
					internalName: "networkPhysical", frontEndName: "Network", frontEndNamePlural: "Network",
					labelPreferenceName: "netLbl", labelText: "Network Device", labelHandles: "networkPhysical"],

			(AssetType.NETWORK.toString()): [
					internalName: "networkLogical", frontEndName: "Network", frontEndNamePlural: "Network",
					labelPreferenceName: "netLbl", labelText: "Network Device", labelHandles: "networkLogical"],

			Other: [
					internalName: "other", frontEndName: "Other Device", frontEndNamePlural: "Other Devices",
					labelPreferenceName: "oLbl", labelText: "Other Device", labelHandles: "other"]
	].asImmutable()

	private static final Map<String, String> TITLE_BY_FILTER = [
			all: 'Devices', other: 'Other Devices', physical: 'Physical Device', physicalServer: 'Physical Server',
			server: 'Server', storage: 'Storage Device', virtualServer: 'Virtual Server'].asImmutable()

	private static final Map<String, String> RACK_MAP = [
			rackId: 'id', Tag: 'tag', Location: 'location', Room: 'room', RoomX: 'roomX', RoomY: 'roomY',
			PowerA: 'powerA', PowerB: 'powerB', PowerC: 'powerC', Type: 'rackType', Front: 'front',
			Model: 'model', Source: 'source'].asImmutable()

	JdbcTemplate jdbcTemplate
	NamedParameterJdbcTemplate namedParameterJdbcTemplate

	def assetEntityAttributeLoaderService
	def customDomainService
	def partyRelationshipService
	def progressService
	def projectService
	def rackService
	def roomService
	def taskService
	def userPreferenceService
    def assetService
    def commentService
	def tagAssetService
	AssetOptionsService assetOptionsService

	/**
	 * This map contains a key for each asset class and a list of their
	 * related asset types.
	 */
	private static final Map<String, Map<String, Object>> typesInfoByClassMap = [
			'APPLICATION':     [assetClass: AssetClass.APPLICATION, domain: Application],
			'SERVER-DEVICE':   [assetClass: AssetClass.DEVICE,      domain: AssetEntity,
			                    assetType:  AssetType.serverTypes],
			'DATABASE':        [assetClass: AssetClass.DATABASE,    domain: Database ],
			'NETWORK-DEVICE':  [assetClass: AssetClass.DEVICE,      domain: AssetEntity,
			                    assetType:  AssetType.networkDeviceTypes],
			// 'NETWORK-LOGICAL': [],
			'STORAGE-DEVICE':  [assetClass: AssetClass.DEVICE,      domain: AssetEntity,
			                    assetType:  AssetType.storageTypes],
			'STORAGE-LOGICAL': [assetClass: AssetClass.STORAGE,     domain: Files],
			'OTHER-DEVICE':    [assetClass: AssetClass.DEVICE,      domain: AssetEntity,
			                    assetType:  AssetType.nonOtherTypes, notIn: true]].asImmutable()

	def getAssetClasses() {
		return AssetClass.classOptions
	}

	/**
	 * Returns the assets for the given class name.
	 * @param params: map of parameters to query for assets. Keys: assetClass, page, max.
	 * @param limitResults: boolean to indicate that pagination/limit/offset are required.
	 */
	def getAssetsByClass(Map params, boolean limitResults = false) {
		def results = []
		Project project = securityService.userCurrentProject
		def additionalFilters = [sort:'assetName']

		// We'll query for assets if there's a valid asset class (and project).
		if (project && typesInfoByClassMap.containsKey(params.assetClass)) {

			def typeInfo = typesInfoByClassMap[params.assetClass]
			def assetClass = typeInfo.assetClass

			// Checks if paging is required.
			if (limitResults) {
				int max = Math.min(params.int('max', 10), 25)
				int currentPage = params.int('page', 1)
				additionalFilters.max = max
				additionalFilters.offset = currentPage == 1 ? 0 : (currentPage - 1) * max
			}

			// TODO: We should include more fields.
			StringBuilder query = new StringBuilder("SELECT a.id as id, a.assetName as text")

			StringBuilder fromQuery = new StringBuilder(" FROM ")
				.append(typeInfo.domain.name)
				.append(" AS a")

			StringBuilder whereQuery = new StringBuilder(" WHERE a.project=:project AND a.assetClass=:assetClass")

			def qparams = [project:project, assetClass:typeInfo.assetClass]

			def doJoin = typeInfo.containsKey('assetType')
			def notIn = typeInfo.containsKey('notIn') && typeInfo.notIn

			if (doJoin) {
				if (notIn) {
					fromQuery.append(" LEFT OUTER JOIN a.model AS m")
					whereQuery.append(" AND COALESCE(m.assetType,'') NOT")
				} else {
					fromQuery.append(" JOIN a.model AS m")
					whereQuery.append(" AND m.assetType ")
				}
				whereQuery.append(' IN (:assetType)')
				qparams.assetType = typeInfo.assetType
			}

			query.append(fromQuery).append(whereQuery)
			query.append(' ORDER BY text')

			List assets = typeInfo.domain.executeQuery(query.toString(), qparams, additionalFilters)
			assets.each { a -> results << [id: a[0], name: a[1]]}
			return results
		}

		return results
	}

	/**
	 * Used by the various asset controllers to return the JSON response to the callers when updating existing asset
	 * @param model
	 * @param errorMsg
	 */
	void renderUpdateAssetJsonResponse(controller, Map model, errorMsg) {
		renderSaveUpdateJsonResponse(controller, model, errorMsg, false)
	}

	/**
	 * The private method used by renderSaveAssetJsonResponse and renderUpdateJsonResponse
	 */
	private void renderSaveUpdateJsonResponse(controller, Map model, errorMsg, boolean isSave) {
		// Handle results as standardized Ajax return value
		if (errorMsg) {
			controller.render ServiceResults.errors(errorMsg) as JSON
		} else if (!model) {
			def msg = "An error occurred after ${isSave ? 'creating' : 'updating'} the asset only affects displaying it. Please refresh the list to view the asset."
			controller.render ServiceResults.errors(msg) as JSON
		} else {
			controller.render(ServiceResults.success(model) as JSON)
		}
	}

	/**
	 * Used to assign a device to a blade chassis or rack appropriately. In the case of a VM it will clear out
	 * all of the rack/chassis properties appropriately that shouldn't be set in the first place.
	 * @param project - the project object that the device belongs to
	 * @param device - the device to be assigned
	 * @param params - the input parameters passed in from the device create/edit form (TODO : JPM 10/2014 : Define what params are used)
	 */
	void assignDeviceToChassisOrRack(Project project, AssetEntity device, Map params) {
		if (device.isaBlade()) {
			if (NumberUtil.toLong(params.roomSourceId) > 0) {
				assignBladeToChassis(project, device, params.sourceChassis, true)
			}
			if (NumberUtil.toLong(params.roomTargetId) > 0) {
				assignBladeToChassis(project, device, params.targetChassis, false)
			}
		} else if (device.isaVM()) {
			// Clear out various physical assignments that shouldn't be there anyways
			[	'rackSource', 'sourceRackPosition',
			    'rackTarget', 'targetRackPosition',
			    'sourceChassis', 'sourceBladePosition',
			    'targetChassis', 'targetBladePosition'
			].each { prop -> device[prop] = null }
		} else {
			// This should handle all rackable devices
			// Set the source/target rack appropriate and create the rack appropriately
			assignAssetToRack(project, device, params.rackSourceId, params.rackSource, true)
			assignAssetToRack(project, device, params.rackTargetId, params.rackTarget, false)
		}
	}

	/**
	 * Used to an asset to a Bundle and validate that it can be assigned
	 * @param project - the user's current project
	 * @param asset - the asset object to be assigned
	 * @param bundleId - the id of the bundle to assign to
	 */
	void assignAssetToBundle(Project project, AssetEntity asset, String bundleId) {
		Long id = NumberUtil.toLong(bundleId)

		if (id == null) {
			throw new InvalidRequestException('Invalid Bundle id was specified')
		}
		MoveBundle mb = MoveBundle.get(id)
		if (!mb) {
			throw new InvalidRequestException('Unable to find the Bundle id that was specified')
		}
		if (mb.project != project) {
			securityService.reportViolation("Attempted to assign asset to bundle ($id) not associated with project ($project.id)")
			throw new InvalidRequestException('Unable to find the Bundle id that was specified')
		}

		asset.moveBundle = mb
	}

	/**
	 * Used to set the source and target rooms on a device that was passed in from the form
	 * @param project - the current project of the user
	 * @param asset - the asset being assigned
	 * @param roomId - the id of the room to assign to (0=Unassign, -1=Create new room, >0 Assign)
	 * @param location - the name of the location if creating a new location/room
	 * @param roomName - the name of the room if it is being created
	 * @param isSource - true indicates that the assignment will be assigned to the source otherwise at the target
	 */
	void assignAssetToRoom(Project project, AssetEntity asset, String roomId, String location, String roomName, boolean isSource) {

		String srcOrTarget = isSource ? 'Source' : 'Target'
		def roomProp = "room$srcOrTarget"
		Long id = NumberUtil.toLong(roomId)
		if (id == null) {
			log.warn "assignAssetToRoom() called with invalid room id ($roomId)"
			throw new InvalidRequestException("Room id was invalid")
		}

		// TODO : JPM 9/2014 : If moving to a different room then we need to disconnect cabling (enhancement)
		switch (id) {
			case -1:
				// Create a new room
				if (StringUtil.isBlank(location) && StringUtil.isBlank(roomName)) {
					throw new InvalidRequestException("Creating a $srcOrTarget room requires both the location and room name".toString())
				}

				def room = roomService.findOrCreateRoom(project, location, roomName, '', isSource)
				if (room) {
					asset[roomProp] = room
					if (asset.isaBlade()) {
						// Need to clear out the chassis assignment since it can't be assigned to chassis since it is a new room
						assignBladeToChassis(project, asset, '0', isSource)
					}
				} else {
					throw new RuntimeException("Unable to create new room $location/$roomName ($srcOrTarget)".toString())
				}
				break

			case 0:
				// Unassign the room
				asset[roomProp] = null
				break

			default:
				// Attempt to assign the Asset to the Room as long as it legitimate
				def room = Room.get(id)
				if (room) {
					if (room.project == project) {
						if ((isSource && room.source == 1) || (!isSource && room.source == 0)) {
							asset[roomProp] = room
						} else {
							throw new InvalidRequestException("Referenced room ($room) was not a $srcOrTarget room".toString())
						}
					} else {
						securityService.reportViolation("Attempted to access room ($id) not associated with project ($project.id)")
						throw new UnauthorizedException("Referenced room ($id) is missing")
					}
				} else {
					throw new InvalidRequestException("Referenced room ($id) was not found".toString())
				}
		}
	}

	/**
	 * Used to assign the asset to source and target rack with the params thatt were passed in from the form. If the rack is being
	 * created then it will use the Room associated to the asset in the source or target room appropriately.
	 * @param project - the current project of the user
	 * @param asset - the asset/device being assigned to a rack
	 * @param rackId - the id of the rack to assign to (0=Unassign, -1=Create new rack, >0 Assign)
	 * @param rackName - the name of the rack if it is being created
	 * @param isSource - true indicates that the assignment will be done at the source otherwise at the target
	 */
	void assignAssetToRack(Project project, AssetEntity asset, String rackId, String rackName, boolean isSource) {
		def srcOrTarget = isSource ? 'Source' : 'Target'
		def rackProp = "rack$srcOrTarget"

		log.debug "assignAssetToRack($project.id, $asset.id, $rackId, $rackName, $isSource)"

		// TODO : JPM 9/2014 : If moving to a different room then we need to disconnect cabling (enhancement)
		def assetType = asset.model?.assetType
		if (asset.isaBlade() || asset.isaVM()) {
			// If asset model is VM or BLADE we should remove rack assignments period
			asset[rackProp] = null
			with(asset){
				sourceBladePosition = null
				sourceRackPosition = null
				targetBladePosition = null
				targetRackPosition = null
			}
			return
		}

		Long id = NumberUtil.toLong(rackId)
		if (id == null) {
			log.warn "assignAssetToRack() called with invalid rack id ($rackId)"
			throw new InvalidRequestException("Rack id was invalid")
		}

		Room room = asset["room$srcOrTarget"]

		switch (id) {
			case -1:
				// Create a new rack
				rackName = rackName?.trim()
				def rack = rackService.findOrCreateRack(room, rackName)
				if (rack) {
					asset[rackProp] = rack
				} else {
					throw new RuntimeException("Unable to create new rack $rackName ($srcOrTarget)".toString())
				}
				break

			case 0:
				// Unassign the room
				asset[rackProp] = null
				break

			default:
				// Attempt to assign the Asset to the Room as long as it legitimate
				def rack = Rack.get(id)
				if (rack) {
					if (rack.project == project) {
						if ((isSource && rack.source == 1) || (!isSource && rack.source == 0)) {
							asset[rackProp] = rack
						} else {
							throw new InvalidRequestException("Referenced rack ($rack) was not a $srcOrTarget rack".toString())
						}
					} else {
						securityService.reportViolation("Attempted to access rack ($id) not associated with project ($project.id)")
						throw new UnauthorizedException("Referenced rack ($id) is missing")
					}
				} else {
					throw new InvalidRequestException("Referenced rack ($id) was not found".toString())
				}
		}
	}

	/**
	 * Used to set a blade to a chassis in the source or target locations
	 * @param project - the current project of the user
	 * @param blade - the blade being assigned
	 * @param chassisId - the id of the chassis to assign to
	 * @param isSource - true indicates that the assignment will be done at the source otherwise at the target
	 * @param bladePosition - the position to assign the blade to (optional)
	 * @return a string that represents any warnings
	 */
	@Transactional(noRollbackFor=[InvalidRequestException, EmptyResultException])
	String assignBladeToChassis(Project project, AssetEntity blade, String chassisId, boolean isSource, String bladePosition=null) {
		String warnings
		def srcOrTarget = isSource ? 'Source' : 'Target'
		def roomProp = "room$srcOrTarget"
		def chassisProp = isSource ? 'sourceChassis' : 'targetChassis'

		if (!blade.isaBlade()) {
			throw new InvalidRequestException("Attempted to assign a non-blade type asset to a chassis (type $blade.assetType)".toString())
		}

		log.debug "assignBladeToChassis($project.id, $blade.id, $chassisId, $isSource) - chassisProp=$chassisProp, bladePosition=$bladePosition"
		// TODO : JPM 9/2014 : If moving to a different room then we need to disconnect cabling (enhancement)

		// Clear out some non-blade type fields that sometimes get set
		if (isSource) {
			blade.rackSource = null
			blade.sourceRackPosition = null
		} else {
			blade.rackTarget = null
			blade.targetRackPosition = null
		}

		if (chassisId == '0') {
			blade[chassisProp] = null
		} else {
			// Look to see if we really have a chassis and assign it appropriately
			def chassis = AssetEntityHelper.getAssetById(project, AssetClass.DEVICE, chassisId)
			if (!chassis) {
				throw new EmptyResultException("Unable to find chassis")
			}
			String assetType = chassis.model?.assetType
			if (assetType != AssetType.BLADE_CHASSIS.toString()) {
				throw new InvalidRequestException("Attempted to assign a blade to a non-chassis type device (type $assetType)".toString())
			}

			if (blade[chassisProp] != chassis) {
				log.info "Assigned Blade $blade to chassis $chassis for project $project"
				// We're good so assign the blade to the chassis and the room that the chassis is in
				blade[chassisProp] = chassis
				blade[roomProp] = chassis[roomProp]
			} else {
				log.debug "assignBladeToChassis() no changes to the chassis assignment"
			}

			// Try to assign the blade position if supplied
			if (!StringUtil.isBlank(bladePosition)) {
				int bp = NumberUtil.toTinyInt(bladePosition, -1)
				if (bp && (bp > -1)) {
					if (bp > chassis.model?.bladeCount) {
						warnings = "position ($bp) exceeds chassis capacity ($chassis.model.bladeCount)"
					} else {
						if (isSource) {
							blade.sourceBladePosition = bp
						} else {
							blade.targetBladePosition = bp
						}
					}
				} else {
					warnings = "position ($bladePosition) specified is invalid"
				}
			}
		}
		return warnings
	}

	/**
	 * Updates an asset and its dependencies for all entity types.
	 * @param project : Instance of current project
	 * @param assetEntity : instance of entity including Server, Application, Database, Files
	 * @param params : params map received from client side
	 */
	def createOrUpdateAssetEntityAndDependencies(Project project, AssetEntity assetEntity, Map params) {
		String error
		String errObject = 'dependencies'
		try {
			if (!assetEntity.validate() || !assetEntity.save(flush:true)) {
				errObject = 'asset'
				throw new DomainUpdateException('Unable to update asset ' + GormUtil.errorsAsUL(assetEntity))
			}

			// Verifying assetEntity assigned to the project
			validateAssetsAssocToProject([assetEntity.id], project)

			//
			// Handle deleting first
			//

			// Collecting deleted deps ids and fetching there instances list
			List toDelDepIds = params.deletedDep ? params.deletedDep.split(",").collect { NumberUtil.toLong(it, 0L)} : []
			List toDelDepObjs = toDelDepIds ? AssetDependency.getAll(toDelDepIds).findAll() : []

			// Delete any dependencies that were listed in the params.deletedDep parameter
			if (toDelDepObjs) {
				// Gather all of the assets referenced in the dependendencies and make sure that they are associated to the project
				List allReferencedAssetIds = (toDelDepObjs*.assetId + toDelDepObjs*.dependentId).unique()
				validateAssetsAssocToProject(allReferencedAssetIds, project)

				// Delete the dependencies
				AssetDependency.executeUpdate('delete AssetDependency where id in (:ids)', [ids: toDelDepObjs.id])
			}

			// Add/Update Support dependencies
			addOrUpdateDependencies(project, 'support', assetEntity,  params)

			// Add/Update Dependent dependencies
			addOrUpdateDependencies(project, 'dependent', assetEntity,  params)

		} catch (DomainUpdateException | InvalidRequestException e) {
			error = e.message
		} catch (RuntimeException rte) {
			//rte.printStackTrace()
			log.error ExceptionUtil.stackTraceToString(rte, 60)
			error = 'An error occurred that prevented the update'
		}

		if (error) {
			assetEntity.discard()
			transactionStatus.setRollbackOnly()
			throw new DomainUpdateException(error)
		}
	}

	void createOrUpdateDependencies(Project project, AssetEntity assetEntity, Map params) {

		// Delete dependencies marked for deletion
		if (params.deletedDep) {
			AssetDependency.where {
				id in params.deletedDep
			}.deleteAll()
		}
	}



	/**
	 * Helper Method to de Delete a Dependency from an Asset Entity
	 * @param project
	 * @param assetEntity
	 * @param dependencyId
	 */
	void deleteAssetEntityDependencyOrException(Project project, AssetEntity assetEntity, Long dependencyId) {
		String error
		try {
			if (!assetEntity.validate() || !assetEntity.save(flush:true)) {
				throw new DomainUpdateException('Unable to update asset ' + GormUtil.errorsAsUL(assetEntity))
			}

			// Verifying assetEntity assigned to the project
			validateAssetsAssocToProject([assetEntity.id], project)

			// Collecting dependency id and fetching the instance
			AssetDependency toDelDepObj = dependencyId ? AssetDependency.get(dependencyId).find() : []

			// Delete dependency
			if (toDelDepObj) {
				// Gather the assets referenced by the dependendency and make sure is associated to the project
				validateAssetsAssocToProject([toDelDepObj.id], project)

				// Delete the dependencies
				AssetDependency.executeUpdate('delete AssetDependency where id = (:id)', [id: toDelDepObj.id])
			}

		} catch (DomainUpdateException | InvalidRequestException e) {
			error = e.message
		} catch (RuntimeException rte) {
			//rte.printStackTrace()
			log.error ExceptionUtil.stackTraceToString(rte, 60)
			error = 'An error occurred that prevented the update'
		}

		if (error) {
			assetEntity.discard()
			transactionStatus.setRollbackOnly()
			throw new DomainUpdateException(error)
		}
	}

	private void updateAssetDependencyOrException(Project project, AssetEntity assetEntity, Long dependencyId, Map params) {

		validateAssetsAssocToProject([assetEntity.id], project)

		List<String> propNames = ['dataFlowFreq', 'type', 'status', 'dataFlowDirection', 'comment', 'c1', 'c2', 'c3', 'c4']

		AssetDependency assetDependency = AssetDependency.get(dependencyId)
		if (!assetDependency) {
			throw new InvalidRequestException("Unable to find referenced dependency ($dependencyId)")
		}

		AssetEntity depAsset = AssetEntity.get(assetEntity.id)
		if (!depAsset) {
			throw new InvalidRequestException("Unable to find asset ($depAssetId)")
		}

		AssetDependency.withNewSession { hibernateSession ->
			AssetDependency dupAd = AssetDependency.findByAssetAndDependent(assetEntity, depAsset)
			if (dupAd && (dependencyId < 1 || (dependencyId > 0 && dependencyId != dupAd.id))) {
				throw new InvalidRequestException("Duplicate dependencies not allow for $depAsset.assetName")
			}
		}

		// Update the fields
		propNames.each { String paramName ->
			if (params.containsKey(paramName)) {
				assetDependency[paramName] = params[paramName]
			} else {
				log.warn "addOrUpdateDependencies() request was missing property $paramName, user=$securityService.currentUsername, asset=$asset"
			}
		}

		assetDependency.updatedBy = securityService.loadCurrentPerson()

		log.debug "updateAssetDependency() Attempting to UPDATE dependency ($assetDependency.id) $assetDependency.asset.id/$assetDependency.dependent.id : changed fields=$assetDependency.dirtyPropertyNames"
		if (!assetDependency.validate() || !assetDependency.save(force:true)) {
			throw new DomainUpdateException("Unable to save dependency for $assetDependency.asset / $assetDependency.dependent", assetDependency)
		}

	}

	/**
	 * A helper method for createOrUpdateAssetEntityAndDependencies which does the actual adds and
	 * updates of dependencies from the web request
	 * @param project - instance of the user's currently assigned project
	 * @param depType - what dependency type is being updated (options support|dependent)
	 * @param asset - the AssetEntity instance being referenced
	 * @param params - map of params received from browser
	 * @throws InvalidRequestException for various unexpected conditions
	 * @throws DomainUpdateException for errors in data validation
	 */
	private void addOrUpdateDependencies(Project project, String depType, AssetEntity asset, Map params) {

		def (existingDeps, newDeps) = parseParamsForDependencyAssetIds(depType, params)

		// Check that all of the referenced assets are associated with the project
		List allAssetIds = (existingDeps.values() + newDeps.values()).unique()
		validateAssetsAssocToProject(allAssetIds, project)

		List<String> propNames = ['dataFlowFreq', 'type', 'status', 'comment']

		// Closure used to perform the actual update that is called below in loops for existing and new dependencies
		def updateDependency = { depId, assetDependency, depAssetId ->
			boolean isNew = depId < 1

			AssetEntity depAsset = AssetEntity.get(depAssetId)
			if (!depAsset) {
				throw new InvalidRequestException("Unable to find $depType asset ($depAssetId)")
			}

			// Check to see if there is already a duplicate reference to the dependent
			// note that we are using withNewSession so that it doesn't cause the notorious 'Not Processed by Flush()' hibernate error
			// TODO : JPM 10/2014 : Change query to readOnly:true or load entire list into memory so we don't do individual queries
			AssetDependency.withNewSession { hibernateSession ->
				AssetDependency dupAd
				if (depType == 'support') {
					dupAd = AssetDependency.findByAssetAndDependent(depAsset, asset)
				} else {
					dupAd = AssetDependency.findByAssetAndDependent(asset, depAsset)
				}
				if (dupAd && (depId < 1 || (depId > 0 && depId != dupAd.id))) {
					throw new InvalidRequestException("Duplicate dependencies not allow for $depType $depAsset.assetName")
				}
			}

			// Update the fields
			propNames.each { String name ->
				String paramName = name + '_' + depType + '_' + depId
				if (params.containsKey(paramName)) {
					assetDependency[name] = params[paramName]
				} else {
					log.warn "addOrUpdateDependencies() request was missing property $paramName, user=$securityService.currentUsername, asset=$asset"
				}
			}

			assetDependency.updatedBy = securityService.loadCurrentPerson()
			if (isNew) {
				assetDependency.createdBy = assetDependency.updatedBy
			}

			if (depType == 'support') {
				assetDependency.asset = depAsset
				assetDependency.dependent = asset
			} else {
				assetDependency.asset = asset
				assetDependency.dependent = depAsset
			}

			// Deal with the move bundle assignment for the dependent asset
			String mbStrId = params['moveBundle_' + depType + '_' + depId]
			Long mbId = NumberUtil.toLong(mbStrId)
			if (mbId == null) {
				throw new DomainUpdateException("A move bundle must be specified for the $depType dependency for $depAsset")
			}
			if (mbId != depAsset.moveBundle.id) {
				assignAssetToBundle(project, depAsset, mbStrId)
			}

			log.debug "addOrUpdateDependencies() Attempting to ${isNew ? 'CREATE' : 'UPDATE'} dependency ($assetDependency.id) $assetDependency.asset.id/$assetDependency.dependent.id : changed fields=$assetDependency.dirtyPropertyNames"
			if (!assetDependency.validate() || !assetDependency.save(force:true)) {
				throw new DomainUpdateException("Unable to save $depType dependency for $assetDependency.asset / $assetDependency.dependent", assetDependency)
			}
		}

		// Update existing dependencies
		existingDeps.each { depId, depAssetId ->
			// Check to see if they are trying assign the asset to itself
			if (asset.id == depId) {
				throw new InvalidRequestException("Associating asset ($asset.name) to itself is not allowed")
			}

			AssetDependency ad = AssetDependency.get(depId)
			if (!ad) {
				throw new InvalidRequestException("Unable to find referenced dependency ($depId)")
			}

			updateDependency(depId, ad, depAssetId)
		}

		// Update new dependencies
		newDeps.each { depId, depAssetId ->
			// Check to see if they are trying assign the asset to itself
			if (asset.id == depId) {
				throw new InvalidRequestException("Associating asset ($asset.name) to itself is not allowed")
			}

			// Create a new dependency that will be saved
			updateDependency(depId, new AssetDependency(), depAssetId)
		}
	}

	/**
	 * Helper method used to parse the dependency id and reference asset from the paramaters of a request
	 *
	 * Get list of all the AssetDependency ids referenced by stripping off the suffix of the asset_$type_ID param. For each
	 * form table row there will be variable representing each property of the domain followed by the type (support|dependent)
	 * and the id of the domain. Values greater (>) than zero (0) reference existing records and <=0 are for new records.
	 * @param depType - what dependency type is being updated (options support|dependent)
	 * @param params - map of params received from browser
	 * @return Existing Dependency List and New Dependency List, each containing maps of the dependency id : asset reference id
	 */
	private List parseParamsForDependencyAssetIds(String depType, Map params) {
		// Get list of all the AssetDependency ids referenced by stripping off the suffix of the asset_$type_ID param. For each
		// form table row there will be variable representing each property of the domain followed by the type (support|dependent)
		// and the id of the domain. Values greater (>) than zero (0) reference existing records and <=0 are for new records.
		//
		// This first loop will find all the referenced dependencies and new dependencies constructing
		// a map of the id and the referenced asset and dependent asset.
		String regex = 'asset_' + depType + '_(.+)'
		Map existingDeps = [:]
		Map newDeps = [:]
		params.each { n,v ->
			n.find(regex, { match, id ->
				id = NumberUtil.toLong(id)
				if (id == null) {
					throw new InvalidRequestException("An invalid asset reference id was received ($n:$v)")
				}

				// Fetch the asset id
				Long assetId = NumberUtil.toLong(v)
				if (assetId == null || assetId < 1) {
					throw new InvalidRequestException("A referenced asset id was invalid ($id)")
				}

				if (id > 0) {
					existingDeps[id] = assetId
				} else {
					newDeps[id] = assetId
				}
			})
		}
		return [existingDeps, newDeps]
	}

	/**
	 * Verify that referenced asset id(s) are associated to the specified project. If any are not then an exception is thrown.
	 * @param assetIds - a list of asset ids to check
	 * @param project - instance of project
	 */
	private void validateAssetsAssocToProject(List assetIds, Project project) {
		Long invalidAssetId
		// Use withNewSession so that it doesn't cause the notorious 'Not Processed by Flush()' hibernate error
		AssetEntity.withNewSession {
			invalidAssetId = AssetEntity.getAll(assetIds).findAll().find { it.projectId != project.id }?.id
		}
		if (invalidAssetId) {
			securityService.reportViolation("In validateAssetsAssocToProject() an attempt to access asset $invalidAssetId not associated with project $project.id")
			throw new InvalidRequestException("Invalid asset id $invalidAssetId referenced for project $project.name")
		}
	}

	/**
	 * @param project
	 * @return list of entities
	 */
	def getSpecialExportData(Project project) {
		jdbcTemplate.queryForList("""
			(SELECT
				server.asset_entity_id AS server_id,
				app.asset_entity_id AS app_id,
				server.asset_name AS server_name,
				server.asset_type AS server_type,
				IFNULL(app.asset_name,'') AS app_name,
				IFNULL(sme,'') AS tru, IFNULL(sme2,'') AS tru2,
				IFNULL(mb.name,'') AS move_bundle,
				if (mb.name='mx','',IFNULL(date_format(mb.start_time,'%m/%d'),'')) AS move_date,
				adb.dependency_bundle AS group_id,
				IFNULL(server.plan_status,'') AS status,
				IFNULL(server.environment, '') AS environment,
				IFNULL(application.criticality, '') AS criticality
			FROM asset_entity server
			JOIN asset_dependency srvappdep ON server.asset_entity_id = srvappdep.dependent_id
			JOIN asset_entity app ON app.asset_entity_id = srvappdep.asset_id AND app.asset_type = 'Application'
			LEFT OUTER JOIN application ON application.app_id = app.asset_entity_id
			LEFT OUTER JOIN move_bundle mb ON mb.move_bundle_id=server.move_bundle_id
			LEFT OUTER JOIN asset_dependency_bundle adb ON adb.asset_id = server.asset_entity_id
			WHERE
				server.project_id=?
				AND server.asset_type IN ('Server','VM', 'Load Balancer','Network', 'Storage', 'Blade')
				ORDER BY app_name, server_name
			)
			UNION DISTINCT

			(SELECT
				dbsrv.asset_entity_id AS server_id,
				app.asset_entity_id AS app_id,
				dbsrv.asset_name AS server_name,
				dbsrv.asset_type server_type,
				IFNULL(app.asset_name,'') AS app_name,
				IFNULL(sme,'') AS tru, IFNULL(sme2,'') AS tru2,
				IFNULL(mb.name,'') AS move_bundle,
				if (mb.name='mx','',IFNULL(date_format(mb.start_time,'%m/%d'),'')) AS move_date,
				adb.dependency_bundle AS group_id,
				IFNULL(dbsrv.plan_status,'') AS status,
				IFNULL(app.environment, '') AS environment,
				IFNULL(applic.criticality, '') AS criticality
			FROM asset_entity app
			JOIN application applic ON applic.app_id=app.asset_entity_id
			JOIN asset_dependency appdbdep ON appdbdep.asset_id = app.asset_entity_id #AND appdbdep.type='DB'
			JOIN asset_entity db ON db.asset_entity_id = appdbdep.dependent_id AND db.asset_type = 'Database'
			JOIN asset_dependency dbsrvdep ON dbsrvdep.asset_id = db.asset_entity_id
			JOIN asset_entity dbsrv ON dbsrv.asset_entity_id = dbsrvdep.dependent_id AND dbsrv.asset_type IN ('Server','VM')
			LEFT OUTER JOIN move_bundle mb ON mb.move_bundle_id=dbsrv.move_bundle_id
			LEFT OUTER JOIN asset_dependency_bundle adb ON adb.asset_id = dbsrv.asset_entity_id
			WHERE
				app.project_id=?
				AND app.asset_type = 'Application'
			)
			UNION DISTINCT
			(SELECT
				clustersrv.asset_entity_id AS server_id,
				app.asset_entity_id AS app_id,
				clustersrv.asset_name AS server_name,
				clustersrv.asset_type server_type,
				IFNULL(app.asset_name,'') AS app_name,
				IFNULL(sme,'') AS tru, IFNULL(sme2,'') AS tru2,
				IFNULL(mb.name,'') AS move_bundle,
				if (mb.name='mx','',IFNULL(date_format(mb.start_time,'%m/%d'),'')) AS move_date,
				adb.dependency_bundle AS group_id,
				IFNULL(clustersrv.plan_status,'') AS status,
				IFNULL(app.environment, '') AS environment,
				IFNULL(applic.criticality, '') AS criticality
			 FROM asset_entity app
			JOIN application applic ON applic.app_id=app.asset_entity_id
			JOIN asset_dependency appdbdep ON appdbdep.asset_id = app.asset_entity_id # AND appdbdep.type='DB'
			JOIN asset_entity db ON db.asset_entity_id = appdbdep.dependent_id AND db.asset_type = 'Database'
			JOIN asset_dependency dbclusterdep ON dbclusterdep.asset_id = db.asset_entity_id
			JOIN asset_entity dbcluster ON dbcluster.asset_entity_id = dbclusterdep.dependent_id AND dbcluster.asset_type = 'Database'
			JOIN asset_dependency clustersrvdep ON clustersrvdep.asset_id = dbcluster.asset_entity_id
			JOIN asset_entity clustersrv ON clustersrv.asset_entity_id = clustersrvdep.dependent_id AND clustersrv.asset_type in ('Server','VM')
			LEFT OUTER JOIN move_bundle mb ON mb.move_bundle_id=clustersrv.move_bundle_id
			LEFT OUTER JOIN asset_dependency_bundle adb ON adb.asset_id = clustersrv.asset_entity_id
			WHERE
				app.project_id=?
				AND app.asset_type = 'Application')""", project.id, project.id, project.id)
	}

	/**
	 * Delete asset and associated records - use this method when we want to delete any asset
	 * @param asset - the asset object to be deleted
	 */
	void deleteAsset(AssetEntity asset) {
		deleteAssets([asset.id])
	}

	/**
	 * Used to delete a list of any of the asset class domain records. It will also delete or null the
	 * references in other domains appropriately.
	 * @param assetIds - a list of validated asset ids to be deleted
	 * @return the number of assets that were deleted
	 */
	int deleteAssets(List<Long> assetIds) {
		List<AssetEntity> assets = AssetEntity.where { id in assetIds}.list()

		ProjectAssetMap.where { asset in assets }.deleteAll()

		ProjectTeam.executeUpdate('UPDATE ProjectTeam SET latestAsset=null WHERE latestAsset.id in (:assets)', [assets: assetIds])

		// Delete asset comments
		AssetComment.where {
			assetEntity in assets
			commentType != AssetCommentType.TASK
		}.deleteAll()

		// Null out asset references in Tasks
		AssetComment.executeUpdate('''
			UPDATE AssetComment c SET c.assetEntity=null
			WHERE c.assetEntity.id IN (:assets)
			''',
			[assets: assetIds])

		// Delete cabling where asset is the From in the relationship
		AssetCableMap.where { assetFrom in assets }.deleteAll()

		// Null out cable references where the asset is the To in the relationship
		AssetCableMap.executeUpdate('''
			UPDATE AssetCableMap
			SET cableStatus=:status, assetTo=null, assetToPort=null
			WHERE assetTo.id in (:assets)''', [assets: assetIds] + [status: AssetCableStatus.UNKNOWN])

		AssetDependency.where {
			asset in assets || dependent in assets
		}.deleteAll()

		AssetDependencyBundle.where { asset in assets }.deleteAll()

		// Clear any possible Chassis references
		AssetEntity.executeUpdate('''
			UPDATE AssetEntity SET sourceChassis=NULL, sourceBladePosition=NULL
			WHERE sourceChassis.id in (:assets)
			''',
			[assets: assetIds])

		AssetEntity.executeUpdate('''
			UPDATE AssetEntity SET targetChassis=NULL, targetBladePosition=NULL
			WHERE targetChassis.id in (:assets)
			''',
			[assets: assetIds])

		// Delete a few Application related domains for those where the id matches an Application. This
		// shouldn't be an issue when deleting other asset classes because nothing will be found.
		ApplicationAssetMap.executeUpdate('''
			DELETE ApplicationAssetMap
			WHERE application.id in :assetIds OR asset.id in :assetIds''',
			[assetIds:assetIds])

		AppMoveEvent.executeUpdate('DELETE AppMoveEvent WHERE application.id in :assetIds',
			[assetIds:assetIds] )

		// Last but not least, delete the asset itself. Note that GORM/Hibernate is smart
		// enough to know when a subclass of AssetEntity is being references so deleting AssetEntity will
		// delete Application, Database or other domains that extend it. Pretty cool - huh!
		int count = AssetEntity.where { id in assetIds }.deleteAll()

		return count
	}

	/**
	 * Used to gather all of the assets for a project by asset class with option to return just individual classes
	 * @param project
	 * @param groups - an array of Asset Types to only return those types (optional)
	 * @return Map of the assets by type along with the AssetOptions dependencyType and dependencyStatus lists
	 */
	Map entityInfo(Project project, List groups=null) {
		def map = [servers: [], applications: [], dbs: [], files: [], networks: [], dependencyType: [], dependencyStatus: []]
		if (groups == null || groups.contains(AssetType.SERVER.toString())) {
			map.servers = AssetEntity.executeQuery('''
					SELECT id, assetName FROM AssetEntity
					WHERE assetClass=:ac AND assetType in (:types) AND project=:project
					ORDER BY assetName''',
					[ac: AssetClass.DEVICE, types: AssetType.allServerTypes, project: project])
		}

		if (groups == null || groups.contains(AssetType.APPLICATION.toString())) {
			map.applications = Application.executeQuery(
					'SELECT id, assetName FROM Application ' +
					'WHERE assetClass=:ac AND project=:project ORDER BY assetName',
					[ac: AssetClass.APPLICATION, project: project])
		}

		if (groups == null || groups.contains(AssetType.DATABASE.toString())) {
			map.dbs = Database.executeQuery(
					'SELECT id, assetName FROM Database ' +
					'WHERE assetClass=:ac AND project=:project ORDER BY assetName',
					[ac: AssetClass.DATABASE, project:project])
		}

		if (groups == null || groups.contains(AssetType.STORAGE.toString())) {
			map.files = Files.executeQuery(
					'SELECT id, assetName FROM Files ' +
					'WHERE assetClass=:ac AND project=:project ORDER BY assetName',
					[ac: AssetClass.STORAGE, project: project])
			map.files +=  AssetEntity.executeQuery('''
					SELECT id, assetName FROM AssetEntity
					WHERE assetClass=:ac AND project=:project AND COALESCE(assetType, '')='Storage'
					ORDER BY assetName''',
					[ac: AssetClass.DEVICE, project: project])
		}

		// NOTE - the networks is REALLY OTHER devices other than servers
		if (groups == null || groups.contains(AssetType.NETWORK.toString())) {
			map.networks = AssetEntity.executeQuery('''
					SELECT id, assetName FROM AssetEntity
					WHERE assetClass=:ac AND project=:project AND COALESCE(assetType,'') NOT IN (:types)
					ORDER BY assetName''',
					[ac: AssetClass.DEVICE, project: project, types: AssetType.nonOtherTypes])
		}

		if (groups == null) {
			map.dependencyType = getDependencyTypes()
			map.dependencyStatus = getDependencyStatuses()
		}

		return map
	}

	/**
	 * The types used to assign to AssetDependency.type
	 * @return the values
	 */
	List<String> getDependencyTypes() {
		return assetService.getDependencyTypeOptions()
	}

	/**
	 * The valid values that can be assigned to AssetDependency.status
	 * @return the values
	 */
	List<String> getDependencyStatuses() {
		return assetService.getDependencyStatusOptions()
	}

	/**
	 * Asset Environment options
	 * @return the values
	 */
	List<String> getAssetEnvironmentOptions() {
		return assetService.getAssetEnvironmentOptions()
	}

	/**
	 * Asset Status options
	 * @return the values
	 */
	List<String> getAssetPlanStatusOptions() {
		return assetService.getAssetPlanStatusOptions()
	}

	/**
	 * Asset Priority Options.
	 * @return the values
	 */
	List<String> getAssetPriorityOptions() {
		return assetService.getAssetPriorityOptions()
	}

	/**
	 * Get dependent assets of an asset
	 * @param AssetEntity - the asset that we're finding the dependent assets
	 * @return List of Assets
	 */
	List getDependentAssets(asset) {
		if (asset?.id) {
			AssetDependency.executeQuery('FROM AssetDependency WHERE asset=? ' +
			                             'ORDER BY dependent.assetType, dependent.assetName', [asset])
		}
	}

	/**
	 * Get supporting assets of an asset
	 * @param AssetEntity - the asset that we're finding the dependent assets
	 * @return List of Assets
	 */
	List<AssetDependency> getSupportingAssets(AssetEntity asset) {
		if (asset?.id) {
			AssetDependency.executeQuery('FROM AssetDependency WHERE dependent=? ' +
			                             'ORDER BY asset.assetType, asset.assetName', [asset])
		}
	}

	/**
	 * Return a list with the information for the dependents for this asset.
	 * @param assetEntity
	 * @param dependents : true -> dependents; false -> supporting
	 * @return
	 */
	List<Map> getDependentsOrSupporting(AssetEntity assetEntity, boolean dependents = true) {
		String targetField = dependents ? 'dependent' : 'asset'
		String sourceField = dependents? 'asset' : 'dependent'
		List dependentsInfo = AssetDependency.createCriteria().list {
			createAlias('asset', 'asset')
			createAlias('dependent', 'dependent')
			eq(sourceField, assetEntity)
			projections {
				property('id')
				property('comment')
				property('status')
				property('type')
				property('dataFlowDirection')
				property('dataFlowFreq')
				property("${targetField}")

				order("${targetField}.assetType")
				order("${targetField}.assetName")
			}
		}

		List<Map> results = []
		for (depInfo in dependentsInfo) {
			AssetEntity target = depInfo[6]
			results << [
				id: depInfo[0],
				comment: depInfo[1],
				status: depInfo[2],
				type: depInfo[3],
				dataFlowDirection: depInfo[4],
				dataFlowFreq: depInfo[5],
				asset: [
					id: target.id,
					name: target.assetName,
					assetType: AssetClass.getClassOptionForAsset(target),
					moveBundle: [
						id: target.moveBundle?.id,
						name: target.moveBundle?.name
					]
				]
			]
		}

		return results
	}


	/**
	 * Returns a list of MoveBundles for a project
	 * @param project - the Project object to look for
	 * @return list of MoveBundles
	 */
	List<Map> getMoveBundles(Project project) {
		List<String> properties = [
			'id', 'name', 'description', 'dateCreated', 'lastUpdated', 'moveBundleSteps', 'completionTime',
			'operationalOrder', 'operationalOrder', 'useForPlanning', 'workflowCode', 'project'
		]
		return GormUtil.listDomainForProperties(project, MoveBundle, properties, [['name']])
	}

	/**
	 * Returns a list of MoveEvents for a project
	 * @param project - the Project object to look for
	 * @return list of MoveEvents
	 */
	List getMoveEvents(Project project) {
		if (project) {
			MoveEvent.findAllByProject(project, [sort: 'name'])
		}
	}

	/**
	 * Retrieve a list of valid AssetTypes for devices
	 * @return List of AssetType strings
	 */
	List<String> getDeviceAssetTypeOptions() {
		Model.executeQuery('select distinct assetType from Model where assetType is not null order by assetType')
	}

	/**
	 * Used to get the user's preference for the asset list size/rows per page
	 * @return the number of rows to display per page
	 */
	String getAssetListSizePref() {
		// TODO - JPM 08/2014 - seems like we could convert the values to Integer (improvement)
		userPreferenceService.getPreference(PREF.ASSET_LIST_SIZE) ?: '25'
	}

	/**
	 * Used to retrieve the asset and model that will be used for the Device Create form
	 */
	@Transactional(readOnly = true)
	List getDeviceAndModelForCreate(Project project, Map params) {

		def (device, model) = getCommonDeviceModelForCreateEdit(project, null, params)

		// Attempt to set the default bundle for the device based on project and then on user's preferences
		def bundle = project.defaultBundle

		if (!bundle) {
			def bundleId = userPreferenceService.getPreference(PREF.MOVE_BUNDLE)
			if (bundleId) {
				bundle = MoveBundle.read(bundleId)
			}
		}

		if (bundle && bundle.project == project) {
			device.moveBundle = bundle
		}

		return [device, model]
	}

	/**
	 * Used to retrieve the asset and model that will be used for the Device Edit form
	 *
	 * TODO : JPM 9/2014 : these methods should be renamed from getDeviceModel to getDeviceAndModel to avoid confusion (improvement)
	 */
	@Transactional(readOnly = true)
	List getDeviceModelForEdit(Project project, deviceId, Map params) {
		println "*** in getDeviceModelForEdit()"
		def (device, model) = getCommonDeviceModelForCreateEdit(project, deviceId, params)
		if (device) {
			// TODO : JPM 9/2014 : refactor the quote strip into StringUtil.stripQuotes method or escape the name.
			// This is done to fix issue with putting device name into javascript links
			model.quotelessName = device.assetName?.replaceAll('\"', {''})
		}
		return [device, model]
	}

	/**
	 * Used to get the model properties that are common between the Create and Edit forms. It will also
	 * lookup the device if the id is presented. If the id is presented but is not found or references
	 * another project's asset the function will return [null, null]. If successful it will return the
	 * device and model map.
	 * For Create purpose, the Manufacturer and Model will default to the user's session variables
	 *
	 * @param project - the project that the user is assigned to
	 * @param deviceId - the id number of the device to lookup. Use null for Create action
	 * @param params - the http params
	 * @return A list containing [device object, model map]
	 */
	@Transactional(readOnly = true)
	private List getCommonDeviceModelForCreateEdit(Project project, deviceId, Map params) {
		boolean isNew = deviceId == null
		def (device, model) = getCommonDeviceAndModel(project, deviceId, params)

		// Check to see if someone is screwing around with the deviceId
		if (device == null && model == null) {
			log.error "**** getCommonDeviceModelForCreateEdit() didn't get the device"
			return [null, null]
		}

		if (isNew) {
			device = new AssetEntity(assetType: params.initialAssetType ?: '') // clear out the default
			device.project = project

			// Set any defined default values on the device for the custom fields
			assetService.setCustomDefaultValues(device)
		}

		// Stick questionmark on the end of the model name if it is unvalidated
		String modelName = device.model?.modelName ?: 'Undetermined'
		if (!device.model?.isValid()) {
			modelName += ' ?'
		}

		def assetType = device.assetType
		model.putAll(
			assetEntityInstance: device,
			assetType: assetType,
			manufacturer: device.manufacturer,
			priorityOption: getAssetPriorityOptions(),
			manufacturers: getManufacturers(assetType),
			models: getModelSortedByStatus(device.manufacturer),
			// TODO : JPM 9/2014 : Determine what nonNetworkTypes is used for in the view (clean up if unnecessary)
			modelName: modelName,
			nonNetworkTypes: AssetType.nonNetworkTypes,
			railTypeOption: DeviceUtils.getAssetRailTypeOptions(),
			// TODO : JPM 9/2014 : determine if the views use source/targetRacks - I believe these can be removed as they are replaced by source/targetRackSelect
			sourceRacks: [],
			targetRacks: [],
			sourceChassisSelect: [],
			targetChassisSelect: [],
			version: device.version)

		// List of the room and racks to be used in the SELECTs
		model.sourceRoomSelect = DeviceUtils.getRoomSelectOptions(project, true, true)
		model.targetRoomSelect = DeviceUtils.getRoomSelectOptions(project, false, true)
		model.sourceRackSelect = DeviceUtils.getRackSelectOptions(project, device?.roomSourceId, true)
		model.targetRackSelect = DeviceUtils.getRackSelectOptions(project, device?.roomTargetId, true)

		model.putAll(getDefaultModelForEdits('AssetEntity', project, device, params))

		// Set the Custom Fields
		model.customs = getCustomFieldsSettings(project, device.assetClass.name(), true)

		if (device) {
			// TODO : JPM 9/2014 : Need to make the value flip based on user pref to show name or tag (enhancement TM-3390)
			// Populate the listings of the Chassis SELECT name/values
			model.sourceChassisSelect = DeviceUtils.getChassisSelectOptions(project, device?.roomSourceId)
			model.targetChassisSelect = DeviceUtils.getChassisSelectOptions(project, device?.roomTargetId)
		}

		// This is used to track the current assetType in the crud form. If the asset is new, it will default to Server otherwise
		// it will use that of the model the asset is assigned to.
		model.currentAssetType = device.assetType

		model.rooms = getRooms(project)

		return [device, model]
	}

	/**
	 * Used to lookup the asset and return the common model properties for any of the CRUD pages
	 * @param project - the project the user is assigned to
	 * @param deviceId - the device id of the device to lookup
	 * @param params - the parameters passed from the browser
	 * @return (device,model) the device if found and the map of model properties
	 */
	@Transactional(readOnly = true)
	private List getCommonDeviceAndModel(Project project, deviceId, Map params) {
		AssetEntity device = AssetEntityHelper.getAssetById(project, AssetClass.DEVICE, deviceId)
		Map model = [:]
		if (device) {
			// Load up any default model properties
		}
		return [device, model]
	}

	/**
	 * Used to provide the default/common properties shared between all of the Asset Create/Edit views
	 * @param
	 * @return a Map that includes the list of common properties
	 */
	@Transactional(readOnly = true)
	Map getDefaultModelForEdits(String type, Project project, Object asset, Map params) {
		String domain = asset.assetClass.toString()
		Map standardFieldSpecs = customDomainService.standardFieldSpecsByField(project, domain)
		List customFields = getCustomFieldsSettings(project, domain, true)

		Map map = [
			assetId: 			asset.id,
			project: 			project,

			// Used for change detection/optimistic locking
			version: 			asset.version,

			// The name of the asset that is quote escaped to prevent lists from erroring with links
			// TODO - this function should be replace with a generic HtmlUtil method - this function is to single purposed...
			escapedName: 		getEscapedName(asset),

			// Various Select Option lists
			environmentOptions: getAssetEnvironmentOptions(),
			moveBundleList: 	assetService.getMoveBundleOptions(project),
			planStatusOptions: 	getAssetPlanStatusOptions(),
			priorityOption:		assetService.getAssetPriorityOptions(),
			// Required for Supports On and Depends On
			dependencyMap:		dependencyEditMap(asset.project, asset),
			dataFlowFreq:		AssetDependency.constraints.dataFlowFreq.inList,

			// The page to return to after submitting changes (2018-11 JPM - believe this to be legacy and not needed)
			redirectTo: params.redirectTo,

			// Field Specifications
			customs: customFields,
			standardFieldSpecs: standardFieldSpecs,
		]

		// Additional Select Option lists for Device type
		if (asset.assetClass == AssetClass.DEVICE) {
			map << DeviceUtils.deviceModelOptions(project, asset)
		}

		map
	}

	/**
	 * The default/common properties shared between all of the Asset Show views
	 */
	@Transactional(readOnly = true)
	Map getCommonModelForShows(String type, Project project, Map params, assetEntity = null) {

		// log.debug "### getCommonModelForShows() type=$type, project=$project.id, asset=${assetEntity? assetEntity.id : 'null'}"
		if (assetEntity == null) {
			assetEntity = AssetEntity.read(params.id)
		}

		def assetComment
		// TODO : JPM 7/2017 : getCommonModelForShows - determine what this AssetComment logic is doing as it looks obsolete
		if (AssetComment.executeQuery('select count(*) from AssetComment ' +
			'where assetEntity=? and commentType=? and dateResolved=?',
			[assetEntity, 'issue', null])[0])
		{
			assetComment = "issue"
		} else if (assetEntity && AssetComment.countByAssetEntity(assetEntity)) {
			assetComment = "comment"
		} else {
			assetComment = "blank"
		}

		List<AssetDependency> dependentAssets = assetEntity.requiredDependencies()
		List<AssetDependency> supportAssets = assetEntity.supportedDependencies()

		String userTzId = userPreferenceService.timeZone
		DateFormat formatter = TimeUtil.createFormatter(TimeUtil.FORMAT_DATE_TIME)

		def prefValue = userPreferenceService.getPreference(PREF.SHOW_ALL_ASSET_TASKS) ?: 'FALSE'
		def viewUnpublishedValue = userPreferenceService.getPreference(PREF.VIEW_UNPUBLISHED) ?: 'false'
		def depBundle = AssetDependencyBundle.findByAsset(assetEntity)?.dependencyBundle // AKA dependency group
		// Obtains the domain out of the asset type string
		String domain = AssetClass.getDomainForAssetType(type)
		Map standardFieldSpecs = customDomainService.standardFieldSpecsByField(project, domain)

		def customFields = getCustomFieldsSettings(project, assetEntity.assetClass.toString(), true)
		def assetCommentList = []

        if(securityService.hasPermission(Permission.TaskView)) {
            assetCommentList = taskService.findAllByAssetEntity(assetEntity)
        }

        if(securityService.hasPermission(Permission.CommentView)) {
            assetCommentList.addAll(commentService.findAllByAssetEntity(assetEntity))
        }

		[   assetId: assetEntity?.id,
		    assetComment: assetComment,
		    assetCommentList: assetCommentList,
		    dateFormat: userPreferenceService.getDateFormat(),
			dependencyBundleNumber: depBundle,
			dependentAssets: dependentAssets,
			errors: params.errors,
			escapedName: getEscapedName(assetEntity),
			prefValue: prefValue,
			project: project,
			client: project.client,
			redirectTo: params.redirectTo,
			supportAssets: supportAssets,
			viewUnpublishedValue: viewUnpublishedValue,
			hasPublishPermission: securityService.hasPermission(Permission.TaskPublish),
			customs: customFields,
			dateCreated: TimeUtil.formatDateTimeWithTZ(userTzId, assetEntity.dateCreated, formatter),
			lastUpdated: TimeUtil.formatDateTimeWithTZ(userTzId, assetEntity.lastUpdated, formatter),
			standardFieldSpecs: standardFieldSpecs
		]
	}

	/**
	 * The default/common properties shared between all of the Asset Create views
	 */
	@Transactional(readOnly = true)
	Map getCommonModelForCreate(String type, Project project, assetEntity) {
		def prefValue = userPreferenceService.getPreference(PREF.SHOW_ALL_ASSET_TASKS) ?: 'FALSE'
		def viewUnpublishedValue = userPreferenceService.getPreference(PREF.VIEW_UNPUBLISHED) ?: 'false'
		// Obtains the domain out of the asset type string
		String domain = AssetClass.getDomainForAssetType(type)
		Map standardFieldSpecs = customDomainService.standardFieldSpecsByField(project, domain)

		List customFields = getCustomFieldsSettings(project, assetEntity.assetClass.toString(), true)

		[
			prefValue: prefValue,
			project: project,
			client: project.client,
			viewUnpublishedValue: viewUnpublishedValue,
			hasPublishPermission: securityService.hasPermission(Permission.TaskPublish),
			customs: customFields,
			standardFieldSpecs: standardFieldSpecs
		]
	}

	/**
	 * Used to provide the default properties used for the Asset Dependency views
	 * @param listType - indicates the type of list [Application|AssetEntity|Files|Storage]
	 * @param moveEvent
	 * @param params - the params from the HTTP request
	 * @param filters - the map of the filter settings
	 * @return a Map that includes all of the common properties shared between all Asset List views
	 */
	@Transactional(readOnly = true)
	Map getDefaultModelForLists(AssetClass ac, String listType, Project project, Object fieldPrefs, Map params, Object filters) {

		Map model = [
			assetClassOptions: AssetClass.classOptions,
			assetDependency: new AssetDependency(),
			dependencyStatus: getDependencyStatuses(),
			dependencyType: getDependencyTypes(),
			event: params.moveEvent,
			fixedFilter: params.filter as Boolean,
			filter: params.filter,
            hasPerm: securityService.hasPermission(Permission.AssetEdit),
            canViewComments: securityService.hasPermission(Permission.CommentView),
            canViewTasks: securityService.hasPermission(Permission.TaskView),
            canCreateComments: securityService.hasPermission(Permission.CommentCreate),
            canCreateTasks: securityService.hasPermission(Permission.TaskCreate),
			justPlanning: userPreferenceService.getPreference(PREF.ASSET_JUST_PLANNING) ?: 'true',
			modelPref: null,        // Set below
			moveBundleId: params.moveBundleId,
			moveBundle: filters?.moveBundleFilter ?: '',
			moveBundleList: getMoveBundles(project),
			moveEvent: null,        // Set below
			planStatus: filters?.planStatusFilter ?:'',
			plannedStatus: params.plannedStatus,
			projectId: project.id,
			sizePref: getAssetListSizePref(),
			sortIndex: filters?.sortIndex,
			sortOrder: filters?.sortOrder,
			staffRoles: taskService.getRolesForStaff(),
			toValidate: params.toValidate,
			unassigned: params.unassigned,
			validation: params.validation]

		// Override the Use Just For Planning if a URL requests it (e.g. Planning Dashboard)
		/*
		// This was being added to correct the issue when coming from the Planning Dashboard but there are some ill-effects still
		if (params.justPlanning)
			model.justPlanning=true
		*/

		def moveEvent = null
		if (params.moveEvent && params.moveEvent.isNumber()) {
			moveEvent = MoveEvent.findByProjectAndId(project, params.moveEvent)
		}
		model.moveEvent = moveEvent

		// Set the list of viewable and selectable field specs
		model.fieldSpecs = getViewableFieldSpecs(project, ac)

		// Used to display column names in jqgrid dynamically
		def modelPref = [:]
		fieldPrefs.each { key, value ->
			modelPref[key] = getFieldLabel(model.fieldSpecs, value)
		}
		model.modelPref = modelPref

		return model
	}

	/**
	 * Get label for jqgrid
	 * @param fieldSpecs
	 * @param field
	 * @return
	 */
	private String getFieldLabel(List<Map<String, String>> fieldSpecs, String field) {
		Map<String, String> fieldSpec = fieldSpecs.find { it.attributeCode == field }
		if (fieldSpec) {
			return StringUtil.sanitizeJavaScript(fieldSpec.frontendLabel)
		} else {
			return StringUtil.capitalize(field)
		}
	}

	/**
	 * Get viewable and selectable field specs to construct list view column selector
	 * for AssetClass.* entities types
	 * @param assetClass
	 * @return
	 */
	private List<Map<String, String>> getViewableFieldSpecs(Project project, AssetClass assetClass) {
		Map fieldSpecs = customDomainService.allFieldSpecs(project, assetClass.toString())
		List<Map<String, String>> attributes = null

		// Pull out the field specs from the Map
		attributes = fieldSpecs?."${assetClass.toString().toUpperCase()}"?.fields

		if (attributes) {
			// filter viewable only fields and sort them by label
			attributes = attributes.findAll({ fieldSpec ->
				fieldSpec.show == 1 &&
				! COLUMN_PROPS_TO_EXCLUDE[assetClass].contains(fieldSpec.field)
			}).collect {
				fieldSpec -> [attributeCode: fieldSpec.field, frontendLabel: fieldSpec.label]
			}.sort {
				fieldSpecA, fieldSpecB -> fieldSpecA.frontendLabel <=> fieldSpecB.frontendLabel
			}
		} else {
			// If we didn't get fields defined there is a serious problem and we should just stop
            throw new ConfigurationException("No Field Specification found for project ${project.id} and asset class ${assetClassType}")
		}

		return attributes
	}

	/**
	 * This method returns the settings for the custom fields for the given
	 * asset type. Results are sorted by order and field.
	 *
	 * Implementation Details: CustomDomainService works with a "domain"
	 * instead of an "asset type". Also, it returns a Map of the form:
	 * [domain : list of settings]. This method is responsible for resolving
	 * the domain for a given asset type, extracting the list of settings
	 * from the map and sorting the results.
	 *
	 * The sorting criteria is: order, label.
	 *
	 * @param project - the project for which the asset field settings is needed
	 * @param assetClassName - the name of the asset class to get custom field settings for
	 * @param showOnly - a flag to request only those fields marked as shown
	 * @return list with the settings for the custom fields.
	 * @TODO Refactor getCustomFieldsSettings to new AssetService
	 */
	private List getCustomFieldsSettings(Project project, String assetClassName, boolean showOnly) {
		// This list will contain the settings correctly sorted
		List customs

		// Resolve the domain for the asset type
		String domain = assetClassName.toUpperCase()

		Map settingsMap = customDomainService.customFieldSpecs(project, assetClassName, showOnly)
		if (settingsMap && settingsMap[domain]) {
			// Strips the list of fields from the result map
			customs = settingsMap[domain].fields
			// Sorts the results based on order and field.
			customs = customs.sort { i,j ->
				i.order <=> j.order ?: i.label <=> j.label
			}
		} else {
			throw new ConfigurationException("Unable to load Custom Fields Settings for project ${project.id} and class ${assetClassName}")
		}

		return customs
	}

	/**
	 * Resolves the display string for the shutdownBy, startupBy, testingBy fields by either
	 * getting the name of the person or stripping the prefix for SME/AppOwner or Role
	 * @param byValue - application's shutdownBy, startupBy, or testingBy raw value
	 * @param stripPrefix - if true or not specified, the function will remove the # or @ character from the string
	 * @return value to display
	 */
	def resolveByName(byValue, stripPrefix = true) {
		def byObj = ''
		if (byValue) {
			if (byValue.isNumber()) {
				byObj = Person.read(Long.parseLong(byValue))
			} else {
				byObj = (stripPrefix && ['@','#'].contains(byValue[0])) ? byValue[1..-1] : byValue
			}
		}
		return byObj
	}

	def getAssetsByType(assetType) {
		def entities = []
		def types

		Project project = securityService.userCurrentProject

		if (assetType) {
			if (AssetType.allServerTypes.contains(assetType)) {
				types = AssetType.serverTypes
			} else if (AssetType.storageTypes.contains(assetType)) {
				types = AssetType.storageTypes
			} else if ([AssetType.APPLICATION.toString(), AssetType.DATABASE.toString()].contains(assetType)) {
				types = [assetType]
			}
		}

		// log.info "getAssetsByType() types=$types"

		if (types) {
			entities = AssetEntity.findAllByProjectAndAssetTypeInList(project, types, [sort:'assetName'])
		} else {
			log.warn "getAssetsByType() calledwith unhandled type '$assetType'"
		}

		/*
			if (assetType == 'Server' || assetType == 'Blade' || assetType == 'VM') {
				entities = AssetEntity.findAll('from AssetEntity where assetType in (:type) and project = :project order by assetName asc ',
					[type:["Server", "VM", "Blade"], project:project])
			} else if (assetType != 'Application' && assetType != 'Database' && assetType != 'Files') {
				entities = AssetEntity.findAll('from AssetEntity where assetType not in (:type) and project = :project order by assetName asc ',
					[type:["Server", "VM", "Blade", "Application", "Database", "Files"], project:project])
			} else {
				entities = AssetEntity.findAll('from AssetEntity where assetType = ? and project = ? order by assetName asc ',[assetType, project])
			}
		}
		*/
		return entities
	}

	/**
	 * Used to delete assets in bulk with a list of asset ids for a specific project
	 * @param project - the project that the assets should belong to
	 * @param domainToDelete - the name of the domain to be deleted
	 * @param assetIdList - list of ids for which assets are requested to deleted
	 * @return a count of the number of records that are deleted
	 */
	String deleteBulkAssets(Project project, String type, List<String> assetIdList) {

        // if no ids given don't process anything.
		if (assetIdList.isEmpty()) {
            return "0 $type records were deleted"
    }

		List<Long> assetIds = NumberUtil.toPositiveLongList(assetIdList)
		log.debug "deleteBulkAssets: $assetIds to be deleted"
		int count = assetIds.size()

		// Now make sure that the ids are associated to the project
		List<Long> validatedAssetIds =
			AssetEntity.where {
				project == project
				id in assetIds
			}
			.projections { property 'id' }
			.list()

		if (count != validatedAssetIds.size()) {
			List<Long> idsNotInProject = assetIds - validatedAssetIds
			log.warn "deleteBulkAssets called with asset ids not assigned to project ($assetIds)"
		}

		if (validatedAssetIds.size() > 0) {
       count = deleteAssets(validatedAssetIds)
    } else {
			count = 0
		}

		return "$count $type record${count==1? ' was' : 's were'} deleted"
	}

	/**
	 * Update assets bundle in dependencies
	 * @param project - the project that the user presently assigned to
	 * @param userLogin - the user making the change request
	 * @param entity - the AssetEntity (or other asset classes) to be reassigned
	 * @param moveBundleId - the id of the bundle to assign to
	 * @return Null if successful otherwise the error that occurred
	 */
	// replaced with assignAssetToBundle
	/*
	private String updateBundle(Project project, UserLogin userLogin, AssetEntity entity, moveBundleId) {
		String error
		Long id = NumberUtil.toLong(moveBundleId)
		if (!id) {
			error = "Invalid move bundle id was received"
		} else {
			MoveBundle mb = MoveBundle.get(id)
			if (!mb) {
				error = "Specified move bundle ($id) was not found"
			} else {
				if (mb.project.id != project.id) {
					securityService.reportViolation("Attempted to assign asset ($entity.id) to bundle ($id) not associated with project ($project.id)")
					error = "Specified move bundle id ($id) was not found"
				} else {
					entity.moveBundle = mb
					if (!entity.validate() || !entity.save(flush:true)) {
						error = GormUtil.allErrorsString(entity)
					}
				}
			}
		}
		return error
	}
	*/

	/*
	 * Update assets cabling data for selected list of assets
	 * @param list of assets to be updated
	 */
	def updateCablingOfAssets(modelAssetsList) {
		modelAssetsList.each { assetEntity ->
			AssetCableMap.executeUpdate("Update AssetCableMap set cableStatus=?, assetTo=null, assetToPort=null where assetTo=?",[AssetCableStatus.UNKNOWN, assetEntity])

			AssetCableMap.executeUpdate("Delete AssetCableMap where assetFrom=?",[assetEntity])

			assetEntityAttributeLoaderService.createModelConnectors(assetEntity)
		}
		/*existingAssetsList.each { assetEntity ->
			AssetCableMap.executeUpdate("""Update AssetCableMap set toAssetRack='$assetEntity.targetRack',
					toAssetUposition=$assetEntity.targetRackPosition where assetTo = ? """,[assetEntity])
		}*/
	}

	/**
	 * Get the customised query based on the application preference
	 * @param appPref(List of key value column preferences)
	 * @return query,joinQuery
	 */
	def getAppCustomQuery(appPref) {
		StringBuilder query = new StringBuilder('')
		StringBuilder joinQuery = new StringBuilder('')

		if(appPref){
			for (String value in appPref.values()) {
				/*
				 TODO: the logic for sme, sme2 and 'by' fields needs to be changed.
				 We'll probably address that with TM-5931. In the meantime I'm using
				 CONCAT_WS for sme and sme2, to join first, middle and last name
				 with a separator.
				*/
				switch (value) {
					case 'sme':
						query.append("CONCAT_WS(' ', NULLIF(p.first_name, ''), NULLIF(p.middle_name, ''), NULLIF(p.last_name, '')) AS sme,")
						joinQuery.append("\n LEFT OUTER JOIN person p ON p.person_id=a.sme_id \n")
						break
					case 'sme2':
						query.append("CONCAT_WS(' ', NULLIF(p1.first_name, ''), NULLIF(p1.middle_name, ''), NULLIF(p1.last_name, '')) AS sme2,")
						joinQuery.append("\n LEFT OUTER JOIN person p1 ON p1.person_id=a.sme2_id \n")
						break
					case 'modifiedBy':
						query.append("CONCAT(CONCAT(p2.first_name, ' '), IFNULL(p2.last_name,'')) AS modifiedBy,")
						joinQuery.append("\n LEFT OUTER JOIN person p2 ON p2.person_id=ae.modified_by \n")
						break
					case 'lastUpdated':
						query.append("ee.last_updated AS $value,")
						joinQuery.append("\n LEFT OUTER JOIN eav_entity ee ON ee.entity_id=ae.asset_entity_id \n")
						break
					case 'event':
						query.append("me.move_event_id AS event,")
						joinQuery.append("\n LEFT OUTER JOIN move_event me ON me.move_event_id=mb.move_event_id \n")
						break
					case 'appOwner':
						query.append("CONCAT_WS(' ', NULLIF(p3.first_name, ''), NULLIF(p3.middle_name, ''), NULLIF(p3.last_name, '')) AS appOwner,")
						joinQuery.append("\n LEFT OUTER JOIN person p3 ON p3.person_id= ae.app_owner_id \n")
						break
					case ~/appVersion|appVendor|appTech|appAccess|appSource|license|businessUnit|appFunction|criticality|userCount|userLocations|useFrequency|drRpoDesc|drRtoDesc|shutdownFixed|moveDowntimeTolerance|testProc|startupProc|url|shutdownDuration|startupFixed|startupDuration|testingFixed|testingDuration/:
						query.append("a.${WebUtil.splitCamelCase(value)} AS $value,")
						break
					case ~/custom\d{1,3}/:
						query.append("ae.$value AS $value,")
						break
					case ~/validation|latency|planStatus|moveBundle/:
						// Handled by the calling routine
						break
					case 'tagAssets':
						query.append("""
							CONCAT(
			                    '[',
			                    if(
			                        ta.tag_asset_id,
			                        group_concat(
			                            json_object('id', ta.tag_asset_id, 'tagId', t.tag_id, 'name', t.name, 'description', t.description, 'color', t.color)
			                        ),
			                        ''
			                    ),
			                    ']'
			                ) as tagAssets, """)
						joinQuery.append("""
								LEFT OUTER JOIN tag_asset ta on ae.asset_entity_id = ta.asset_id
								LEFT OUTER JOIN tag t on t.tag_id = ta.tag_id
							""")
						break
					case ~/shutdownBy|startupBy|testingBy/:
						Map<String,String> byPrefixes = [shutdownBy: "sdb", startupBy: "sub", testingBy: "teb"]
						String byProperty = WebUtil.splitCamelCase(value)
						String prefix = byPrefixes[value]
						query.append("(IF(${byProperty} REGEXP '^[0-9]{1,}\$', CONCAT_WS(' ',  NULLIF(${prefix}.first_name, ''), " +
								"NULLIF(${prefix}.middle_name, ''), NULLIF(${prefix}.last_name, '')), a.${byProperty})) as ${value},")
	      				joinQuery.append("\n LEFT OUTER JOIN person ${prefix} ON ${prefix}.person_id=a.${byProperty} \n")

						break
					default:
						query.append("ae.${WebUtil.splitCamelCase(value)} AS $value,")
				}
			}
		}

		return [query: query.toString(), joinQuery: joinQuery.toString()]
	}

	/**
	 * Returns the default optional/customizable columns
	 * @param prefName - the preference name for the various asset lists
	 * @return appPref
	 */
	// TODO : JPM 9/2014 : Rename getExistingPref method to getColumnPreferences
	Map getExistingPref(com.tdsops.tm.enums.domain.UserPreferenceEnum prefName) {
		def colPref
		def existingPref = userPreferenceService.getPreference(prefName)

		if (existingPref) {
			// TODO  JPM 9/2014 : I'm assuming that the JSON.parse function could throw an error if the json is corrupt so there should be a try/catch
			colPref = JSON.parse(existingPref)
		}

		if (!colPref) {
			switch (prefName) {
				case PREF.App_Columns:
					colPref = ['1': 'sme', '2': 'environment', '3': 'validation', '4': 'planStatus', '5': 'moveBundle']
					break
				case PREF.Asset_Columns:
					colPref = ['1': 'rackSource','2': 'environment','3': 'assetTag','4': 'serialNumber','5': 'validation']
					break
				case PREF.Physical_Columns:
					colPref = ['1': 'sourceRack','2': 'environment','3': 'assetTag','4': 'serialNumber','5': 'validation']
					break
				case PREF.Database_Columns:
					colPref = ['1': 'dbFormat','2': 'size','3': 'validation','4': 'planStatus','5': 'moveBundle']
					break
				case PREF.Storage_Columns:
					colPref = ['1': 'fileFormat','2': 'size','3': 'validation','4': 'planStatus','5': 'moveBundle']
					break
				case PREF.Task_Columns:
					colPref = ['1': 'assetName','2': 'assetType','3': 'assignedTo','4': 'role', '5': 'category']
					break
				case PREF.Model_Columns:
					colPref = ['1': 'description','2': 'assetType','3': 'powerUse','4': 'modelConnectors']
					break
				case PREF.Dep_Columns:
					colPref = ['1': 'frequency','2': 'comment']
					break
			}
		}

		return colPref
	}

	/**
	 * Save cabling data to database while importing.
	 */
	def saveImportCables(cablingSheet) {
		def warnMsg = []
		def cablingSkipped = 0
		def cablingUpdated = 0
		Project project = securityService.userCurrentProject
		for (int r = 2; r < cablingSheet.getLastRowNum(); r++) {
			int cols = 0
			def isNew = false
			def cableType=WorkbookUtil.getStringCellValue(cablingSheet, cols, r).replace("'","\\'")
			def fromAsset = AssetEntity.get(NumberUtils.toDouble(WorkbookUtil.getStringCellValue(cablingSheet, ++cols, r).replace("'","\\'"), 0).round())
			def fromAssetName=WorkbookUtil.getStringCellValue(cablingSheet, ++cols, r).replace("'","\\'")
			def fromConnectorLabel =WorkbookUtil.getStringCellValue(cablingSheet, ++cols, r).replace("'","\\'")
			//if assetId is not there then get asset from assetname and fromConnector
			if (!fromAsset && fromAssetName) {
				fromAsset = AssetEntity.findByAssetNameAndProject(fromAssetName, project)?.find {
					it.model.modelConnectors?.label.contains(fromConnectorLabel)
				}
			}
			def toAsset = AssetEntity.get(NumberUtils.toDouble(WorkbookUtil.getStringCellValue(cablingSheet, ++cols, r).replace("'","\\'"), 0).round())
			def toAssetName = WorkbookUtil.getStringCellValue(cablingSheet, ++cols, r).replace("'","\\'")
			def toConnectorLabel = WorkbookUtil.getStringCellValue(cablingSheet, ++cols, r).replace("'","\\'")
			String toConnectorTemp = cableType == 'Power' ? fromConnectorLabel : toConnectorLabel
			//if toAssetId is not there then get asset from assetname and toConnectorLabel
			if (!toAsset && toAssetName) {
				if (cableType != 'Power') {
					toAsset = AssetEntity.findByAssetNameAndProject(toAssetName, project)?.find {
						it.model.modelConnectors?.label.contains(toConnectorLabel)
					}
				} else {
					toAsset = fromAsset
				}
			}
			String cableComment = WorkbookUtil.getStringCellValue(cablingSheet, ++cols, r).replace("'","\\'")
			String cableColor = WorkbookUtil.getStringCellValue(cablingSheet, ++cols, r).replace("'","\\'")
			String room = WorkbookUtil.getStringCellValue(cablingSheet, ++cols, r).replace("'","\\'")
			String cableStatus = WorkbookUtil.getStringCellValue(cablingSheet, ++cols, r).replace("'","\\'")
			String roomType = WorkbookUtil.getStringCellValue(cablingSheet, ++cols, r).replace("'","\\'")
			if (fromAsset) {
				String fromAssetConnectorsLabels = fromAsset.model?.modelConnectors?.label
				if (fromAssetConnectorsLabels?.contains(fromConnectorLabel)) {
					def fromConnector = fromAsset.model?.modelConnectors.find { it.label == fromConnectorLabel }
					def assetCable = AssetCableMap.findByAssetFromAndAssetFromPort(fromAsset,fromConnector)
					if (!assetCable) {
						log.info "Cable not found for $fromAsset and $fromConnector"
						warnMsg << "row (${r + 1}) with connector $fromConnectorLabel and Asset Name $fromAssetName don't have a cable"
						cablingSkipped++
						continue
					}

					if (toAsset) {
						def toAssetconnectorLabels = toAsset.model?.modelConnectors?.label
						if (toAssetconnectorLabels.contains(toConnectorTemp)) {
							if (cableType == 'Power') {
								assetCable.toPower = toConnectorLabel
							} else {
								def toConnector = toAsset.model?.modelConnectors.find { it.label == toConnectorTemp }
								def previousCable = AssetCableMap.findByAssetToAndAssetToPort(toAsset,toConnector)
								if (previousCable !=assetCable) {
									// Release the connection from other port to connect with FromPorts
									AssetCableMap.executeUpdate("""Update AssetCableMap set assetTo=null,assetToPort=null, cableColor=null
										where assetTo = ? and assetToPort = ? """,[toAsset, toConnector])
								}
								assetCable.assetToPort = toConnector
							}
						}
						assetCable.assetTo = toAsset
					} else {
						assetCable.assetTo = null
						assetCable.assetToPort = null
						assetCable.toPower = ''
					}

					if (AssetCableMap.constraints.cableColor.inList.contains(cableColor)) {
						assetCable.cableColor = cableColor
					}

					assetCable.cableComment = cableComment
					assetCable.cableStatus = cableStatus

					if (AssetCableMap.constraints.assetLoc.inList.contains(roomType))
						assetCable.assetLoc= roomType

					if (assetCable.dirtyPropertyNames.size()) {
						assetCable.save(flush: true)
						cablingUpdated++
					}
				} else {
					warnMsg << "row (${r + 1}) with connector $fromConnectorLabel and Asset Name $fromAssetName does not exist & skipped"
					cablingSkipped++
				}
			} else {
				warnMsg << "row (${r + 1}) with connector $fromConnectorLabel and Asset Name $fromAssetName does not exist & skipped"
				cablingSkipped++
			}
		}

		[warnMsg: warnMsg, cablingSkipped: cablingSkipped, cablingUpdated: cablingUpdated]
	}

	/**
	 * Used to create or fetch target asset cables.
	 */
	def createOrFetchTargetAssetCables(AssetEntity assetEntity) {
		def cableExist = AssetCableMap.findAllByAssetFromAndAssetLoc(assetEntity, 'T')
		if (!cableExist) {
			AssetCableMap.findAllByAssetFromAndAssetLoc(assetEntity, 'S').each {
				cableExist << save(new AssetCableMap(cable: it.cable, cableStatus: AssetCableStatus.EMPTY,
						assetFrom: it.assetFrom, assetFromPort: it.assetFromPort, assetLoc: 'T'))
			}
		}
		return cableExist
	}

	/**
	 * This is used to escape quotes in a string to be used in Javascript
	 * TODO : JPM 9/2014 : getEscapeName should be refactored into a reusable function in String or HtmlUtil as it should not be SOOOOO tied to an asset
	 */
	def getEscapedName(assetEntity, ignoreSingleQuotes = false) {
		def name = ''
		if (assetEntity.assetName) {
			name = SEU.escapeHtml(SEU.escapeJavaScript(assetEntity.assetName))
		}
		return name
	}

	List<Manufacturer> getManufacturers(assetType) {
		return Model.findAll("From Model where assetType = ? group by manufacturer order by manufacturer.name",[assetType])?.manufacturer
	}

	/**
	 * Sort model by status full, valid and new to display at Asset CRUD
	 * @param manufacturerInstance : instance of Manufacturer for which model list is requested
	 * @return : model list
	 */
	Map getModelSortedByStatus(Manufacturer mfr) {
		def models = [:]
		if (mfr) {
			def mfrList = Model.findAllByManufacturer(mfr, [sort: 'modelName', order: 'asc'])
			def modelListFull = mfrList.findAll { it.modelStatus == 'full' }
			def modelListNew = mfrList.findAll { !['full', 'valid'].contains(it.modelStatus) }
			models.Validated = mfrList.findAll { it.modelStatus == 'valid' }
			models.Unvalidated = modelListFull + modelListNew
		}
		else {
			models.Validated = []
			models.Unvalidated = []
		}

		return models
	}

	def getRooms(Project project) {
		Room.executeQuery('FROM Room WHERE project=? order by location, roomName', [project])
	}

	/**
	 * Retrieves the values of DEVICE properties based on the attribute name
	 * @param asset - the asset to retrieve the property from
	 * @param attribute - the attribute name
	 * @return The string value of the object property value
	 */
	Object getAssetAttributeValue(Object asset, String attribute) {
		def value
		def fieldMap = [Location:'location', Room:'roomName', Rack:'tag']
		switch (attribute) {
			case ~/XXX(source|target)(Location|Room|Rack)/:
				def disp = Matcher.lastMatcher[0][1]	// Disposition
				def prop = Matcher.lastMatcher[0][2]

				def assetProperty = (prop == 'Rack' ? 'rack' : 'room') + disp.capitalize()
				def refProperty = fieldMap[prop]
				log.debug "getAssetAttributeValue() assetProperty=$assetProperty,  refProperty=$refProperty, disp=$disp, prop=$prop"
				if (!assetProperty || !refProperty) {
					throw new RuntimeException("getAssetAttributeValue() unable to map '$attribute'")
				}

				value = asset[assetProperty][refProperty]?.toString()
				break
			default:
				value = String.valueOf(asset[attribute])
				log.debug "getAssetAttributeValue() attribute $attribute, value $value"
		}
		return StringUtil.defaultIfEmpty(value, '')
	}

	/**
	 * A local function used to add a cell to a row in the supplied sheet
	 * TODO : JPM 8/2015 : The WorkbookUtil methods have column, row order but everybody else in the world are row, column...
	 */
	private void addCell(sheet, rowIdx, columnIdx, value, type=null) {
		WorkbookUtil.addCell(sheet, columnIdx, rowIdx, value, type)
	}

	/**
	 * Updates progress for the give key only when is around 5%
	 * @param key to update
	 * @param progressCount progress count
	 * @param progressTotal total number of items
	 * @param description description used on update
	 * @param updateOnPercent used to determine the step size for updating the progress bar.
	 **/
	private void updateProgress(key, progressCount, progressTotal, description, updateOnPercent = 0.05) {
		int stepSize = Math.round(progressTotal * updateOnPercent)
		if ((progressTotal > 0) && (stepSize > 0) && ((progressCount % stepSize) == 0)) {
			progressService.update(key, ((int)((progressCount / progressTotal) * 100)), description)
		}
	}

	/* -------------------------------------------------------
	 * To check the sheet headers
	 * @param attributeList, SheetColumnNames
	 * @author Mallikarjun
	 * @return bollenValue
	 *------------------------------------------------------- */
	private boolean checkHeader(def list, def serverSheetColumnNames, missingHeader = "") {
		int size = list.size()
		for (int coll = 0; coll < size; coll++) {
			if (serverSheetColumnNames.containsKey(list[coll]) || list[coll] == "DepGroup") {
				//Nonthing to perform.
			} else {
				missingHeader = missingHeader + ", " + list[coll]
			}
		}

		missingHeader == ""
	}

	/**
	 * Update sheet's column header with custom labels
	 * @param sheet : sheet's instance
	 * @param entityDTAMap : dataTransferEntityMap for entity type
	 * @param sheetColumnNames : column Names
	 * @param project : project instance
	 * @return
	 */
	// TODO : JPM 9/2014 : The updateColumnHeaders probable won't work beyond 24 custom columns how this is written - should use regex test instead
	// customLabels is defined as a static at the top
	def updateColumnHeaders(sheet, entityDTAMap, sheetColumnNames, project) {
		for (int head =0; head <= sheetColumnNames.size(); head++) {
			def cellData = sheet.getRow(0).getCell(head)?.getStringCellValue()
			def attributeMap = entityDTAMap.find{it.columnName ==  cellData }?.eavAttribute
			if (attributeMap?.attributeCode && customLabels.contains(cellData)) {
				def columnLabel = project[attributeMap?.attributeCode] ? project[attributeMap?.attributeCode] : cellData
				addCell(sheet, 0, head, columnLabel)
			}
		}
		return sheet
	}

	/**
	 * Used by the AssetEntity List to populate the initial List view
	 */
	Map getDeviceModelForList(Project project, HttpSession session, Map params, String tzId) {
		def filters = session.AE?.JQ_FILTERS
		session.AE?.JQ_FILTERS = []

		def prefType = PREF.Asset_Columns
		def fieldPrefs = getExistingPref(prefType)

		// The hack for the dot notation
		fieldPrefs.each {k,v -> fieldPrefs[k] = v }

		Map model = getDefaultModelForLists(AssetClass.DEVICE, 'AssetEntity', project, fieldPrefs, params, filters)

		// Check the getDefaultModelForLists before adding to this list. This should ONLY be AssetEntity specific properties
		model.assetName = filters?.assetNameFilter ?:''
		model.assetPref = fieldPrefs
		model.assetTag = filters?.assetTagFilter ?:''
		model.assetType = filters?.assetTypeFilter ?:''
		model.model = filters?.modelFilter ?:''
		model.manufacturer = filters?.manufacturer ?:''
		model.prefType = prefType
		model.serialNumber = filters?.serialNumberFilter ?:''
		model.sourceLocationName = filters?.locationSourceFilter ?:''
		model.sourceRackName = filters?.rackSourceFilter ?:''
		model.targetLocationName = filters?.locationTargetFilter ?:''
		model.targetRackName = filters?.rackTargetFilter ?:''
		// Used for filter toValidate from the Planning Dashboard - want a better parameter (JPM 9/2014)
		model.type = params.type

		model.title = (TITLE_BY_FILTER.containsKey(params.filter) ? TITLE_BY_FILTER[params.filter] : TITLE_BY_FILTER['all']) + ' List'

		return model
	}


	long countServers(Project project = null){
		if(project == null){
			project = securityService.userCurrentProject
		}

		if(!project) return 0L

		def sql = """
			SELECT
				count(distinct ae.asset_entity_id)
			FROM asset_entity ae
			LEFT OUTER JOIN move_bundle mb ON mb.move_bundle_id=ae.move_bundle_id
			LEFT OUTER JOIN move_event me ON me.move_event_id=mb.move_event_id
			LEFT OUTER JOIN model m ON m.model_id=ae.model_id
			LEFT OUTER JOIN asset_comment at ON at.asset_entity_id=ae.asset_entity_id AND at.comment_type = 'issue'
			LEFT OUTER JOIN asset_comment ac ON ac.asset_entity_id=ae.asset_entity_id AND ac.comment_type = 'comment'
			LEFT OUTER JOIN rack AS srcRack ON srcRack.rack_id=ae.rack_source_id
			LEFT OUTER JOIN room AS srcRoom ON srcRoom.room_id=ae.room_source_id
			LEFT OUTER JOIN manufacturer manu ON manu.manufacturer_id=m.manufacturer_id
			WHERE ae.project_id = :pid
			AND ae.asset_class = 'DEVICE'
			AND mb.use_for_planning=true
			AND ae.asset_class='DEVICE'
			AND ae.asset_type IN ('Server','Appliance','Blade','VM','Virtual')
		"""
		/* id = 2445 */
		namedParameterJdbcTemplate.queryForObject(sql, [pid: project.id], Long.class)
	}
	/**
	 * Used to retrieve the data used by the AssetEntity List
	 */
	Map getDeviceDataForList(Project project, HttpSession session, Map params) {
		def filterParams = [
				assetName: params.assetName,
				assetType: params.assetType,
				depConflicts: params.depConflicts,
				depNumber: params.depNumber,
				depToResolve: params.depToResolve,
				event: params.event,
				model: params.model,
				manufacturer: params.manufacturer,
				moveBundle: params.moveBundle,
				planStatus: params.planStatus,
				sourceLocationName: params.sourceLocationName,
				sourceRackName: params.sourceRackName,
		]
		def validSords = ['asc', 'desc']
		String sortOrder = validSords.indexOf(params.sord) != -1 ? params.sord : 'asc'
		int maxRows = params.rows.toInteger()
		int currentPage = params.page.toInteger() ?: 1
		int rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows

		def moveBundleList

		// Get the list of fields for the domain
		Map fieldNameMap = customDomainService.fieldNamesAsMap(project, AssetClass.DEVICE.toString(), true)

		def prefType = PREF.Asset_Columns
		def assetPref= getExistingPref(prefType)

		List assetPrefColumns = assetPref*.value
		for (String fieldName in assetPrefColumns) {
			if (fieldNameMap.containsKey(fieldName)) {
				filterParams[fieldName] = params[fieldName]
			}
		}

		// Lookup the field name reference for the sort
		def sortIndex = (params.sidx in filterParams.keySet() ? params.sidx : 'assetName')

		// This is used by the JQ-Grid some how
		session.AE = [:]

		userPreferenceService.setPreference(PREF.ASSET_LIST_SIZE, maxRows)

		if (params.event && params.event.isNumber()) {
			def moveEvent = MoveEvent.read(params.event)
			moveBundleList = moveEvent?.moveBundles?.findAll {it.useForPlanning == true}
		} else {
			moveBundleList = MoveBundle.findAllByProjectAndUseForPlanning(project,true)
		}

		def assetType = params.filter ? ApplicationConstants.assetFilters[ params.filter ] : []

		def bundleList = params.moveBundle ? MoveBundle.findAllByNameIlikeAndProject("%$params.moveBundle%", project) : []

		StringBuilder altColumns = new StringBuilder()
		StringBuilder joinQuery = new StringBuilder()

		// Until sourceRack is optional on the list we have to do this one
		altColumns.append("\n, srcRack.tag AS rackSource, srcRoom.location AS locationSource ")
		joinQuery.append("\nLEFT OUTER JOIN rack AS srcRack ON srcRack.rack_id=ae.rack_source_id ")
		joinQuery.append("\nLEFT OUTER JOIN room AS srcRoom ON srcRoom.room_id=ae.room_source_id ")

		// join the manufacturer name from the model if it exists, otherwise from the asset's manufacturer property
		altColumns.append(", IFNULL(manu.name, manu2.name) AS manufacturer")
		joinQuery.append("\nLEFT OUTER JOIN manufacturer manu ON manu.manufacturer_id=m.manufacturer_id ")
		joinQuery.append("\nLEFT OUTER JOIN manufacturer manu2 ON manu2.manufacturer_id=ae.manufacturer_id ")


		boolean srcRoomAdded = true 	// Can set to false if the above lines are removed
		boolean tgtRoomAdded = false

		// Tweak the columns selected and addition joins based on the user's selected columns
		assetPref.each { key,value->
			switch (value) {
				case 'appOwner':
					altColumns.append(", CONCAT(CONCAT(p1.first_name, ' '), IFNULL(p1.last_name,'')) AS appOwner")
					joinQuery.append("\nLEFT OUTER JOIN person p1 ON p1.person_id=ae.app_owner_id ")
					break
				case 'os':
					altColumns.append(", ae.hinfo AS os")
					break
				case ~/custom\d{1,}/:
					altColumns.append(", ae.$value AS $value")
					break
				case 'lastUpdated':
					altColumns.append(", ee.last_updated AS $value")
					joinQuery.append("\nLEFT OUTER JOIN eav_entity ee ON ee.entity_id=ae.asset_entity_id ")
					break
				case 'modifiedBy':
					altColumns.append(", CONCAT(CONCAT(p.first_name, ' '), IFNULL(p.last_name,'')) AS modifiedBy")
					joinQuery.append("\nLEFT OUTER JOIN person p ON p.person_id=ae.modified_by ")
					break

				case ~/(location|room)Source/:
					// This is a hack for the columns that were moved to the Room domain property map attributes
					def locOrRoom = Matcher.lastMatcher[0][1]

					if (!srcRoomAdded) {
						joinQuery.append("\nLEFT OUTER JOIN room srcRoom ON srcRoom.room_id=ae.room_source_id ")
						srcRoomAdded = true
					}
					if (locOrRoom == 'location') {
						// Note that this is added by default above
						// altColumns.append(', srcRoom.location AS sourceLocation')
					} else if (locOrRoom == 'room') {
						altColumns.append(', srcRoom.room_name AS roomSource')
					} else {
						throw new RuntimeException("Unhandled condition for property ($value)")
					}
					break
					joinQuery.append("\nLEFT OUTER JOIN rack tgtRack ON tgtRack.rack_id=ae.rack_target_id ")
				case ~/rackSource|moveBundle/:
					// Handled by the default columns
					break

				case ~/(location|room)Target/:
					// This is a hack for the columns that were moved to the Room domain property map attributes
					def locOrRoom = Matcher.lastMatcher[0][1]

					if (!tgtRoomAdded) {
						joinQuery.append("\nLEFT OUTER JOIN room tgtRoom ON tgtRoom.room_id=ae.room_target_id ")
						tgtRoomAdded = true
					}
					if (locOrRoom == 'location') {
						altColumns.append(', tgtRoom.location AS locationTarget')
					} else if (locOrRoom == 'room') {
						altColumns.append(', tgtRoom.room_name AS roomTarget')
					} else {
						throw new RuntimeException("Unhandled condition for property ($value)")
					}
					break
				case 'rackTarget':
					// Property was moved to the Rack domain
					altColumns.append(", tgtRack.tag AS rackTarget")
					joinQuery.append("\nLEFT OUTER JOIN rack tgtRack ON tgtRack.rack_id=ae.rack_target_id ")
					break
				case 'sourceChassis':
					altColumns.append(", aeSourceChassis.asset_name AS sourceChassis")
					joinQuery.append("\nLEFT OUTER JOIN asset_entity aeSourceChassis ON aeSourceChassis.asset_entity_id=ae.source_chassis_id ")
					break
				case 'targetChassis':
					altColumns.append(", aeTargetChassis.asset_name AS targetChassis")
					joinQuery.append("\nLEFT OUTER JOIN asset_entity aeTargetChassis ON aeTargetChassis.asset_entity_id=ae.target_chassis_id ")
					break

				case 'validation':
					break
				case 'tagAssets':
					altColumns.append("""
						, CONCAT(
		                    '[',
		                    if(
		                        ta.tag_asset_id,
		                        group_concat(
		                            json_object('id', ta.tag_asset_id, 'tagId', t.tag_id, 'name', t.name, 'description', t.description, 'color', t.color)
		                        ),
		                        ''
		                    ),
		                    ']'
		                ) as tagAssets""")
					joinQuery.append("""
						LEFT OUTER JOIN tag_asset ta on ae.asset_entity_id = ta.asset_id
						LEFT OUTER JOIN tag t on t.tag_id = ta.tag_id
						""")
					break
				default:
					altColumns.append(", ae.${WebUtil.splitCamelCase(value)} AS $value ")
			}
		}

		def query = new StringBuilder("""
			SELECT * FROM (
				SELECT ae.asset_entity_id AS assetId, ae.asset_name AS assetName,
				ae.asset_type AS assetType, m.name AS model,
				(SELECT if (count(ac_task.comment_type) = 0, 'tasks','noTasks') FROM asset_comment ac_task WHERE ac_task.asset_entity_id=ae.asset_entity_id AND ac_task.comment_type = 'issue') AS tasksStatus,
				(SELECT if (count(ac_comment.comment_type = 0), 'comments','noComments') FROM asset_comment ac_comment WHERE ac_comment.asset_entity_id=ae.asset_entity_id AND ac_comment.comment_type = 'comment') AS commentsStatus,
				me.move_event_id AS event, ae.plan_status AS planStatus,
				mb.name AS moveBundle, ae.validation AS validation
			""")

		if (altColumns.length())
			query.append("\n$altColumns")

		query.append("""
				FROM asset_entity ae
				LEFT OUTER JOIN move_bundle mb ON mb.move_bundle_id=ae.move_bundle_id
				LEFT OUTER JOIN move_event me ON me.move_event_id=mb.move_event_id
				LEFT OUTER JOIN model m ON m.model_id=ae.model_id
				""")

		if (joinQuery.length())
			query.append(joinQuery)

		//
		// Begin the WHERE section of the query
		//
		query.append("\nWHERE ae.project_id = $project.id\nAND ae.asset_class = '$AssetClass.DEVICE'")

		def justPlanning = userPreferenceService.getPreference(PREF.ASSET_JUST_PLANNING)?:'true'
		/*
		// This was being added to correct the issue when coming from the Planning Dashboard but there are some ill-effects still
		if (params.justPlanning)
			justPlanning = params.justPlanning
		*/
		if (justPlanning == 'true')
			query.append("\nAND mb.use_for_planning=$justPlanning")

		query.append("\nAND ae.asset_class='$AssetClass.DEVICE'")

		def filter = params.filter ?: 'all'

		// Filter the list of assets based on if param listType == 'server' to all server types otherwise filter NOT server types
		switch(filter) {
			case 'physical':
				query.append("\nAND COALESCE(ae.asset_type,'') NOT IN (${GormUtil.asQuoteCommaDelimitedString(AssetType.virtualServerTypes)}) ")
				break
			case 'physicalServer':
				def phyServerTypes = AssetType.allServerTypes - AssetType.virtualServerTypes
				query.append("\nAND ae.asset_type IN (${GormUtil.asQuoteCommaDelimitedString(phyServerTypes)}) ")
				break
			case 'server':
				query.append("\nAND ae.asset_type IN (${GormUtil.asQuoteCommaDelimitedString(AssetType.allServerTypes)}) ")
				break
			case 'storage':
				query.append("\nAND ae.asset_type IN (${GormUtil.asQuoteCommaDelimitedString(AssetType.storageTypes)}) ")
				break
			case 'virtualServer':
				query.append("\nAND ae.asset_type IN (${GormUtil.asQuoteCommaDelimitedString(AssetType.virtualServerTypes)}) ")
				break
			case 'other':
				query.append("\nAND COALESCE(ae.asset_type,'') NOT IN (${GormUtil.asQuoteCommaDelimitedString(AssetType.nonOtherTypes)}) ")
				break

			case 'all':
				break
		}

		if (params.event && params.event.isNumber() && moveBundleList)
			query.append("\nAND ae.move_bundle_id IN (${GormUtil.asQuoteCommaDelimitedString(moveBundleList.id)})")

		if (params.unassigned) {
			def unasgnMB = MoveBundle.findAll("\nFROM MoveBundle mb WHERE mb.moveEvent IS NULL AND mb.useForPlanning=true AND mb.project=:project ", [project:project])

			if (unasgnMB) {
				def unasgnmbId = GormUtil.asQuoteCommaDelimitedString(unasgnMB?.id)
				query.append("\nAND (ae.move_bundle_id IN ($unasgnmbId) OR ae.move_bundle_id IS NULL)")
			}
		}

		query.append("\nGROUP BY assetId \n) AS assets")

		// Setup a helper closure that is used to set WHERE or AND for the additional query specifications
		def firstWhere = true
		def whereAnd = {
			if (firstWhere) {
				firstWhere = false
				return ' WHERE'
			} else {
				return ' AND'
			}
		}



		// Handle the filtering by each column's text field
		def whereConditions = []
		def queryParams = [:]
		filterParams.each { key, val ->
			if (val?.trim()) {
				FieldSearchData fieldSearchData = new FieldSearchData([
						domain: AssetEntity,
						column: key,
						filter: val,
						columnAlias: "assets.${key}"

				])

				SqlUtil.parseParameter(fieldSearchData)

				whereConditions << fieldSearchData.sqlSearchExpression
				queryParams += fieldSearchData.sqlSearchParameters
			}
		}

		if (whereConditions.size()) {
			firstWhere = false
			query.append(" WHERE ${whereConditions.join(" AND ")}")
		}
		if (params.moveBundleId) {
			// TODO : JPM 9/2014 : params.moveBundleId!='unAssigned' - is that even possible anymore? moveBundle can't be unassigned...
			if (params.moveBundleId!='unAssigned') {
				def bundleName = MoveBundle.get(params.moveBundleId)?.name
				query.append(whereAnd() + " assets.moveBundle  = '$bundleName' ")
			} else {
				query.append(whereAnd() + " assets.moveBundle IS NULL ")
			}
		}

		if (params.type && params.type == 'toValidate') {
			query.append(whereAnd() + " assets.validation='${ValidationType.UNKNOWN}' ") //eq ('validation','Discovery')
		}

		// Allow filtering on the Validate
		if (params.toValidate && ValidationType.list.contains(params.toValidate)) {
			query.append(whereAnd() + " assets.validation='$params.toValidate' ")
		}

		if (params.plannedStatus) {
			query.append(whereAnd() + " assets.planStatus='$params.plannedStatus'")
		}
		query.append(" ORDER BY $sortIndex $sortOrder")
		// log.debug  "query = $query"

		def assetList = []
		if (queryParams.size()) {
			assetList = namedParameterJdbcTemplate.queryForList(query.toString(), queryParams)
		} else {
			try {
				assetList = jdbcTemplate.queryForList(query.toString())
			} catch (e) {
				log.error "getDeviceDataForList() encountered SQL error : ${e.getMessage()}"
				throw new LogicException("Unabled to perform query based on parameters and user preferences")
			}
		}

		// Cut the list of selected applications down to only the rows that will be shown in the grid
		def totalRows = assetList.size()
		def numberOfPages = Math.ceil(totalRows / maxRows)
		if (totalRows > 0) {
			assetList = assetList[rowOffset..Math.min(rowOffset + maxRows, totalRows - 1)]
		}
		else {
			assetList = []
		}

		def results = assetList?.collect {
			def commentType = it.commentType
			[id: it.assetId,
			 cell: ['', // The action checkbox
			        it.assetName,
			        it.assetType ?: '',
			        it.manufacturer,
			        it.model,
			        it.sourceLocationName,
			        it[assetPref['1']] ?: '',
			        it[assetPref['2']] ?: '',
			        it[assetPref['3']] ?: '',
			        it[assetPref['4']] ?: '',
			        it[assetPref['5']] ?: '',
			        it.planStatus,
			        it.moveBundle,
			        /*it.depNumber, (it.depToResolve == 0)?(''):(it.depToResolve), (it.depConflicts == 0)?(''):(it.depConflicts),*/
			        it.tasksStatus,
			        it.assetType,
			        it.event,
			        it.commentsStatus]
			]
		}

		return [rows: results, page: currentPage, records: totalRows, total: numberOfPages]
	}

	/**
	 * Returns the list of models from a specific manufacturer and asset type
	 *
	 * @param manufacturerId the id of the manufacturer
	 * @param assetType the type of asset
	 * @param term the term to be searched
	 * @return the map of models
	 */
	List modelsOf(String manufacturerId, String assetType, String term) {
		def manufacturer
		def result = []
		List words = []

		def manuId = NumberUtil.toLong(manufacturerId)
		if (manuId) {
			manufacturer = Manufacturer.read(manuId)
		}

		def hql = new StringBuilder('SELECT m.id, m.assetType, m.modelName, man.id as manId, man.name as manName, m.modelStatus, m.usize as usize FROM Model m JOIN m.manufacturer as man')

		def params = []

		StringBuilder where = new StringBuilder()

		if (manufacturer) {
			SqlUtil.appendToWhere(where, "m.manufacturer=?", 'AND')
			params << manufacturer
		}

		if (StringUtils.isNotBlank(assetType)) {
			SqlUtil.appendToWhere(where, "m.assetType=?", 'AND')
			params << assetType
		}

		if (StringUtils.isNotBlank(term)) {
			words = StringUtil.split(term)
			List likeWords = SqlUtil.formatForLike(words)
			StringBuilder search = new StringBuilder()

			if (!manufacturer) {
				SqlUtil.appendToWhere(where, 'm.manufacturer.id = man.id', 'AND')
				SqlUtil.appendToWhere(search, SqlUtil.matchWords('man.name', words, false, false), 'OR')
				params.addAll(likeWords)
			}

			if (!assetType) {
				SqlUtil.appendToWhere(search, SqlUtil.matchWords('m.assetType', words, false, false), 'OR')
				params.addAll(likeWords)
			}

			SqlUtil.appendToWhere(search, SqlUtil.matchWords('m.modelName', words, false, false), 'OR')
			params.addAll(likeWords)

			SqlUtil.appendToWhere(where, '(' + search + ')', 'AND')
		}

		if (where.size())
			hql.append(' WHERE ' + where)

		// Construct to the orderby
		StringBuilder orderBy = new StringBuilder()
		if (!manufacturer)
			orderBy.append('man.name')
		orderBy.append((orderBy.size() ? ', ' : '') + 'm.modelName')
		hql.append(' ORDER BY ' + orderBy)

		String query=hql.toString()
		// log.debug "modelsOf() manufacturerId=$manufacturerId, term=($term) assetType=$assetType, query=$query, params=$params"
		def models = Model.executeQuery(query, params)
		// log.debug "modelsOf() found ${models.size()} rows"
		int added=0
		int max=100
		boolean hasMultiWordFilter = words.size() > 1
		for (int i=0; i < models.size(); i++) {
			def model = models[i]

			if (added >= max)
				break

			boolean isValid = (model[5] == 'valid')
			def title = ''
			// Construct the title based on what the user is filtering on to fill in what is unknown
			if (!manufacturer)
				title = model[4] + ' - '
			title += model[2]
			if (!assetType)
				title += " (${model[1]})"
			if (!isValid)
				title += ' ?'

			// If the user search is on multiple words then we need to match on all of them
			if (hasMultiWordFilter && !StringUtil.containsAll(title, words))
				continue

			// TODO : JPM 10/2014 - reduce the variable name sizes
			result << [
					id: model[0],
					assetType: model[1],
					manufacturerId: model[3],
					manufacturerName: model[4],
					name: model[2],
					text: title,
					isValid: isValid,
					usize: model[6]
			]
			added++
		}

		return result
	}

	/**
	 * Obtains the list of manufactures using the assetType and term
	 *
	 * @param assetType the type of asset
	 * @param term the term to be searched
	 * @return the map of manufacturers
	 */
	def manufacturersOf(assetType, term) {
		String projectionFields = 'SELECT distinct m.id, m.name FROM Manufacturer m'
		String joinTables = ""
		List<String> conditions = []
		String condition = ''
		String orderBy = ' ORDER BY m.name'
		Map hqlParams = [:]

		if (StringUtils.isNotBlank(term)) {
			conditions << 'm.name LIKE :manufacturerName'
			hqlParams['manufacturerName'] = '%' + term + '%'
		}

		if (StringUtils.isNotBlank(assetType)) {
			joinTables = ' LEFT OUTER JOIN m.models as model'
			conditions << 'model.assetType = :modelAssetType'
			hqlParams['modelAssetType'] = assetType
		}

		if (conditions) {
			condition = ' WHERE ' + conditions.join(' AND ')
		}

		String hqlQuery = projectionFields + joinTables + condition + orderBy

		def manufacturers = Manufacturer.executeQuery(hqlQuery, hqlParams)
		def result = manufacturers.collect { manufacturer ->
			return [
					"id" : manufacturer[0],
					"text" : manufacturer[1]
			]
		}

		return result
	}

	/**
	 * Obtains the list of asset types using the manufacturer and term
	 *
	 * @param manufacturer the manufacturer
	 * @param term the term to be searched
	 * @return the map of asset types
	 */
	def assetTypesOf(String manufacturerId, String term) {
		if(StringUtils.isBlank(manufacturerId) && StringUtils.isBlank(term)){
			List<AssetOptions> assetOptions =  AssetOptions.findAllByType(AssetOptions.AssetOptionsType.ASSET_TYPE, [sort: 'value', order: 'asc'])

			List<Map> results = assetOptions.collect { options ->
				[id: options.value, text : options.value]
			}

			return results
		}

		def hql = "SELECT distinct m.assetType as assetType FROM Model m WHERE m.assetType is not null "
		def joinTables = " "
		def condition = ""
		def hqlParams = []

		if (StringUtils.isNotBlank(term)) {
			condition = condition + "AND m.assetType LIKE ?"
			hqlParams.add("%" + term + "%")
		}

		if (StringUtils.isNotBlank(manufacturerId) && manufacturerId.isLong()) {
			condition = condition + "AND m.manufacturer.id = ?"
			hqlParams.add(manufacturerId.toLong())
		}

		def models = Model.executeQuery(hql + joinTables + condition, hqlParams)
		def result = models.collect { model -> [id: model, text : model] }

		result = result.sort {it.text}
		return result
	}

	/**
	 * Return the map of information for editing dependencies.
	 * @param project
	 * @param asset
	 * @return
	 */
	@Transactional(readOnly = true)
	Map dependencyEditMap(Project project, AssetEntity asset) {

		if (!asset) {
			throw new InvalidRequestException('An invalid asset id was requested')
		}

		return [
			assetClassOptions: AssetClass.classOptions,
			dependentAssets: getDependentsOrSupporting(asset.id ? asset : null, true),
			dependencyStatus: getDependencyStatuses(),
			dependencyType: getDependencyTypes(),
			moveBundleList: getMoveBundles(project),
			nonNetworkTypes: AssetType.nonNetworkTypes,
			supportAssets: getDependentsOrSupporting(asset.id ? asset : null, false)
		]
	}

	/**
	 * Return the map of information for show/create dependencies.
	 * @param project
	 * @return
	 */
	@Transactional(readOnly = true)
	Map dependencyCreateMap(Project project) {
		assetService.dependencyCreateMap(project)
		return [
				assetClassOptions: AssetClass.classOptions,
				dependencyStatus: getDependencyStatuses(),
				dependencyType: getDependencyTypes(),
				moveBundleList: getMoveBundles(project),
				nonNetworkTypes: AssetType.nonNetworkTypes,
		]
	}

	/**
	 * Used to retrieve the model used to display Asset Dependencies Edit view
	 * @param params - the Request params
	 * @return a Map of all properties used by the view
	 */
	@Transactional(readOnly = true)
	Map dependencyEditMap(params) {
		Project project = securityService.userCurrentProject
		Long id = params.long('id')
		if (!id) {
			throw new InvalidRequestException('An invalid asset id was requested')
		}

		AssetEntity assetEntity = AssetEntity.get(id)
		if (!assetEntity) {
			securityService.reportViolation('attempted to access an asset unassociated to current project')
			throw new InvalidRequestException('Unable to find requested asset')
		}

		if (assetEntity.project.id != project.id) {
			securityService.reportViolation('attempted to access an asset unassociated to current project')
			throw new InvalidRequestException('Unable to find requested asset')
		}

		// TODO - JPM 8/2014 - Why do we have this? Seems like we should NOT be passing that to the template...
		List nonNetworkTypes = [
			AssetType.SERVER,
			AssetType.APPLICATION,
			AssetType.VM,
			AssetType.FILES,
			AssetType.DATABASE,
			AssetType.BLADE
		]*.toString()

		Map map = [
			assetClassOptions: AssetClass.classOptions,
			assetEntity      : assetEntity,
			dependencyStatus : getDependencyStatuses(),
			dependencyType   : getDependencyTypes(),
			whom             : params.whom,
			nonNetworkTypes  : nonNetworkTypes,
			supportAssets    : getSupportingAssets(assetEntity),
			dependentAssets  : getDependentAssets(assetEntity),
			moveBundleList   : assetService.getMoveBundleOptions(project)
		]

		return map
	}


	/**
	 * Used to clone an asset and optionally the dependencies associated with the asset
	 * @param command - command object with the parameters for the clone operation (id, name and dependencies flag).
	 * @param errors - an initialize List object that will be populated with one or more error messages if the method fails
	 * @return the id number of the newly created asset
	 */
	Long clone(Project project, CloneAssetCommand command, List<String> errors) {
		AssetEntity clonedAsset
		if(!errors) {
			//  params son assetId
			AssetEntity assetToClone = GormUtil.findInProject(project, AssetEntity, command.assetId)
			if (!assetToClone) {
				errors << "The asset specified to clone was not found"
			} else{
				Map defaultValues = [
					assetName : command.name,
					validation: ValidationType.UNKNOWN,
					environment: ''
				]
				if (assetToClone.isaDevice()) {
					defaultValues.assetTag = projectService.getNextAssetTag(assetToClone.project)
				}
				clonedAsset = assetToClone.clone(defaultValues)

				// Cloning assets dependencies if requested
				if (clonedAsset.save() && command.cloneDependencies) {
					for (dependency in assetToClone.supportedDependencies()) {
						AssetDependency clonedDependency = dependency.clone([
								dependent: clonedAsset,
								status   : AssetDependencyStatus.QUESTIONED
						])

						clonedDependency.save()
					}
					for (dependency in assetToClone.requiredDependencies()) {
						AssetDependency clonedDependency = dependency.clone([
								asset : clonedAsset,
								status: AssetDependencyStatus.QUESTIONED
						])
						clonedDependency.save()
					}
				}
				// copy asset Tags
				List<Long> sourceTagIds = assetToClone?.tagAssets.collect{it.tag.id}
				if (sourceTagIds) {
					tagAssetService.applyTags(assetToClone.project, sourceTagIds, clonedAsset.id)
				}

				return clonedAsset.id
			}
		}
	}

	/**
	 * Create or update an asset based on the command object received.
	 * @param command
	 * @return
	 */
	AssetEntity saveOrUpdateAsset(AssetCommand command) {
		AssetSaveUpdateStrategy strategy = AssetSaveUpdateStrategy.getInstanceFor(command)
		return strategy.saveOrUpdateAsset()
	}

	/**
	 * Update the lastUpdated field on a series of assets.
	 *
	 * This method helps to keep consistency, and update assets accordingly,
	 * when performing bulk update operations on objects that have a relationship with assets,
	 * such as TagAsset. this will take in the subquery from a bulk change generated from
	 * dataviewService.getAssetIdsHql using the field specs.
	 *
	 * @param project
	 * @param assetQuery - query generated from the field specs using dataviewService.getAssetIdsHql.
	 * @param assetQueryParams - parameters for assetQuery
	 */
	void bulkBumpAssetLastUpdated(Project project, String assetQuery, Map assetQueryParams) {
		if (project) {
			String query = """
				UPDATE AssetEntity SET lastUpdated = :lastUpdated
				WHERE id IN ($assetQuery) AND project = :project
			"""

			Map params = [project: project, lastUpdated: TimeUtil.nowGMT()]
			params.putAll(assetQueryParams)
			AssetEntity.executeUpdate(query, params)
		}
	}

	/**
	 * Update the lastUpdated field on a series of assets.
	 *
	 * This method helps to keep consistency, and update assets.
	 *
	 * @param project
	 * @param assetIds - a list of asset ids
	 */
	void bulkBumpAssetLastUpdated(Project project, Set<Long> assetIds) {
		if (project) {
			String query = """
				UPDATE AssetEntity SET lastUpdated = :lastUpdated
				WHERE id IN (:assetIds) AND project = :project
			"""

			Map params = [project: project, lastUpdated: TimeUtil.nowGMT(), assetIds: assetIds]
			AssetEntity.executeUpdate(query, params)
		}
	}

}
