import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.AssetDependencyStatus
import com.tdsops.tm.enums.domain.ValidationType
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.security.Permission
import net.transitionmanager.service.AssetEntityService
import net.transitionmanager.service.SecurityService
import org.grails.datastore.mapping.query.api.BuildableCriteria

/**
 * Created by @oluna on 4/5/17.
 */

@Slf4j
@Secured('isAuthenticated()')
class WsAssetController implements ControllerMethods {
	SecurityService securityService
	AssetEntityService assetEntityService

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

		AssetEntity clonedAsset
		if(!errors) {
			AssetEntity assetToClone = AssetEntity.get(assetId)
			if(!assetToClone) {
				errors << "The asset specified to clone was not found"

			}else{
				//check that the asset is part of the project
				if(!securityService.isCurrentProjectId(assetToClone.projectId)){
					log.error(
							"Security Violation, user {} attempted to access an asset not associated to the project",
							securityService.getCurrentUsername()
					)
					errors << "Asset not found in current project"
				}

				if(!errors) {
					clonedAsset = assetToClone.clone([
							assetName : name,
							validation: ValidationType.DIS
					])

					// Cloning assets dependencies if requested
					if (clonedAsset.save() && dependencies) {
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
				}
			}
		}

		if(errors){
			renderFailureJson(errors)
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

		def dependencyMap = [
			"assetA" : [
					"name": assetA.assetName,
					"assetClass": assetAClassLabel.value,
					"environment": assetA.environment,
					"bundle": assetA.moveBundleName,
					"planStatus": assetA.planStatus,
					"dependency": dependencyA,
					"dependencyClass": dependencyA?.dependent?.assetClass
			],
			"assetB" : [
					"name": assetB.assetName,
					"assetClass": assetBClassLabel.value,
					"environment": assetB.environment,
					"bundle": assetB.moveBundleName,
					"planStatus": assetB.planStatus,
					"dependency": dependencyB,
					"dependencyClass": dependencyB?.dependent?.assetClass
			],
			"dataFlowFreq": AssetDependency.constraints.dataFlowFreq.inList,
			"dependencyType": assetEntityService.entityInfo(currentProject).dependencyType,
			"dependencyStatus": assetEntityService.entityInfo(currentProject).dependencyStatus,
			"editPermission": securityService.hasPermission(Permission.AssetEdit)
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

}
