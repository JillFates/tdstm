import grails.converters.JSON

import java.text.SimpleDateFormat

import jxl.*
import jxl.read.biff.*
import jxl.write.*

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils
import org.apache.shiro.SecurityUtils
import org.codehaus.groovy.grails.commons.ApplicationHolder

import java.util.regex.Matcher

// Used to wire up bindData
//import org.codehaus.groovy.grails.web.metaclass.BindDynamicMethod
//import org.codehaus.groovy.grails.commons.metaclass.GroovyDynamicMethodsInterceptor
//import org.springframework.validation.BindingResult

import com.tds.asset.Application
import com.tds.asset.ApplicationAssetMap
import com.tds.asset.AssetCableMap
import com.tds.asset.AssetComment
import com.tds.asset.AssetDependency
import com.tds.asset.AssetDependencyBundle
import com.tds.asset.AssetEntity
import com.tds.asset.AssetEntityVarchar
import com.tds.asset.AssetOptions
import com.tds.asset.AssetTransition
import com.tds.asset.AssetType
import com.tds.asset.Database
import com.tds.asset.Files
import com.tdsops.tm.domain.AssetEntityHelper
import com.tdsops.tm.enums.domain.AssetCableStatus
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.ValidationType
import com.tdsops.tm.enums.domain.SizeScale
import com.tdssrc.grails.ApplicationConstants
import com.tdssrc.eav.EavAttribute
import com.tdssrc.eav.EavAttributeOption
import com.tdssrc.eav.EavAttributeSet
import com.tdssrc.grails.DateUtil
import com.tdssrc.grails.ExceptionUtil
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WebUtil
import com.tdsops.common.sql.SqlUtil
import com.tdssrc.eav.EavEntityAttribute
import com.tdsops.common.lang.ExceptionUtil

class AssetEntityService {

	// TODO : JPM 9/2014 : determine if customLabels is used as it does NOT have all of the values it should
	protected static customLabels = ['Custom1','Custom2','Custom3','Custom4','Custom5','Custom6','Custom7','Custom8','Custom9','Custom10',
		'Custom11','Custom12','Custom13','Custom14','Custom15','Custom16','Custom17','Custom18','Custom19','Custom20','Custom21','Custom22','Custom23','Custom24']
	
	// TODO : JPM 9/2014 : determine if bundleMoveAndClientTeams is used as the team functionality has been RIPPED out of TM
	protected static bundleMoveAndClientTeams = ['sourceTeamMt','sourceTeamLog','sourceTeamSa','sourceTeamDba','targetTeamMt','targetTeamLog','targetTeamSa','targetTeamDba']

	// This is a list of properties that should be excluded from the custom column select list
	private static COLUMN_PROPS_TO_EXCLUDE = [
		(AssetClass.APPLICATION): [],
		(AssetClass.DATABASE): [],
		(AssetClass.DEVICE): ['assetType', 'model', 'planStatus', 'moveBundle', 'sourceLocation', 'sourceRack',
		// TODO : JPM 9/2014 : This list can be removed as part of TM-3311
		'sourceTeamDba', 'sourceTeamDba', 'sourceTeamLog', 'sourceTeamSa', 'sourceTeamMt', 'targetTeamDba', 'targetTeamDba', 'targetTeamLog', 'targetTeamSa', 'targetTeamMt'
		],
		(AssetClass.STORAGE): [] 
	]

	// The follow define the various properties that can be used with bindData to assign domain.properties
	static CUSTOM_PROPERTIES = [ 
		'custom1', 'custom2', 'custom3', 'custom4', 'custom5', 'custom6', 'custom7', 'custom8', 'custom9', 'custom10',
		'custom11', 'custom12', 'custom13', 'custom14', 'custom15', 'custom16', 'custom17', 'custom18', 'custom19', 'custom20',
		'custom21', 'custom22', 'custom23', 'custom24', 'custom25', 'custom26', 'custom27', 'custom28', 'custom29', 'custom30',
		'custom31', 'custom32', 'custom33', 'custom34', 'custom35', 'custom36', 'custom37', 'custom38', 'custom39', 'custom40',
		'custom41', 'custom42', 'custom43', 'custom44', 'custom45', 'custom46', 'custom47', 'custom48', 'custom49', 'custom50',
		'custom51', 'custom52', 'custom53', 'custom54', 'custom55', 'custom56', 'custom57', 'custom58', 'custom59', 'custom60',
		'custom61', 'custom62', 'custom63', 'custom64']
	
	// Common properties for all asset classes (Application, Database, Files/Storate, Device)
	static ASSET_PROPERTIES = [ 'assetName',  'shortName', 'priority', 'planStatus',  'department', 'costCenter', 
		'maintContract', 'maintExpDate', 'retireDate', 'description', 'supportType', 'environment', 'serialNumber', 'validation', 'externalRefId', 
		'size', 'scale', 'rateOfChange'
	]
	// 'purchaseDate', 'purchasePrice', 

	// Properties strictly for DEVICES (a.k.a. AssetEntity)
	static DEVICE_PROPERTIES = [ 
		'assetTag', 'assetType', 'ipAddress', 'os', 'usize', 'truck', 'cart', 'shelf', 'railType', 
		'sourceBladePosition', 'targetRackPosition',
		'sourceRackPosition', 'targetBladePosition'
	]

	// Properties strictly for APPLICATION
	static APPLICATION_PROPERTIES = []
	// Properties strictly for APPLICATION
	static DATABASE_PROPERTIES = []

	// List of all of the Integer properties for the potentially any of the asset classes
	static ASSET_INTEGER_PROPERTIES = ['size', 'rateOfChange', 'priority', 'sourceBladePosition', 'targetBladePosition', 'sourceRackPosition', 'targetRackPosition']

	static transactional = true

	def assetEntityAttributeLoaderService
	def partyRelationshipService
	def progressService
	def projectService
	def rackService
	def roomService
	def securityService
	def taskService
	def userPreferenceService

	def jdbcTemplate

