import grails.converters.JSON
import com.tdssrc.grails.GormUtil
import com.tdsops.tm.enums.domain.EntityType;

class CommonController {
	def securityService
	def projectService
	
	def index = { }

	/**
	 * Initializing Help Text for a given entity type.
	 *@param : entityType type of entity.
	 *@return : Json data.
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
			def attributes = projectService.getAttributes(entityType)?.attributeCode
			attributes.each{f->
				keyValueMap << [(f): keyMap[f]?:(f.contains('custom')? f: '')]
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
		def fields = JSON.parse(request.JSON.fields);
		def category = EntityType.getListAsCategory(entityType)
		def project = securityService.getUserCurrentProject()
		try{
			def attributes = projectService.getAttributes(entityType)?.attributeCode
			def assetTypes=EntityType.list
			fields.each{
				project[it.label]=it.id
			}
			if(!project.validate() || !project.save(flush:true)){
				def etext = "Project customs unable to Update "+GormUtil.allErrorsString( project )
				log.error( etext )
			}
			attributes.each{ k ->
				def keyMap = KeyValue.findAllByCategoryAndKey(category, k).find{it.project==project}
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
	
	/**
	 *This action is used to get Key,values of Help Text and append to asset cruds.
	 *@param : entityType type of entity.
	 *@return : json data.
	 */
	def getTooltips ={
		def returnMap =[:]
		def entityType = EntityType.getKeyByText(params.type)
		def category = EntityType.getListAsCategory( entityType )
		def project = securityService.getUserCurrentProject()
		try{
			def attributes = projectService.getAttributes(entityType)?.attributeCode
			attributes.each{ k ->
				def keyMap = KeyValue.findAllByCategoryAndKey(category, k).find{it.project==project}
				returnMap << [(k): keyMap?.value]
			}
		}catch(Exception ex){
			log.error "An error occurred : ${ex}"
		}
		render returnMap as JSON
	}
	
}
