import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.FilenameFormat
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.FilenameUtil
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
import grails.gsp.PageRenderer
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.command.AssetCommand
import net.transitionmanager.command.BundleChangeCommand
import net.transitionmanager.command.CloneAssetCommand
import net.transitionmanager.command.UniqueNameCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.service.ApplicationService
import net.transitionmanager.service.AssetEntityService
import net.transitionmanager.service.AssetService
import net.transitionmanager.service.ControllerService
import net.transitionmanager.service.DatabaseService
import net.transitionmanager.service.DeviceService
import net.transitionmanager.service.StorageService
import net.transitionmanager.service.UserPreferenceService

import java.text.DateFormat
/**
 * Created by @oluna on 4/5/17.
 */

@Slf4j
@Secured('isAuthenticated()')
class WsAssetController implements ControllerMethods {

	ApplicationService applicationService
	AssetEntityService assetEntityService
	AssetService assetService
	ControllerService controllerService
	DatabaseService databaseService
	DeviceService deviceService
	PageRenderer groovyPageRenderer
	StorageService storageService
	UserPreferenceService userPreferenceService

	/**
	 * Check for uniqueness of the asset name, it can be checked against the AssetClass of another asset
	 *
	 * 	@param name <Name of the asset to check>
	 * 	@param assetId <asset to filter the class from>
	 */
	@HasPermission(Permission.AssetView)
	def checkForUniqueName(){
		UniqueNameCommand command = populateCommandObject(UniqueNameCommand)
		boolean unique = true
		AssetClass assetClassSample
		Long foundAssetId
		String assetClass = ""

		List<String> errors = []

		if(command.assetId){
			AssetEntity sampleAssetEntity = fetchDomain(AssetEntity, [id: command.assetId])
			//check that the asset is part of the project
			if(!securityService.isCurrentProjectId(sampleAssetEntity?.projectId)){
				securityService.reportViolation(
						"Security Violation, user {} attempted to access an asset not associated to the project"
				)
				errors << "Asset not found in current project"
			}
			assetClassSample = sampleAssetEntity?.assetClass
		}

		if(errors){
			renderFailureJson(errors)

		} else {
			AssetEntity assetEntity = AssetEntity.createCriteria().get {
				and {
					eq('assetName', command.name, [ignoreCase: true])
					if(assetClassSample){
						eq('assetClass', assetClassSample)
					}
				}
				maxResults(1)
			}

			if (assetEntity) {
				unique = false
				foundAssetId = assetEntity.id
				assetClass = assetEntity.assetClass.toString()
			}

			Map<String, ?> jsonMap = [unique: unique]
			if (!unique) {
				jsonMap.assetId = foundAssetId
				jsonMap.assetClass = assetClass
			}

			renderSuccessJson(jsonMap)
		}
	}

	/**
	 *
	 * @param assetId	Id of the asset that we are going to clone
	 * @param name		name of the new cloned instance
	 * @param dependencies if true we will clone all the *supported* and *required* dependencies of
	 * 			the original asset into the new one. defaults to false
	 */
	@HasPermission(Permission.AssetCreate)
	def clone(Long assetId, String name, Boolean dependencies){
		log.debug("assetId: {}, name: {}, dependencies: {}", assetId, name, dependencies)

		List<String> errors = []
		CloneAssetCommand command = populateCommandObject(CloneAssetCommand)
		Project project = getProjectForWs()

		if (command.cloneDependencies && !securityService.hasPermission(Permission.AssetCloneDependencies)) {
			securityService.reportViolation(
					"Security Violation, user {} doesn't have the correct permission to Clone Asset Dependencies"
			)
			errors << "You don't have the correct permission to Clone Assets Dependencies"
		}
		// cloning asset
		Long clonedAssetId = assetEntityService.clone(project, command, errors)
		if(errors){
			renderFailureJson(errors)
		}else{
			renderSuccessJson([assetId : clonedAssetId])
		}
	}

	@HasPermission(Permission.AssetView)
	def getAsset(Long id){
		AssetEntity asset = AssetEntity.get(id)
		if(asset) {
			renderSuccessJson([result: asset])
		} else {
			response.status = 404 //Not Found
			render "${id} not found."
		}
	}

