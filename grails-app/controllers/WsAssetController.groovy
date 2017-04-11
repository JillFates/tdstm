import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.AssetDependencyStatus
import com.tdsops.tm.enums.domain.ValidationType
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.security.Permission
import net.transitionmanager.service.SecurityService

/**
 * Created by @oluna on 4/5/17.
 */

@Slf4j
@Secured('isAuthenticated()')
class WsAssetController implements ControllerMethods {
	SecurityService securityService

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
		long foundAssetId = 0L
		String assetClass = ""

		List<String> errors = []

		if(assetId){
			AssetEntity sampleAssetEntity = AssetEntity.get(assetId)
			//check that the asset is part of the project
			if(!securityService.isCurrentProjectId(sampleAssetEntity.projectId)){
				log.error(
						"Security Violation, user {} attempted to access an asset not associated to the project",
						securityService.getCurrentUsername()
				)
				errors << "Asset not found in current project"
			}
			assetClassSample = sampleAssetEntity.assetClass
		}

		if(!errors) {
			AssetEntity assetEntity
			if (assetClassSample) {
				assetEntity = AssetEntity.findByAssetNameAndAssetClass(name, assetClassSample)
			} else {
				assetEntity = AssetEntity.findByAssetName(name)
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

			renderAsJson(jsonMap)
		}else{
			renderFailureJson(errors)
		}
	}

	/**
	 *
	 * @param id
	 * @param name
	 * @param dependencies default (groovy)false
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
			renderFailureJson("You don't have the correct permission to Clone Assets Dependencies")
			return
		}


		if(!errors) {
			AssetEntity assetToClone = AssetEntity.get(assetId)
			if(assetToClone) {
				//check that the asset is part of the project
				if(!securityService.isCurrentProjectId(assetToClone.projectId)){
					log.error(
							"Security Violation, user {} attempted to access an asset not associated to the project",
							securityService.getCurrentUsername()
					)
					errors << "Asset not found in current project"
				}

				if(!errors) {
					AssetEntity clonedAsset = assetToClone.clone([
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
			}else{
				errors << "The asset specified to clone was not found"
			}
		}

		if(!errors){
			renderAsJson([assetId : clonedAsset.id])
		}else{
			renderFailureJson(errors)
		}
	}

}