	/**
	 * Constructor
	 */
	/*
	AssetEntityService() {
		// This will wire up the bindData method to the service
		// See http://nerderg.com/Grails
 		GroovyDynamicMethodsInterceptor i = new GroovyDynamicMethodsInterceptor(this)
        i.addDynamicMethodInvocation(new BindDynamicMethod())
	}
	*/

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
	 * @param userLogin - the user making the change
	 * @param device - the device to be assigned
	 * @param params - the input parameters passed in from the device create/edit form (TODO : JPM 10/2014 : Define what params are used)
	 */
	void assignDeviceToChassisOrRack(Project project, UserLogin userLogin, AssetEntity device, params) {
		if (device.isaBlade()) {
			if (NumberUtil.toLong(params.roomSourceId) > 0)
				assignBladeToChassis(project, device, params.sourceChassis, true) 
			if (NumberUtil.toLong(params.roomTargetId) > 0)
				assignBladeToChassis(project, device, params.targetChassis, false)
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

		if (id.is(null)) {
			throw new InvalidRequestException('Invalid Bundle id was specified')
		}
		MoveBundle mb = MoveBundle.get(id)
		if (! mb) {
			throw new InvalidRequestException('Unable to find the Bundle id that was specified')
		}
		if (mb.project != project) {
			securityService.reportViolation("Attempted to assign asset to bundle ($id) not associated with project (${project.id})".toString())
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
		def srcOrTrgt = (isSource ? 'Source' : 'Target')
		def roomProp = "room$srcOrTrgt"
		def id = NumberUtil.toLong(roomId)
		if (id.is(null)) {
			log.warn "assignAssetToRoom() called with invalid room id ($roomId)"
			throw new InvalidRequestException("Room id was invalid")
		}

		// TODO : JPM 9/2014 : If moving to a different room then we need to disconnect cabling (enhancement)

		switch (id) {
			case -1:
				// Create a new room
				if (location.trim().size()==0 || roomName.trim().size()==0) {
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
					throw new RuntimeException("Unable to create new room $location/$roomName ($srcOrTrgt)".toString())
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
						if ( (isSource && room.source == 1) || (!isSource && room.source == 0)) {
							asset[roomProp] = room
						} else {
							throw new InvalidRequestException("Referenced room ($room) was not a $srcOrTrgt room".toString())
						}
					} else {
						securityService.reportViolation("Attempted to access room ($id) not associated with project (${project.id})".toString())
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
		def srcOrTrgt = (isSource ? 'Source' : 'Target')
		def rackProp = "rack$srcOrTrgt"

		log.debug "assignAssetToRack(${project.id}, ${asset.id}, $rackId, $rackName, $isSource)"

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

		def id = NumberUtil.toLong(rackId)
		if (id.is(null)) {
			log.warn "assignAssetToRack() called with invalid rack id ($rackId)"
			throw new InvalidRequestException("Rack id was invalid")
		}

		Room room = asset["room$srcOrTrgt"]

		switch (id) {
			case -1:
				// Create a new rack
				rackName = rackName?.trim()
				if (rackName?.size()==0) {
					throw new InvalidRequestException("Creating a $srcOrTarget rack requires a name".toString())
				}

				def rack = rackService.findOrCreateRack(room, rackName) 
				if (rack) {
					asset[rackProp] = rack
				} else {
					throw new RuntimeException("Unable to create new rack $rackName ($srcOrTrgt)".toString())
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
						if ( (isSource && rack.source == 1) || (!isSource && rack.source == 0)) {
							asset[rackProp] = rack
						} else {
							throw new InvalidRequestException("Referenced rack ($rack) was not a $srcOrTrgt rack".toString())
						}
					} else {
						securityService.reportViolation("Attempted to access rack ($id) not associated with project (${project.id})".toString())
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
	 */
	void assignBladeToChassis(Project project, AssetEntity blade, String chassisId, boolean isSource) {
		def srcOrTrgt = (isSource ? 'Source' : 'Target')
		def roomProp = "room$srcOrTrgt"
		def chassisProp = (isSource ? 'sourceChassis' : 'targetChassis')

		def assetType = blade.model?.assetType
		if (assetType != AssetType.BLADE.toString()) {
			throw new InvalidRequestException("Attempted to assign a non-blade type asset to a chassis (type $assetType)".toString())
		}

		log.debug "assignBladeToChassis(${project.id}, ${blade.id}, $chassisId, $isSource) - chassisProp=$chassisProp"
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
				throw new EmptyResultException("Unable to find chassis".toString())
			}
			assetType = chassis.model?.assetType
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
		}


	}

	/**
	 * This method is used to update dependencies for all entity types
	 * @param project : Instance of current project
	 * @param loginUser : Instance of current logged in user
	 * @param assetEntity : instance of entity including Server, Application, Database, Files
	 * @param params : params map received from client side
	 * @return List of error came while updating dependencies (if any)
	 */	
	List<String> createOrUpdateAssetEntityDependencies(Project project, UserLogin userLogin, AssetEntity assetEntity, def params) {
		List errors = []

		AssetDependency.withTransaction() { status->
			try {
				// Verifying assetEntity assigned to the project
				validateAssetsAssocToProject([assetEntity.id], project) 

				//
				// Handle deleting first
				// 

				// Collecting deleted deps ids and fetching there instances list
				List toDelDepIds = params.deletedDep ? params.deletedDep.split(",").collect { NumberUtil.toLong(it, 0L)} : []
				List toDelDepObjs = ( toDelDepIds.size() ? AssetDependency.findAllByIdInList(toDelDepIds) : [] )
				
				// Delete any dependencies that were listed in the params.deletedDep parameter
				if (toDelDepObjs) {
					// Gather all of the assets referenced in the dependendencies and make sure that they are associated to the project
					List allReferencedAssetIds = ( toDelDepObjs?.asset?.id + toDelDepObjs?.dependent?.id ).unique()
					validateAssetsAssocToProject(allReferencedAssetIds, project)

					// Delete the dependencies
					AssetDependency.executeUpdate('delete AssetDependency where id in ( :dependencyIds ) ', [dependencyIds:toDelDepObjs.id])
				}

				// Add/Update Support dependencies 
				addOrUpdateDependencies(project, userLogin, 'support', assetEntity,  params)

				// Add/Update Dependent dependencies 
				addOrUpdateDependencies(project, userLogin, 'dependent', assetEntity,  params)

			} catch (DomainUpdateException e) {		
				errors << e.getMessage()
			} catch (InvalidRequestException e) {
				errors << e.getMessage()
			} catch (RuntimeException rte) {
				//rte.printStackTrace()
				log.error ExceptionUtil.stackTraceToString(rte, 60)
				errors << 'An error occurred that prevented the update'
			}

			if (errors.size())
				status.setRollbackOnly()			
		}

		return errors
	}

	/**
	 * A helper method for createOrUpdateAssetEntityDependencies which does the actual adds and updates of dependencies from the web request
	 * @param project - instance of the user's currently assigned project
	 * @param userLogin - the user object that made the request
	 * @param depType - what dependency type is being updated (options support|dependent)
	 * @param asset - the AssetEntity instance being referenced
	 * @param params - map of params received from browser
	 * @throws InvalidRequestException for various unexpected conditions
	 * @throws DomainUpdateException for errors in data validation
	 */
	private void addOrUpdateDependencies(Project project, UserLogin userLogin, String depType, AssetEntity asset, params) {
	
		def (existingDeps, newDeps) = parseParamsForDependencyAssetIds(depType, params)

		// Check that all of the referenced assets are associated with the project
		List allAssetIds = (existingDeps.values() + newDeps.values()).unique()
		validateAssetsAssocToProject(allAssetIds, project)

		List propNames = ['dataFlowFreq', 'type', 'status', 'comment']

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
			AssetDependency.withNewSession() { status ->
				AssetDependency dupAd 
				if (depType == 'support') {
					dupAd = AssetDependency.findByAssetAndDependent(depAsset, asset)
				} else {
					dupAd = AssetDependency.findByAssetAndDependent(asset, depAsset)
				}
				if (dupAd && (depId < 1 || (depId >0 && depId != dupAd.id))) {
					throw new InvalidRequestException("Duplicate dependencies not allow for $depType ${depAsset.assetName}")
				}
			}

			// Update the fields
			propNames.each { name ->
				String paramName = "${name}_${depType}_${depId}"
				if (params.containsKey(paramName)) {
					assetDependency[name] = params[paramName]
				} else {
					log.warn "addOrUpdateDependencies() request was missing property $paramName, user=$userLogin, asset=$asset"
				}
			}

			if (isNew)
				assetDependency.createdBy = userLogin.person

			assetDependency.updatedBy = userLogin.person

			if (depType == 'support') {
				assetDependency.asset = depAsset
				assetDependency.dependent = asset
			} else {
				assetDependency.asset = asset
				assetDependency.dependent = depAsset
			}

			// Deal with the move bundle assignment for the dependent asset
			String mbStrId = params["moveBundle_${depType}_${depId}"]
			Long mbId = NumberUtil.toLong(mbStrId)
			if (mbId == null) {
				throw new DomainUpdateException("A move bundle must be specified for the $depType dependency for ${depAsset}")
			}
			if (mbId != depAsset.moveBundle.id) {
				assignAssetToBundle(project, depAsset, mbStrId)
			}

			log.debug "addOrUpdateDependencies() Attempting to ${isNew ? 'CREATE' : 'UPDATE'} dependency (${assetDependency.id}) ${assetDependency.asset.id}/${assetDependency.dependent.id} : changed fields=${assetDependency.dirtyPropertyNames}"
			if (! assetDependency.validate() || ! assetDependency.save(force:true)) {
				throw new DomainUpdateException("Unable to save $depType dependency for ${assetDependency.asset} / ${assetDependency.dependent}", assetDependency)
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
			AssetDependency ad = new AssetDependency() 

			updateDependency(depId, ad, depAssetId)
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
	private List parseParamsForDependencyAssetIds(String depType, params) {
		// Get list of all the AssetDependency ids referenced by stripping off the suffix of the asset_$type_ID param. For each 
		// form table row there will be variable representing each property of the domain followed by the type (support|dependent)
		// and the id of the domain. Values greater (>) than zero (0) reference existing records and <=0 are for new records.
		//
		// This first loop will find all the referenced dependencies and new dependencies constructing a map of the id and the referenced asset and dependent
		// asset.
		def regex = "asset_${depType}_(.+)"
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
					existingDeps.put(id, assetId)
				} else {
					newDeps.put(id, assetId)
				}
			} )
		}
		return [existingDeps, newDeps]
	}

	/**
	 * Helper function used to verify that referenced asset id(s) are associated to the specified project. If any are not then an exception is thrown.
	 * @param assetIds - a list of asset ids to check
	 * @param project - instance of project
	 */
	private void validateAssetsAssocToProject(List assetIds, Project project) {
		def invalidAssetId
		// Use withNewSession so that it doesn't cause the notorious 'Not Processed by Flush()' hibernate error
		AssetEntity.withNewSession() { status ->
			def assetList = AssetEntity.findAllByIdInList( assetIds, [readOnly:true] )
			def invalidAsset = assetList.find { it.project.id != project.id}
			invalidAssetId = invalidAsset?.id
		}
		if (invalidAssetId) {
			securityService.reportViolation("In validateAssetsAssocToProject() an attempt to access asset ${invalidAsset.id} not associated with project ${project.id}")
			throw new InvalidRequestException("Invalid asset id ${invalidAsset.id} referenced for project ${project.name}")
		}
	}
	
	/**
	 * Delete all files that are match with params criteria
	 * @param files path, file name startsWith
	 */
	def deleteTempGraphFiles(path, startsWith) {
		// TODO : JPM 10/2014 : deleteTempGraphFiles function should NOT be allowed the way this looks so it was disabled and should be deleted
		log.error "deleteTempGraphFiles() should NEVER be called but obviously it is..."
		/*
		def filePath = ApplicationHolder.application.parentContext.getResource(path).file
		// Get file path
		def dir = new File( "${filePath.absolutePath}" )
		def children = dir.list()
		if ( children ) {
			for (int i=0; i<children.length; i++) {
				// Get filename
				def filename = children[i]
				if ( filename.startsWith(startsWith) ) {
					def jsonFile =  ApplicationHolder.application.parentContext.getResource( "${path}/${filename}" ).getFile()
					jsonFile?.delete()
				}
			}
		}
		*/
	}

	/**
	 * @param project
	 * @return list of entities 
	 */
	def getSpecialExportData( project ){
		String queryForSpecialExport = """ ( SELECT
				server.asset_entity_id AS server_id,
				app.asset_entity_id AS app_id,
				server.asset_name AS server_name,
				server.asset_type AS server_type,
				IFNULL(app.asset_name,'') AS app_name,
				IFNULL(sme,'') AS tru, IFNULL(sme2,'') AS tru2,
				IFNULL(mb.name,'') AS move_bundle,
				IF(mb.name='mx','',IFNULL(date_format(mb.start_time,'%m/%d'),'')) AS move_date,
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
				server.project_id=${project.id}
				AND server.asset_type IN ('Server','VM', 'Load Balancer','Network', 'Storage', 'Blade')
				ORDER BY app_name, server_name
			)
			UNION DISTINCT
			
			 ( SELECT
				dbsrv.asset_entity_id AS server_id,
				app.asset_entity_id AS app_id,
				dbsrv.asset_name AS server_name,
				dbsrv.asset_type server_type,
				IFNULL(app.asset_name,'') AS app_name,
				IFNULL(sme,'') AS tru, IFNULL(sme2,'') AS tru2,
				IFNULL(mb.name,'') AS move_bundle,
				IF(mb.name='mx','',IFNULL(date_format(mb.start_time,'%m/%d'),'')) AS move_date,
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
				app.project_id=${project.id}
				AND app.asset_type = 'Application'
			)
			UNION DISTINCT
			( SELECT
				clustersrv.asset_entity_id AS server_id,
				app.asset_entity_id AS app_id,
				clustersrv.asset_name AS server_name,
				clustersrv.asset_type server_type,
				IFNULL(app.asset_name,'') AS app_name,
				IFNULL(sme,'') AS tru, IFNULL(sme2,'') AS tru2,
				IFNULL(mb.name,'') AS move_bundle,
				IF(mb.name='mx','',IFNULL(date_format(mb.start_time,'%m/%d'),'')) AS move_date,
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
				app.project_id=${project.id}
				AND app.asset_type = 'Application' )"""

	  def splList =   jdbcTemplate.queryForList( queryForSpecialExport )
											
	  return splList
	}
	
	/**
	 * Delete asset and associated records, User this method when we want to delete an Entity
	 * @param assetEntityInstance
	 * @return
	 */
	def deleteAsset(assetEntity){
		ProjectAssetMap.executeUpdate("delete from ProjectAssetMap pam where pam.asset = :asset",[asset:assetEntity])
		AssetTransition.executeUpdate("delete from AssetTransition ast where ast.assetEntity = :asset",[asset:assetEntity])
		AssetComment.executeUpdate("update AssetComment ac set ac.assetEntity = null where ac.assetEntity = :asset",[asset:assetEntity])
		ApplicationAssetMap.executeUpdate("delete from ApplicationAssetMap aam where aam.asset = :asset",[asset:assetEntity])
		AssetEntityVarchar.executeUpdate("delete from AssetEntityVarchar aev where aev.assetEntity = :asset",[asset:assetEntity])
		ProjectTeam.executeUpdate("update ProjectTeam pt set pt.latestAsset = null where pt.latestAsset = :asset",[asset:assetEntity])
		AssetCableMap.executeUpdate("delete AssetCableMap where assetFrom = :asset",[asset:assetEntity])
		AssetCableMap.executeUpdate("""Update AssetCableMap 
			set cableStatus='${AssetCableStatus.UNKNOWN}', assetTo=null,
			assetToPort=null 
			where assetTo = :asset""", [asset:assetEntity])
		AssetDependency.executeUpdate("delete AssetDependency where asset = :asset or dependent = :dependent ",[asset:assetEntity, dependent:assetEntity])
		AssetDependencyBundle.executeUpdate("delete from AssetDependencyBundle ad where ad.asset = :asset",[asset:assetEntity])
	}
	
	/**
	 * Used to gather all of the assets for a project by asset class with option to return just individual classes
	 * @param project
	 * @param groups - an array of Asset Types to only return those types (optional)
	 * @return Map of the assets by type along with the AssetOptions dependencyType and dependencyStatus lists
	 */
	Map entityInfo(Project project, List groups=null){
		def map = [ servers:[], applications:[], dbs:[], files:[], networks:[], dependencyType:[], dependencyStatus:[] ]

        if (groups == null || groups.contains(AssetType.SERVER.toString())) {
			map.servers = AssetEntity.executeQuery(
				"SELECT a.id, a.assetName FROM AssetEntity a " + 
				"WHERE assetClass=:ac AND assetType in (:types) AND project=:project ORDER BY assetName", 
				[ac:AssetClass.DEVICE, types:AssetType.getAllServerTypes(), project:project] )
		}

		if (groups == null || groups.contains(AssetType.APPLICATION.toString())) {
			map.applications = Application.executeQuery(
				"SELECT a.id, a.assetName FROM Application a " +
				"WHERE assetClass=:ac AND project=:project ORDER BY assetName", 
				[ac:AssetClass.APPLICATION, project:project] )
		}

		if (groups == null || groups.contains(AssetType.DATABASE.toString())) {
			map.dbs = Database.executeQuery(
				"SELECT d.id, d.assetName FROM Database d " +
				"WHERE assetClass=:ac AND project=:project ORDER BY assetName", 
				[ac:AssetClass.DATABASE, project:project] )
		}
		
		if (groups == null || groups.contains(AssetType.STORAGE.toString())) {
			map.files = Files.executeQuery(
				"SELECT f.id, f.assetName FROM Files f " + 
				"WHERE assetClass=:ac AND project=:project ORDER BY assetName", 
				[ac:AssetClass.STORAGE, project:project] )
		}

		// NOTE - the networks is REALLY OTHER devices other than servers
		if (groups == null || groups.contains(AssetType.NETWORK.toString())) {
			map.networks = AssetEntity.executeQuery(
				"SELECT a.id, a.assetName FROM AssetEntity a " + 
				"WHERE assetClass=:ac AND project=:project AND COALESCE(a.assetType,'') NOT IN (:types) ORDER BY assetName",
				[ ac:AssetClass.DEVICE, project:project, types:AssetType.getNonOtherTypes() ] )
		}

		if (groups == null) {
			map.dependencyType = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_TYPE)
			map.dependencyStatus = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_STATUS)
		}

		return map
	}

	/**
	 * Used to get the list of Type used to assign to AssetDependency.type
	 * @return List of the types
	 */
	List getDependencyTypes() {
		return AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_TYPE)?.value
	}