	/**
	 * Search for All Dependencies of Asset A related to B,
	 * There is no way to determinate the direction of the line when it have bi-directional links
	 * @param assetA
	 * @param assetB
	 * @return
	 */
	@HasPermission(Permission.AssetView)
	def getAssetDependencies(Long assetAId, Long assetBId){
		AssetEntity assetA = AssetEntity.get(assetAId)
		AssetEntity assetB = AssetEntity.get(assetBId)
		// check that the assets are part of the project
		if(!securityService.isCurrentProjectId(assetA.projectId) || !securityService.isCurrentProjectId(assetB.projectId)){
			log.error(
					"Security Violation, user {} attempted to access an asset not associated to the project",
					securityService.getCurrentUsername()
			)
			errors << "Asset not found in current project"
		}

		List<AssetDependency> requiredDependenciesA = assetA.requiredDependencies()
		AssetDependency dependencyA =  requiredDependenciesA.find {
			it.dependent.id == assetBId
		}

		List<AssetDependency> requiredDependenciesB = assetB.requiredDependencies()
		AssetDependency dependencyB =  requiredDependenciesB.find {
			it.dependent.id == assetAId
		}

		def assetAClassLabel = AssetClass.classOptions.find {
			it.key == AssetClass.getClassOptionForAsset(assetA)
		}

		def assetBClassLabel = AssetClass.classOptions.find {
			it.key == AssetClass.getClassOptionForAsset(assetB)
		}

		def Project currentProject = securityService.getUserCurrentProject()
		String userTzId = userPreferenceService.timeZone
		DateFormat formatter = TimeUtil.createFormatter(TimeUtil.FORMAT_DATE_TIME)

		def dependencyMap = [
			"assetA"          : [
					"name"           : assetA.assetName,
					"assetClass"     : assetAClassLabel.value,
					"environment"    : assetA.environment,
					"bundle"         : assetA.moveBundleName,
					"planStatus"     : assetA.planStatus,
					"dependency"     : dependencyA,
					"dependencyClass": dependencyA?.dependent?.assetClass,
					dateCreated      : TimeUtil.formatDateTimeWithTZ(userTzId, assetA.dateCreated, formatter),
					lastUpdated      : TimeUtil.formatDateTimeWithTZ(userTzId, assetA.lastUpdated, formatter)
			],
			"assetB"          : [
					"name"           : assetB.assetName,
					"assetClass"     : assetBClassLabel.value,
					"environment"    : assetB.environment,
					"bundle"         : assetB.moveBundleName,
					"planStatus"     : assetB.planStatus,
					"dependency"     : dependencyB,
					"dependencyClass": dependencyB?.dependent?.assetClass,
					dateCreated      : TimeUtil.formatDateTimeWithTZ(userTzId, assetB.dateCreated, formatter),
					lastUpdated      : TimeUtil.formatDateTimeWithTZ(userTzId, assetB.lastUpdated, formatter)
			],
			"dataFlowFreq"    : AssetDependency.constraints.dataFlowFreq.inList,
			"dependencyType"  : assetEntityService.entityInfo(currentProject).dependencyType,
			"dependencyStatus": assetEntityService.entityInfo(currentProject).dependencyStatus,
			"editPermission"  : securityService.hasPermission(Permission.AssetEdit)
		]
		renderSuccessJson(dependencyMap)
	}

	/**
	 * Delete a dependency from an Asset.
	 * @return
	 */
	@HasPermission(Permission.AssetEdit)
	def deleteAssetDependency(){
		AssetEntity assetEntity = AssetEntity.get(request.JSON.assetId)
		assetEntityService.deleteAssetEntityDependencyOrException(securityService.getUserCurrentProject(), assetEntity, request.JSON.dependencyId)
		renderSuccessJson()
	}

   /**
    * Delete multiple Asset Dependencies.
    * @param : dependencyIds[]  : list of ids for which assets are requested to be deleted
    */
   @HasPermission(Permission.AssetEdit)
   def bulkDeleteDependencies(){
      Project project = projectForWs
      renderAsJson(resp: assetService.bulkDeleteDependencies(project, params.list("dependencyIds[]")))
   }

	/**
	 * Update Asset Dependency Fields
	 * @return
	 */
	@HasPermission(Permission.AssetEdit)
	def updateCommonAssetDependencyFields() {
		AssetEntity asset = AssetEntity.get(request.JSON.dependency.asset.id)
		assetEntityService.updateAssetDependencyOrException(securityService.getUserCurrentProject(), asset, request.JSON.dependency.id, request.JSON.dependency)
		renderSuccessJson()
	}

