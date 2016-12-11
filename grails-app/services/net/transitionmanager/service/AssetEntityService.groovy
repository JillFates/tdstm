package net.transitionmanager.service

import com.tds.asset.Application
import com.tds.asset.ApplicationAssetMap
import com.tds.asset.AssetCableMap
import com.tds.asset.AssetComment
import com.tds.asset.AssetDependency
import com.tds.asset.AssetDependencyBundle
import com.tds.asset.AssetEntity
import com.tds.asset.AssetEntityVarchar
import com.tds.asset.AssetOptions
import com.tds.asset.AssetType
import com.tds.asset.Database
import com.tds.asset.Files
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.common.sql.SqlUtil
import com.tdsops.tm.domain.AssetEntityHelper
import com.tdsops.tm.enums.domain.AssetCableStatus
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.EntityType
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdsops.tm.enums.domain.ValidationType
import com.tdssrc.eav.EavAttribute
import com.tdssrc.eav.EavAttributeOption
import com.tdssrc.grails.ApplicationConstants
import com.tdssrc.grails.ExportUtil
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WebUtil
import com.tdssrc.grails.WorkbookUtil
import net.transitionmanager.controller.ServiceResults
import net.transitionmanager.domain.DataTransferAttributeMap
import net.transitionmanager.domain.DataTransferSet
import net.transitionmanager.domain.KeyValue
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
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.utils.Profiler

import org.apache.commons.lang.StringEscapeUtils as SEU
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.math.NumberUtils
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Workbook
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.hibernate.Criteria
import org.hibernate.transform.Transformers
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

import grails.converters.JSON
import grails.transaction.Transactional
import javax.servlet.http.HttpSession
import java.util.regex.Matcher

@Transactional
class AssetEntityService implements ServiceMethods {

	// TODO : JPM 9/2014 : determine if customLabels is used as it does NOT have all of the values it should
	protected static final List<String> customLabels = (1..Project.CUSTOM_FIELD_COUNT).collect { 'Custom' + it }.asImmutable()

	// TODO : JPM 9/2014 : determine if bundleMoveAndClientTeams is used as the team functionality has been RIPPED out of TM
	protected static final List<String> bundleMoveAndClientTeams = ['sourceTeamMt', 'sourceTeamLog', 'sourceTeamSa',
	                                                                'sourceTeamDba', 'targetTeamMt', 'targetTeamLog',
	                                                                'targetTeamSa', 'targetTeamDba'].asImmutable()

	// properties that should be excluded from the custom column select list
	private static final Map<String, List<String>> COLUMN_PROPS_TO_EXCLUDE = [
			(AssetClass.APPLICATION): [],
			(AssetClass.DATABASE): [],
			(AssetClass.DEVICE): ['assetType', 'model', 'planStatus', 'moveBundle', 'sourceLocation',
			                      // TODO : JPM 9/2014 : This list can be removed as part of TM-3311
			                      'sourceTeamDba', 'sourceTeamDba', 'sourceTeamLog', 'sourceTeamSa', 'sourceTeamMt',
			                      'targetTeamDba', 'targetTeamDba', 'targetTeamLog', 'targetTeamSa', 'targetTeamMt'
			],
			(AssetClass.STORAGE): []
	].asImmutable()

	// The follow define the various properties that can be used with bindData to assign domain.properties
	static final List<String> CUSTOM_PROPERTIES = (1..Project.CUSTOM_FIELD_COUNT).collect { 'custom' + it }.asImmutable()

	// Common properties for all asset classes (Application, Database, Files/Storate, Device)
	static final List<String> ASSET_PROPERTIES = ['assetName',  'shortName', 'priority', 'planStatus',  'department',
	                                              'costCenter', 'maintContract', 'maintExpDate', 'retireDate',
	                                              'description', 'supportType', 'environment', 'serialNumber',
	                                              'validation', 'externalRefId', 'size', 'scale', 'rateOfChange'].asImmutable()
	// 'purchaseDate', 'purchasePrice',

	// Properties strictly for DEVICES (a.k.a. AssetEntity)
	static final List<String> DEVICE_PROPERTIES = [
			'assetTag', 'assetType', 'ipAddress', 'os', 'usize', 'truck', 'cart', 'shelf', 'railType',
			'sourceBladePosition', 'targetRackPosition', 'sourceRackPosition', 'targetBladePosition'
	].asImmutable()

	// Properties strictly for ASSETS that are date (a.k.a. AssetEntity)
	static final List<String> ASSET_DATE_PROPERTIES = ['purchaseDate', 'maintExpDate', 'retireDate'].asImmutable()

	// List of all of the Integer properties for the potentially any of the asset classes
	static final List<String> ASSET_INTEGER_PROPERTIES = ['size', 'rateOfChange', 'priority', 'sourceBladePosition',
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
			all: 'All Devices', other: 'Other Devices', physical: 'Physical Device', physicalServer: 'Physical Server',
			server: 'Server', storage: 'Storage Device', virtualServer: 'Virtual Server'].asImmutable()

	private static final Map<String, String> RACK_MAP = [
			rackId: 'id', Tag: 'tag', Location: 'location', Room: 'room', RoomX: 'roomX', RoomY: 'roomY',
			PowerA: 'powerA', PowerB: 'powerB', PowerC: 'powerC', Type: 'rackType', Front: 'front',
			Model: 'model', Source: 'source'].asImmutable()

	AssetEntityAttributeLoaderService assetEntityAttributeLoaderService
	GrailsApplication grailsApplication
	JdbcTemplate jdbcTemplate
	NamedParameterJdbcTemplate namedParameterJdbcTemplate
	PartyRelationshipService partyRelationshipService
	ProgressService progressService
	ProjectService projectService
	RackService rackService
	RoomService roomService
	SecurityService securityService
	TaskService taskService
	UserPreferenceService userPreferenceService

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
			StringBuffer query = new StringBuffer("SELECT a.id as id, a.assetName as text")

			StringBuffer fromQuery = new StringBuffer(" FROM ")
					.append(typeInfo.domain.name)
					.append(" AS a")

