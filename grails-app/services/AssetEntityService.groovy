import org.jsecurity.SecurityUtils

import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tdssrc.grails.GormUtil



class AssetEntityService {

    static transactional = true

    def createOrUpdateApplicationDependencies(def params, def applicationInstance) {
		
		def principal = SecurityUtils.subject.principal
		def loginUser = UserLogin.findByUsername(principal)
		
		def supportCount = Integer.parseInt(params.supportCount)
		AssetDependency.executeUpdate("delete AssetDependency where dependent = ? ",[applicationInstance])
		
		for(int i=0; i< supportCount; i++){
			def supportAsset = params["asset_support_"+i]
			if(supportAsset){
				def asset = AssetEntity.findByIdAndProject(supportAsset, applicationInstance.project)
				if(asset){
					def assetDependency = AssetDependency.findByAssetAndDependent(asset,applicationInstance)
					if(assetDependency){
						assetDependency.dataFlowFreq = params["dataFlowFreq_support_"+i]
						assetDependency.type = params["dtype_support_"+i]
						assetDependency.status = params["status_support_"+i]
						assetDependency.updatedBy = loginUser?.person
					} else {
						assetDependency = new AssetDependency(
																asset : asset,
																dependent : applicationInstance,
																dataFlowFreq : params["dataFlowFreq_support_"+i],
																type : params["dtype_support_"+i],
																status : params["status_support_"+i],
																updatedBy : loginUser?.person,
																createdBy : loginUser?.person
																)
					}
					if ( !assetDependency.validate() || !assetDependency.save(flush:true) ) {
						def etext = "Unable to create assetDependency" +
						GormUtil.allErrorsString( assetDependency )
						   println etext
					}
				}
			}
		}
		
		def dependentCount = Integer.parseInt(params.dependentCount)
		AssetDependency.executeUpdate("delete AssetDependency where asset = ? ",[applicationInstance])
		
		for(int i=0; i< dependentCount; i++){
			def dependentAsset = params["asset_dependent_"+i]
			if(dependentAsset){
				def asset = AssetEntity.findByIdAndProject(dependentAsset, applicationInstance.project)
				if(asset){
					def assetDependency = AssetDependency.findByAssetAndDependent(applicationInstance,asset)
					if(assetDependency){
						assetDependency.dataFlowFreq = params["dataFlowFreq_dependent_"+i]
						assetDependency.type = params["dtype_dependent_"+i]
						assetDependency.status = params["status_dependent_"+i]
						assetDependency.updatedBy = loginUser?.person
					} else {
						assetDependency = new AssetDependency(
																asset : applicationInstance,
																dependent : asset,
																dataFlowFreq : params["dataFlowFreq_dependent_"+i],
																type : params["dtype_dependent_"+i],
																status : params["status_dependent_"+i],
																updatedBy : loginUser?.person,
																createdBy : loginUser?.person
																)
					}
					if ( !assetDependency.validate() || !assetDependency.save() ) {
						def etext = "Unable to create assetDependency" +
						GormUtil.allErrorsString( assetDependency )
						   println etext
					}
				}
			}
		}
    }
}