	/**
	 * Used to retrieve the Angular HTML template used for the show or edit views of an asset
	 * @param id - the id of the asset to generate the show view for
	 * @param mode - the mode of the template to render [edit|show]
	 * @return html template
	 */
	@HasPermission(Permission.AssetView)
	def getTemplate(Long id, String mode) {
		final List modes = ['edit','show']
		if (! modes.contains(mode) || id == null ) {
			sendBadRequest()
			return
		}

		// Load the asset and validate that it is part of the project
		AssetEntity asset = fetchDomain(AssetEntity, params)
		if (! asset) {
			sendNotFound()
			return
		}

		Map model = [ asset: asset ]
		String domainName = asset.assetClass.toString()
		switch (domainName) {
			case "APPLICATION":
				model << applicationService.getModelForShow(asset.project, asset, params)
				break
			case "DEVICE":
				model << deviceService.getModelForShow(asset.project, asset, params)
				break
			case "STORAGE":
				model << storageService.getModelForShow(asset.project, asset, params)
				break
			case "DATABASE":
				model << databaseService.getModelForShow(asset.project, asset, params)
				break
			default:
				model << assetEntityService.getCommonModelForShows(domainName, asset.project, params)
				break
		}

		domainName=domainName.toLowerCase()
		try {
			String pageHtml = groovyPageRenderer.render(view: "/angular/$domainName/$mode", model: model)
			if (pageHtml) {
				render pageHtml
			} else {
				log.error "getTemplate() Generate page failed domainName=$domainName, mode=$mode\n  model:$model"
				sendNotFound()
			}
		} catch (e) {
			log.error "getTemplate() Generate page for domainName=$domainName, mode=$mode had an exception: ${e.getMessage()}"
			sendNotFound()
		}
	}

	/**
	 * Used to retrieve the Angular HTML template used for the create views of an asset
	 * @param domainName - the domain of the asset to generate the create view for
	 * @return html template
	 */
	@HasPermission(Permission.AssetCreate)
	def getCreateTemplate(String domainName) {
		final List domains = ['APPLICATION','DEVICE','STORAGE','DATABASE']
		if (!domains.contains(domainName)) {
			sendBadRequest()
			return
		}

		Map model = [:]

		switch (domainName) {
			case "APPLICATION":
				model << applicationService.getModelForCreate(params)
				break
			case "DEVICE":
				model << deviceService.getModelForCreate(params)
				break
			case "STORAGE":
				model << storageService.getModelForCreate(params)
				break
			case "DATABASE":
				model << databaseService.getModelForCreate(params)
				break
		}

		domainName=domainName.toLowerCase()
		try {
			String pageHtml = groovyPageRenderer.render(view: "/angular/$domainName/create", model: model)
			if (pageHtml) {
				render pageHtml
			} else {
				log.error "getCreateTemplate() Generate page failed domainName=$domainName\n  model:$model"
				sendNotFound()
			}
		} catch (e) {
			log.error "getCreateTemplate() Generate page for domainName=$domainName had an exception: ${e.getMessage()}"
			sendNotFound()
		}
	}

	/**
	 * Used to retrieve the model data for the show view of an asset
	 * @param id - the id of the asset to retrieve the model for
	 * @return JSON map
	 */
	@HasPermission(Permission.AssetView)
	def getModel(Long id, String mode) {
		final List modes = ['edit','show']
		if (! modes.contains(mode) || id == null) {
			sendBadRequest()
			return
		}

		AssetEntity asset = fetchDomain(AssetEntity, params)
		if (asset) {
			Map model = [
				asset: asset
			]
			String domainName = AssetClass.getDomainForAssetType(asset.assetClass.toString())
            // prepare value names as well (needed for UI)
            if (model.asset.manufacturer) {
                model.manufacturerName = model.asset.manufacturer.name;
            }
            if (model.asset.model) {
                model.modelName = model.asset.model.modelName;
            }
			if (mode == 'show') {
				model << assetEntityService.getCommonModelForShows(domainName, asset.project, params)
			} else {
				model << assetEntityService.getDefaultModelForEdits(domainName, asset.project, asset, params)
				// Required for Supports On and Depends On
				model.dependencyMap = assetEntityService.dependencyEditMap(asset.project, asset)
				model.dataFlowFreq = AssetDependency.constraints.dataFlowFreq.inList;
			}
			renderAsJson(model)
		}
	}

	/**
	 * Used to retrieve the model data for the List o parameters required for Support and Depend of an asset
	 * @param id - the id of the asset to retrieve the model for
	 * @return JSON map
	 */
	@HasPermission(Permission.AssetView)
	def getDefaultCreateModel() {
		Project project = securityService.getUserCurrentProject()
		Map model = [:]
		// Required for Supports On and Depends On
		model.dependencyMap = assetEntityService.dependencyCreateMap(project)
		model.dataFlowFreq = AssetDependency.constraints.dataFlowFreq.inList;
		model.environmentOptions = assetEntityService.getAssetEnvironmentOptions()
		model.planStatusOptions = assetEntityService.getAssetPlanStatusOptions()
		renderAsJson(model)
	}

