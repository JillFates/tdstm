import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
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
	 * @param name 	Name of the asset to check
	 * @param assetId	asset
	 */
	def checkForUniqueName(){
		def jsonRequest = request.JSON
		String name = jsonRequest.name ?: ""
		Long assetId = jsonRequest.assetId

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
}
