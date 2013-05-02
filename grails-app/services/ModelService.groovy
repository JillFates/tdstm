import org.apache.shiro.SecurityUtils
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass

import com.tds.asset.AssetCableMap
import com.tds.asset.AssetEntity



class ModelService {

    static transactional = true
	def sessionFactory
	def assetEntityAttributeLoaderService
   /**
	 * @param fromModel : instance of the model that is being merged
	 * @param toModel : instance of toModel 
	 * @return : updated assetCount
	 */
	def mergeModel(fromModel, toModel){
		//	Revise Asset, and any other records that may point to this model
		def fromModelAssets = AssetEntity.findAllByModel( fromModel )
		def assetUpdated =0 // assetUpdated flag to count the assets updated by merging models .
		
		fromModelAssets.each{ assetEntity->
			assetEntity.model = toModel
			assetEntity.assetType = toModel.assetType
			if(assetEntity.save(flush:true)){
				assetUpdated++
			}
			assetEntityAttributeLoaderService.updateModelConnectors( assetEntity )
		}
		
		// Delete model associated record
		AssetCableMap.executeUpdate("delete AssetCableMap where fromConnectorNumber in (from ModelConnector where model = ${fromModel.id})")
		AssetCableMap.executeUpdate("""Update AssetCableMap set status='missing',toAsset=null,
												toConnectorNumber=null,toAssetRack=null,toAssetUposition=null
												where toConnectorNumber in (from ModelConnector where model = ${fromModel.id})""")
		ModelConnector.executeUpdate("delete ModelConnector where model = ?",[fromModel])
		
		
		// Coping data from other models into any blank field in the target model.
		def modelDomain = new DefaultGrailsDomainClass( Model.class )
		modelDomain.properties.each{
			def prop = it.name
			// Restricting few fields to be updated e.g. 'id', 'modelName', 'modelConnectors', 'racks' .
			def notToUpdate = ['beforeDelete','beforeInsert', 'beforeUpdate','id', 'modelName', 'modelConnectors', 'racks']
			if(it.isPersistent() && !toModel."${prop}" && !notToUpdate.contains(prop)){
				toModel."${prop}" = fromModel."${prop}"
			}
		}
		if(!toModel.save(flush:true)){
			toModel.errors.allErrors.each{println it}
		}
		
		// Add to the AKA field list in the target record
		def toModelAlias = ModelAlias.findAllByModel(toModel).name
		if(!toModelAlias.contains(fromModel.modelName)){
			def fromModelAlias = ModelAlias.findAllByModel(fromModel)
			ModelAlias.executeUpdate("delete from ModelAlias mo where mo.model = ${fromModel.id}")
			
			fromModelAlias.each{
				toModel.findOrCreateAliasByName(it.name, true)
			}
			//merging fromModel as AKA of toModel
			toModel.findOrCreateAliasByName(fromModel.modelName, true)
			
			// Delete model record
			fromModel.delete()
			
			def principal = SecurityUtils.subject?.principal
			def user
			if( principal ){
				user = UserLogin.findByUsername( principal )
				def person = user.person
				def bonusScore = person.modelScoreBonus ? person.modelScoreBonus:0
				if(user){
					person.modelScoreBonus = bonusScore+10
					person.modelScore = person.modelScoreBonus + person.modelScore
					person.save(flush:true)
				}
			}
			/**/
		} else {
			//	Delete model record
			fromModel.delete()
			sessionFactory.getCurrentSession().flush();
		}
		// Return to model list view with the flash message "Merge completed."
		return assetUpdated
	}
}