	/**
	 * Endpoint that needs to be executed when the user changes the selected
	 * bundle in dropdowns.
	 * The request must have: assetId, dependencyId and status.
	 */
	@HasPermission(Permission.BundleView)
	def retrieveBundleChange() {
		// Fetch the information for this request.
		BundleChangeCommand command = populateCommandObject(BundleChangeCommand)

		Project project = getProjectForWs()

		// The id of the bundle retrieve from the asset in the dependency (if such dependency exists).
		Long depBundleId = null
		// The id of the  bundle for the asset received in the request.
		Long assetBundleId = null
		// The status of the dependency.
		String status = null

		// Can't use fetchDomain because it throws an exception when it doesn't find a result.
		AssetDependency dependency = GormUtil.findInProject(project, AssetDependency, command.dependencyId, false)
		// If the dependency exists, use its status and corresponding asset's bundle in the response.
		if (dependency) {
			AssetEntity depAsset = (command.type == 'support') ? dependency.asset : dependency.dependent
			depBundleId = depAsset.moveBundle.id
			status = dependency.status
		}

		// Can't use fetchDomain because it throws an exception when it doesn't find a result.
		AssetEntity asset = GormUtil.findInProject(project, AssetEntity, command.assetId, false)
		// If the given assetId exists, use that asset's bundle in the response.
		if (asset) {
			assetBundleId = asset.moveBundle.id
		}

		renderAsJson(id: assetBundleId, depBundle: depBundleId, status: status)
	}

	/**
	 * Used to delete one or more assets for the current project 
	 * @params ids - a list of asset id numbers
	 * @return JSON Success response with data.message with results
	 */
	@HasPermission(Permission.AssetDelete)
	def deleteAssets() {
		String message = assetEntityService.deleteBulkAssets(projectForWs, 'Assets', request.getJSON().ids)
		renderSuccessJson(message:message)
	}

	/**
	 * This end point gets called from the View Manager when the user tries to export a View.
	 * This generates the required filename in the appropriate format and returns it to the front end.
	 * The file name is returned with no file extension as is attached by the View Manager.
	 * Also the date is not returned, as it will be attached by the front end.
	 *
	 * @param viewName  The name of the view to save provided by the user
	 * @return  The full file name formatted according to the given rules for a View Manager export file.
	 */
	def exportFilename() {
		Project project = securityService.userCurrentProject
		String viewName = request.JSON.viewName
		if (!viewName) {
			return renderFailureJson('Error: viewName cannot be null.')
		}
		Map params = [project: project, viewName: viewName, excludeDate: true]
		return renderSuccessJson(FilenameUtil.buildFilename(FilenameFormat.PROJECT_VIEW_DATE, params))
	}

	/**
	 * Save a new asset.
	 *
	 * @return
	 */
	@HasPermission(Permission.AssetCreate)
	def saveAsset() {
		// Populate the command with the data coming from the request.
		AssetCommand command = populateCommandObject(AssetCommand)
		// Save the new asset.
		AssetEntity asset = assetEntityService.saveOrUpdateAsset(command)
		renderSuccessJson(['id': asset.id])
	}

	/**
	 * Update an asset
	 * @return
	 */
	@HasPermission(Permission.AssetEdit)
	def updateAsset(Long id) {
		// Populate the command with the data coming from the request.
		AssetCommand command = populateCommandObject(AssetCommand)
		// Update the asset.
		assetEntityService.saveOrUpdateAsset(command)
		renderSuccessJson('Success!')
	}

	/**
	 * Retrieve the Chassis options for the given room.
	 * @param id
	 * @return
	 */
	@HasPermission(Permission.AssetCreate)
	def retrieveChassisSelectOptions(Long id) {
		Project project = getProjectForWs()
		List chassisOptions = assetEntityService.getChassisSelectOptions(project, id)
		renderSuccessJson(chassisOptions)
	}

	/**
	 * Retrieve the Rack options for the given room.
	 * @param id
	 * @return
	 */
	@HasPermission(Permission.AssetCreate)
	def retrieveRackSelectOptions(Long id) {
		Project project = getProjectForWs()
		List rackOptions = assetEntityService.getRackSelectOptions(project, id, true)
		renderSuccessJson(rackOptions)
	}

	/**
	 * Return a list with all the Asset Class Options
	 */
	@HasPermission(Permission.AssetView)
	def retrieveAssetClassOptions() {
		renderSuccessJson(AssetClass.classOptions)
	}


}
