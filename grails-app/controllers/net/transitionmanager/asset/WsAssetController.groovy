package net.transitionmanager.asset

import com.tdsops.common.lang.CollectionUtils
import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.FilenameFormat
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.UserPreferenceEnum
import com.tdssrc.grails.FilenameUtil
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import grails.gsp.PageRenderer
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.command.AssetCommand
import net.transitionmanager.command.AssetCommentSaveUpdateCommand
import net.transitionmanager.command.BundleChangeCommand
import net.transitionmanager.command.CloneAssetCommand
import net.transitionmanager.command.UniqueNameCommand
import net.transitionmanager.command.assetentity.BulkDeleteDependenciesCommand
import net.transitionmanager.common.ControllerService
import net.transitionmanager.common.CustomDomainService
import net.transitionmanager.common.ProgressService
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.person.UserPreferenceService
import net.transitionmanager.party.PartyRelationshipService
import net.transitionmanager.project.MoveBundleService
import net.transitionmanager.project.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.security.UserLogin
import net.transitionmanager.task.AssetComment
import net.transitionmanager.task.TaskService
import net.transitionmanager.utils.Profiler
import org.quartz.Scheduler
import org.quartz.Trigger
import org.quartz.impl.triggers.SimpleTriggerImpl
/**
 * Created by @oluna on 4/5/17.
 */

@Slf4j
@Secured('isAuthenticated()')
class WsAssetController implements ControllerMethods {


	ApplicationService applicationService
	AssetEntityService assetEntityService
	AssetService       assetService
	CommentService     commentService
	TaskService        taskService
	ControllerService  controllerService
	DatabaseService    databaseService
	DeviceService      deviceService
	MoveBundleService  moveBundleService
	PageRenderer       groovyPageRenderer
	ProgressService    progressService
	Scheduler          quartzScheduler
	StorageService     storageService
    PartyRelationshipService partyRelationshipService
    CustomDomainService customDomainService
	UserPreferenceService userPreferenceService