			StringBuffer whereQuery = new StringBuffer(" WHERE a.project=:project AND a.assetClass=:assetClass")

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
			def assets = typeInfo.domain.executeQuery(query.toString(), qparams, additionalFilters)
			assets.each { a -> results << [id: a[0], name: a[1]]}
			return results
		}

		return results
	}

	/**
	 * Used to validate a list of assets belong to the project supplied and will throw an error if otherwise
	 * @param assetIdList - a list of asset ids
	 * @param project - the currently selected project
	 * @return A list of the invalid asset ids otherwise an empty list
	 */
	@Transactional(readOnly = true)
	List validateAssetList(List assetIdList, Project project) {
		assetIdList = assetIdList.unique()
		List allAssets = AssetEntity.getAll(assetIdList).findAll()
		List invalidIds = allAssets.findAll { it.project.id != project.id }*.id
		List missingIds = []
		assetIdList.each { x ->
			if (!allAssets.find { it.id == x }) {
				missingIds << x
			}
		}
		// log.debug "**** assetIdList=$assetIdList, allAssets=${allAssets*.id}, invalidIds=$invalidIds, missingIds=$missingIds"

		if (invalidIds) {
			securityService.reportViolation("validateAssetList() attempted to access asset(s) ($invalidIds) not assigned to current project ($project.id)")
		}
		if (missingIds)
			invalidIds += missingIds

		return invalidIds
	}

	/**
	 * Used by the various asset controllers to return the JSON response to the callers when creating new asset
	 * @param model
	 * @param errorMsg
	 */
	void renderSaveAssetJsonResponse(controller, Map model, errorMsg) {
		renderSaveUpdateJsonResponse(controller, model, errorMsg, true)
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
			assignAssetToRack(project, device, params.rackSourceId, params.sourceRack, true)
			assignAssetToRack(project, device, params.rackTargetId, params.targetRack, false)
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
				if (location.trim().size() == 0 || roomName.trim().size() == 0) {
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
		if ([AssetType.BLADE.toString(), AssetType.VM.toString()].contains(assetType)) {
			// If asset model is VM or BLADE we should remove rack assignments period
			asset[rackProp] = null
			if (isSource) {
				asset.sourceBladePosition = null
				asset.sourceRackPosition = null
			} else {
				asset.targetBladePosition = null
				asset.targetRackPosition = null
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
				if (!rackName) {
					throw new InvalidRequestException("Creating a $srcOrTarget rack requires a name".toString())
				}

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
		List errors = []
		String errObject = 'dependencies'
		try {
			if (!assetEntity.validate() || !assetEntity.save(flush:true)) {
				errObject = 'asset'
				throw new DomainUpdateException('Unable to update asset ' + GormUtil.allErrorsString(assetEntity))
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
			errors << e.message
		} catch (RuntimeException rte) {
			//rte.printStackTrace()
			log.error ExceptionUtil.stackTraceToString(rte, 60)
			errors << 'An error occurred that prevented the update'
		}

		if (errors.size()) {
			assetEntity.discard()
			transactionStatus.setRollbackOnly()
			throw new DomainUpdateException("Unable to update $errObject : $errors".toString())
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
	 */
	void deleteAsset(AssetEntity asset) {
		Map<String, Object> assetMap = [asset: asset]
		ProjectAssetMap.executeUpdate('DELETE ProjectAssetMap WHERE asset=:asset', assetMap)
		AssetComment.executeUpdate('UPDATE AssetComment SET assetEntity=null WHERE assetEntity=:asset', assetMap)
		ApplicationAssetMap.executeUpdate('DELETE ApplicationAssetMap WHERE asset=:asset', assetMap)
		AssetEntityVarchar.executeUpdate('DELETE AssetEntityVarchar WHERE assetEntity=:asset', assetMap)
		ProjectTeam.executeUpdate('UPDATE ProjectTeam SET latestAsset=null WHERE latestAsset=:asset', assetMap)
		AssetCableMap.executeUpdate('DELETE AssetCableMap WHERE assetFrom=:asset', assetMap)
		AssetCableMap.executeUpdate('''
			UPDATE AssetCableMap
			SET cableStatus=:status, assetTo=null, assetToPort=null
			WHERE assetTo=:asset''', assetMap + [status: AssetCableStatus.UNKNOWN])
		AssetDependency.executeUpdate('DELETE AssetDependency WHERE asset=:asset or dependent=:asset ', assetMap)
		AssetDependencyBundle.executeUpdate('DELETE AssetDependencyBundle WHERE asset = :asset', assetMap)

		// Clear any possible Chassis references
		AssetEntity.executeUpdate(
				'UPDATE AssetEntity SET sourceChassis=NULL, sourceBladePosition=NULL WHERE sourceChassis=:asset',
				assetMap)
		AssetEntity.executeUpdate(
				'UPDATE AssetEntity SET targetChassis=NULL, targetBladePosition=NULL WHERE targetChassis=:asset',
				assetMap)
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
		AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_TYPE)*.value
	}

	/**
	 * The valid values that can be assigned to AssetDependency.status
	 * @return the values
	 */
	List<String> getDependencyStatuses() {
		AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_STATUS)*.value
	}

	/**
	 * Asset Environment options
	 * @return the values
	 */
	List<String> getAssetEnvironmentOptions() {
		return AssetOptions.findAllByType(AssetOptions.AssetOptionsType.ENVIRONMENT_OPTION)*.value
	}

	/**
	 * Asset Status options
	 * @return the values
	 */
	List<String> getAssetPlanStatusOptions() {
		return AssetOptions.findAllByType(AssetOptions.AssetOptionsType.STATUS_OPTION)*.value
	}

	/**
	 * Asset Priority Options.
	 * @return the values
	 */
	List<String> getAssetPriorityOptions() {
		return AssetOptions.findAllByType(AssetOptions.AssetOptionsType.PRIORITY_OPTION)*.value
	}

	/**
	 * Use to get the list of the device RailType Options
	 * @return List of RailTypes
	 */
	List<String> getAssetRailTypeOptions() {
		EavAttributeOption.findAllByAttribute(EavAttribute.findByAttributeCode('railType'))*.value
	}

	/**
	 * Used to retrieve the assettype attribute object
	 * @param the name of the attribute
	 * @return the Attribute object
	 */
	Object getPropertyAttribute(String property) {
		EavAttribute.findByAttributeCode(property)
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
	 * Returns a list of MoveBundles for a project
	 * @param project - the Project object to look for
	 * @return list of MoveBundles
	 */
	List<MoveBundle> getMoveBundles(Project project) {
		project ? MoveBundle.findAllByProject(project, [sort: 'name']) : []
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
	 */
	// TODO : JPM 9/2014 : these methods should be renamed from getDeviceModel to getDeviceAndModel to avoid confusion (improvement)
	@Transactional(readOnly = true)
	List getDeviceModelForEdit(Project project, deviceId, Map params) {
		def (device, model) = getCommonDeviceModelForCreateEdit(project, deviceId, params)
		if (device) {
			// TODO : JPM 9/2014 : refactor the quote strip into StringUtil.stripQuotes method or escape the name. This is done to fix issue with putting device name into javascript links
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
			manufacturers: getManufacturers(assetType),
			models: getModelSortedByStatus(device.manufacturer),
			// TODO : JPM 9/2014 : Determine what nonNetworkTypes is used for in the view (clean up if unnecessary)
			modelName: modelName,
			nonNetworkTypes: AssetType.nonNetworkTypes,
			railTypeOption: getAssetRailTypeOptions(),
			// TODO : JPM 9/2014 : determine if the views use source/targetRacks - I believe these can be removed as they are replaced by source/targetRackSelect
			sourceRacks: [],
			targetRacks: [],
			sourceChassisSelect: [],
			targetChassisSelect: [],
			version: device.version)

		// List of the room and racks to be used in the SELECTs
		model.sourceRoomSelect = getRoomSelectOptions(project, true, true)
		model.targetRoomSelect = getRoomSelectOptions(project, false, true)
		model.sourceRackSelect = getRackSelectOptions(project, device?.roomSourceId, true)
		model.targetRackSelect = getRackSelectOptions(project, device?.roomTargetId, true)

		model.putAll(getDefaultModelForEdits('AssetEntity', project, device, params))

		if (device) {
			// TODO : JPM 9/2014 : Need to make the value flip based on user pref to show name or tag (enhancement TM-3390)
			// Populate the listings of the Chassis SELECT name/values
			model.sourceChassisSelect = getChassisSelectOptions(project, device?.roomSourceId)
			model.targetChassisSelect = getChassisSelectOptions(project, device?.roomTargetId)
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

		//assert ['Database'].contains(type)
		def configMap = getConfig(type, asset?.validation ?: 'Discovery')

		def assetTypeAttribute = getPropertyAttribute('assetType')
		//def validationType = asset.validation
		def highlightMap = getHighlightedInfo(type, asset, configMap)
		def dependentAssets = getDependentAssets(asset)
		def supportAssets = getSupportingAssets(asset)

		// TODO - JPM 8/2014 - Need to see if Edit even uses the servers list at all. If so, this needs to join the model to filter on assetType
		def servers = AssetEntity.executeQuery('''
			FROM AssetEntity
			WHERE project=:project AND assetClass=:ac AND assetType IN (:types)
			ORDER BY assetName''', [project: project, ac: AssetClass.DEVICE, types: AssetType.serverTypes])

		[assetId: asset.id,
		 assetTypeAttribute: assetTypeAttribute,
		 assetTypeOptions: getDeviceAssetTypeOptions(),
		 config: configMap.config,
		 customs: configMap.customs,
		 dependencyStatus: getDependencyStatuses(),
		 dependencyType: getDependencyTypes(),
		 dependentAssets: dependentAssets,
		 environmentOptions: getAssetEnvironmentOptions(),
		 // The name of the asset that is quote escaped to prevent lists from erroring with links
		 // TODO - this function should be replace with a generic HtmlUtil method - this function is to single purposed...
		 escapedName: getEscapedName(asset),
		 highlightMap: highlightMap,
		 moveBundleList: getMoveBundles(project),
		 planStatusOptions: getAssetPlanStatusOptions(),
		 project: project,
		 projectId: project.id,
		 priorityOption: getAssetPriorityOptions(),
		 // The page to return to after submitting changes
		 redirectTo: params.redirectTo,
		 servers: servers,
		 supportAssets: supportAssets,
		 version: asset.version]
	}

	/**
	 * The default/common properties shared between all of the Asset Show views
	 */
	@Transactional(readOnly = true)
	Map getCommonModelForShows(String type, Project project, Map params, assetEntity = null) {

		log.debug "### getCommonModelForShows() type=$type, project=$project.id, asset=${assetEntity? assetEntity.id : 'null'}"
		if (assetEntity == null) {
			assetEntity = AssetEntity.read(params.id)
		}

		def assetComment
		if (AssetComment.executeQuery('select count(*) from AssetComment ' +
		                              'where assetEntity=? and commentType=? and isResolved=?',
		                              [assetEntity, 'issue', 0])[0]) {
			assetComment = "issue"
		} else if (assetEntity && AssetComment.countByAssetEntity(assetEntity)) {
			assetComment = "comment"
		} else {
			assetComment = "blank"
		}

		def projectAttributes = projectService.getAttributes(type)
		def configMap = getConfig(type, assetEntity.validation, projectAttributes)
		List<AssetDependency> dependentAssets = AssetDependency.executeQuery(
			'from AssetDependency where asset=? order by dependent.assetType, dependent.assetName asc',
			[assetEntity])
		def supportAssets = AssetDependency.executeQuery(
			'from AssetDependency a where dependent=? order by a.asset.assetType, a.asset.assetName asc',
			[assetEntity])
		def highlightMap = getHighlightedInfo(type, assetEntity, configMap, projectAttributes)
		def prefValue = userPreferenceService.getPreference(PREF.SHOW_ALL_ASSET_TASKS) ?: 'FALSE'
		def viewUnpublishedValue = userPreferenceService.getPreference(PREF.VIEW_UNPUBLISHED) ?: 'false'
		def depBundle = AssetDependencyBundle.findByAsset(assetEntity)?.dependencyBundle // AKA dependency group

		[assetId: assetEntity?.id,
		 assetComment: assetComment,
		 assetCommentList: AssetComment.findAllByAssetEntity(assetEntity),
		 config: configMap.config,
		 customs: configMap.customs,
		 dependencyBundleNumber: depBundle,
		 dependentAssets: dependentAssets,
		 errors: params.errors,
		 escapedName: getEscapedName(assetEntity),
		 highlightMap: highlightMap,
		 prefValue: prefValue,
		 project: project,
		 client: project.client,
		 redirectTo: params.redirectTo,
		 supportAssets: supportAssets,
		 viewUnpublishedValue: viewUnpublishedValue,
		 hasPublishPermission: securityService.hasPermission("PublishTasks")]
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
			attributesList: [],		// Set below
			dependencyStatus: getDependencyStatuses(),
			dependencyType: getDependencyTypes(),
			event: params.moveEvent,
			fixedFilter: params.filter as Boolean,
			filter: params.filter,
			hasPerm: securityService.hasPermission("AssetEdit"),
			justPlanning: userPreferenceService.getPreference(PREF.ASSET_JUST_PLANNING) ?: 'true',
			modelPref: null,		// Set below
			moveBundleId: params.moveBundleId,
			moveBundle: filters?.moveBundleFilter ?: '',
			moveBundleList: getMoveBundles(project),
			moveEvent: null,		// Set below
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

		// Get the list of attributes that the user can select for columns
		def attributes = projectService.getAttributes(listType)

		// Create a list of the "custom##" fields that are currently selectable
		def projectCustoms = project.customFieldsShown + 1
		List<String> nonCustomList = project.customFieldsShown != Project.CUSTOM_FIELD_COUNT ?
				(projectCustoms..Project.CUSTOM_FIELD_COUNT).collect { 'custom' + it } : []

		// Remove the non project specific attributes and sort them by attributeCode
		def appAttributes = attributes.findAll {
			it.attributeCode != "assetName" &&
			it.attributeCode != "manufacturer" &&
			!(it.attributeCode in nonCustomList) &&
			!COLUMN_PROPS_TO_EXCLUDE[ac].contains(it.attributeCode)
		}

		// Used to display column names in jqgrid dynamically
		def modelPref = [:]
		fieldPrefs.each { key, value ->
			modelPref[key] = getAttributeFrontendLabel(value, attributes.find { it.attributeCode == value }?.frontendLabel)
		}
		model.modelPref = modelPref

		// Compose the list of Asset properties that the user can select and use for filters
		def attributesList = appAttributes.collect { attribute ->
			[attributeCode: attribute.attributeCode,
			 frontendLabel: getAttributeFrontendLabel(attribute.attributeCode, attribute.frontendLabel)]
		}

		// Sorts attributesList alphabetically
		attributesList.sort { it.frontendLabel }
		model.attributesList = attributesList

		return model
	}

	/**
	 * Used to get an Select Option list of the Rooms for the source or target of the specified Project
	 * @param project
	 * @param isSource - flag to indicate that it should return Source rooms if true
	 * @param allowAdd - flag if true will include a Add Room... option (-1) default true
	 * @return List<Map<id,value>>
	 */
	List getRoomSelectOptions(Project project, boolean isSource, boolean allowAdd = true) {
		def rsl = []
		if (allowAdd) {
			rsl << [id: -1, value: 'Add Room...']
		}

		Room.executeQuery(
				'from Room where project=:p and source=:s order by location, roomName',
				[p: project, s: isSource ? 1 : 0]).each { Room room -> rsl << [id: room.id, value: room.location + ' / ' + room.roomName] }
		return rsl
	}

	/**
	 * Used to get a list of the options available for a Rack
	 * @param project
	 * @param room - the room  idto find associated racks for
	 * @param allowAdd - flag if true will include a Add Room... option (-1) default true
	 * @return List<Map<id,value>>
	 */
	List getRackSelectOptions(Project project, roomId, allowAdd) {
		def rsl = []
		if (allowAdd) {
			rsl << [id: -1, value: 'Add Rack...']
		}

		roomId = NumberUtil.toLong(roomId)
		if (roomId) {
			def racks = Rack.executeQuery('''
				from Rack r inner join r.model m
				where r.project=:p and r.room.id=:r and m.assetType=:t
				order by r.tag
			''', [p: project, r: roomId, t: 'Rack'])
			racks.each { rack ->
					rsl << [id: rack[0].id, value: rack[0].tag]
			}
		}

		return rsl
	}

	/**
	 * Used to get an Select Option list of the chassis located in the specified Room
	 * @param project
	 * @param roomId - the id of the room to find chassis in
	 * @return List<Map<id, value>>
	 */
	List getChassisSelectOptions(Project project, roomId) {
		def rsl = [ ]
		roomId = NumberUtil.toLong(roomId)
		if (roomId) {
			def room = Room.get(roomId)
			if (room.project != project) {
				securityService.reportViolation("Attemped to assess room ($roomId') unassociated with project ($project.id)")
			}
			else {
				def roomProp = room.source ? 'roomSource' : 'roomTarget'
				def chassisList = getAssetsWithCriteriaMap(project, AssetClass.DEVICE, [(roomProp): room, assetType: AssetType.bladeChassisTypes])

				chassisList.each { rsl << [id: it.id, value: it.assetName + '/' + it.assetTag] }
			}
		}

		return rsl
	}

	/**
	 * Used to retrieve a list of assets based on with a criteria map
	 * @param project - the project object for the associated assets
	 * @param ac - the AssetClass to retrieve
	 * @param criteriaMap - the property names to be associated
	 * @param includeJoinData - a flag that when true, if there is a join in the query (e.g. referencing assetType) will return a multi-dimensional array of the dataset
	 * @return the list of assets found
	 */
	List getAssetsWithCriteriaMap(Project project, AssetClass ac, Map criteriaMap, boolean includeJoinData=false) {
		def params = [project: project, ac: ac]
		String domainName = AssetClass.domainNameFor(ac)
		Object domainClass = AssetClass.domainClassFor(ac)
		StringBuilder from = new StringBuilder("from $domainName a ")
		StringBuilder where = new StringBuilder("where a.project=:project and a.assetClass=:ac ")
		def map
		boolean hasJoin = false

		// Go through the params and construct the query appropriately
		criteriaMap.each { propName, value ->

			if (propName == 'assetType') {
				from.append('inner join a.model m ')
				map = SqlUtil.whereExpression('m.assetType', value, 'assetType')
				hasJoin = true
			} else {
				map = SqlUtil.whereExpression('a.' + propName, value, propName)
			}

			if (map) {
				where.append(' and ' + map.sql)
				params[propName] = map.param
			} else {
				log.error "getAssetsWithCriteriaMap() SqlUtil.whereExpression() returned no value property $propName, criteria $value"
				return null
			}
		}

		String hql = from.toString() + where
		// log.debug "getAssetsWithCriteriaMap() HQL=$hql, params=$params"
		def assets = domainClass.findAll(hql, params)
		// log.debug "getAssetsWithCriteriaMap() found ${assets.size()} : $assets"

		if (assets && hasJoin) {
			if (!includeJoinData) {
				// Just get the Asset objects and exclude the joined domains
				assets = assets.collect { it[0] }
			}
		}
		return assets
	}

	/**
	 * Get config by entityType and validation
	 */
	Map getConfig(String type, String validation, projectAttributes = null) {
		Project project = securityService.userCurrentProject
		def allconfig = projectService.getConfigByEntity(type)
		List<Map<String, String>> fields = projectService.getFields(type, projectAttributes) + projectService.getCustoms(projectAttributes)
		def config = [:]
		String validationType = ValidationType.valuesAsMap[validation]
		fields.each { Map f ->
			if (allconfig[f.label]) {
				config[f.label] = allconfig[f.label]['phase'][validationType]
			}
		}

		//used to hide the customs whose fieldImportance is "H"
		def customs = []
		def hiddenConfig = []
		if (project.customFieldsShown > 0) {
			(1..(project.customFieldsShown)).each { i ->
				customs << i
				if (config.('custom'+i) == 'H')
					hiddenConfig << i
			}
		}

		customs.removeAll(hiddenConfig)

		[project: project, config: config, customs: customs]
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
	 * Delete assets by asset type.
	 * @param type
	 * @param assetList - list of ids for which assets are requested to deleted
	 * @return
	 */
	String deleteBulkAssets(type, assetList) {
		def resp
		def assetNames = []
		try {
			//Collecting as a list of data type long
			assetList = assetList*.toLong()
			if (type == "dependencies") {
				AssetDependency.getAll(assetList).findAll().each { ad ->
					assetNames << ad.dependent?.assetName + "  AND Asset  " + ad.asset?.assetName
					ad.delete()
				}
			}
			else {
				AssetEntity.getAll(assetList).findAll().each { ae ->
					assetNames << ae.assetName
					deleteAsset(ae)
					ae.delete()
				}
			}
			resp = "$type ${WebUtil.listAsMultiValueString(assetNames)} deleted."
		}catch(Exception e) {
			e.printStackTrace()
			resp = "Error while deleting $type"
		}
		return resp
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

	/*
	 * export cabling data.
	 */
	def cablingReportData(assetCablesList, cablingSheet, progressCount, progressTotal, updateOnPercent, key) {

		assetCablesList.eachWithIndex { cabling, int idx ->
			def currentCabling = cabling.get(Criteria.ROOT_ALIAS)
			progressCount++
			updateProgress(key, progressCount, progressTotal, 'In progress', 0.01)
			addCell(cablingSheet, idx + 2, 0, String.valueOf(currentCabling.assetFromPort?.type ?: ''))
			addCell(cablingSheet, idx + 2, 1, currentCabling.assetFrom?.id ?: '', Cell.CELL_TYPE_NUMERIC)
			addCell(cablingSheet, idx + 2, 2, String.valueOf(currentCabling.assetFrom?.assetName ?: ''))
			addCell(cablingSheet, idx + 2, 3, String.valueOf(currentCabling.assetFromPort?.label ?: ''))
			addCell(cablingSheet, idx + 2, 4, currentCabling.assetTo?.id ?: '', Cell.CELL_TYPE_NUMERIC)
			addCell(cablingSheet, idx + 2, 5, String.valueOf(currentCabling.assetTo?.assetName ?: ''))
			if (currentCabling.assetFromPort?.type && currentCabling.assetFromPort.type !='Power') {
				addCell(cablingSheet, idx + 2, 6, String.valueOf(currentCabling.assetToPort?.label ?: ''))
			} else {
				addCell(cablingSheet, idx + 2, 6, String.valueOf(currentCabling.toPower ?: ''))
			}
			addCell(cablingSheet, idx + 2, 7, String.valueOf(currentCabling.cableComment ?: ''))
			addCell(cablingSheet, idx + 2, 8, String.valueOf(currentCabling.cableColor ?: ''))
			if (currentCabling.assetFrom?.sourceRoom) {
				addCell(cablingSheet, idx + 2, 9, String.valueOf(currentCabling.assetFrom.rackSource?.location + "/" +
						currentCabling.assetFrom.sourceRoom + "/" + currentCabling.assetFrom.sourceRack))
			} else if (currentCabling.assetFrom?.targetRoom) {
				addCell(cablingSheet, idx + 2, 9, String.valueOf(currentCabling.assetFrom.rackTarget?.location + "/" +
						currentCabling.assetFrom.targetRoom + "/" + currentCabling.assetFrom.targetRack))
			} else {
				addCell(cablingSheet, idx + 2, 9, '')
			}
			addCell(cablingSheet, idx + 2, 10, String.valueOf(currentCabling.cableStatus ?: ''))
			addCell(cablingSheet, idx + 2, 11, String.valueOf(currentCabling.assetLoc ?: ''))
			//GormUtil.flushAndClearSession(progressCount)
		}
	}

	/**
	 * Determine the frontEndLabel for the attribute.
	 */
	String getAttributeFrontendLabel(String attributeCode, String frontendLabel) {
		Project project = securityService.userCurrentProject
		return (attributeCode.contains('custom') && project[attributeCode]) ? project[attributeCode] : frontendLabel
	}

	/**
	 * Get the customised query based on the application preference
	 * @param appPref(List of key value column preferences)
	 * @return query,joinQuery
	 */
	def getAppCustomQuery(appPref) {
		String query = ''
		String joinQuery = ''
		for (String value in appPref.values()) {
			switch (value) {
				case 'sme':
					query += "CONCAT(CONCAT(p.first_name, ' '), IFNULL(p.last_name,'')) AS sme,"
					joinQuery += "\n LEFT OUTER JOIN person p ON p.person_id=a.sme_id \n"
					break
				case 'sme2':
					query += "CONCAT(CONCAT(p1.first_name, ' '), IFNULL(p1.last_name,'')) AS sme2,"
					joinQuery += "\n LEFT OUTER JOIN person p1 ON p1.person_id=a.sme2_id \n"
					break
				case 'modifiedBy':
					query += "CONCAT(CONCAT(p2.first_name, ' '), IFNULL(p2.last_name,'')) AS modifiedBy,"
					joinQuery += "\n LEFT OUTER JOIN person p2 ON p2.person_id=ae.modified_by \n"
					break
				case 'lastUpdated':
					query += "ee.last_updated AS $value,"
					joinQuery += "\n LEFT OUTER JOIN eav_entity ee ON ee.entity_id=ae.asset_entity_id \n"
					break
				case 'event':
					query += "me.move_event_id AS event,"
					joinQuery += "\n LEFT OUTER JOIN move_event me ON me.move_event_id=mb.move_event_id \n"
					break
				case 'appOwner':
					query += "CONCAT(CONCAT(p3.first_name, ' '), IFNULL(p3.last_name,'')) AS appOwner,"
					joinQuery += "\n LEFT OUTER JOIN person p3 ON p3.person_id= ae.app_owner_id \n"
					break
				case ~/appVersion|appVendor|appTech|appAccess|appSource|license|businessUnit|appFunction|criticality|userCount|userLocations|useFrequency|drRpoDesc|drRtoDesc|shutdownFixed|moveDowntimeTolerance|testProc|startupProc|url|shutdownBy|shutdownDuration|startupBy|startupFixed|startupDuration|testingBy|testingFixed|testingDuration/:
					query += "a.${WebUtil.splitCamelCase(value)} AS $value,"
					break
				case ~/custom\d{1,3}/:
					query += "ae.$value AS $value,"
					break
				case ~/validation|latency|planStatus|moveBundle/:
					// Handled by the calling routine
					break
				default:
					query +="ae.${WebUtil.splitCamelCase(value)} AS $value,"
			}
		}

		[query: query, joinQuery: joinQuery]
	}

	/**
	 * Returns the default optional/customizable columns
	 * @param prefName - the preference name for the various asset lists
	 * @return appPref
	 */
	// TODO : JPM 9/2014 : Rename getExistingPref method to getColumnPreferences
	Map getExistingPref(String prefName) {
		def colPref
		def existingPref = userPreferenceService.getPreference(prefName)

		if (existingPref) {
			// TODO  JPM 9/2014 : I'm assuming that the JSON.parse function could throw an error if the json is corrupt so there should be a try/catch
			colPref = JSON.parse(existingPref)
		}

		if (!colPref) {
			switch (prefName) {
				case 'App_Columns':
					colPref = ['1': 'sme', '2': 'environment', '3': 'validation', '4': 'planStatus', '5': 'moveBundle']
					break
				case 'Asset_Columns':
					colPref = ['1': 'sourceRack','2': 'environment','3': 'assetTag','4': 'serialNumber','5': 'validation']
					break
				case 'Physical_Columns':
					colPref = ['1': 'sourceRack','2': 'environment','3': 'assetTag','4': 'serialNumber','5': 'validation']
					break
				case 'Database_Columns':
					colPref = ['1': 'dbFormat','2': 'size','3': 'validation','4': 'planStatus','5': 'moveBundle']
					break
				case 'Storage_Columns':
					colPref = ['1': 'fileFormat','2': 'size','3': 'validation','4': 'planStatus','5': 'moveBundle']
					break
				case 'Task_Columns':
					colPref = ['1': 'assetName','2': 'assetType','3': 'assignedTo','4': 'role', '5': 'category']
					break
				case 'Model_Columns':
					colPref = ['1': 'description','2': 'assetType','3': 'powerUse','4': 'modelConnectors']
					break
				case 'Dep_Columns':
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
	 * Add the css for the labels which fieldImportance is 'C','I'
	 */
	Map getHighlightedInfo(forWhom, assetEntity, configMap, projectAttributes = null) {
		def highlightMap = [:]
		(projectService.getFields(forWhom, projectAttributes) + projectService.getCustoms(projectAttributes)).each { f ->
			def configMaps = configMap.config
			if (configMaps.(f.label) in ['C','I'] && (assetEntity && !assetEntity.(f.label))) {
				highlightMap[f.label] = 'highField'
			}
		}
		return highlightMap
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
/*
		def size = assetEntity.assetName?.size() ?: 0
		for (int i = 0; i < size; ++i)
			if (assetEntity.assetName[i] == "'")
				name = name + "\\'"
			else if (ignoreSingleQuotes && assetEntity.assetName[i] == '"')
				name = name + '\\"'
			else
				name = name + assetEntity.assetName[i]
*/
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

	/**
	 * This is the actual method that will generate the Excel export which it typically called by a Quartz
	 * job so that the user request can happen quickly. The job then will post progress updates to report back
	 * to the user referencing the params.key.
	 * The user's system timezone is critical to computing the datetimes in the spreadsheet since the application
	 * runs in GMT but Excel/Apache POI assume that the spreadsheet is in the system timezone. So if we generate a
	 * spreadsheet and stuff a
	 * @param Datatransferset,Project,Movebundle
	 *		params.key - the key to reference for progress status
	 *		params.projectId - the project for which to export assets
	 *		params.bundle - the bundle to export
	 *		params.dataTransferSet -
	 *		params.username - the user that made the request
	 *		params.tzId - the user's timezone (IMPORTANT that this be their system TZ and not their user preference*)
	 * 		params.userDTFormat - the user's Date Format (MIDDLE_ENDIAN, LITTLE_ENDIAN)
	 *		params.asset - flag to export devices
	 *		params.application - flag to export apps
	 *		params.database - flag to export databases
	 *		params.files - flag to export storage
	 *		params.dependency - flag to export dependencies
	 *		params.room - flag to export room details
	 *		params.rack - flag to export rack data
	 *		params.cable - flag to export cabling
	 *		params.comment - flag to export comments
	 **/
	void export(Map params) {
		def key = params.key
		String projectId = params.projectId
		Project project = Project.get(projectId)
		if (!project) {
			progressService.update(key, 100, 'Cancelled', 'Project is required.')
			return
		}

		boolean profilerEnabled = params[Profiler.KEY_NAME] == Profiler.KEY_NAME
		Profiler profiler = Profiler.create(profilerEnabled, key)

		final String mainProfTag = 'EXPORT'
		profiler.beginInfo(mainProfTag)

		// Helper closure that returns the size of an object or zero (0) if it is null
		def sizeOf = { obj -> (obj ? obj.size : 0)}

		try {
			def progressCount = 0
			def progressTotal = 0
			def missingHeader = ""

			//get project Id
			def dataTransferSet = params.dataTransferSet
			def bundleNameList = new StringBuffer()
			UserLogin userLogin = UserLogin.findByUsername(params.username)

			def bundle = params.bundle
			def bundleSize = bundle.size()
			bundleNameList.append(bundle[0] != "" ? (bundleSize == 1 ? MoveBundle.read(bundle[0]).name : bundleSize + 'Bundles') : 'All')

			def dataTransferSetInstance = DataTransferSet.get(dataTransferSet)

			// Maps for each class for mapping spreadsheet column names to the domain properties
			def serverDTAMap, appDTAMap, dbDTAMap, fileDTAMap

			// Will hold the list of the assets for each of the classes
			List assets, applications, databases, files

			def assetEntityInstance

			def serverSheet
			def appSheet
			def dbSheet
			def storageSheet
			def titleSheet
			def exportedEntity = ""

			Map serverMap = [:]
			Map serverSheetColumnNames = [:]
			def serverColumnNameList = []
			Map serverSheetNameMap = [:]
			def serverDataTransferAttributeMapSheetName

			Map appMap = [:]
			Map appSheetColumnNames = [:]
			def appColumnNameList = []
			Map appSheetNameMap = [:]
			def appDataTransferAttributeMapSheetName

			def dbMap = [:]
			def dbSheetColumnNames = [:]
			def dbColumnNameList = []
			def dbSheetNameMap = [:]
			def dbDataTransferAttributeMapSheetName

			def fileMap = [:]
			def storageSheetColumnNames = [:]
			def fileColumnNameList = []
			def storageSheetNameMap = [:]
			def fileDataTransferAttributeMapSheetName

			// Flags to indicate which tabs to export based on which checkboxes selected in the UI
			boolean doDevice = params.asset == 'asset'
			boolean doApp = params.application == 'application'
			boolean doDB = params.database == 'database'
			boolean doStorage = params.files == 'files'
			boolean doDependency = params.dependency == 'dependency'
			boolean doRoom = params.room == 'room'
			boolean doRack = params.rack == 'rack'
			boolean doCabling = params.cabling == 'cable'
			boolean doComment = params.comment == 'comment'

			// Queries for main assets
			String deviceQuery
			String applicationQuery
			String databaseQuery
			String filesQuery

			//
			// Load the asset lists and property map files based on ALL or selected bundles for the classes selected for export
			//
			String query = ' WHERE d.project=:project '
			Map queryParams = [project: project]
			// Setup for multiple bundle selection
			if (bundle[0]) {
				query += " AND d.moveBundle.id IN(:bundleIds)"
				queryParams.bundleIds = bundle*.toLong()
			}

			// The following size related variables are used to update the progress meter.

			int assetSize = 0
			int appSize = 0
			int dbSize = 0
			int fileSize = 0
			int roomSize = 0
			int rackSize = 0
			int commentSize = 0
			int cablingSize = 0
			int dependencySize = 0

			def getAssetList = { String hql, Map queryArgs ->
				Room.executeQuery(hql, queryArgs, [readOnly: true, fetchSize: 1000])
			}

			def countRows = { String hql, Map queryArgs ->
				Room.executeQuery('SELECT COUNT(*) ' + hql, queryArgs)[0]
			}

			profiler.lap(mainProfTag, 'Initial loading')

			if (doDevice) {
				deviceQuery = "FROM AssetEntity d" + query + " AND d.assetClass='" + AssetClass.DEVICE + "'"
				assetSize = countRows(deviceQuery, queryParams)
				progressTotal += assetSize
			}
			if (doApp) {
				applicationQuery = "FROM Application d" + query
				appSize = countRows(applicationQuery, queryParams)
				progressTotal += appSize
			}
			if (doDB) {
				databaseQuery = "FROM Database d" + query
				dbSize = countRows(databaseQuery, queryParams)
				progressTotal += dbSize
			}
			if (doStorage) {
				filesQuery = "FROM Files d" + query
				fileSize = countRows(filesQuery, queryParams)
				progressTotal += fileSize
			}

			if (doDependency) {
				dependencySize  = AssetDependency.executeQuery(
						'SELECT COUNT(*) FROM AssetDependency WHERE asset.project = ?',
						[project])[0]
				progressTotal += dependencySize
			}

			if (doRoom) {
				roomSize = Room.executeQuery("SELECT COUNT(*) FROM Room WHERE project = ?", [project])[0]
				progressTotal += roomSize
			}

			if (doRack) {
				rackSize = Rack.executeQuery("SELECT COUNT(*) FROM Rack WHERE project = ?", [project])[0]
				progressTotal += rackSize
			}

			if (doCabling) {
				cablingSize = AssetCableMap.executeQuery('''
					select count(*) from AssetCableMap acm
					where acm.assetFrom.project = ?''', [project])[0]
				progressTotal += cablingSize
			}

			if (doComment) {
				commentSize = AssetComment.executeQuery('''
					SELECT COUNT(*) FROM AssetComment
					WHERE project = ?
					  AND commentType = 'comment'
					  AND assetEntity IS NOT NULL
				''', [project])[0]
				progressTotal += commentSize
			}

			profiler.lap(mainProfTag, 'Got row counts')

			// This variable is used to determine when to call the updateProgress
			float updateOnPercent = 0.01

			// Used by the profile sampling to determine # of rows to profile and how often within the dataset
			double percentToProfile = 25.0		// Sample 5% of all of data
			double frequencyToProfile = 5.0		// Sample the data every 5% of the way

			// Have to load the maps because we update the column names across the top for all sheets
			serverDTAMap = DataTransferAttributeMap.findAllByDataTransferSetAndSheetName(dataTransferSetInstance,"Devices")
			appDTAMap =  DataTransferAttributeMap.findAllByDataTransferSetAndSheetName(dataTransferSetInstance,"Applications")
			dbDTAMap =  DataTransferAttributeMap.findAllByDataTransferSetAndSheetName(dataTransferSetInstance,"Databases")
			fileDTAMap =  DataTransferAttributeMap.findAllByDataTransferSetAndSheetName(dataTransferSetInstance,"Files")

			def fileExtension = "xlsx"
			if (params.exportFormat == "xls") {
				fileExtension = "xls"
			}

			// Going to use temporary file because we were getting out of memory errors constantly on staging server

			def tzId = params.tzId
			def userDTFormat = params.userDTFormat
			def currDate = TimeUtil.nowGMT()
			def exportDate = TimeUtil.formatDateTimeWithTZ(tzId, userDTFormat, currDate, TimeUtil.FORMAT_DATE_TIME_5)

			profiler.lap(mainProfTag, 'Loaded DTAMaps')

			List assetDepBundleList = AssetDependencyBundle.executeQuery(
					'select asset.id, dependencyBundle from AssetDependencyBundle where project=:project',
					[project: project])
			Map assetDepBundleMap = new HashMap(assetDepBundleList.size())
			assetDepBundleList.each {
				assetDepBundleMap[it[0].toString()] = it[1]
			}
			profiler.lap(mainProfTag, 'Created asset dep bundles')

			Workbook book = ExportUtil.loadSpreadsheetTemplate('/templates/TDSMaster_template.' + fileExtension)
			profiler.lap(mainProfTag, 'Loaded workbook template')

			// Helper closure used to retrieve the sheet
			def getWorksheet = { sheetName ->
				def s = book.getSheet(sheetName)
				if (s) {
					return s
				} else {
					throw new RuntimeException("Unable to find sheet $sheetName in the uploaded spreadsheet")
				}
			}

			//
			// Load maps appropriately based on if we're exporting the tab
			//
			def serverCol, appCol, dbCol, filesCol

			// TODO : JPM : The following 4 blocks of code should be able to be reduced to a shared closure

			// Device
			serverDTAMap.eachWithIndex { item, pos ->
				serverMap[item.columnName] = null
				serverColumnNameList.add(item.columnName)
				serverSheetNameMap.sheetName = item.sheetName?.trim()
			}
			serverMap.DepGroup = null
			serverColumnNameList.add("DepGroup")
			serverSheet = getWorksheet('Devices')
			serverCol = serverSheet.getRow(0).getLastCellNum()
			for (int c = 0; c < serverCol; c++) {
				def serverCellContent = serverSheet.getRow(0).getCell(c).getStringCellValue()
				serverSheetColumnNames[serverCellContent] = c
				if (serverMap.containsKey(serverCellContent)) {
					serverMap[serverCellContent] = c
				}
			}

			// Application
			appDTAMap.eachWithIndex { item, pos ->
				appMap[item.columnName] = null
				appColumnNameList << item.columnName
				appSheetNameMap.sheetName = item.sheetName?.trim()
			}
			appMap.DepGroup = null
			appColumnNameList << "DepGroup"
			appSheet = getWorksheet('Applications')
			appCol = appSheet.getRow(0).getLastCellNum()
			for (int c = 0; c < appCol; c++) {
				def appCellContent = appSheet.getRow(0).getCell(c).getStringCellValue()
				appSheetColumnNames[appCellContent] = c
				if (appMap.containsKey(appCellContent)) {
					appMap[appCellContent] = c
				}
			}

			// Database
			dbDTAMap.eachWithIndex { item, pos ->
				dbMap[item.columnName] = null
				dbColumnNameList << item.columnName
				dbSheetNameMap.sheetName = item.sheetName?.trim()
			}
			dbMap.DepGroup = null
			dbColumnNameList << "DepGroup"
			dbSheet = getWorksheet('Databases')
			dbCol = dbSheet.getRow(0).getLastCellNum()
			for (int c = 0; c < dbCol; c++) {
				def dbCellContent = dbSheet.getRow(0).getCell(c).getStringCellValue()
				dbSheetColumnNames[dbCellContent] = c
				if (dbMap.containsKey(dbCellContent)) {
					dbMap[dbCellContent] = c
				}
			}

			// Storage
			fileDTAMap.eachWithIndex { item, pos ->
				fileMap[item.columnName] = null
				fileColumnNameList << item.columnName
				storageSheetNameMap.sheetName = item.sheetName?.trim()
			}
			fileMap.DepGroup = null
			fileColumnNameList.add("DepGroup")
			storageSheet = getWorksheet('Storage')
			filesCol = storageSheet.getRow(0).getLastCellNum()
			for (int c = 0; c < filesCol; c++) {
				def fileCellContent = storageSheet.getRow(0).getCell(c).getStringCellValue()
				storageSheetColumnNames[fileCellContent] = c
				if (fileMap.containsKey(fileCellContent)) {
					fileMap[fileCellContent] = c
				}
			}

			profiler.lap(mainProfTag, 'Read Spreadsheet Tabs')

			// Helper closure to create a text list from an array for debugging
			def xportList = { list ->
				def out = ''
				def x = 0
				list.each { out += "$x=$it\n"; x++}
				return out
			}

			//calling method to check for Header
			def serverCheckCol = checkHeader(serverColumnNameList, serverSheetColumnNames, missingHeader)
			def appCheckCol = checkHeader(appColumnNameList, appSheetColumnNames, missingHeader)
			def dbCheckCol = checkHeader(dbColumnNameList, dbSheetColumnNames, missingHeader)
			def filesCheckCol = checkHeader(fileColumnNameList, storageSheetColumnNames, missingHeader)

			profiler.lap(mainProfTag, 'Validated headers')

			// Statement to check Headers if header are not found it will return Error message
			if (serverCheckCol == false || appCheckCol == false || dbCheckCol == false || filesCheckCol == false) {
				missingHeader = missingHeader.replaceFirst(",","")

				progressService.update(key, 100, 'Cancelled', " Column Headers : $missingHeader not found, Please check it.")

				return
			} else {
				//Add Title Information to master SpreadSheet
				titleSheet = book.getSheet("Title")
				if (titleSheet != null) {
					WorkbookUtil.addCell(titleSheet, 1, 2, project.client.toString())
					WorkbookUtil.addCell(titleSheet, 1, 3, projectId)
					WorkbookUtil.addCell(titleSheet, 2, 3, project.name)
					WorkbookUtil.addCell(titleSheet, 1, 4, partyRelationshipService.getProjectManagers(project))
					WorkbookUtil.addCell(titleSheet, 1, 5, bundleNameList.toString())
					WorkbookUtil.addCell(titleSheet, 1, 6, userLogin.person.toString())

					def exportedOn = TimeUtil.formatDateTimeWithTZ(tzId, userDTFormat, new Date(), TimeUtil.FORMAT_DATE_TIME_22)
					WorkbookUtil.addCell(titleSheet, 1, 7, exportedOn)
					WorkbookUtil.addCell(titleSheet, 1, 8, tzId)
					WorkbookUtil.addCell(titleSheet, 1, 9, userDTFormat)

					WorkbookUtil.addCell(titleSheet, 30, 0, 'Note: All times are in ' + (tzId ?: 'EDT') + ' time zone')
				}
				profiler.lap(mainProfTag, 'Updated title sheet')

				//update data from Asset Entity table to EXCEL

				def serverColumnNameListSize = sizeOf(serverColumnNameList)
				def appcolumnNameListSize = sizeOf(appColumnNameList)
				def dbcolumnNameListSize = sizeOf(dbColumnNameList)
				def filecolumnNameListSize = fileColumnNameList.size()

				// update column header
				updateColumnHeaders(serverSheet, serverDTAMap, serverSheetColumnNames, project)
				updateColumnHeaders(appSheet, appDTAMap, appSheetColumnNames, project)
				updateColumnHeaders(dbSheet, dbDTAMap, dbSheetColumnNames, project)
				updateColumnHeaders(storageSheet, fileDTAMap, storageSheetColumnNames, project)

				profiler.lap(mainProfTag, 'Updating spreadsheet headers')

				def validationSheet = getWorksheet("Validation")

				Map optionsSize = [:]

				def writeValidationColumn = { assetOptions, col, optionKey ->
					assetOptions.eachWithIndex { option, int idx ->
						WorkbookUtil.addCell(validationSheet, col, idx + 1, option.value as String)
					}
					optionsSize[optionKey] = assetOptions.size() + 1 // +1 because of the header.
				}

				def writeValidationSheet = {
					profiler.beginInfo "Validations"
					profiler.lap("Validations", "Starting to export Validation values")
					int col = 0
					writeValidationColumn(getAssetEnvironmentOptions(), col++, "Environment")
					writeValidationColumn(getAssetPriorityOptions(), col++, "Priority")
					writeValidationColumn(getAssetPlanStatusOptions(), col++, "PlanStatus")
					writeValidationColumn(getDependencyTypes(), col++, "DepType")
					writeValidationColumn(getDependencyStatuses(), col, "DepStatus")
					WorkbookUtil.makeSheetReadOnly(validationSheet)
					profiler.lap("Validations", "Finished exporting Validation values")
					profiler.endInfo("Validations", "Finished writing validation values to sheet.")
				}

				writeValidationSheet()

				def calcProfilerCriteria = { datasetSize ->
					int sampleQty = 1
					int sampleModulus = 1
					// int numOfSampleSets = (int)(1/frequencyToProfile)
					int numOfSampleSets = Math.round(100/frequencyToProfile)

					if (datasetSize > 0) {
						sampleQty = Math.round((datasetSize * percentToProfile / 100) / numOfSampleSets)
						if (sampleQty == 0) {
							sampleQty = 1
						}

						// sampleModulus = (int)(datasetSize / numOfSampleSets)
						sampleModulus = Math.floor(datasetSize / numOfSampleSets)
						if (sampleModulus == 0) {
							sampleModulus = datasetSize
						}
					}

					return [sampleQty, sampleModulus]
				}

				// The threshold (milliseconds) to warning on when the processing time is exceeded for a row
				Map profileRowThresholds = [
						(AssetClass.DEVICE): 30,
						(AssetClass.APPLICATION): 30
				]

				// The maximum # of row level threshold violations to log
				final int profileThresholdLogLimit = 1000

				// Will log if setting a field exceeds this threadhold (msec)
				final int profileThresholdSettingField = 2

				final String thresholdWarnMsg = 'A total of %d row(s) exceeded the duration threshold of %d ms'

				// The following variables are used to control the profiling behavior
				//
				// Counter used to count the number of threshold violations
				int profileThresholdViolations
				int profileHighwaterMark = 0
				String profileHighwaterAsset = ''
				int profileSampleQty
				int profileSampleModulus
				int profileSamplingTics
				long lapDuration
				boolean profilingRow
				Map durationMatrix = [:]

				profiler.log(Profiler.LOG_TYPE.INFO, 'Initialization took (%s)', [profiler.getSinceStart(mainProfTag)])

				//
				// Device Export
				//
				if (doDevice) {
					profiler.beginInfo "Devices"

					profiler.lap("Devices", "Devices Started")

					WorkbookUtil.addCellValidation(validationSheet, serverSheet, 0, 1, optionsSize.Environment, serverMap["Environment"], 1, assetSize)
					WorkbookUtil.addCellValidation(validationSheet, serverSheet, 1, 1, optionsSize.Priority, serverMap["Priority"], 1, assetSize)
					WorkbookUtil.addCellValidation(validationSheet, serverSheet, 2, 1, optionsSize.PlanStatus, serverMap["PlanStatus"], 1, assetSize)

					profiler.lap("Devices", "Validations added")

					exportedEntity += 'S'
					int deviceCount = 0
					profileThresholdViolations = 0
					profilingRow = false

					String silentTag = 'DevRowSilent'

					if (profilerEnabled) {
						(profileSampleQty, profileSampleModulus) = calcProfilerCriteria(assetSize)
						// log.debug "Devices Export profileSampleQty=$profileSampleQty, profileSampleModulus=$profileSampleModulus, assetSize=$assetSize"
					}

					profiler.lap('Devices', 'Entering while loop')
					assets = getAssetList(deviceQuery, queryParams)

					assets.each { currentAsset ->

						profiler.beginSilent(silentTag)

						if (profilerEnabled) {
							if (deviceCount == 0 || (deviceCount % profileSampleModulus == 0)) {
								profilingRow = true
								profileSamplingTics = profileSampleQty
								profiler.lapReset('Devices')
							}

							if (profilingRow) {
								if (profileSamplingTics == 0) {
									profilingRow = false
								} else {
									profileSamplingTics--
									profiler.begin 'Device Row'
								}
							}
						}

						deviceCount++
						progressCount++

						if (profilingRow) {
							profiler.lap('Devices', 'Read device %d of %d, id=%d', [deviceCount, assetSize, currentAsset.id])
						}

						updateProgress(key, progressCount, progressTotal, 'In progress', updateOnPercent)

						// Add assetId for walkthrough template only.
						if (serverSheetColumnNames.containsKey("assetId")) {
							addCell(serverSheet, deviceCount, 0, currentAsset.id, Cell.CELL_TYPE_NUMERIC)
						}

						if (profilingRow) {
							profiler.lap('Devices', 'Update Progress')
							profiler.begin('Device Fields')
						}

						for (int coll = 0; coll < serverColumnNameListSize; coll++) {

							def attribute = serverDTAMap.eavAttribute.attributeCode[coll]
							def colName = serverColumnNameList.get(coll)
							def colNum = serverMap[colName]
							def a = currentAsset

							//if (deviceCount == 1)
							//	log.debug "Device Export - attribute=$attribute, colName=$colName, colNum=$colNum"

							if (profilingRow) {
								// log.debug "SET VAR TIME = " + profiler.getLapDuration('Devices').toMilliseconds()
								lapDuration = profiler.getLapDuration('Devices').toMilliseconds()
								if (lapDuration > profileThresholdSettingField) {
									profiler.log(Profiler.LOG_TYPE.INFO, 'Set var %s (%s msec)', [colName, lapDuration.toString()])
								} else {
									profiler.lapReset('Devices')
								}
							}

							def propValue = null
							if (attribute) {
								propValue = a.(serverDTAMap.eavAttribute.attributeCode[coll])
							}
							// (attribute && a.(serverDTAMap.eavAttribute.attributeCode[coll]) == null)
							if (!(propValue == null || ((propValue instanceof String) && propValue.size() == 0))) {
								switch(colName) {
									case 'DepGroup':
										// TODO : JPM 9/2014 : Should load the dependency bundle list into memory so we don't do queries for each record
										addCell(serverSheet, deviceCount, colNum, assetDepBundleMap[a.id.toString()])
										break
									case ~/usize|SourcePos|TargetPos/:
										def pos = a[attribute] ?: 0
										// Don't bother populating position if it is a zero
										if (pos == 0)
											continue
										addCell(serverSheet, deviceCount, colNum, (Double)pos, Cell.CELL_TYPE_NUMERIC)
										break

									case ~/Retire|MaintExp/:
										addCell(serverSheet, deviceCount, colNum,
												TimeUtil.formatDate(userDTFormat, a[attribute], TimeUtil.FORMAT_DATE_TIME_12))
										break

									case ~/Modified Date/:
										if (a[attribute]) {
											addCell(serverSheet, deviceCount, colNum,
													TimeUtil.formatDateTimeWithTZ(tzId, userDTFormat, a[attribute],
															TimeUtil.FORMAT_DATE_TIME_2))
										}
										break

									case ~/Source Blade|Target Blade/:
										def chassis = a[attribute]
										def value = ""
										if (chassis) {
											value = "id:" + chassis.id + " " + chassis.assetName
										}
										addCell(serverSheet, deviceCount, colNum, value)
										break

									default:
										def value = StringUtil.defaultIfEmpty(String.valueOf(a[attribute]), '')
										addCell(serverSheet, deviceCount, colNum, value)
								}
							}

							if (profilingRow) {
								lapDuration = profiler.getLapDuration('Devices').toMilliseconds()
								if (lapDuration > 2) {
									profiler.log(Profiler.LOG_TYPE.INFO, 'Set CELL %s (%s msec)', [colName, lapDuration.toString()])
								} else {
									profiler.lapReset('Devices')
								}
							}
						}

						if (profilingRow) {
							profiler.end('Device Fields')
							profiler.end('Device Row')
						}

						if (profilerEnabled) {
							// If the row duration exceeds the threshold we'll report it
							lapDuration = profiler.getLapDuration(silentTag).toMilliseconds()
							if (lapDuration > profileRowThresholds[AssetClass.DEVICE]) {
								profileThresholdViolations++
								profiler.log(Profiler.LOG_TYPE.WARN, "Asset '$currentAsset.assetName' (id:$currentAsset.id) exceeded duration threshold ($lapDuration msec)")
								if (lapDuration > profileHighwaterMark) {
									// Log each row that bumps up the highwater mark
									profileHighwaterMark = lapDuration
									profileHighwaterAsset = "$currentAsset.assetName (id:$currentAsset.id)"
								}
							}

							// Compute the Duration Matrix to categorize all of the rows
							String durationGroup = (((int) Math.floor(lapDuration / 10)) * 10).toString()
							if (!durationMatrix.containsKey(durationGroup)) {
								durationMatrix[durationGroup] = [count: 1, duration: lapDuration, average: lapDuration]
							} else {
								durationMatrix[durationGroup].count++
								durationMatrix[durationGroup].duration += lapDuration
								durationMatrix[durationGroup].average = (int)(durationMatrix[durationGroup].duration / durationMatrix[durationGroup].count)
							}
						}

						profiler.endSilent(silentTag)
					} // assets.each
					assets = null
					profiler.endInfo("Devices", "processed %d rows", [assetSize])
					if (profileThresholdViolations > 0) {
						profiler.log(Profiler.LOG_TYPE.WARN, thresholdWarnMsg, [profileThresholdViolations, profileRowThresholds[AssetClass.DEVICE]])
					}

					if (profilerEnabled) {
						if (profileHighwaterAsset) {
							profiler.log(Profiler.LOG_TYPE.WARN, "Asset $profileHighwaterAsset set the highwater mark ($profileHighwaterMark msec)")
						}

						int durSum = durationMatrix.values().sum { it.duration }
						if (durSum > 0) {
							durationMatrix.each {k,v ->
								durationMatrix[k].perc = Math.round(v.duration/durSum*100)
							}
						}
						profiler.log(Profiler.LOG_TYPE.INFO, "Duration Matrix: $durationMatrix")
					}
				} else {
					// Add validations for the first row (which is blank)
					WorkbookUtil.addCellValidation(validationSheet, serverSheet, 0, 1, optionsSize.Environment,
							serverMap["Environment"], 1, 1)
					WorkbookUtil.addCellValidation(validationSheet, serverSheet, 1, 1, optionsSize.Priority,
							serverMap["Priority"], 1, 1)
					WorkbookUtil.addCellValidation(validationSheet, serverSheet, 2, 1, optionsSize.PlanStatus,
							serverMap["PlanStatus"], 1, 1)
				}

				//
				// Application Export
				//
				// profiler.lapInfo("EXPORT")
				if (doApp) {
					//log.info "export() starting export of Applications"
					profiler.beginInfo "Applications"

					profiler.lap("Applications", "Adding Validations")

					WorkbookUtil.addCellValidation(validationSheet, appSheet, 0, 1, optionsSize.Environment,
							appSheetColumnNames["Environment"], 1, appSize)
					WorkbookUtil.addCellValidation(validationSheet, appSheet, 2, 1, optionsSize.PlanStatus,
							appSheetColumnNames["PlanStatus"], 1, appSize)

					profiler.lap("Applications", "Validations added.")

					exportedEntity += 'A'

					applications = getAssetList(applicationQuery, queryParams)

					// This determines which columns are added as Number vs Label
					def numericCols = []
					def stringCols = ['Version']

					// Flag to know if the AppId Column exists
					def idColName = 'appId'
					def hasIdCol = appSheetColumnNames.containsKey(idColName)

					int applicationCount = 0
					//applications.each { app ->
					applications.each { app ->
						progressCount++
						/*
						if (progressCount < 10) {
							profiler.lap("Applications", "applications %d of %d", [progressCount, progressTotal])
						}
						*/

						updateProgress(key, progressCount, progressTotal, 'In progress', updateOnPercent)
						applicationCount++

						// Add the appId column to column 0 if it exists
						if (hasIdCol) {
							addCell(appSheet, applicationCount, appSheetColumnNames[idColName], (Double)(app.id), Cell.CELL_TYPE_NUMERIC)
						}

						for (int i=0; i < appColumnNameList.size(); i++) {
							def colName = appColumnNameList[i]

							// If the column isn't in the spreadsheet we'll skip over it
							if (!appSheetColumnNames.containsKey(colName)) {
								log.info "export() : skipping column $colName that is not in spreadsheet"
								continue
							}

							// Get the column number in the spreadsheet that contains the column name
							def colNum = appSheetColumnNames[colName]

							// Get the asset column name via the appDTAMap which is indexed to match the appColumnNameList
							def assetColName = appDTAMap.eavAttribute.attributeCode[i]

							def colVal = ''
							switch(colName) {
								case 'AppId':
									colVal = app.id
									break
								case 'AppOwner':
									colVal = app.appOwner
									break
								case 'DepGroup':
									// Find the Dependency Group that this app is bound to
									colVal = assetDepBundleMap[app.id.toString()]
									break
								case ~/ShutdownBy|StartupBy|TestingBy/:
									colVal = app[assetColName] ? resolveByName(app[assetColName], false)?.toString() : ''
									break
								case ~/ShutdownFixed|StartupFixed|TestingFixed/:
									colVal = app[assetColName] ? 'Yes' : 'No'
									//log.info "export() : field class type=$app[assetColName].className()}"
									break
								case ~/Retire|MaintExp/:
									colVal = TimeUtil.formatDate(userDTFormat, app[assetColName], TimeUtil.FORMAT_DATE_TIME_12)
									break
								case ~/Modified Date/:
									colVal = TimeUtil.formatDateTimeWithTZ(tzId, userDTFormat, app[assetColName], TimeUtil.FORMAT_DATE_TIME_2)
									break
								default:
									colVal = app[assetColName]
							}

							if (!(colVal == null || ((colVal instanceof String) && colVal.size() == 0))) {
								if (colVal?.class?.name == 'Person') {
									colVal = colVal.toString()
								}

								if (numericCols.contains(colName)) {
									addCell(appSheet, applicationCount, colNum, (Double)colVal, Cell.CELL_TYPE_NUMERIC)
								}
								else if (stringCols.contains(colName)) {
									addCell(appSheet, applicationCount, colNum, colVal.toString(), Cell.CELL_TYPE_STRING)
								}
								else {
									addCell(appSheet, applicationCount, colNum, colVal.toString())
								}
							}
						}
						//GormUtil.flushAndClearSession(progressCount)
					}

					applications = null
					profiler.endInfo("Applications", "processed %d rows", [appSize])
				} else {
					// Add validation for the first row
					WorkbookUtil.addCellValidation(validationSheet, appSheet, 0, 1, optionsSize.Environment,
							appSheetColumnNames["Environment"], 1, 1)
					WorkbookUtil.addCellValidation(validationSheet, appSheet, 2, 1, optionsSize.PlanStatus,
							appSheetColumnNames["PlanStatus"], 1, 1)
				}
				// profiler.lapInfo("EXPORT")

				//
				// Database
				//
				if (doDB) {
					profiler.beginInfo "Databases"
					profiler.lap("Databases", "Adding Validations")

					WorkbookUtil.addCellValidation(validationSheet, dbSheet, 0, 1, optionsSize.Environment,
							dbMap["Environment"], 1, dbSize)
					WorkbookUtil.addCellValidation(validationSheet, dbSheet, 2, 1, optionsSize.PlanStatus,
							dbMap["PlanStatus"], 1, dbSize)

					profiler.lap("Databases", "Validations added.")

					exportedEntity += "D"
					int databaseCount = 0
					databases = getAssetList(databaseQuery, queryParams)
					//for (int r = 1; r <= dbSize; r++) {
					databases.each { currentDatabase ->
						progressCount++
						databaseCount++
						updateProgress(key, progressCount, progressTotal, 'In progress', updateOnPercent)
						//Add assetId for walkthrough template only.
						if (dbSheetColumnNames.containsKey("dbId")) {
							addCell(dbSheet, databaseCount, 0, (currentDatabase.id), Cell.CELL_TYPE_NUMERIC)
						}
						for (int coll = 0; coll < dbcolumnNameListSize; coll++) {
							def addContentToSheet
							def attribute = dbDTAMap.eavAttribute.attributeCode[coll]
							//if attributeCode is sourceTeamMt or targetTeamMt export the teamCode
							def colName = dbColumnNameList.get(coll)
							if (colName == "DepGroup") {
								addCell(dbSheet, databaseCount, dbMap[colName], assetDepBundleMap[currentDatabase.id.toString()])
							} else if (attribute in ['retireDate', 'maintExpDate', 'lastUpdated']) {
								def dateValue = currentDatabase.(dbDTAMap.eavAttribute.attributeCode[coll])
								if (dateValue) {
									if (attribute == 'lastUpdated') {
										dateValue = TimeUtil.formatDateTimeWithTZ(tzId, userDTFormat, dateValue, TimeUtil.FORMAT_DATE_TIME_12)
									} else {
										dateValue = TimeUtil.formatDate(userDTFormat, dateValue, TimeUtil.FORMAT_DATE_TIME_12)
									}
								} else {
									dateValue =''
								}
								addCell(dbSheet, databaseCount, dbMap[colName], dateValue)
							} else {
							}
						}
						//GormUtil.flushAndClearSession(progressCount)

					}
					databases = null
					profiler.endInfo("Databases", "processed %d rows", [dbSize])
				} else {
					// Adds validation to just the first row
					WorkbookUtil.addCellValidation(validationSheet, dbSheet, 0, 1, optionsSize.Environment,
							dbMap["Environment"], 1, 1)
					WorkbookUtil.addCellValidation(validationSheet, dbSheet, 2, 1, optionsSize.PlanStatus,
							dbMap["PlanStatus"], 1, 1)
				}

				//
				// Storage (files)
				//
				if (doStorage) {
					profiler.beginInfo "Logical Storage"

					profiler.lap("Logical Storage", "Adding Validations")

					WorkbookUtil.addCellValidation(validationSheet, storageSheet, 0, 1, optionsSize.Environment,
							fileMap["Environment"], 1, fileSize)
					WorkbookUtil.addCellValidation(validationSheet, storageSheet, 2, 1, optionsSize.PlanStatus,
							fileMap["PlanStatus"], 1, fileSize)

					profiler.lap("Logical Storage", "Validations added.")

					exportedEntity += "F"
					files = getAssetList(filesQuery, queryParams)

					//for (int r = 1; r <= fileSize; r++) {
					int filesCount = 0
					files.each { currentFile ->
						progressCount++
						filesCount++
						updateProgress(key, progressCount, progressTotal, 'In progress', updateOnPercent)

						// Add assetId for walkthrough template only.
						if (storageSheetColumnNames.containsKey("filesId")) {
							addCell(storageSheet, filesCount, 0, (currentFile.id), Cell.CELL_TYPE_NUMERIC)
						}

						for (int coll = 0; coll < filecolumnNameListSize; coll++) {
							def attribute = fileDTAMap.eavAttribute.attributeCode[coll]
							def colName = fileColumnNameList.get(coll)
							if (colName == "DepGroup") {
								addCell(storageSheet, filesCount, fileMap[colName], assetDepBundleMap[currentFile.id.toString()])
							} else if (attribute in ['retireDate', 'maintExpDate', 'lastUpdated']) {
								def dateValue = currentFile.(fileDTAMap.eavAttribute.attributeCode[coll])
								if (dateValue) {
									if (attribute == 'lastUpdated') {
										dateValue = TimeUtil.formatDateTimeWithTZ(tzId, userDTFormat, dateValue, TimeUtil.FORMAT_DATE_TIME_12)
									} else {
										dateValue = TimeUtil.formatDate(userDTFormat, dateValue, TimeUtil.FORMAT_DATE_TIME_12)
									}
								} else {
									dateValue =''
								}
								addCell(storageSheet, filesCount, fileMap[colName], dateValue)
							} else {
								def prop = currentFile[attribute]
								if (!(prop == null || ((prop instanceof String) && prop.size() == 0))) {
									addCell(storageSheet, filesCount, fileMap[colName], String.valueOf(prop))
								}
							}
						}

						//GormUtil.flushAndClearSession(progressCount)
					}
					files = null
					profiler.endInfo("Logical Storage", "processed %d rows", [fileSize])
				} else {
					// Adds validations to the first row
					WorkbookUtil.addCellValidation(validationSheet, storageSheet, 0, 1, optionsSize.Environment,
							fileMap["Environment"], 1, 1)
					WorkbookUtil.addCellValidation(validationSheet, storageSheet, 2, 1, optionsSize.PlanStatus,
							fileMap["PlanStatus"], 1, 1)
				}

				//
				// Dependencies
				//
				def dependencySheet = getWorksheet('Dependencies')
				List depProjectionFields = [
						"id",
						"asset.id",
						"a.assetName",
						"a.assetType",
						"d.id",
						"d.assetName",
						"d.assetType",
						"type",
						"dataFlowFreq",
						"dataFlowDirection",
						"status",
						"comment",
						"c1",
						"c2",
						"c3",
						"c4"
				]
				if (doDependency) {
					profiler.beginInfo "Dependencies"
					exportedEntity += "X"

					profiler.lap("Dependencies", "Adding Validations")
					WorkbookUtil.addCellValidation(validationSheet, dependencySheet, 3, 1, optionsSize.DepType,
							depProjectionFields.indexOf("type"), 1, dependencySize)
					WorkbookUtil.addCellValidation(validationSheet, dependencySheet, 4, 1, optionsSize.DepStatus,
							depProjectionFields.indexOf("status"), 1, dependencySize)
					profiler.lap("Dependencies", "Validations added.")

					List results = AssetDependency.createCriteria().list {
						createAlias("asset", "a")
						createAlias("dependent", "d")
						and {
							eq("a.project", project)
						}
						projections {
							depProjectionFields.each {
								property(it)
							}
						}

						fetchSize 1000
						readOnly true
					}

					results.eachWithIndex { dependency, int r ->
						progressCount++
						updateProgress(key, progressCount, progressTotal, 'In progress', updateOnPercent)

						for (int i = 0; i < depProjectionFields.size(); i++) {
							def prop = dependency[i]
							if (!(prop == null || ((prop instanceof String) && prop.size() == 0))) {
								addCell(dependencySheet, r + 1, i, prop)
							}
						}
						//GormUtil.flushAndClearSession(progressCount)
					}
					results = null
					profiler.endInfo("Dependencies", "processed %d rows", [dependencySize])
				} else {
					// Adds validations to the first dependency row
					WorkbookUtil.addCellValidation(validationSheet, dependencySheet, 3, 1, optionsSize.DepType,
							depProjectionFields.indexOf("type"), 1, 1)
					WorkbookUtil.addCellValidation(validationSheet, dependencySheet, 4, 1, optionsSize.DepStatus,
							depProjectionFields.indexOf("status"), 1, 1)
				}

				//
				// Room Export
				//
				if (doRoom) {
					profiler.beginInfo "Rooms"

					exportedEntity += "R"

					def roomSheet = getWorksheet('Room')

					List projectionFields = [
							"id",
							"roomName",
							"location",
							"roomDepth",
							"roomWidth",
							"source",
							"address",
							"city",
							"country",
							"stateProv",
							"postalCode",
							"dateCreated",
							"lastUpdated"
					]

					// These fields can be exported as they are. 5 is intentionally omitted.
					List regularFields = [0, 1, 2, 3, 4, 6, 7, 8, 9, 10]
					// These fields are dates and need to be converted to the user's TZ.
					List dateFields = [11, 12]

					List results = Room.createCriteria().list{

						and{
							eq("project", project)
						}

						projections{
							projectionFields.each {
								property(it)
							}
						}

						fetchSize 1000
						readOnly true
					}

					results.eachWithIndex{ room, r ->
						progressCount++
						updateProgress(key, progressCount, progressTotal, 'In progress', updateOnPercent)
						// Export most fields.
						regularFields.each { col ->
							def prop = room[col]
							if (!(prop == null || ((prop instanceof String) && prop.size() == 0))) {
								addCell(roomSheet, r + 1, col, String.valueOf(prop?:""))
							}

						}
						// Export date fields using the user's TZ.
						dateFields.each { col->
							addCell(roomSheet, r, col, TimeUtil.formatDateTimeWithTZ(tzId, userDTFormat, room[col], TimeUtil.FORMAT_DATE))
						}
						// Export 'source' or 'target' accordingly.
						addCell(roomSheet, r, 5, String.valueOf(room[5] == 1 ? "Source" : "Target"))
						//GormUtil.flushAndClearSession(progressCount)
					}
					profiler.endInfo("Rooms", "processed %d rows", [roomSize])
					results = null
				}

				//
				// Rack Export
				//
				if (doRack) {
					profiler.beginInfo "Racks"
					exportedEntity +="r"

					def rackSheet = getWorksheet('Rack')

					def racks = Rack.createCriteria().list{
						and {
							eq("project", project)
						}

						resultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
						fetchSize 1000
						readOnly true
					}

					def rackCol = rackSheet.getRow(0).getLastCellNum()
					def rackSheetColumns = []

					for (int c = 0; c < rackCol; c++) {
						def rackCellContent = rackSheet.getRow(0).getCell(c).getStringCellValue()
						rackSheetColumns << rackCellContent
					}

					racks.eachWithIndex{ rack, idx ->
						def currentRack = rack.get(Criteria.ROOT_ALIAS)
						progressCount++
						updateProgress(key, progressCount, progressTotal, 'In progress', updateOnPercent)

						rackSheetColumns.eachWithIndex{column, i->
							def addContentToSheet
							if (column == 'rackId') {
								addCell(rackSheet, idx + 1, 0, (racks[idx].id), Cell.CELL_TYPE_NUMERIC)
							} else {
								def prop = currentRack[RACK_MAP[column]]
								if (column == "Source") {
									addCell(rackSheet, idx + 1, i, String.valueOf(prop == 1 ? "Source" : "Target"))
								}
								else {
									if (!(prop == null || ((prop instanceof String) && prop.size() == 0))) {
										addCell(rackSheet, idx + 1, i, String.valueOf(prop))
									}
								}
							}
						}
						//GormUtil.flushAndClearSession(progressCount)
					}
					racks = null
					profiler.endInfo("Racks", "processed %d rows", [rackSize])
				}
			}

			//
			// Cabling Export
			//
			if (doCabling) {
				profiler.beginInfo "Cabling"

				exportedEntity += "c"

				if (cablingSize > 0) {
					def cablingSheet = getWorksheet("Cabling")

					//def cablingList = AssetCableMap.findAll("from AssetCableMap acm where acm.assetFrom.project.id=?", [project.id], [readOnly:true])
					def cablingList = AssetCableMap.createCriteria().list{
						createAlias("assetFrom", "af")
						and{
							eq("af.project", project)
						}
						resultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
						fetchSize 1000
					}

					log.debug("export() Cabling found ${sizeOf(cablingList)} mappings")

					cablingReportData(cablingList, cablingSheet, progressCount, progressTotal, updateOnPercent, key)
					progressCount += cablingSize
					cablingList = null
				}
				profiler.endInfo("Cabling", "processed %d rows", [cablingSize])
			}

			//
			// Comments Export
			//
			if (doComment) {
				profiler.beginInfo "Comments"

				exportedEntity += "M"

				if (commentSize > 0) {
					def commentSheet = getWorksheet("Comments")

					List commentList = AssetComment.createCriteria().list {
						and {
							eq('project', project)
							eq('commentType', 'comment')
							isNotNull('assetEntity')

						}
						resultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
						setReadOnly true
						fetchSize 1000
					}

					def assetId
					def createdDate
					def createdBy
					def commentId
					def category
					def comment

					commentList.eachWithIndex{ comm, idx ->
						def currentComment = comm.get(Criteria.ROOT_ALIAS)
						progressCount++
						updateProgress(key, progressCount, progressTotal, 'In progress', updateOnPercent)
						def dateCommentCreated = ''
						if (currentComment.dateCreated) {
							dateCommentCreated = TimeUtil.formatDateTimeWithTZ(tzId, userDTFormat, currentComment.dateCreated, TimeUtil.FORMAT_DATE)
						}

						addCell(commentSheet, idx, 0, String.valueOf(currentComment.id))
						addCell(commentSheet, idx, 1, currentComment.assetEntity.id, Cell.CELL_TYPE_NUMERIC)
						addCell(commentSheet, idx, 2, String.valueOf(currentComment.category))
						addCell(commentSheet, idx, 3, String.valueOf(dateCommentCreated))
						addCell(commentSheet, idx, 4, String.valueOf(currentComment.createdBy))
						addCell(commentSheet, idx, 5, String.valueOf(currentComment.comment))
						//GormUtil.flushAndClearSession(progressCount)
					}

					commentList = null
				}

				profiler.endInfo("Comments", "processed %d rows", [commentSize])
			}

			//
			// Wrap up the process
			//
			profiler.lap(mainProfTag, 'All sheets populated')

			profiler.begin("RECALCULATE_FORMULAS")
			book.setForceFormulaRecalculation(true)
			profiler.end("RECALCULATE_FORMULAS")

			File tempExportFile = File.createTempFile("assetEntityExport_" + UUID.randomUUID(), null)
			progressService.updateData(key, 'filename', tempExportFile.getAbsolutePath())
			ExportUtil.writeToFile book, tempExportFile

			// The filename will consiste of the following:
			//    - Owner of the Project
			//    - Name of the Project or ID
			//    - Bundle name(s) selected or ALL
			//    - Letters symbolizing each of the tabs that were exported
			//    - The date that the spreadsheet was exported
			//	  - The file extension to use
			String filename = project.owner.toString() + '-' +
					(project.name ?: project.id) + '-' + bundleNameList + '-' + exportedEntity + '-' + exportDate + '.' + fileExtension

			filename = StringUtil.sanitizeAndStripSpaces(filename)

			def contenttype = grailsApplication.config.grails.mime.types[fileExtension]
			progressService.updateData(key, 'contenttype', contenttype)
			progressService.updateData(key, 'header', 'attachment; filename="' + filename + '"')
			progressService.update(key, 100, 'Completed')

			profiler.lap(mainProfTag, "Streamed spreadsheet to browser")
		} catch (e) {
			log.error("Error: $e.message", e)
			progressService.update(key, 100, 'Cancelled', "An unexpected exception occurred while exporting to Excel")
		} finally {
			profiler.endInfo(mainProfTag, "FINISHED")
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

		// def prefType = (listType == 'server') ? 'Asset_Columns' : 'Physical_Columns'
		def prefType = 'Asset_Columns'
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
		model.sourceLocation = filters?.sourceLocationFilter ?:''
		model.sourceRack = filters?.sourceRackFilter ?:''
		model.targetLocation = filters?.targetLocationFilter ?:''
		model.targetRack = filters?.targetRackFilter ?:''
		// Used for filter toValidate from the Planning Dashboard - want a better parameter (JPM 9/2014)
		model.type = params.type

		model.title = (TITLE_BY_FILTER.containsKey(params.filter) ? TITLE_BY_FILTER[params.filter] : TITLE_BY_FILTER['all']) + ' List'

		return model
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
				sourceLocation: params.sourceLocation,
				sourceRack: params.sourceRack,
		]
		def validSords = ['asc', 'desc']
		String sortOrder = validSords.indexOf(params.sord) != -1 ? params.sord : 'asc'
		int maxRows = params.rows.toInteger()
		int currentPage = params.page.toInteger() ?: 1
		int rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows

		def moveBundleList

		def attributes = projectService.getAttributes('AssetEntity')

		// def prefType = (listType == 'server') ? 'Asset_Columns' : 'Physical_Columns'
		def prefType = 'Asset_Columns'
		def assetPref= getExistingPref(prefType)

		def assetPrefVal = assetPref*.value
		attributes.each { attribute ->
			if (attribute.attributeCode in assetPrefVal) {
				filterParams[attribute.attributeCode] = params[attribute.attributeCode]
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

		StringBuffer altColumns = new StringBuffer()
		StringBuffer joinQuery = new StringBuffer()

		// Until sourceRack is optional on the list we have to do this one
		altColumns.append("\n, srcRack.tag AS sourceRack, srcRoom.location AS sourceLocation")
		joinQuery.append("\nLEFT OUTER JOIN rack AS srcRack ON srcRack.rack_id=ae.rack_source_id ")
		joinQuery.append("\nLEFT OUTER JOIN room AS srcRoom ON srcRoom.room_id=ae.room_source_id ")

		altColumns.append(", manu.name AS manufacturer")
		joinQuery.append("\nLEFT OUTER JOIN manufacturer manu ON manu.manufacturer_id=m.manufacturer_id ")

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

				case ~/source(Location|Room)/:
					// This is a hack for the columns that were moved to the Room domain property map attributes
					def locOrRoom = Matcher.lastMatcher[0][1]

					if (!srcRoomAdded) {
						joinQuery.append("\nLEFT OUTER JOIN room srcRoom ON srcRoom.room_id=ae.room_source_id ")
						srcRoomAdded = true
					}
					if (locOrRoom == 'Location') {
						// Note that this is added by default above
						// altColumns.append(', srcRoom.location AS sourceLocation')
					} else if (locOrRoom == 'Room') {
						altColumns.append(', srcRoom.room_name AS sourceRoom')
					} else {
						throw new RuntimeException("Unhandled condition for property ($value)")
					}
					break
				case ~/sourceRack|moveBundle/:
					// Handled by the default columns
					break

				case ~/target(Location|Room)/:
					// This is a hack for the columns that were moved to the Room domain property map attributes
					def locOrRoom = Matcher.lastMatcher[0][1]

					if (!tgtRoomAdded) {
						joinQuery.append("\nLEFT OUTER JOIN room tgtRoom ON tgtRoom.room_id=ae.room_target_id ")
						tgtRoomAdded = true
					}
					if (locOrRoom == 'Location') {
						altColumns.append(', tgtRoom.location AS targetLocation')
					} else if (locOrRoom == 'Room') {
						altColumns.append(', tgtRoom.room_name AS targetRoom')
					} else {
						throw new RuntimeException("Unhandled condition for property ($value)")
					}
					break
				case 'targetRack':
					// Property was moved to the Rack domain
					altColumns.append(", tgtRack.tag AS targetRack")
					joinQuery.append("\nLEFT OUTER JOIN rack tgtRack ON tgtRack.rack_id=ae.rack_target_id ")
					break
				case 'sourceChassis':
					altColumns.append(", aeSourceChassis.asset_name AS sourceChassis")
					joinQuery.append("\nLEFT OUTER JOIN asset_entity aeSourceChassis ON aeSourceChassis.asset_entity_id=ae.source_chassis_id ")
					break
				case 'targerChassis':
					altColumns.append(", aeTargetChassis.asset_name AS targetChassis")
					joinQuery.append("\nLEFT OUTER JOIN asset_entity aeTargetChassis ON aeTargetChassis.asset_entity_id=ae.targer_chassis_id ")
					break

				case 'validation':
					break
				default:
					altColumns.append(", ae.${WebUtil.splitCamelCase(value)} AS $value ")
			}
		}

		def query = new StringBuffer("""
			SELECT * FROM (
				SELECT ae.asset_entity_id AS assetId, ae.asset_name AS assetName,
				ae.asset_type AS assetType, m.name AS model,
				if (at.comment_type IS NULL, 'noTasks','tasks') AS tasksStatus,
				if (ac.comment_type IS NULL, 'noComments','comments') AS commentsStatus,
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
				LEFT OUTER JOIN asset_comment at ON at.asset_entity_id=ae.asset_entity_id AND at.comment_type = '$AssetCommentType.TASK'
				LEFT OUTER JOIN asset_comment ac ON ac.asset_entity_id=ae.asset_entity_id AND ac.comment_type = '$AssetCommentType.COMMENT'
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
		filterParams.each {key, val ->
			if (val && val.trim().size()) {
				whereConditions << SqlUtil.parseParameter(key, val, queryParams, AssetEntity)
			}
		}

		if (whereConditions.size()) {
			firstWhere = false
			query.append(" WHERE assets.${whereConditions.join(" AND assets.")}")
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
			query.append(whereAnd() + " assets.validation='Discovery' ") //eq ('validation','Discovery')
		}

		// Allow filtering on the Validate
		if (params.toValidate && ValidationType.list.contains(params.toValidate)) {
			query.append(whereAnd() + " assets.validation='$params.toValidate' ")
		}

		if (params.plannedStatus) {
			query.append(whereAnd() + " assets.planStatus='$params.plannedStatus'")
		}
		query.append(" ORDER BY $sortIndex $sortOrder")
		log.debug  "query = $query"

		def assetList = []
		if (queryParams.size()) {
			assetList = namedParameterJdbcTemplate.queryForList(query.toString(), queryParams)
		} else {
			assetList = jdbcTemplate.queryForList(query.toString())
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
			        it.sourceLocation,
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

		def hql = new StringBuffer('SELECT m.id, m.assetType, m.modelName, man.id as manId, man.name as manName, m.modelStatus, m.usize as usize FROM Model m JOIN m.manufacturer as man')

		def params = []

		StringBuffer where = new StringBuffer()

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
			StringBuffer search = new StringBuffer()

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
		StringBuffer orderBy = new StringBuffer()
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
		def hql = "SELECT distinct m.id, m.name FROM Manufacturer m"
		def joinTables = ""
		def condition = ""
		def hqlParams = []

		if (StringUtils.isNotBlank(term)) {
			if (hqlParams.isEmpty()) {
				condition = condition + " WHERE m.name LIKE ?"
			} else {
				condition = condition + " AND m.name LIKE ?"
			}
			hqlParams.add("%" + term + "%")
		}

		if (StringUtils.isNotBlank(assetType)) {
			joinTables = " LEFT OUTER JOIN m.models as model"
			if (hqlParams.isEmpty()) {
				condition = condition + " WHERE model.assetType = ?"
			} else {
				condition = condition + " AND model.assetType = ?"
			}
			hqlParams.add(assetType)
		}

		def manufacturers = Manufacturer.executeQuery(hql + joinTables + condition, hqlParams)
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
	def assetTypesOf(manufacturerId, term) {
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
	 * Used to get Key,values of Help Text and append to asset cruds.
	 * @param entityType type of entity.
	 * @param project to look for
	 * @return tooltips map
	 */
	Map<String, String> retrieveTooltips(String entityType, Project project) {
		Map<String, String> returnMap = [:]
		String category = EntityType.getListAsCategory(entityType)
		try {
			for (String attributeCode in projectService.getAttributes(entityType)*.attributeCode) {
				KeyValue keyMap = KeyValue.findAllByCategoryAndKey(category, attributeCode).find { it.project == project }
				returnMap[attributeCode] = keyMap?.value
			}
		}catch(Exception ex) {
			log.error("An error occurred : $ex.message", ex)
		}
		return returnMap
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
		if (! id) {
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
			assetEntity: assetEntity,
			dependencyStatus: getDependencyStatuses(),
			dependencyType: getDependencyTypes(),
			whom: params.whom,
			nonNetworkTypes: nonNetworkTypes,
			supportAssets: getSupportingAssets(assetEntity),
			dependentAssets: getDependentAssets(assetEntity),
			moveBundleList: getMoveBundles(project)
		]

		return map
	}
}
