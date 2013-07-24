import grails.converters.JSON
import com.tdssrc.eav.EavAttribute
import com.tdssrc.grails.GormUtil
import com.tdsops.tm.enums.domain.EntityType;
import com.tdssrc.eav.EavEntityType

class CommonController {
	def securityService
	def index = { }

	/**
	 * Initialising Help Text for a given entity type.
	 *@param : entityType type of entity.
	 *@return : json data.
	 */

	def tooltips = {
		def entityType = request.JSON.entityType
		def project = securityService.getUserCurrentProject()
		def defProject= Project.getDefaultProject()
		def category = EntityType.getListAsCategory(entityType)
		def keyValueMap = [:]
		def keyMap = [:]
		def kv = KeyValue.getAll(project, category, defProject)
		kv.each{ k -> keyMap << [(k.key) : (k.value)]}
		if( !keyMap ){
			def eavEntityType = EavEntityType.findByDomainName(entityType)
			def attributes = EavAttribute.findAllByEntityType( eavEntityType )?.attributeCode
			attributes.each{f->
				keyValueMap << [(f): keyMap[f]?:""]
			}
		}else{
			keyValueMap = keyMap
		}
		def returnMap =[(entityType):keyValueMap]
		render returnMap as JSON
	}

	/**
	 *This action is used to update Help Text and display it to user
	 *@param : entityType type of entity.
	 *@return success string.
	 */

	def tooltipsUpdate = {
		def entityType = request.JSON.entityType
		def helpText = request.JSON.jsonString
		def category = EntityType.getListAsCategory(entityType)
		def project = securityService.getUserCurrentProject()
		try{
			def eavEntityType = EavEntityType.findByDomainName(entityType)
			def attributes = EavAttribute.findAllByEntityType( eavEntityType )?.attributeCode
			attributes.each{ k ->
				def keyMap = KeyValue.findByCategoryAndKey(category, k).find{it.project==project}
				if(!keyMap)
					keyMap = new KeyValue( project:project ,category:category, key:k, value:helpText.("$k"))
				else{
					keyMap.value = helpText.("$k")
				}
				if(!keyMap.validate() || !keyMap.save(flush:true)){
					def etext = "tooltipsUpdate Unable to create HelpText"+GormUtil.allErrorsString( keyMap )
					log.error( etext )
				}
			}
		} catch(Exception ex){
			log.error "An error occurred : ${ex}"
		}
		render "success"
	}
}