	/**
	 * Check for uniqueness of the asset name, it can be checked against the AssetClass of another asset
	 *
	 * 	@param name <Name of the asset to check>
	 * 	@param assetId <asset to filter the class from>
	 */
	@HasPermission(Permission.AssetView)
	def checkForUniqueName(){
		UniqueNameCommand command = populateCommandObject(UniqueNameCommand, false)
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
		CloneAssetCommand command = populateCommandObject(CloneAssetCommand, false)
		Project project = getProjectForWs()

		if (command.cloneDependencies && !securityService.hasPermission(Permission.AssetCloneDependencies)) {
			securityService.reportViolation(
					"Security Violation, user {} doesn't have the correct permission to Clone Asset Dependencies"
			)
			errors << "You don't have the correct permission to Clone Assets Dependencies"
		}
		// Clone the asset.
		AssetEntity clonedAsset = assetEntityService.clone(project, command, errors)
		if(errors){
			renderErrorJson(errors)
		}else{
			renderSuccessJson([assetId : clonedAsset.id])
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
		Project project = getProjectForWs()
		renderSuccessJson(assetEntityService.getAssetDependenciesBetweenAssets(project, assetAId, assetBId))
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
    * @param : dependencies : list of ids for which assets are requested to be deleted
    */
   @HasPermission(Permission.AssetEdit)
   def bulkDeleteDependencies(){
	   Project project = projectForWs

	   BulkDeleteDependenciesCommand command = populateCommandObject(BulkDeleteDependenciesCommand)
	   renderAsJson(resp: assetService.bulkDeleteDependencies(project, command.dependencies))
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

		Map model = [
			asset           : asset,
			dependencyBundle: AssetDependencyBundle.findByAsset(asset)?.dependencyBundle,
			taskCount       : taskService.countByAssetEntity(asset),
			commentCount    : commentService.countByAssetEntity(asset)
		]

		String domainName = asset.assetClass.toString()

		switch (domainName) {
			case "APPLICATION":
				model << applicationService.getModelForShow(asset.project, asset, params, false)
				break
			case "DEVICE":
				model << deviceService.getModelForShow(asset.project, asset, params, false)
				break
			case "STORAGE":
				model << storageService.getModelForShow(asset.project, asset, params, false)
				break
			case "DATABASE":
				model << databaseService.getModelForShow(asset.project, asset, params, false)
				break
			default:
				model << assetEntityService.getCommonModelForShows(domainName, asset.project, params, asset,false)
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

		// Set the default values on the custom properties
		assetService.setCustomDefaultValues(model['assetInstance'])

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
	def showModel(Long id) {
		getModel(id, 'show')
	}

	/**
	 * Used to retrieve the model data for the edit view of an asset
	 * @param id - the id of the asset to retrieve the model for
	 * @return JSON map
	 */
	@HasPermission(Permission.AssetEdit)
	def editModel(Long id) {
		getModel(id, 'edit')
	}

	/**
	 * @param id - the id of the asset to retrieve the model for
	 * @return JSON map
	 */
	private def getModel(Long id, String mode) {
		final List modes = ['edit','show']
		if (! modes.contains(mode) || id == null) {
			sendBadRequest()
			return
		}

		Map model = [
			asset: fetchDomain(AssetEntity, params)
		]

		if (model.asset.manufacturer) {
			model.manufacturerName = model.asset.manufacturer.name;
		}

		if (model.asset.model) {
			model.modelName = model.asset.model.modelName;
		}

		Project project = projectForWs
		model.personList = partyRelationshipService.getProjectApplicationStaff(project)

		String domainName = AssetClass.getDomainForAssetType(model.asset.assetClass.toString())
		if (mode == 'show') {
			model << assetEntityService.getCommonModelForShows(domainName, model.asset.project, params)
		} else {
			model << assetEntityService.getDefaultModelForEdits(domainName, model.asset.project, model.asset, params)
		}

		renderAsJson(model)
	}

	/**
	 * Used to retrieve the model data for the List o parameters required for Support and Depend of an asset
	 * @param id - the id of the asset to retrieve the model for
	 * @return JSON map
	 */
	@HasPermission(Permission.AssetView)
	def getDefaultCreateModel(String assetClass) {
		Project project = projectForWs
		Map model = assetService.getCreateModel(getProjectForWs(), assetClass)

		// Add the custom field specs to the model
		model.put('customs', assetEntityService.getCustomFieldsSettings(project, assetClass, true) )

		// Add the standardFieldASpecs to the model
		assetService.addFieldSpecsToCrudModel(project, assetClass, model)

		renderAsJson( model )
	}

	/**
	 * Endpoint that needs to be executed when the user changes the selected
	 * bundle in dropdowns.
	 * The request must have: assetId, dependencyId and status.
	 */
	@HasPermission(Permission.BundleView)
	def retrieveBundleChange() {
		BundleChangeCommand command = populateCommandObject(BundleChangeCommand, false)
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
		AssetCommand command = populateCommandObject(AssetCommand, false)
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
		AssetCommand command = populateCommandObject(AssetCommand, false)
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
		List chassisOptions = DeviceUtils.getChassisSelectOptions(project, id)
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
		List rackOptions = DeviceUtils.getRackSelectOptions(project, id, true)
		renderSuccessJson(rackOptions)
	}

	/**
	 * Return a list with all the Asset Class Options
	 */
	@HasPermission(Permission.AssetView)
	def retrieveAssetClassOptions() {
		renderSuccessJson(AssetClass.classOptions)
	}

	/**
	 * Return the list of dependencies for the Dependency List.
	 */
	@HasPermission(Permission.AssetView)
	def listDependencies() {
		Project project = getProjectForWs()
		Map jsonParams = request.JSON
		int maxRows = NumberUtil.toPositiveInteger(jsonParams['rows'], 25)
		int currentPage =NumberUtil.toPositiveInteger(jsonParams['page'], 1)
		int rowOffset = (currentPage - 1) * maxRows
		Map paginationParams = [max: maxRows, offset: rowOffset]
		Map sortingParams = [index: jsonParams['sidx'] , order: jsonParams['sord']]
		renderSuccessJson(assetEntityService.listDependencies(project, jsonParams, sortingParams, paginationParams))
	}

	/**
	 * Delete a comment given its id.
	 */
	@HasPermission(Permission.CommentDelete)
	def deleteComment(Long id) {
		// Retrieve the project for the current user.
		Project project = getProjectForWs()
		// Delete the comment
		commentService.deleteComment(project, id)
		renderSuccessJson()

	}

	/**
	 * Update an AssetComment
	 */
	@HasPermission(Permission.CommentEdit)
	def updateComment(Long id) {
		// Update the comment.
		saveOrUpdateComment()
		renderSuccessJson()
	}

	/**
	 * Update an AssetComment
	 */
	@HasPermission(Permission.CommentCreate)
	def saveComment() {
		// Save the comment.
		saveOrUpdateComment()
		renderSuccessJson()

	}

	/**
	 * Create or Update an AssetComment
	 * @param id
	 * @param command
	 */
	private void saveOrUpdateComment() {
		// Retrieve the project for the user.
		Project project = getProjectForWs()
		// Populate the command object with the data coming from the request
		AssetCommentSaveUpdateCommand command = populateCommandObject(AssetCommentSaveUpdateCommand, false)
		// Save or update the comment
		commentService.saveOrUpdateAssetComment(project, command)
	}


	/**
	 * Get all the comments associated with the current project
	 */
	@HasPermission(Permission.CommentView)
	def listComments() {
		Project project = securityService.userCurrentProject
		boolean viewUnpublished = securityService.viewUnpublished()
		List<AssetComment> assetComments = commentService.listAssetComments(project, viewUnpublished)
		renderAsJson (assetComments*.toMap() )
	}


	/**
	 * Return the content for the Asset Summary Table
	 * @return the content for the asset table.
	 */
	def getSummaryTable() {
		Project project = getProjectForWs()
		String justPlanningPref = userPreferenceService.getPreference(null, UserPreferenceEnum.ASSET_JUST_PLANNING, "false")
		Boolean justPlanning = StringUtil.toBoolean(justPlanningPref)
		renderSuccessJson(assetEntityService.getAssetSummary(project, justPlanning))
	}

	@HasPermission(Permission.AssetExport)
	def bundlesAndPreferencesForAssetExport() {
		Project project = getProjectForWs()
		renderSuccessJson([bundles:moveBundleService.lookupList(project),
		                   userPreferences: userPreferenceService.getExportPreferences()])
	}

	/**
	 * Update the asset export related preferences based on parameters and then trigger the asset export job.
	 *
	 * @return job key
	 */
	@HasPermission(Permission.AssetExport)
	def exportAssets() {
		Map paramsMap = request.JSON
		paramsMap.bundle = CollectionUtils.asList(paramsMap.bundle)
		UserLogin currentUser = securityService.userLogin
		userPreferenceService.getExportPreferences().each { String preferenceName, String preferenceValue ->
			if (!paramsMap.containsKey(preferenceName)) {
				throw new InvalidParamException("Missing parameter '${preferenceName}' for asset export.")
			}
			String parameterValue = paramsMap[preferenceName].toString()
			if (parameterValue != preferenceValue) {
				userPreferenceService.setPreference(currentUser, preferenceName, parameterValue)
			}
		}

		String key = "AssetExport-" + UUID.randomUUID()
		progressService.create(key)

		log.info "Initiate Export"

		// Delay 2 seconds to allow this current transaction to commit before firing off the job
		Trigger trigger = new SimpleTriggerImpl("TM-" + key, null, new Date(System.currentTimeMillis() + 2000))
		trigger.jobDataMap.putAll(paramsMap)
		trigger.jobDataMap.key = key
		trigger.jobDataMap.username = securityService.currentUsername
		trigger.jobDataMap.projectId = securityService.userCurrentProjectId
		trigger.jobDataMap.tzId = userPreferenceService.timeZone
		trigger.jobDataMap.userDTFormat = userPreferenceService.dateFormat
		trigger.jobDataMap[Profiler.KEY_NAME] = session[Profiler.KEY_NAME]

		trigger.setJobName('ExportAssetEntityJob')
		trigger.setJobGroup('tdstm-export-asset')
		quartzScheduler.scheduleJob(trigger)

		progressService.update(key, 1, 'In progress')

		renderSuccessJson(key: key)
	}

	/**
	 * Get basic asset fields based on its id and dependency group id.
	 * @return
	 */
	def getAssetForDependencyGroup(Long assetId) {
		Project project = getProjectForWs()
		AssetEntity asset = fetchDomain(AssetEntity, [id: assetId], project)

		renderSuccessJson([
		        id: asset.id,
				name: asset.assetName,
				description: asset.description,
				moveBundle: asset.moveBundle?.name,
				depGroup: AssetDependencyBundle.findByAsset(asset)?.dependencyBundle
		])
	}

	@HasPermission(Permission.AssetView)
	def dependenciesForAsset(Long assetId) {
		Project project = getProjectForWs()
		renderSuccessJson(assetService.getDependenciesMapForAsset(project, assetId))
	}

}
