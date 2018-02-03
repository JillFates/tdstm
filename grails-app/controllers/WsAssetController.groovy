import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.TimeUtil
import grails.gsp.PageRenderer
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.service.ApplicationService
import net.transitionmanager.service.AssetEntityService
import net.transitionmanager.service.ControllerService
import net.transitionmanager.service.DatabaseService
import net.transitionmanager.service.DeviceService
import net.transitionmanager.service.StorageService
import net.transitionmanager.service.UserPreferenceService
import org.grails.datastore.mapping.query.api.BuildableCriteria
import grails.gsp.PageRenderer

import java.text.DateFormat

/**
 * Created by @oluna on 4/5/17.
 */

@Slf4j
@Secured('isAuthenticated()')
class WsAssetController implements ControllerMethods {

	ApplicationService applicationService
	AssetEntityService assetEntityService
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
	def checkForUniqueName(String name, Long assetId){

		boolean unique = true
		AssetClass assetClassSample
		Long foundAssetId
		String assetClass = ""

		List<String> errors = []

		if(assetId){
			AssetEntity sampleAssetEntity = AssetEntity.get(assetId)
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
					eq('assetName', name, [ignoreCase: true])
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
		if(!assetId){
			errors << "The asset id was missing"
		}
		if(!name){
			errors << "The new asset name is missing"
		}
		if(dependencies &&
				!securityService.hasPermission(Permission.AssetCloneDependencies)
		){
			securityService.reportViolation(
					"Security Violation, user {} doesn't have the correct permission to Clone Asset Dependencies"
			)
			errors << "You don't have the correct permission to Clone Assets Dependencies"
		}
		// cloning asset
		Long clonedAssetId = assetEntityService.clone(assetId, name, dependencies, errors)
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
			if (mode == 'show') {
				model << assetEntityService.getCommonModelForShows(domainName, asset.project, params)
			} else {
				model << assetEntityService.getDefaultModelForEdits(domainName, asset.project, asset, params)
			}
			log.debug "\n\n*** showModel()\n domainName=$domainName\nmodel:$model"
			renderAsJson(model)
		}
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
}