	/**
	 * Used to get the list of Status used to assign to AssetDependency.status
	 * @return List of the types
	 */
	List getDependencyStatuses() {
		return AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_STATUS)?.value
	}

	/**
	 * Use to get the list of Asset Environment options
	 * @return List of options
	 */
	List getAssetEnvironmentOptions() {
		return AssetOptions.findAllByType(AssetOptions.AssetOptionsType.ENVIRONMENT_OPTION)?.value
	}

	/**
	 * Use to get the list of Asset Environment options
	 * @return List of options
	 */
	List getAssetPlanStatusOptions() {
		return AssetOptions.findAllByType(AssetOptions.AssetOptionsType.STATUS_OPTION)?.value
	}

	/**
	 * Use to get the list of Priority Options
	 * @return List of Priority values 
	 */
	List getAssetPriorityOptions() {	
		return AssetOptions.findAllByType(AssetOptions.AssetOptionsType.PRIORITY_OPTION)?.value
	}

	/**
	 * Use to get the list of the device RailType Options
	 * @return List of RailTypes
	 */
	List getAssetRailTypeOptions() {
		def railTypeAttribute = EavAttribute.findByAttributeCode('railType')
		return EavAttributeOption.findAllByAttribute(railTypeAttribute)?.value
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
	 * Use to get the list of Asset Type options
	 * @return List of options
	 */
	List getPropertyOptions(Object propertyAttrib) {
		return EavAttributeOption.findAllByAttribute(propertyAttrib)?.value
	}

	/**
	 * Get dependent assets of an asset
	 * @param AssetEntity - the asset that we're finding the dependent assets
	 * @return List of Assets
	 */
	List getDependentAssets(Object asset) {
		List list
		if (asset?.id)
			list = AssetDependency.findAll("FROM AssetDependency a WHERE asset=? ORDER BY a.dependent.assetType, a.dependent.assetName",[asset])

		return list
	}

	/**
	 * Get supporting assets of an asset
	 * @param AssetEntity - the asset that we're finding the dependent assets
	 * @return List of Assets
	 */
	List getSupportingAssets(Object asset) {
		List list
		if (asset?.id)
			list = AssetDependency.findAll("FROM AssetDependency a WHERE dependent=? ORDER BY a.asset.assetType,a.asset.assetName", [asset])

		return list
	}

	/**
	 * Returns a list of MoveBundles for a project
	 * @param project - the Project object to look for
	 * @return list of MoveBundles
	 */
	List getMoveBundles(Project project) {
		List list = []
		if (project)
			list = MoveBundle.findAllByProject(project, [sort:'name'])
		return list
	}

	/**
	 * Returns a list of MoveEvents for a project
	 * @param project - the Project object to look for
	 * @return list of MoveEvents
	 */
	List getMoveEvents(Project project) {
		List list
		if (project)
			list = MoveEvent.findAllByProject(project,[sort:'name'])

		return list
	}

	/**
	 * Used to retrieve a list of valid AssetTypes for devices
	 * @param project - the project the user is assigned to
	 * @return List of AssetType strings
	 */
	List<String> getDeviceAssetTypeOptions(Project project) {
		def list = Model.executeQuery('select distinct assetType from Model where assetType is not null order by assetType')
		return list
	}

	/**
	 * Used to get the user's preference for the asset list size/rows per page
	 * @return the number of rows to display per page
	 */
	String getAssetListSizePref() {
		// TODO - JPM 08/2014 - seems like we could convert the values to Integer (improvement)
		return ( userPreferenceService.getPreference("assetListSize") ?: '25' )
	}

	/**
	 * Used to retrieve the asset and model that will be used for the Device Create form
	 */
	List getDeviceAndModelForCreate(Project project, Object params) {	

		def (device, model) = getCommonDeviceModelForCreateEdit(project, null, params)

		// Attempt to set the default bundle for the device based on the user's preferences
		def bundleId = userPreferenceService.get('MOVE_BUNDLE')
		if (bundleId) {
			def bundle = MoveBundle.read(bundleId)
			if (bundle && bundle.project == project) {
				device.moveBundle = bundle
			}
		}

		return [device, model]
	}

	/**
	 * Used to retrieve the asset and model that will be used for the Device Edit form
	 */
	// TODO : JPM 9/2014 : these methods should be renamed from getDeviceModel to getDeviceAndModel to avoid confusion (improvement)
	List getDeviceModelForEdit(Project project, Object deviceId, Object params) {
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
	private List getCommonDeviceModelForCreateEdit(Project project, Object deviceId, Object params) {
		boolean isNew = deviceId == null
		def (device, model) = getCommonDeviceAndModel(project, deviceId, params)

		// Check to see if someone is screwing around with the deviceId
		if (device == null && model == null) {
			log.error "**** getCommonDeviceModelForCreateEdit() didn't get the device"
			return [null, null]
		}

		if (isNew) {
			device = new AssetEntity()
			device.assetType = params.initialAssetType?:''	// clear out the default
		}

		// Stick questionmark on the end of the model name if it is unvalidated
		String modelName = device.model?.modelName ?: 'Undetermined'
		if (! device.model?.isValid())
			modelName += ' ?'

		def assetType = device.assetType
		model.putAll( [
			assetEntityInstance: device,
			assetType: assetType,
			manufacturer: device.manufacturer,
			manufacturers: getManufacturers( assetType ), 
			models: getModelSortedByStatus( device.manufacturer ),
			// TODO : JPM 9/2014 : Determine what nonNetworkTypes is used for in the view (clean up if unnecessary)
			modelName: modelName,
			nonNetworkTypes: AssetType.getNonNetworkTypes(), 	
			railTypeOption: getAssetRailTypeOptions(),
			// TODO : JPM 9/2014 : determine if the views use source/targetRacks - I believe these can be removed as they are replaced by source/targetRackSelect 
			sourceRacks: [],
			targetRacks: [],
			sourceChassisSelect: [],
			targetChassisSelect: [],
			version: device.version,
		] )

		// List of the room and racks to be used in the SELECTs
		model.sourceRoomSelect = getRoomSelectOptions(project, true, true)
		model.targetRoomSelect = getRoomSelectOptions(project, false, true)
		model.sourceRackSelect = getRackSelectOptions(project, device?.roomSource?.id, true) 
		model.targetRackSelect = getRackSelectOptions(project, device?.roomTarget?.id, true) 

		model.putAll( getDefaultModelForEdits('AssetEntity', project, device, params) )

		if (device) {
			// TODO : JPM 9/2014 : Need to make the value flip based on user pref to show name or tag (enhancement TM-3390)
			// Populate the listings of the Chassis SELECT name/values
			model.sourceChassisSelect = getChassisSelectOptions(project, device?.roomSource?.id)
			model.targetChassisSelect = getChassisSelectOptions(project, device?.roomTarget?.id)
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
	private List getCommonDeviceAndModel(Project project, Object deviceId, Object params) {
		AssetEntity device = AssetEntityHelper.getAssetById(project, AssetClass.DEVICE, deviceId)
		Map model = [:]
		if (device) {
			// Load up any default model properties
		}
		return [device, model]
	}

	/**
	 * Used to provide the default/common properties shared between all of the Asset Edit views
	 * @param
	 * @return a Map that includes the list of common properties
	 */
	Map getDefaultModelForEdits(String type, Project project, Object asset, Object params) {

		//assert ['Database'].contains(type)
		def configMap = getConfig('AssetEntity', asset?.validation ?: 'Discovery')

		def assetTypeAttribute = getPropertyAttribute('assetType')
		//def validationType = asset.validation 
		def highlightMap = getHighlightedInfo(type, asset, configMap)
		def dependentAssets = getDependentAssets(asset)
		def supportAssets = getSupportingAssets(asset)

		// TODO - JPM 8/2014 - Need to see if Edit even uses the servers list at all. If so, this needs to join the model to filter on assetType 
		def servers = AssetEntity.findAll("FROM AssetEntity WHERE project=:project AND assetClass=:ac AND assetType IN (:types) ORDER BY assetName",
			[project:project, ac: AssetClass.DEVICE, types:AssetType.getServerTypes()])

		Map model = [
			assetId: asset.id,
			assetTypeAttribute: assetTypeAttribute,
			assetTypeOptions: getDeviceAssetTypeOptions(project),
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
			version: asset.version
		]

		return model
	}

	/**
	 * Used to provide the default/common properties shared between all of the Asset Show views
	 * @param
	 * @return a Map that includes the list of common properties
	 */
	Map getCommonModelForShows(String type, Project project, Object params, Object assetEntity=null) {

		log.debug "### getCommonModelForShows() type=$type, project=${project.id}, asset=${assetEntity? assetEntity.id : 'null'}"
		if (assetEntity == null) {
			assetEntity = AssetEntity.read(params.id)
		}

		def assetComment
		if (AssetComment.find("from AssetComment where assetEntity = ${assetEntity?.id} and commentType = ? and isResolved = ?",['issue',0])) {
			assetComment = "issue"
		} else if (AssetComment.find('from AssetComment where assetEntity = '+ assetEntity?.id)) {
			assetComment = "comment"
		} else {
			assetComment = "blank"
		}

		def assetCommentList = AssetComment.findAllByAssetEntity(assetEntity)

		def validationType = assetEntity.validation
		
		def configMap = getConfig(type, validationType)

		def dependentAssets = AssetDependency.findAll("from AssetDependency as a where asset = ? order by a.dependent.assetType, a.dependent.assetName asc",[assetEntity])
		
		def supportAssets = AssetDependency.findAll("from AssetDependency as a where dependent = ? order by a.asset.assetType, a.asset.assetName asc",[assetEntity])

		def highlightMap = getHighlightedInfo(type, assetEntity, configMap)

		def prefValue= userPreferenceService.getPreference("showAllAssetTasks") ?: 'FALSE'

		def depBundle = AssetDependencyBundle.findByAsset(assetEntity)?.dependencyBundle // AKA dependency group

		Map model = [
			assetId: assetEntity?.id,
			assetComment:assetComment, 
			assetCommentList:assetCommentList,
			config:configMap.config, 
			customs:configMap.customs, 
			dependencyBundleNumber: depBundle,
			dependentAssets:dependentAssets, 
			errors:params?.errors, 
			escapedName:getEscapedName(assetEntity),
			highlightMap:highlightMap, 
			prefValue:prefValue, 
			project:project,
			redirectTo:params.redirectTo, 
			supportAssets:supportAssets
		]

		return model
	}

	/** 
	 * Used to provide the default properties used for the Asset Dependency views
	 * @param listType - indicates the type of list [Application|AssetEntity|Files|Storage]
	 * @param moveEvent
	 * @param params - the params from the HTTP request
	 * @param filters - the map of the filter settings
	 * @return a Map that includes all of the common properties shared between all Asset List views
	 */
	Map getDefaultModelForLists(AssetClass ac, String listType, Project project, Object fieldPrefs, Object params, Object filters) {

		Map model = [
			assetClassOptions: AssetClass.getClassOptions(),
			assetDependency: new AssetDependency(), 
			attributesList: [],		// Set below
			dependencyStatus: getDependencyStatuses(),
			dependencyType: getDependencyTypes(), 
			event: params.moveEvent, 
			fixedFilter: (params.filter ? true : false),
			filter: params.filter,
			hasPerm: RolePermissions.hasPermission("AssetEdit"),
			justPlanning: userPreferenceService.getPreference("assetJustPlanning") ?: 'true',
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
			validation: params.validation
		]

		// Override the Use Just For Planning if a URL requests it (e.g. Planning Dashboard)
		/*
		// This was being added to correct the issue when coming from the Planning Dashboard but there are some ill-effects still
		if (params.justPlanning) 
			model.justPlanning=true
		*/
		
		def moveEvent = null
		if (params.moveEvent && params.moveEvent.isNumber()) {
			moveEvent = MoveEvent.findByProjectAndId(project, params.moveEvent )
		}
		model.moveEvent = moveEvent

		// Get the list of attributes that the user can select for columns
		def attributes = projectService.getAttributes(listType)

		// Create a list of the "custom##" fields that are currently selectable
		def projectCustoms = project.customFieldsShown+1
		def nonCustomList = project.customFieldsShown != Project.CUSTOM_FIELD_COUNT ? (projectCustoms..Project.CUSTOM_FIELD_COUNT).collect{"custom"+it} : []
		
		// Remove the non project specific attributes and sort them by attributeCode
		def appAttributes = attributes.findAll{ 
			it.attributeCode!="assetName" && 
			! (it.attributeCode in nonCustomList) && 
			! COLUMN_PROPS_TO_EXCLUDE[ac].contains(it.attributeCode)
		}

		// Used to display column names in jqgrid dynamically
		def modelPref = [:]
		fieldPrefs.each { key, value ->
			modelPref << [(key): getAttributeFrontendLabel(value, attributes.find{it.attributeCode==value}?.frontendLabel)]
		}
		model.modelPref = modelPref

		// Compose the list of Asset properties that the user can select and use for filters
		def attributesList= (appAttributes).collect{ attribute ->
			[attributeCode: attribute.attributeCode, frontendLabel: getAttributeFrontendLabel(attribute.attributeCode, attribute.frontendLabel)]
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
	 List getRoomSelectOptions(Project project, isSource, allowAdd=true) {
		def rsl = [ ]
		if (allowAdd)
			rsl << [id:-1, value: 'Add Room...']

		def rooms = Room.findAll('from Room r where r.project=:p and source=:s order by location, roomName', [p:project, s:(isSource ? 1 : 0)])
		rooms.each { rsl << [id:it.id, value:it.location+' / '+it.roomName] }
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
		def rsl = [ ]
		if (allowAdd)
			rsl << [id:-1, value: 'Add Rack...']

		roomId = NumberUtil.toLong(roomId)
		if (roomId) {
			def rooms = Rack.findAll('from Rack r inner join r.model m where r.project=:p and r.room.id=:r and m.assetType=:t order by tag', [p:project, r:roomId, t:'Rack'])
			rooms.each { rsl << [id:it[0].id, value:it[0].tag] }
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
				securityService.reportViolation("Attemped to assess room ($roomId') unassociated with project (${project.id})")

			} else {
				def roomProp = (room.source ? 'roomSource' : 'roomTarget')
				def chassisTypes = AssetType.getBladeChassisTypes()
				def chassisList = getAssetsWithCriteriaMap(project, AssetClass.DEVICE, [(roomProp): room, assetType: chassisTypes] )

				chassisList.each { rsl << [ id:it.id, value: "${it.assetName}/${it.assetTag}" ] }
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
				map = SqlUtil.whereExpression("a.$propName", value, propName)
			}

			if (map) {
				where.append(' and ' + map.sql)
				params.putAll((propName): map.param)
			} else {
				log.error "getAssetsWithCriteriaMap() SqlUtil.whereExpression() returned no value property $propName, criteria $value"
				return null
			}
		}

		String hql = from.toString() + where.toString()
		// log.debug "getAssetsWithCriteriaMap() HQL=$hql, params=$params"
		def assets = domainClass.findAll(hql, params)
		// log.debug "getAssetsWithCriteriaMap() found ${assets.size()} : $assets"

		if (assets && hasJoin) {
			if (! includeJoinData) {
				// Just get the Asset objects and exclude the joined domains
				assets = assets.collect { it[0] }
			}
		}
		return assets
	}

	/**
	 * Used to convert the Maint Expiration and Retire date parameters from strings to Dates
	 * @param params - the map of the params
	 */
	public void parseMaintExpDateAndRetireDate(Object params, userTimeZone) {
		params.maintExpDate = GormUtil.convertInToGMT( DateUtil.mdyToDate(params.maintExpDate), userTimeZone )
		params.retireDate   = GormUtil.convertInToGMT( DateUtil.mdyToDate(params.retireDate ), userTimeZone )
	}

	/**
	 * This method is used to get config by entityType and validation
	 * @param type,validation
	 * @return
	 */
	def getConfig (def type, def validation) {
		def project = securityService.getUserCurrentProject()
		def allconfig = projectService.getConfigByEntity(type)
		def fields = projectService.getFields(type) + projectService.getCustoms()
		def config = [:]
		def validationType
		def valList=ValidationType.getValuesAsMap()
		valList.each{v ->
			if(v.key==validation)
				validationType=v.value
		}
		fields.each{f ->
			if(allconfig[f.label])
			config << [(f.label):allconfig[f.label]['phase'][validationType]]
			
		}
		
		//used to hide the customs whose fieldImportance is "H"
		def customs = []
		def hiddenConfig = []
		(1..(project.customFieldsShown)).each{i->
			customs << i
			if(config.('custom'+i)=='H')
			hiddenConfig << i
		}
		customs.removeAll(hiddenConfig)
		
		return [project:project, config:config, customs:customs]
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
		if(byValue) {
			if (byValue.isNumber()) {
				byObj = Person.read(Long.parseLong(byValue))
			} else {
				byObj = ( stripPrefix && ['@','#'].contains(byValue[0])) ? byValue[1..-1] : byValue
			}
		}
		return byObj
	}

	/**
	 * This method is used to get assets by asset type
	 * @param assetType
	 * @return
	 */
	def getAssetsByType(assetType, project=null) {
		def entities = []
		def types

		if (!project) 
			project = securityService.getUserCurrentProject()

		if (assetType) {
			if (AssetType.getAllServerTypes().contains(assetType)) {
				types = AssetType.getAllServerTypes()
			} else if (AssetType.getStorageTypes().contains(assetType)) {
				types = AssetType.getStorageTypes()
			} else if ( [AssetType.APPLICATION.toString(), AssetType.DATABASE.toString()].contains(assetType)) {
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
			if (assetType=='Server' || assetType=='Blade' || assetType=='VM'){
			  entities = AssetEntity.findAll('from AssetEntity where assetType in (:type) and project = :project order by assetName asc ',
				  [type:["Server", "VM", "Blade"], project:project])
			} else if (assetType != 'Application' && assetType != 'Database' && assetType != 'Files'){
			  entities = AssetEntity.findAll('from AssetEntity where assetType not in (:type) and project = :project order by assetName asc ',
				  [type:["Server", "VM", "Blade", "Application", "Database", "Files"], project:project])
			} else{
			  entities = AssetEntity.findAll('from AssetEntity where assetType = ? and project = ? order by assetName asc ',[assetType, project])
			}
		}
		*/
		return entities
	}
	/**
	 * This method is used to delete assets by asset type
	 * @param type
	 * @param assetList - list of ids for which assets are requested to deleted
	 * @return
	 */
	def deleteBulkAssets(type, assetList){
		def resp
		def assetNames = []
		try{
			//Collecting as a list of data type long
			assetList = assetList.collect{ return Long.parseLong(it) }
			if(type == "dependencies"){
				def assetDeps = AssetDependency.findAllByIdInList(assetList)
				assetDeps.each{ad->
					assetNames << ad?.dependent?.assetName+"  AND Asset  "+ad?.asset?.assetName
					ad.delete()
				}
			}else{
				def assetEntity = AssetEntity.findAllByIdInList(assetList)
				assetEntity.each{ae->
					assetNames << ae.assetName
					deleteAsset(ae)
					ae.delete()
				}
			}
			def names = WebUtil.listAsMultiValueString( assetNames )
			resp = "$type $names deleted."
		}catch(Exception e){
				e.printStackTrace()
				resp = "Error while deleting $type"
		}
		return resp
	}
	/**
	 * This method is used to update assets bundle in dependencies
	 * @param project - the project that the user presently assigned to
	 * @param userLogin - the user making the change request
	 * @param entity - the AssetEntity (or other asset classes) to be reassigned
	 * @param moveBundleId - the id of the bundle to assign to
	 * @return Null if successful otherwise the error that occurred
	 */
	 // replaced with assignAssetToBundle
	/*
	private String updateBundle(Project project, UserLogin userLogin, AssetEntity entity, moveBundleId){
		String error
		Long id = NumberUtil.toLong(moveBundleId)
		if (! id) {
			error = "Invalid move bundle id was received"
		} else {
			MoveBundle mb = MoveBundle.get(id)
			if (!mb) {
				error = "Specified move bundle ($id) was not found"
			} else {
				if (mb.project.id !== project.id) {
					securityService.reportViolation("Attempted to assign asset (${entity.id}) to bundle ($id) not associated with project (${project.id})".toString())
					error = "Specified move bundle id ($id) was not found"
				} else {
					entity.moveBundle = mb
					if (! entity.validate() || ! entity.save(flush:true)) {
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
	def updateCablingOfAssets( modelAssetsList ) {
		modelAssetsList.each{ assetEntity->
			AssetCableMap.executeUpdate("Update AssetCableMap set cableStatus=?, assetTo=null, assetToPort=null where assetTo=?",[AssetCableStatus.UNKNOWN, assetEntity])
			
			AssetCableMap.executeUpdate("Delete from AssetCableMap where assetFrom=?",[assetEntity])

			assetEntityAttributeLoaderService.createModelConnectors( assetEntity )
		}
		/*existingAssetsList.each{ assetEntity->
			AssetCableMap.executeUpdate("""Update AssetCableMap set toAssetRack='${assetEntity.targetRack}',
					toAssetUposition=${assetEntity.targetRackPosition} where assetTo = ? """,[assetEntity])
		}*/
	}

	/*
	 * export cabling data.
	 * @param assetCablesList
	 * @param cablingSheet
	 */
	def cablingReportData( assetCablesList, cablingSheet ){
		for ( int r = 2; r <= assetCablesList.size(); r++ ) {
			cablingSheet.addCell( new Label( 0, r, String.valueOf(assetCablesList[r-2].assetFromPort.type )) )
			cablingSheet.addCell( new Label( 1, r, String.valueOf(assetCablesList[r-2].assetFrom ? assetCablesList[r-2].assetFrom?.id : "" )) )
			cablingSheet.addCell( new Label( 2, r, String.valueOf(assetCablesList[r-2].assetFrom ? assetCablesList[r-2].assetFrom.assetName : "" )) )
			cablingSheet.addCell( new Label( 3, r, String.valueOf(assetCablesList[r-2].assetFromPort.label )) )
			cablingSheet.addCell( new Label( 4, r, String.valueOf(assetCablesList[r-2].assetTo ? assetCablesList[r-2].assetTo?.id : "" )) )
			cablingSheet.addCell( new Label( 5, r, String.valueOf(assetCablesList[r-2].assetTo ? assetCablesList[r-2].assetTo?.assetName :"" )) )
			if(assetCablesList[r-2].assetFromPort.type !='Power'){
				cablingSheet.addCell( new Label( 6, r, String.valueOf(assetCablesList[r-2].assetToPort ? assetCablesList[r-2].assetToPort?.label :"" )) )
			}else{
				cablingSheet.addCell( new Label( 6, r, String.valueOf(assetCablesList[r-2].toPower?:"" )) )
			}
			cablingSheet.addCell( new Label( 7, r, String.valueOf(assetCablesList[r-2].cableComment?:"" )) )
			cablingSheet.addCell( new Label( 8, r, String.valueOf(assetCablesList[r-2].cableColor?:"" )) )
			if(assetCablesList[r-2].assetFrom.sourceRoom){
				cablingSheet.addCell( new Label( 9, r, String.valueOf(assetCablesList[r-2].assetFrom?.rackSource?.location+"/"+assetCablesList[r-2].assetFrom?.sourceRoom+"/"+assetCablesList[r-2].assetFrom?.sourceRack )) )
			}else if(assetCablesList[r-2].assetFrom.targetRoom){
				cablingSheet.addCell( new Label( 9, r, String.valueOf(assetCablesList[r-2].assetFrom?.rackTarget?.location+"/"+assetCablesList[r-2].assetFrom?.targetRoom+"/"+assetCablesList[r-2].assetFrom?.targetRack )) )
			}else{
				cablingSheet.addCell( new Label( 9, r, '') )
			}
			cablingSheet.addCell( new Label( 10, r, String.valueOf(assetCablesList[r-2].cableStatus?:"" )) )
			cablingSheet.addCell( new Label( 11, r, String.valueOf(assetCablesList[r-2].assetLoc?: "" )) )
		}
	}

	/**
	 * used to get the frontEndLabel of particular attribute
	 * @param attribute
	 * @return frontEndLabel
	 */
	def getAttributeFrontendLabel(attributeCode, frontendLabel){
		def project = securityService.getUserCurrentProject()
		return (attributeCode.contains('custom') && project[attributeCode])? project[attributeCode]:frontendLabel
	}
	/**
	 * Used to get the customised query based on the application preference
	 * @param appPref(List of key value column preferences)
	 * @return query,joinQuery
	 */
	def getAppCustomQuery(appPref){
		def query = ""
		def joinQuery = ""
		appPref.each{key,value->
			switch(value){
				case 'sme':
						query +="CONCAT(CONCAT(p.first_name, ' '), IFNULL(p.last_name,'')) AS sme,"
						joinQuery +="\n LEFT OUTER JOIN person p ON p.person_id=a.sme_id \n"
						break;
				case 'sme2':
						query +="CONCAT(CONCAT(p1.first_name, ' '), IFNULL(p1.last_name,'')) AS sme2,"
						joinQuery +="\n LEFT OUTER JOIN person p1 ON p1.person_id=a.sme2_id \n"
						break;
				case 'modifiedBy':
						query +="CONCAT(CONCAT(p2.first_name, ' '), IFNULL(p2.last_name,'')) AS modifiedBy,"
						joinQuery +="\n LEFT OUTER JOIN person p2 ON p2.person_id=ae.modified_by \n"
						break;
				case 'lastUpdated':
						query +="ee.last_updated AS ${value},"
						joinQuery +="\n LEFT OUTER JOIN eav_entity ee ON ee.entity_id=ae.asset_entity_id \n"
						break;
				case 'moveBundle':
						query +="mb.name AS moveBundle,"
						break;
				case 'event':
						query +="me.move_event_id AS event,"
						joinQuery +="\n LEFT OUTER JOIN move_event me ON me.move_event_id=mb.move_event_id \n"
						break;
				case 'appOwner':
						query +="CONCAT(CONCAT(p3.first_name, ' '), IFNULL(p3.last_name,'')) AS appOwner,"
						joinQuery +="\n LEFT OUTER JOIN person p3 ON p3.person_id= ae.app_owner_id \n"
						break;
				case ~/appVersion|appVendor|appTech|appAccess|appSource|license|businessUnit|appFunction|criticality|userCount|userLocations|useFrequency|drRpoDesc|drRtoDesc|shutdownFixed|moveDowntimeTolerance|testProc|startupProc|url|shutdownBy|shutdownDuration|startupBy|startupFixed|startupDuration|testingBy|testingFixed|testingDuration/:
						query +="a.${WebUtil.splitCamelCase(value)} AS ${value},"
						break;
				case ~/custom1|custom2|custom3|custom4|custom5|custom6|custom7|custom8|custom9|custom10|custom11|custom12|custom13|custom14|custom15|custom16|custom17|custom18|custom19|custom20|custom21|custom22|custom23|custom24|custom25|custom26|custom27|custom28|custom29|custom30|custom31|custom32|custom33|custom34|custom35|custom36|custom37|custom38|custom39|custom40|custom41|custom42|custom43|custom44|custom45|custom46|custom47|custom48|custom49|custom50|custom51|custom52|custom53|custom54|custom55|custom56|custom57|custom58|custom59|custom60|custom61|custom62|custom63|custom64/:
						query +="ae.${value} AS ${value},"
						break;
				case ~/validation|latency|planStatus/:
				break;
				default:
						query +="ae.${WebUtil.splitCamelCase(value)} AS ${value},"
			}
		}
		return [ query:query, joinQuery:joinQuery ]
	}
	/**
	 * Returns the default optional/customizable columns 
	 * @param prefName - the preference name for the various asset lists
	 * @return appPref
	 */
	 // TODO : JPM 9/2014 : Rename getExistingPref method to getColumnPreferences
	Map getExistingPref(prefName){
		def colPref
		def existingPref = userPreferenceService.getPreference(prefName)
		
		if (existingPref) {	
			// TODO : JPM 9/2014 : I'm assuming that the JSON.parse function could throw an error if the json is corrupt so there should be a try/catch
			colPref = JSON.parse(existingPref)
		}

		if (! colPref) {
		
			switch (prefName) {
				case 'App_Columns':
					colPref = ['1':'sme', '2':'validation', '3':'planStatus', '4':'moveBundle']
					break
				case 'Asset_Columns':
					colPref = ['1':'targetLocation','2':'targetRack','3':'assetTag','4':'serialNumber']
					break
				case 'Physical_Columns':
					colPref = ['1':'targetLocation','2':'targetRack','3':'assetTag','4':'serialNumber']
					break
				case 'Database_Columns':
					colPref = ['1':'dbFormat','2':'size','3':'planStatus','4':'moveBundle']
					break
				case 'Storage_Columns':
					colPref = ['1':'fileFormat','2':'size','3':'planStatus','4':'moveBundle']
					break
				case 'Task_Columns':
					colPref = ['1':'assetName','2':'assetType','3':'assignedTo','4':'role', '5':'category']
					break
				case 'Model_Columns':
					colPref = ['1':'description','2':'assetType','3':'powerUse','4':'modelConnectors']
					break
				case 'Dep_Columns':
					colPref = ['1':'frequency','2':'comment']
					break
			}
		}

		return colPref
	}

	/**
	 * Used to save cabling data to database while importing.
	 * 
	 */
	def saveImportCables(cablingSheet){
		def warnMsg=""
		def cablingSkipped = 0
		def cablingUpdated = 0
		def project = securityService.getUserCurrentProject()
		for ( int r = 2; r < cablingSheet.rows; r++ ) {
			int cols = 0 ;
			def isNew = false
			def cableType=cablingSheet.getCell( cols, r ).contents.replace("'","\\'")
			def fromAsset = AssetEntity.get(NumberUtils.toDouble(cablingSheet.getCell( ++cols, r ).contents.replace("'","\\'"), 0).round())
			def fromAssetName=cablingSheet.getCell( ++cols, r ).contents.replace("'","\\'")
			def fromConnectorLabel =cablingSheet.getCell( ++cols, r ).contents.replace("'","\\'")
			//if assetId is not there then get asset from assetname and fromConnector
			if(!fromAsset && fromAssetName){
				fromAsset = AssetEntity.findByAssetNameAndProject( fromAssetName, project)?.find{it.model.modelConnectors?.label.contains(fromConnectorLabel)}
			}
			def toAsset = AssetEntity.get(NumberUtils.toDouble(cablingSheet.getCell( ++cols, r ).contents.replace("'","\\'"), 0).round())
			def toAssetName=cablingSheet.getCell( ++cols, r ).contents.replace("'","\\'")
			def toConnectorLabel=cablingSheet.getCell( ++cols, r ).contents.replace("'","\\'")
			def toConnectorTemp
			if(cableType=='Power')
				toConnectorTemp = fromConnectorLabel
			else
				toConnectorTemp = toConnectorLabel
			//if toAssetId is not there then get asset from assetname and toConnectorLabel
			if(!toAsset && toAssetName){
				if(cableType!='Power')
					toAsset = AssetEntity.findByAssetNameAndProject( toAssetName, project)?.find{it.model.modelConnectors?.label.contains(toConnectorLabel)}
				else
					toAsset = fromAsset
			}
			def cableComment = cablingSheet.getCell( ++cols, r ).contents.replace("'","\\'")
			def cableColor = cablingSheet.getCell( ++cols, r ).contents.replace("'","\\'")
			def room = cablingSheet.getCell( ++cols, r ).contents.replace("'","\\'")
			def cableStatus = cablingSheet.getCell( ++cols, r ).contents.replace("'","\\'")
			def roomType = cablingSheet.getCell( ++cols, r ).contents.replace("'","\\'")
			if(fromAsset){
				def fromAssetConnectorsLabels= fromAsset.model?.modelConnectors?.label
				if(fromAssetConnectorsLabels && fromAssetConnectorsLabels?.contains(fromConnectorLabel)){
					def fromConnector = fromAsset.model?.modelConnectors.find{it.label == fromConnectorLabel}
					def assetCable = AssetCableMap.findByAssetFromAndAssetFromPort(fromAsset,fromConnector)
					if(toAsset){
						def toAssetconnectorLabels= toAsset.model?.modelConnectors?.label
						if(toAssetconnectorLabels.contains(toConnectorTemp)){
							if(cableType=='Power'){
								assetCable.toPower = toConnectorLabel
							}else{
								def toConnector = toAsset.model?.modelConnectors.find{it.label == toConnectorTemp}
								def previousCable = AssetCableMap.findByAssetToAndAssetToPort(toAsset,toConnector)
								if(previousCable !=assetCable){
									// Release the connection from other port to connect with FromPorts
									AssetCableMap.executeUpdate("""Update AssetCableMap set assetTo=null,assetToPort=null, cableColor=null
																			where assetTo = ? and assetToPort = ? """,[toAsset, toConnector])
								}
								assetCable.assetToPort = toConnector
							}
						}
						assetCable.assetTo = toAsset
					}else {
						assetCable.assetTo = null
						assetCable.assetToPort = null
						assetCable.toPower = ''
					}
					
					if(AssetCableMap.constraints.cableColor.inList.contains(cableColor))
						assetCable.cableColor = cableColor
						
					assetCable.cableComment = cableComment
					assetCable.cableStatus = cableStatus
					
					if(AssetCableMap.constraints.assetLoc.inList.contains(roomType))
						assetCable.assetLoc= roomType
						
					if(assetCable.dirtyPropertyNames.size()){
						assetCable.save(flush:true)
						cablingUpdated+=1
					}
					
				}else{
					warnMsg += "<li>row "+(r+1)+" with connector $fromConnectorLabel and Asset Name $fromAssetName does not exist & skipped.</li>"
					cablingSkipped+=1
				}
				
			}else{
				warnMsg += "<li>row "+(r+1)+" with connector $fromConnectorLabel and Asset Name $fromAssetName does not exist & skipped.</li>"
				cablingSkipped+=1
			}
		}
		return [warnMsg:warnMsg, cablingSkipped:cablingSkipped, cablingUpdated:cablingUpdated]
	}

	/**
	 * Used to create or fetch target asset cables.
	 *
	 */
	def createOrFetchTargetAssetCables(assetEntity){
		def cableExist = AssetCableMap.findAllByAssetFromAndAssetLoc(assetEntity,'T')?:[]
		if(!cableExist){
			def sourceCables=AssetCableMap.findAllByAssetFromAndAssetLoc(assetEntity,'S')
			sourceCables.each{
				def targetCable = new AssetCableMap()
				targetCable.cable = it.cable
				targetCable.cableStatus = AssetCableStatus.EMPTY
				targetCable.assetFrom = it.assetFrom
				targetCable.assetFromPort = it.assetFromPort
				targetCable.assetLoc= 'T'
				if(!targetCable.save(flush:true)){
					def etext = "Unable to create AssetCableMap" +
	                GormUtil.allErrorsString( targetCable )
					println etext
				}
				cableExist << targetCable
			}
		}
		return cableExist
	}

	/**
	 * used to add the css for the labels which fieldImportance is 'C','I'
	 * @param forWhom
	 * @param assetEntity
	 * @param configMap
	 * @return
	 */
	def getHighlightedInfo( forWhom, assetEntity, configMap){
		def fields = projectService.getFields(forWhom) + projectService.getCustoms()
		def highlightMap = [:]
		fields.each{f->
			def configMaps=configMap.config
			if(configMaps.(f.label) in ['C','I'] && (assetEntity && !assetEntity.(f.label))){
				highlightMap << [(f.label):'highField']
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
		def size = assetEntity.assetName?.size() ?: 0
		for (int i = 0; i < size; ++i)
			if (assetEntity.assetName[i] == "'")
				name = name + "\\'"
			else if (ignoreSingleQuotes && assetEntity.assetName[i] == '"')
				name = name + '\\"'
			else
				name = name + assetEntity.assetName[i]
		return name
	}

	def getManufacturers(assetType) {
		return Model.findAll("From Model where assetType = ? group by manufacturer order by manufacturer.name",[assetType])?.manufacturer
	}

	/**
	 * This method is used to sort model by status full, valid and new to display at Asset CRUD
	 * @param manufacturerInstance : instance of Manufacturer for which model list is requested
	 * @return : model list 
	 */
	def getModelSortedByStatus (Manufacturer mfr) {
		def models = [Validated:[], Unvalidated:[]]
		if (mfr) {
			def mfrList = Model.findAllByManufacturer( mfr, [sort:'modelName',order:'asc'] )
			def modelListFull = mfrList.findAll{it.modelStatus == 'full'}
			def modelListValid = mfrList.findAll{it.modelStatus == 'valid'}
			def modelListNew = mfrList.findAll{!['full','valid'].contains(it.modelStatus)}
			models.Validated = modelListValid
			models.Unvalidated = modelListFull+modelListNew
		}
		
		return models
	}

	def getRooms(project) {
		return Room.findAll("FROM Room WHERE project =:project order by location, roomName", [project:project])
	}


	/**
	 * Used to retrieve the values of DEVICE properties based on the attribute name
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
	 * Download data form Asset Entity table into Excel file
	 * @param Datatransferset,Project,Movebundle
	 **/
	void export(params) {
		def key = params.key
		def projectId = params.projectId

		// Helper closure that returns the size of an object or zero (0) if it is null
		def sizeOf = { obj -> (obj ? obj.size : 0)}

		try {
		
			java.io.File temp = java.io.File.createTempFile("assetEntityExport_" + UUID.randomUUID().toString(),".xls");
			temp.deleteOnExit();
			
			progressService.updateData(key, 'filename', temp.getAbsolutePath())
			
			
			def progressCount = 0
			def progressTotal = 0
			def missingHeader = ""
			
			//get project Id
			def dataTransferSet = params.dataTransferSet
			def bundleNameList = new StringBuffer()
			def bundles = []
			def principal = params.username
			def loginUser = UserLogin.findByUsername(principal)
			def started
			
			def bundle = params.bundle
			def bundleSize = bundle.size()
			bundleNameList.append(bundle[0] != "" ? (bundleSize==1 ? MoveBundle.read( bundle[0] ).name : bundleSize+'Bundles') : 'All')

			def dataTransferSetInstance = DataTransferSet.findById( dataTransferSet )

			def project = Project.get(projectId)
			if ( project == null) {
				progressService.update(key, 100, 'Cancelled', 'Project is required.')
				return;
			}

			// Maps for each class for mapping spreadsheet column names to the domain properties
			def serverDTAMap, appDTAMap, dbDTAMap, fileDTAMap

			// Will hold the list of the assets for each of the classes
			List asset, application, database, files

			def assetEntityInstance

			def serverSheet
			def appSheet
			def dbSheet
			def storageSheet
			def titleSheet
			def exportedEntity = ""

			def serverMap = [:]
			def serverSheetColumnNames = [:]
			def serverColumnNameList = new ArrayList()
			def serverSheetNameMap = [:]
			def serverDataTransferAttributeMapSheetName

			def appMap = [:]
			def appSheetColumnNames = [:]
			def appColumnNameList = new ArrayList()
			def appSheetNameMap = [:]
			def appDataTransferAttributeMapSheetName

			def dbMap = [:]
			def dbSheetColumnNames = [:]
			def dbColumnNameList = new ArrayList()
			def dbSheetNameMap = [:]
			def dbDataTransferAttributeMapSheetName

			def fileMap = [:]
			def storageSheetColumnNames = [:]
			def fileColumnNameList = new ArrayList()
			def storageSheetNameMap = [:]
			def fileDataTransferAttributeMapSheetName


			// Flags to indicate which tabs to export based on which checkboxes selected in the UI
			boolean doDevice = params.asset=='asset'
			boolean doApp = params.application=='application'
			boolean doDB = params.database=='database'
			boolean doStorage = params.files=='files'
			boolean doDependency = params.dependency=='dependency'
			boolean doRoom = params.room=='room'
			boolean doRack = params.rack=='rack'
			boolean doCabling = params.cabling=='cable'
			boolean doComment = params.comment=='comment'

			//
			// Load the asset lists and property map files based on ALL or selected bundles for the classes selected for export
			//
			String query = ' WHERE d.project=:project '
			Map queryParams = [project:project]
			// Setup for multiple bundle selection
			if (bundle[0]) {
				for ( int i=0; i< bundleSize ; i++ ) {
					bundles << bundle[i].toLong()
				}
				query += ' AND d.moveBundle.id IN (:bundles)'
				queryParams.bundles = bundles
			}
			if ( doDevice ) {
				asset = AssetEntity.findAll('FROM AssetEntity d' + query + " AND d.assetClass='${AssetClass.DEVICE}'", queryParams)
			}
			if ( doApp ) {
				application = Application.findAll('From Application d' + query,  queryParams)
				// application = Application.findAll( "from Application m where m.project = :project and m.moveBundle.id in ( :bundles )", [project:project, bundles: bundles] )
			}
			if ( doDB ) {
				database = Database.findAll('From Database d' + query,  queryParams)
				// database = Database.findAll( "from Database m where m.project = :project and m.moveBundle.id in ( :bundles )",	[project:project, bundles: bundles] )
			}
			if ( doStorage ) {
				files = Files.findAll('From Files d' + query,  queryParams)
				// files = Files.findAll( "from Files m where m.project = :project and m.moveBundle.id in ( :bundles )", [project:project, bundles: bundles] )
			}

			// Have to load the maps because we update the column names across the top for all sheets
			serverDTAMap = DataTransferAttributeMap.findAllByDataTransferSetAndSheetName( dataTransferSetInstance,"Devices" )
			appDTAMap =  DataTransferAttributeMap.findAllByDataTransferSetAndSheetName( dataTransferSetInstance,"Applications" )
			dbDTAMap =  DataTransferAttributeMap.findAllByDataTransferSetAndSheetName( dataTransferSetInstance,"Databases" )
			fileDTAMap =  DataTransferAttributeMap.findAllByDataTransferSetAndSheetName( dataTransferSetInstance,"Files" )
			
			// Compute the total assets to be exported, used for progress meter
			progressTotal = sizeOf(asset) + sizeOf(application) + sizeOf(database) + sizeOf(files)
			
			//get template Excel
			def workbook
			def book
			started = new Date()
			def assetDepBundleList = AssetDependencyBundle.findAllByProject(project)
			def filenametoSet = dataTransferSetInstance.templateFilename
			def file =  ApplicationHolder.application.parentContext.getResource(filenametoSet).getFile()
			// Going to use temporary file because we were getting out of memory errors constantly on staging server

			//set MIME TYPE as Excel
			def exportType = filenametoSet.split("/")[2]
			def masterIndex = exportType.indexOf("Master_template.xls")
			if (masterIndex != -1) {
				exportType = exportType.substring(0, masterIndex)
			}

			SimpleDateFormat exportFileFormat = new SimpleDateFormat("yyyyMMdd")
			SimpleDateFormat stdDateFormat = new SimpleDateFormat("MM-dd-yyyy")

			def tzId = params.tzId
			def currDate = TimeUtil.convertInToUserTZ(TimeUtil.nowGMT(),tzId)
			def exportDate = exportFileFormat.format(currDate)
			def filename = project?.name?.replace(" ","_")+"-"+bundleNameList.toString()

			log.info "export() - Loading appDepBundle took ${TimeUtil.elapsed(started)}"
			started = new Date()

			//create workbook and sheet
			WorkbookSettings wbSetting = new WorkbookSettings()
			wbSetting.setUseTemporaryFileDuringWrite(true)
			workbook = Workbook.getWorkbook( file, wbSetting )
			book = Workbook.createWorkbook( new FileOutputStream(temp), workbook )
			log.info "export() - Creating workbook took ${TimeUtil.elapsed(started)}"
			started = new Date()

			def sheetNames = book.getSheetNames() as List

			// Helper closure used to retrieve the s
			def getWorksheet = { sheetName ->
				if (sheetNames.contains(sheetName)) {
					def index = sheetNames.findIndexOf( { it == sheetName } )
					return book.getSheet( sheetNames[index] )
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
				serverMap.put( item.columnName, null )
				serverColumnNameList.add(item.columnName)
				serverSheetNameMap.put( "sheetName", item.sheetName?.trim() )
			}
			serverMap.put("DepGroup", null )
			serverColumnNameList.add("DepGroup")
			serverSheet = getWorksheet('Devices')
			serverCol = serverSheet.getColumns()
			for ( int c = 0; c < serverCol; c++ ) {
				def serverCellContent = serverSheet.getCell( c, 0 ).contents
				serverSheetColumnNames.put(serverCellContent, c)
				if( serverMap.containsKey( serverCellContent ) ) {
					serverMap.put( serverCellContent, c )
				}
			}

			// Application
			appDTAMap.eachWithIndex { item, pos ->
				appMap.put( item.columnName, null )
				appColumnNameList.add(item.columnName)
				appSheetNameMap.put( "sheetName", item.sheetName?.trim() )
			}
			appMap.put("DepGroup", null )
			appColumnNameList.add("DepGroup")
			appSheet = getWorksheet('Applications')
			appCol = appSheet.getColumns()
			for ( int c = 0; c < appCol; c++ ) {
				def appCellContent = appSheet.getCell( c, 0 ).contents
				appSheetColumnNames.put(appCellContent, c)
				if( appMap.containsKey( appCellContent ) ) {
					appMap.put( appCellContent, c )

				}
			}

			// Database
			dbDTAMap.eachWithIndex { item, pos ->
				dbMap.put( item.columnName, null )
				dbColumnNameList.add(item.columnName)
				dbSheetNameMap.put( "sheetName", item.sheetName?.trim() )
			}
			dbMap.put("DepGroup", null )
			dbColumnNameList.add("DepGroup")
			dbSheet = getWorksheet('Databases')
			dbCol = dbSheet.getColumns()
			for ( int c = 0; c < dbCol; c++ ) {
				def dbCellContent = dbSheet.getCell( c, 0 ).contents
				dbSheetColumnNames.put(dbCellContent, c)
				if( dbMap.containsKey( dbCellContent ) ) {
					dbMap.put( dbCellContent, c )
				}
			}

			// Storage
			fileDTAMap.eachWithIndex { item, pos ->
				fileMap.put( item.columnName, null )
				fileColumnNameList.add(item.columnName)
				storageSheetNameMap.put( "sheetName", item.sheetName?.trim() )
			}
			fileMap.put("DepGroup", null )
			fileColumnNameList.add("DepGroup")
			storageSheet = getWorksheet('Storage')
			filesCol = storageSheet.getColumns()
			for ( int c = 0; c < filesCol; c++ ) {
				def fileCellContent = storageSheet.getCell( c, 0 ).contents
				storageSheetColumnNames.put(fileCellContent, c)
				if( fileMap.containsKey( fileCellContent ) ) {
					fileMap.put( fileCellContent, c )
				}
			}

			log.info "export() - Valdating columns took ${TimeUtil.elapsed(started)}"
			started = new Date()

			// Helper closure to create a text list from an array for debugging
			def xportList = { list ->
				def out = ''
				def x = 0
				list.each { out += "$x=$it\n"; x++}
				return out
			}

			//calling method to check for Header
			def serverCheckCol = checkHeader( serverColumnNameList, serverSheetColumnNames, missingHeader )
			def appCheckCol = checkHeader( appColumnNameList, appSheetColumnNames, missingHeader )
			def dbCheckCol = checkHeader( dbColumnNameList, dbSheetColumnNames, missingHeader )
			def filesCheckCol = checkHeader( fileColumnNameList, storageSheetColumnNames, missingHeader )
			// Statement to check Headers if header are not found it will return Error message
			if ( serverCheckCol == false || appCheckCol == false || dbCheckCol == false || filesCheckCol == false) {
				missingHeader = missingHeader.replaceFirst(",","")
				//flash.message = " Column Headers : ${missingHeader} not found, Please check it."
				//redirect( action:assetImport, params:[message:flash.message] )

				progressService.update(key, 100, 'Cancelled', " Column Headers : ${missingHeader} not found, Please check it.")

				return
			} else {
				//Add Title Information to master SpreadSheet
				titleSheet = book.getSheet("Title")
				SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a");
				if(titleSheet != null) {
					def titleInfoMap = new ArrayList();
					titleInfoMap.add (project.client )
					titleInfoMap.add( projectId )
					titleInfoMap.add( project.name )
					titleInfoMap.add( partyRelationshipService.getProjectManagers(projectId) )
					titleInfoMap.add( format.format( currDate ) )
					titleInfoMap.add( loginUser.person )
					titleInfoMap.add( bundleNameList )
					partyRelationshipService.exportTitleInfo(titleInfoMap,titleSheet)
					titleSheet.addCell(new Label(0,30,"Note: All times are in ${tzId ? tzId : 'EDT'} time zone"))
				}

				//update data from Asset Entity table to EXCEL
				def assetSize = sizeOf(asset)
				def appSize = sizeOf(application)
				def dbSize = sizeOf(database)
				def fileSize = sizeOf(files)
				def serverColumnNameListSize = sizeOf(serverColumnNameList)
				def appcolumnNameListSize = sizeOf(appColumnNameList)
				def dbcolumnNameListSize = sizeOf(dbColumnNameList)
				def filecolumnNameListSize = fileColumnNameList.size()

				// update column header
				updateColumnHeaders(serverSheet, serverDTAMap, serverSheetColumnNames, project)
				updateColumnHeaders(appSheet, appDTAMap, appSheetColumnNames, project)
				updateColumnHeaders(dbSheet, dbDTAMap, dbSheetColumnNames, project)
				updateColumnHeaders(storageSheet, fileDTAMap, storageSheetColumnNames, project)

				log.info "export() - Updating spreadsheet headers took ${TimeUtil.elapsed(started)}"
				started = new Date()
				
				log.debug "Device Export - serverColumnNameList=$serverColumnNameList"

				//
				// Device Export
				//
				if ( doDevice ) {
					exportedEntity += "S"
					for ( int r = 1; r <= assetSize; r++ ) {
						progressCount++
						progressService.update(key, ((int)((progressCount / progressTotal) * 100)), 'In progress')
						
						//Add assetId for walkthrough template only.
						if( serverSheetColumnNames.containsKey("assetId") ) {
							def integerFormat = new WritableCellFormat (NumberFormats.INTEGER)
							def addAssetId = new Number(0, r, (asset[r-1].id))
							serverSheet.addCell( addAssetId )
						}
						
						for ( int coll = 0; coll < serverColumnNameListSize; coll++ ) {
							def addContentToSheet
							def attribute = serverDTAMap.eavAttribute.attributeCode[coll]
							def colName = serverColumnNameList.get(coll)
							def colNum = serverMap[colName]
							def a = asset[r-1]

							if (r==1)
								log.debug "Device Export - attribute=$attribute, colName=$colName, colNum=$colNum"

							if (attribute && a.(serverDTAMap.eavAttribute.attributeCode[coll]) == null ) {
								// Skip populating the cell if the value is null
								continue
							}

							switch(colName) {
								case 'DepGroup':
									// TODO : JPM 9/2014 : Should load the dependency bundle list into memory so we don't do queries for each record
									def depGroup = assetDepBundleList.find{it.asset.id==a.id}?.dependencyBundle?.toString()
									addContentToSheet = new Label(colNum, r, StringUtil.defaultIfEmpty(depGroup, ''))
									break
								case ~/usize|SourcePos|TargetPos/:
									def pos = a[attribute] ?: 0
									// Don't bother populating position if it is a zero
									if (pos == 0) 
										continue
									addContentToSheet = new jxl.write.Number(colNum, r, (Double)pos )
									break
								case ~/Retire|MaintExp|Modified Date/:
									addContentToSheet = new Label(colNum, r, stdDateFormat.format(a[attribute]) )
									break

								case ~/Source Blade|Target Blade/:
									def chassis = a[attribute]
									def value = ""
									if (chassis) {
										value = "id:" + chassis.id + " " + chassis.assetName
									}
									addContentToSheet = new Label(colNum, r, value)
									break

								default:
									def value = StringUtil.defaultIfEmpty( String.valueOf(a[attribute]), '')
									addContentToSheet = new Label(colNum, r, value)
							}

							serverSheet.addCell( addContentToSheet )
						}
					}
					log.info "export() - processing assets took ${TimeUtil.elapsed(started)}"
					started = new Date()
				}

				//
				// Application Export
				//
				if ( doApp ) {
					exportedEntity += 'A'

					// This determines which columns are added as Number vs Label
					def numericCols = []

					// Flag to know if the AppId Column exists
					def idColName = 'appId'
					def hasIdCol = appSheetColumnNames.containsKey(idColName)

					def rowNum = 0
					application.each { app ->
						progressCount++
						progressService.update(key, ((int)((progressCount / progressTotal) * 100)), 'In progress')
						rowNum++

						// Add the appId column to column 0 if it exists
						if (hasIdCol) {
							appSheet.addCell( new jxl.write.Number(appSheetColumnNames[idColName], rowNum, (Double)app.id) )
						}
								
						for (int i=0; i < appColumnNameList.size(); i++) {
							def colName = appColumnNameList[i]

							// If the column isn't in the spreadsheet we'll skip over it
							if ( ! appSheetColumnNames.containsKey(colName)) {
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
									colVal = assetDepBundleList.find {it.asset.id == app.id }?.dependencyBundle?.toString() ?: ''
									break
								case ~/ShutdownBy|StartupBy|TestingBy/:
									colVal = app[assetColName] ? this.resolveByName(app[assetColName], false)?.toString() : ''
									break
								case ~/ShutdownFixed|StartupFixed|TestingFixed/:
									colVal = app[assetColName] ? 'Yes' : 'No'
									//log.info "export() : field class type=$app[assetColName].className()}"
									break
								case ~/Retire|MaintExp|Modified Date/:
									colVal = app[assetColName] ? stdDateFormat.format(app[assetColName]) : ''
									break
								default:
									colVal = app[assetColName]
							}

							// log.info("export() : rowNum=$rowNum, colNum=$colNum, colVal=$colVal")

							if ( colVal != null) {

								if (colVal?.class.name == 'Person') {
									colVal = colVal.toString()
								}

								def cell
								if ( numericCols.contains(colName) )
									cell = new Number(colNum, rowNum, (Double)colVal)
								else
									cell = new Label( colNum, rowNum, String.valueOf(colVal) )

								appSheet.addCell( cell )
							}
						}
					}
					log.info "export() - processing apps took ${TimeUtil.elapsed(started)}"
					started = new Date()
				}

				//
				// Database
				//
				if ( doDB ) {
					exportedEntity += "D"
					for ( int r = 1; r <= dbSize; r++ ) {
						progressCount++
						progressService.update(key, ((int)((progressCount / progressTotal) * 100)), 'In progress')
						//Add assetId for walkthrough template only.
						if( dbSheetColumnNames.containsKey("dbId") ) {
							def integerFormat = new WritableCellFormat (NumberFormats.INTEGER)
							def addDBId = new Number(0, r, (database[r-1].id))
							dbSheet.addCell( addDBId )
						}
						for ( int coll = 0; coll < dbcolumnNameListSize; coll++ ) {
							def addContentToSheet
							def attribute = dbDTAMap.eavAttribute.attributeCode[coll]
							//if attributeCode is sourceTeamMt or targetTeamMt export the teamCode
							def colName = dbColumnNameList.get(coll)
							if(colName == "DepGroup"){
								def depGroup = assetDepBundleList.find{it.asset.id==database[r-1].id}?.dependencyBundle?.toString()
								addContentToSheet = new Label(dbMap[colName], r, depGroup ?:"" )
							} else if(attribute in ["retireDate", "maintExpDate", "lastUpdated"]){
								addContentToSheet = new Label(dbMap[colName], r, (database[r-1].(dbDTAMap.eavAttribute.attributeCode[coll]) ? stdDateFormat.format(database[r-1].(dbDTAMap.eavAttribute.attributeCode[coll])) :''))
							} else if ( database[r-1][attribute] == null ) {
								addContentToSheet = new Label(  dbMap[colName], r, "" )
							}else {
								if( bundleMoveAndClientTeams.contains(dbDTAMap.eavAttribute.attributeCode[coll]) ) {
									addContentToSheet = new Label( dbMap[colName], r, String.valueOf(database[r-1].(dbDTAMap.eavAttribute.attributeCode[coll]).teamCode) )
								}else {
									addContentToSheet = new Label( dbMap[colName], r, String.valueOf(database[r-1].(dbDTAMap.eavAttribute.attributeCode[coll])) )
								}
							}
							dbSheet.addCell( addContentToSheet )
						}
					}
					log.info "export() - processing databases took ${TimeUtil.elapsed(started)}"
					started = new Date()
				}

				//
				// Storage ( files )
				//
				if ( doStorage ) {
					exportedEntity += "F"
					for ( int r = 1; r <= fileSize; r++ ) {
						progressCount++
						progressService.update(key, ((int)((progressCount / progressTotal) * 100)), 'In progress')
						
						// Add assetId for walkthrough template only.
						if ( storageSheetColumnNames.containsKey("filesId") ) {
							def integerFormat = new WritableCellFormat (NumberFormats.INTEGER)
							def addFileId = new Number(0, r, (files[r-1].id))
							storageSheet.addCell( addFileId )
						}

						for ( int coll = 0; coll < filecolumnNameListSize; coll++ ) {
							def addContentToSheet
							def attribute = fileDTAMap.eavAttribute.attributeCode[coll]
							def colName = fileColumnNameList.get(coll)
							if (colName == "DepGroup") {
								def depGroup = assetDepBundleList.find{it.asset.id==files[r-1].id}?.dependencyBundle?.toString()
								addContentToSheet = new Label(fileMap[colName], r, depGroup ?:"" )
							} else if(attribute == "retireDate" || attribute == "maintExpDate" || attribute == "lastUpdated"){
								addContentToSheet = new Label(fileMap[colName], r, (files[r-1].(fileDTAMap.eavAttribute.attributeCode[coll]) ? stdDateFormat.format(files[r-1].(fileDTAMap.eavAttribute.attributeCode[coll])) :''))
							} else if ( files[r-1][attribute] == null ) {
								addContentToSheet = new Label( fileMap[colName], r, "" )
							} else {
								addContentToSheet = new Label( fileMap[colName], r, String.valueOf(files[r-1].(fileDTAMap.eavAttribute.attributeCode[coll])) )
							}

							storageSheet.addCell( addContentToSheet )
						}
					}
					log.info "export() - processing storage took ${TimeUtil.elapsed(started)}"
					started = new Date()
				}
				
				//
				// Dependencies
				//
				if ( doDependency ) {
					exportedEntity += "X"
					def dependencySheet = getWorksheet('Dependencies')

					def assetDependent = AssetDependency.findAll("from AssetDependency where asset.project = ? ",[project])
					def dependencyMap = ['AssetId':1,'AssetName':2,'AssetType':3,'DependentId':4,'DependentName':5,'DependentType':6,'Type':7, 'DataFlowFreq':8, 'DataFlowDirection':9, 'status':10, 'comment':11, 'c1':12, 'c2':13, 'c3':14, 'c4':15]
					def dependencyColumnNameList = ['AssetId','AssetName','AssetType','DependentId','DependentName','DependentType','Type', 'DataFlowFreq', 'DataFlowDirection', 'status', 'comment', 'c1', 'c2', 'c3', 'c4']
					def DTAMap = [0:'asset',1:'assetName',2:'assetType',3:'dependent',4:'dependentName',5:'dependentType',6:'type', 7:'dataFlowFreq', 8:'dataFlowDirection', 9:'status', 10:'comment', 11:'c1', 12:'c2', 13:'c3', 14:'c4']
					//def newDTA = ['assetName','assetType','dependentName','dependentType']
					def dependentSize = assetDependent.size()
					for ( int r = 1; r <= dependentSize; r++ ) {
						//Add assetId for walkthrough template only.
						def integerFormat = new WritableCellFormat (NumberFormats.INTEGER)
						def addAssetDependentId = new Number(0, r, (assetDependent[r-1].id))
						dependencySheet.addCell( addAssetDependentId )

						for ( int coll = 0; coll < 11; coll++ ) {
							def addContentToSheet
							switch(DTAMap[coll]){
								case "assetName":
									addContentToSheet = new Label( dependencyMap[dependencyColumnNameList.get(coll)], r, assetDependent[r-1].asset ? String.valueOf(assetDependent[r-1].(DTAMap[0])?.assetName) :"")
								break;
								case "assetType":
									addContentToSheet = new Label( dependencyMap[dependencyColumnNameList.get(coll)], r, assetDependent[r-1].asset ? String.valueOf(assetDependent[r-1].(DTAMap[0])?.assetType) :"" )
								break;
								case "dependentName":
									addContentToSheet = new Label( dependencyMap[dependencyColumnNameList.get(coll)], r, assetDependent[r-1].dependent ? String.valueOf(assetDependent[r-1].(DTAMap[3]).assetName) : "" )
								break;
								case "dependentType":
									addContentToSheet = new Label( dependencyMap[dependencyColumnNameList.get(coll)], r, assetDependent[r-1].dependent ? String.valueOf(assetDependent[r-1].(DTAMap[3]).assetType) : "")
								break;
								case "dependent":
									addContentToSheet = new Label( dependencyMap[dependencyColumnNameList.get(coll)], r, assetDependent[r-1].(DTAMap[coll]) ? String.valueOf(assetDependent[r-1].(DTAMap[coll]).id) : "")
								break;
								case "asset":
									addContentToSheet = new Label( dependencyMap[dependencyColumnNameList.get(coll)], r, assetDependent[r-1].(DTAMap[coll]) ? String.valueOf(assetDependent[r-1].(DTAMap[coll]).id) : "")
								break;
								default:
									addContentToSheet = new Label( dependencyMap[dependencyColumnNameList.get(coll)], r, assetDependent[r-1].(DTAMap[coll]) ? String.valueOf(assetDependent[r-1].(DTAMap[coll])) : "" )
							}
							dependencySheet.addCell( addContentToSheet )
						}
					}
					log.info "export() - processing dependencies took ${TimeUtil.elapsed(started)}"
					started = new Date()
				}

				//
				// Room Export
				//
				if( doRoom ) {
					exportedEntity += "R"

					def roomSheet = getWorksheet('Room')

					def formatter = new SimpleDateFormat("MM/dd/yyyy")
					def rooms = Room.findAllByProject(project)
					def roomSize = rooms.size()
					def roomMap = ['roomId':'id', 'Name':'roomName', 'Location':'location', 'Depth':'roomDepth', 'Width':'roomWidth',
								   'Source':'source', 'Address':'address', 'City':'city', 'Country':'country', 'StateProv':'stateProv',
								   'Postal Code':'postalCode', 'Date Created':'dateCreated', 'Last Updated':'lastUpdated'
								  ]
					
					def roomCol = storageSheet.getColumns()
					def roomSheetColumns = []
					for ( int c = 0; c < roomCol; c++ ) {
						def roomCellContent = roomSheet.getCell( c, 0 ).contents
						roomSheetColumns << roomCellContent
					}
					roomSheetColumns.removeAll('')
					for ( int r = 1; r <= roomSize; r++ ) {
						roomSheetColumns.eachWithIndex{column, i->
							def addContentToSheet
							if(column == 'roomId'){
								def integerFormat = new WritableCellFormat (NumberFormats.INTEGER)
								addContentToSheet = new Number(0, r, (rooms[r-1].id))
							} else {
							   if(column=='Date Created' || column=='Last Updated')
									   addContentToSheet = new Label( i, r, rooms[r-1]."${roomMap[column]}" ?
										   String.valueOf( formatter.format(rooms[r-1]."${roomMap[column]}")): "" )
							   else if(column =="Source")
									 addContentToSheet = new Label( i, r, String.valueOf(rooms[r-1]."${roomMap[column]}" ==1 ? "Source" : "Target" ) )
							   else
									addContentToSheet = new Label( i, r, String.valueOf(rooms[r-1]."${roomMap[column]}"?: "" ) )
							}
							roomSheet.addCell( addContentToSheet )
						}
				  }
				  log.info "export() - processing rooms took ${TimeUtil.elapsed(started)}"
				  started = new Date()
				}
				
				//
				// Rack Export
				//
				if( doRack ) {
					exportedEntity +="r"

					def rackSheet = getWorksheet('Rack')

					def racks = Rack.findAllByProject(project)
					def rackSize = racks.size()
					def rackMap = ['rackId':'id', 'Tag':'tag', 'Location':'location', 'Room':'room', 'RoomX':'roomX',
								   'RoomY':'roomY', 'PowerA':'powerA', 'PowerB':'powerB', 'PowerC':'powerC', 'Type':'rackType',
								   'Front':'front', 'Model':'model', 'Source':'source', 'Model':'model'
								  ]
					
					def rackCol = storageSheet.getColumns()
					def rackSheetColumns = []
					for ( int c = 0; c < rackCol; c++ ) {
						def rackCellContent = rackSheet.getCell( c, 0 ).contents
						rackSheetColumns << rackCellContent
					}
					rackSheetColumns.removeAll('')
					for ( int r = 1; r <= rackSize; r++ ) {
						rackSheetColumns.eachWithIndex{column, i->
							def addContentToSheet
							if(column == 'rackId'){
								def integerFormat = new WritableCellFormat (NumberFormats.INTEGER)
								addContentToSheet = new Number(0, r, (racks[r-1].id))
							} else {
							   if(column =="Source")
									 addContentToSheet = new Label( i, r, String.valueOf(racks[r-1]."${rackMap[column]}" ==1 ? "Source" : "Target" ) )
							   else
									addContentToSheet = new Label( i, r, String.valueOf(racks[r-1]."${rackMap[column]}"?: "" ) )
							}
							rackSheet.addCell( addContentToSheet )
						}
				  }
				  log.info "export() - processing racks took ${TimeUtil.elapsed(started)}"
				  started = new Date()
				}
				
			}

			//
			// Cabling Export
			// 
			if ( doCabling ) {
				exportedEntity += "c"

				def cablingSheet = getWorksheet("Cabling")

				def cablingList = AssetCableMap.findAll( "from AssetCableMap acm where acm.assetFrom.project.id = $project.id " )

				log.debug("Cabling found ${sizeOf(cablingList)} mappings")

				cablingReportData(cablingList, cablingSheet)
			}

			//
			// Comments Export
			// 
			if ( doComment ) {

				exportedEntity += "M"

				def commentSheet = getWorksheet("Comments")

				def commentIt = new ArrayList()
				SimpleDateFormat createDateFormat = new SimpleDateFormat("MM/dd/yyyy")
				def allAssets

				// TODO : JPM 9/2014 : The way that 
				if (bundle[0] == "" ) {
					allAssets = AssetEntity.findAllByProject( project, params )
				} else {
					allAssets = AssetEntity.findAll( "from AssetEntity m where m.project = :project and m.moveBundle.id in ( :bundles ) ",
						[project:project, bundles: bundles])
				}
				
				allAssets.each{
					commentIt.add(it.id)
				}

				def commentList = new StringBuffer()
				def commentSize = commentIt.size()
				for ( int k=0; k< commentSize ; k++ ) {
					if( k != commentSize - 1) {
						commentList.append( commentIt[k] + "," )
					} else {
						commentList.append( commentIt[k] )
					}
				}
				if (commentList) {
					def assetcomment = AssetComment.findAll("from AssetComment cmt where cmt.assetEntity in ($commentList) and cmt.commentType = 'comment'")
					def assetId
					def createdDate
					def createdBy
					def commentId
					def category
					def comment
					for(int cr=1 ; cr<=assetcomment.size() ; cr++){
						commentId = new Label(0,cr,String.valueOf(assetcomment[cr-1].id))
						commentSheet.addCell(commentId)
						assetId = new Label(1,cr,String.valueOf(assetcomment[cr-1].assetEntity.id))
						commentSheet.addCell(assetId)
						category = new Label(2,cr,String.valueOf(assetcomment[cr-1].category))
						commentSheet.addCell(category)
						createdDate = new Label(3,cr,String.valueOf(assetcomment[cr-1].dateCreated? createDateFormat.format(assetcomment[cr-1].dateCreated) : ''))
						commentSheet.addCell(createdDate)
						createdBy = new Label(4,cr,String.valueOf(assetcomment[cr-1].createdBy))
						commentSheet.addCell(createdBy)
						comment = new Label(5,cr,String.valueOf(assetcomment[cr-1].comment))
						commentSheet.addCell(comment)
					}
				}
			}

			//
			// Wrap up the process
			// 
			filename += "-"+exportedEntity+"-"+exportDate
			book.write()
			book.close()
			progressService.updateData(key, 'header', "attachment; filename=\""+exportType+'-'+filename+".xls\"")
			progressService.update(key, 100, 'Completed')
		
		} catch( Exception exportExp ) {

			exportExp.printStackTrace()
			progressService.update(key, 100, 'Cancelled', "An unexpected exception occurred while exporting to Excel")

			return
		}
	}
	
	/* -------------------------------------------------------
	 * To check the sheet headers
	 * @param attributeList, SheetColumnNames
	 * @author Mallikarjun
	 * @return bollenValue
	 *------------------------------------------------------- */
	def checkHeader( def list, def serverSheetColumnNames, missingHeader = "") {
		def listSize = list.size()
		for ( int coll = 0; coll < listSize; coll++ ) {
			if( serverSheetColumnNames.containsKey( list[coll] ) || list[coll] == "DepGroup") {
				//Nonthing to perform.
			} else {
				missingHeader = missingHeader + ", " + list[coll]
			}
		}
		if( missingHeader == "" ) {
			return true
		} else {
			return false
		}
	}
	
	/**
	 * This method is used to update sheet's column header with custom labels
	 * @param sheet : sheet's instance
	 * @param entityDTAMap : dataTransferEntityMap for entity type
	 * @param sheetColumnNames : column Names
	 * @param project : project instance
	 * @return
	 */
	 // TODO : JPM 9/2014 : The updateColumnHeaders probable won't work beyond 24 custom columns how this is written - should use regex test instead
	 // customLabels is defined as a static at the top
   	def updateColumnHeaders(sheet, entityDTAMap, sheetColumnNames, project){
	   	for ( int head =0; head <= sheetColumnNames.size(); head++ ) {
		   def cellData = sheet.getCell(head,0)?.getContents()
		   def attributeMap = entityDTAMap.find{it.columnName ==  cellData }?.eavAttribute
		   if (attributeMap?.attributeCode && customLabels.contains( cellData )) {
			   def columnLabel = project[attributeMap?.attributeCode] ? project[attributeMap?.attributeCode] : cellData
			   def customColumn = new Label(head,0, columnLabel )
			   sheet.addCell(customColumn)
		   }
	   }
	   return sheet
   }


   /**
    * Used by the AssetEntity List to populate the initial List view
    */
   Map getDeviceModelForList(Project project, UserLogin userLogin, session, params, tzId) {
		def filters = session.AE?.JQ_FILTERS
		session.AE?.JQ_FILTERS = []

		// def prefType = (listType=='server') ? 'Asset_Columns' : 'Physical_Columns'
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
		model.prefType = prefType 
		model.serialNumber = filters?.serialNumberFilter ?:'' 
		model.sourceLocation = filters?.sourceLocationFilter ?:'' 
		model.sourceRack = filters?.sourceRackFilter ?:''
		model.targetLocation = filters?.targetLocationFilter ?:'' 
		model.targetRack = filters?.targetRackFilter ?:'' 
		// Used for filter toValidate from the Planning Dashboard - want a better parameter (JPM 9/2014)
		model.type = params.type

		// The customized title of the list
		def titleByFilter = [
			all: 'All Devices',
			other: 'Other Devices',
			physical: 'Physical Device',
			physicalServer: 'Physical Server',
			server: 'Server',
			storage:  'Storage Device',
			virtualServer: 'Virtual Server',
		]
		model.title = ( titleByFilter.containsKey(params.filter) ? titleByFilter[params.filter] : titleByFilter['all'] ) + ' List'

		return model
	}

   /** 
    * Used to retrieve the data used by the AssetEntity List
    */
   Map getDeviceDataForList(Project project, UserLogin userLogin, session, params, tzId) {
		def filterParams = [
			assetName: params.assetName, 
			assetType: params.assetType, 
			depConflicts: params.depConflicts, 
			depNumber: params.depNumber, 
			depToResolve: params.depToResolve,
			event: params.event,
			model: params.model, 
			moveBundle: params.moveBundle, 
			planStatus: params.planStatus, 
			sourceLocation: params.sourceLocation, 
			sourceRack: params.sourceRack, 
		]
		def validSords = ['asc', 'desc']
		def sortOrder = (validSords.indexOf(params.sord) != -1) ? (params.sord) : ('asc')
		def maxRows = Integer.valueOf(params.rows) 
		def currentPage = Integer.valueOf(params.page) ?: 1
		def rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows

		def moveBundleList
		
		def attributes = projectService.getAttributes('AssetEntity')
		
		// def prefType = (listType=='server') ? 'Asset_Columns' : 'Physical_Columns'
		def prefType = 'Asset_Columns'
		def assetPref= getExistingPref(prefType)
		
		def assetPrefVal = assetPref.collect{it.value}
		attributes.each { attribute ->
			if (attribute.attributeCode in assetPrefVal)
				filterParams << [ (attribute.attributeCode): params[(attribute.attributeCode)]]
		}

		// Lookup the field name reference for the sort
		def sortIndex = (params.sidx in filterParams.keySet() ? params.sidx : 'assetName')
		
		// This is used by the JQ-Grid some how
		session.AE = [:]

		userPreferenceService.setPreference("assetListSize", "${maxRows}")
		
		if (params.event && params.event.isNumber()) {
			def moveEvent = MoveEvent.read( params.event )
			moveBundleList = moveEvent?.moveBundles?.findAll {it.useForPlanning == true}
		} else {
			moveBundleList = MoveBundle.findAllByProjectAndUseForPlanning(project,true)
		}
		
		def assetType = params.filter ? ApplicationConstants.assetFilters[ params.filter ] : []

		def bundleList = params.moveBundle ? MoveBundle.findAllByNameIlikeAndProject("%${params.moveBundle}%", project) : []
		
		StringBuffer altColumns = new StringBuffer()
		StringBuffer joinQuery = new StringBuffer()

		// Until sourceRack is optional on the list we have to do this one
		altColumns.append("\n, srcRack.tag AS sourceRack, srcRoom.location AS sourceLocation")
		joinQuery.append("\nLEFT OUTER JOIN rack AS srcRack ON srcRack.rack_id=ae.rack_source_id ")
		joinQuery.append("\nLEFT OUTER JOIN room AS srcRoom ON srcRoom.room_id=ae.room_source_id ")

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
					altColumns.append(", ae.${value} AS ${value}")
					break
				case 'lastUpdated':
					altColumns.append(", ee.last_updated AS ${value}")
					joinQuery.append("\nLEFT OUTER JOIN eav_entity ee ON ee.entity_id=ae.asset_entity_id ")
					break
				case 'manufacturer':
					altColumns.append(", manu.name AS manufacturer")
					joinQuery.append("\nLEFT OUTER JOIN manufacturer manu ON manu.manufacturer_id=m.manufacturer_id ")
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

				case 'validation':
					break;
				default:
					altColumns.append(", ae.${WebUtil.splitCamelCase(value)} AS ${value} ")
			}
		}

		def query = new StringBuffer(""" 
			SELECT * FROM ( 
				SELECT ae.asset_entity_id AS assetId, ae.asset_name AS assetName, 
				ae.asset_type AS assetType, m.name AS model,  
				IF(at.comment_type IS NULL, 'noTasks','tasks') AS tasksStatus, 
				IF(ac.comment_type IS NULL, 'noComments','comments') AS commentsStatus, 
				me.move_event_id AS event, ae.plan_status AS planStatus, 
				mb.name AS moveBundle, ae.validation AS validation
			""" )

		if (altColumns.length())
			query.append("\n${ altColumns.toString() }")

		query.append("""
				FROM asset_entity ae
				LEFT OUTER JOIN move_bundle mb ON mb.move_bundle_id=ae.move_bundle_id
				LEFT OUTER JOIN move_event me ON me.move_event_id=mb.move_event_id
				LEFT OUTER JOIN model m ON m.model_id=ae.model_id
				LEFT OUTER JOIN asset_comment at ON at.asset_entity_id=ae.asset_entity_id AND at.comment_type = '${AssetCommentType.TASK}'
				LEFT OUTER JOIN asset_comment ac ON ac.asset_entity_id=ae.asset_entity_id AND ac.comment_type = '${AssetCommentType.COMMENT}'
				""")

		if (joinQuery.length())
			query.append(joinQuery)
			
		//
		// Begin the WHERE section of the query	
		//
		query.append("\nWHERE ae.project_id = ${project.id}\nAND ae.asset_class = '${AssetClass.DEVICE}'")

		def justPlanning = userPreferenceService.getPreference("assetJustPlanning")?:'true'
		/*
		// This was being added to correct the issue when coming from the Planning Dashboard but there are some ill-effects still
		if (params.justPlanning)
			justPlanning = params.justPlanning
		*/
		if (justPlanning=='true')
			query.append("\nAND mb.use_for_planning=${justPlanning}")
		
		query.append("\nAND ae.asset_class='${AssetClass.DEVICE}'")

		def filter = params.filter ?: 'all'

		// Filter the list of assets based on if param listType == 'server' to all server types otherwise filter NOT server types
		switch(filter) {
			case 'physical':
				query.append("\nAND COALESCE(ae.asset_type,'') NOT IN (${GormUtil.asQuoteCommaDelimitedString(AssetType.getVirtualServerTypes())}) " )
				break
			case 'physicalServer':
				def phyServerTypes = AssetType.getAllServerTypes() - AssetType.getVirtualServerTypes()
				query.append("\nAND ae.asset_type IN (${GormUtil.asQuoteCommaDelimitedString(phyServerTypes)}) " )
				break
			case 'server':
				query.append("\nAND ae.asset_type IN (${GormUtil.asQuoteCommaDelimitedString(AssetType.getAllServerTypes())}) ")
				break
			case 'storage':
				query.append("\nAND ae.asset_type IN (${GormUtil.asQuoteCommaDelimitedString(AssetType.getStorageTypes())}) " )
				break
			case 'virtualServer':
				query.append("\nAND ae.asset_type IN (${GormUtil.asQuoteCommaDelimitedString(AssetType.getVirtualServerTypes())}) " )
				break
			case 'other':
				query.append("\nAND COALESCE(ae.asset_type,'') NOT IN (${GormUtil.asQuoteCommaDelimitedString(AssetType.getNonOtherTypes())}) " )
				break

 			case 'all': 
				break
		}

		if (params.event && params.event.isNumber() && moveBundleList)
			query.append( "\nAND ae.move_bundle_id IN (${GormUtil.asQuoteCommaDelimitedString(moveBundleList.id)})" )
			
		if (params.unassigned) {
			def unasgnMB = MoveBundle.findAll("\nFROM MoveBundle mb WHERE mb.moveEvent IS NULL AND mb.useForPlanning=true AND mb.project=:project ", [project:project])
			
			if (unasgnMB) {
				def unasgnmbId = GormUtil.asQuoteCommaDelimitedString(unasgnMB?.id)
				query.append( "\nAND (ae.move_bundle_id IN (${unasgnmbId}) OR ae.move_bundle_id IS NULL)" )
			}
		}
			
		query.append("\nGROUP BY assetId ORDER BY ${sortIndex} ${sortOrder}\n) AS assets")
		
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
		filterParams.each { fkey, fvalue ->
			if ( fvalue ) {
				// single quotes are stripped from the filter to prevent SQL injection
				query.append( whereAnd() + " assets.${fkey} LIKE '%${fvalue.replaceAll("'", "")}%'")
				firstWhere = false
			}
		}
		
		if (params.moveBundleId) {
			// TODO : JPM 9/2014 : params.moveBundleId!='unAssigned' - is that even possible anymore? moveBundle can't be unassigned...
			if (params.moveBundleId!='unAssigned') {
				def bundleName = MoveBundle.get(params.moveBundleId)?.name
				query.append( whereAnd() + " assets.moveBundle  = '${bundleName}' ")
			} else {
				query.append( whereAnd() + " assets.moveBundle IS NULL ")
			}
		}
		
		if (params.type && params.type == 'toValidate') {
			query.append( whereAnd() + " assets.validation='Discovery' ") //eq ('validation','Discovery')
		}

		// Allow filtering on the Validate
		if (params.toValidate && params.toValidate && ValidationType.getList().contains(params.toValidate)) {
			query.append( whereAnd() + " assets.validation='${params.toValidate}' ")
		}

		if (params.plannedStatus) {
			query.append(whereAnd() + " assets.planStatus='${params.plannedStatus}'")
		}
		
		log.debug  "query = ${query}"
		def assetList = jdbcTemplate.queryForList(query.toString())
		
		// Cut the list of selected applications down to only the rows that will be shown in the grid
		def totalRows = assetList.size()
		def numberOfPages = Math.ceil(totalRows / maxRows)
		if (totalRows > 0)
			assetList = assetList[rowOffset..Math.min(rowOffset+maxRows,totalRows-1)]
		else
			assetList = []
			
		def results = assetList?.collect {
			def commentType = it.commentType
			[ 	
				cell: [ 
					'', // The action checkbox
					it.assetName, 
					(it.assetType ?: ''), 
					it.model, 
					it.sourceLocation, 
					it.sourceRack,
					( it[ assetPref['1'] ] ?: ''), 
					( it[ assetPref['2'] ] ?: ''), 
					( it[ assetPref['3'] ] ?: ''), 
					( it[ assetPref['4'] ] ?: ''), 
					it.planStatus, 
					it.moveBundle, 
					/*it.depNumber, (it.depToResolve==0)?(''):(it.depToResolve), (it.depConflicts==0)?(''):(it.depConflicts),*/
					it.tasksStatus, 
					it.assetType, 
					it.event, 
					it.commentsStatus
				], id: it.assetId
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
    * @param currentProject the current project
    * @return the map of models
    */
   List modelsOf(String manufacturerId, String assetType, String term, currentProject) {
		def manufacturer
		def result = []
		List words = []

		def manuId = NumberUtil.toLong(manufacturerId)	   
		if (manuId) {
			manufacturer = Manufacturer.read(manuId)
		}

		def hql = new StringBuffer( 'SELECT m.id, m.assetType, m.modelName, man.id as manId, man.name as manName, m.modelStatus, m.usize as usize FROM Model m JOIN m.manufacturer as man')

		def params = []

		StringBuffer where = new StringBuffer('')

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
			List likeWords = SqlUtil.formatForLike( words )
			StringBuffer search = new StringBuffer('')

			if (! manufacturer) {
				SqlUtil.appendToWhere(where, 'm.manufacturer.id = man.id', 'AND')
				SqlUtil.appendToWhere(search, SqlUtil.matchWords('man.name', words, false, false), 'OR')
				params.addAll(likeWords)
			}

			if (! assetType) {
				SqlUtil.appendToWhere(search, SqlUtil.matchWords('m.assetType', words, false, false), 'OR')
				params.addAll(likeWords)
			}

			SqlUtil.appendToWhere(search, SqlUtil.matchWords('m.modelName', words, false, false), 'OR')
			params.addAll(likeWords)

			SqlUtil.appendToWhere(where, '('+search.toString()+')', 'AND')
		}

		if (where.size())
			hql.append(' WHERE ' + where.toString())

		// Construct to the orderby
		StringBuffer orderBy = new StringBuffer('')
		if (!manufacturer)
			orderBy.append('man.name')
		orderBy.append( (orderBy.size() ? ', ' : '') + 'm.modelName')
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
			if (! isValid)
				title += ' ?'

			// If the user search is on multiple words then we need to match on all of them
			if (hasMultiWordFilter && ! StringUtil.containsAll(title, words))
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
    * @param currentProject the current project
    * @return the map of manufacturers
    */
   def manufacturersOf(assetType, term, currentProject) {
	   def hql = "SELECT distinct m.id, m.name FROM Manufacturer m";
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
		   joinTables = " LEFT OUTER JOIN m.models as model";
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
		   ];
	   }
	   
	   return result
   }
}