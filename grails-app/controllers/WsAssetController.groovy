import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.GormUtil
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.service.AssetEntityService
import org.hibernate.Criteria

/**
 * Created by @oluna on 4/5/17.
 */

@Slf4j
@Secured('isAuthenticated()')
class WsAssetController implements ControllerMethods {
	//AssetEntityService assetEntityService

	/**
	 * Check for uniqueness of the asset name, it can be checked against the AssetClass of another asset
	 *
	 * 	@param name <Name of the asset to check>
	 * 	@param assetId <asset to filter the class from>
	 */
	def checkForUniqueName(String name, Long assetId){
		/*def jsonRequest = request.JSON
		String name = jsonRequest.name ?: ""
		Long assetId = jsonRequest.assetId
		*/

		/*
		List<AssetEntity> list = AssetEntity.list()
		list.each {
			log.info("Asset: {}", it)
		}*/

		boolean unique = true
		AssetClass assetClassSample
		long foundAssetId = 0L
		String assetClass = ""

		if(assetId){
			AssetEntity sampleAssetEntity = AssetEntity.get(assetId)
			assetClassSample = sampleAssetEntity.assetClass
		}

		AssetEntity assetEntity
		if(assetClassSample){
			assetEntity = AssetEntity.findByAssetNameAndAssetClass(name, assetClassSample)
		}else{
			assetEntity = AssetEntity.findByAssetName(name)
		}

		if(assetEntity){
			unique = false
			foundAssetId = assetEntity.id
			assetClass = assetEntity.assetClass.toString()
		}

		Map<String, ?> jsonMap = [unique:unique]
		if(!unique){
			jsonMap.assetId = foundAssetId
			jsonMap.assetClass = assetClass
		}

		renderAsJson(jsonMap)
	}

	/**
	 *
	 * @param id
	 * @param name
	 * @param dependencies default "0"
	 */
	def clone(Long id, String name, String dependencies){
		/*
		def jsonRequest = request.JSON
		Long id = jsonRequest.id
		String name = jsonRequest.name
		String dependencies = jsonRequest.dependencies


		AssetEntity assetToClone = AssetEntity.get(id)
		AssetEntity clonedAsset = GormUtil.cloneDomain(assetToClone)

		clonedAsset.assetName = name

		clonedAsset.validation = com.tdsops.tm.enums.domain.ValidationType.DIS

		// Cloning assets
		if (params.cloneDependencies=='1') {
			for (dependency in assetToClone.supportedDependencies() ) {
				AssetDependency clonedDependency = GormUtil.cloneDomain(dependency, [asset:clonedAsset])
				clonedDependency.status = com.tdsops.tm.enums.domain.AssetDependencyStatus.QUESTIONED
			}
			for (dependency in assetToClone.requiredDependencies() ) {
				AssetDependency clonedDependency = GormUtil.cloneDomain(dependency, [dependent:clonedAsset])
				clonedDependency.status = com.tdsops.tm.enums.domain.AssetDependencyStatus.QUESTIONED
			}
		}
		*/
	}

}
