import grails.converters.JSON
import com.tdssrc.grails.GormUtil
import com.tdsops.tm.enums.domain.EntityType;
import com.tdssrc.grails.HtmlUtil

class CommonController {
	def securityService
	def projectService
	def controllerService
	def assetEntityService

	def index() { }

	/**
	 * Initializing Help Text for a given entity type.
	 *@param : entityType type of entity.
	 *@return : Json data.
	 */
	def tooltips() {
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
	def tooltipsUpdate() {
		def project = controllerService.getProjectForPage(this, "EditProjectFieldSettings")
		if (! project) 
			return

		def entityType = request.JSON.entityType
		def helpText = request.JSON.jsonString
		def fields = JSON.parse(request.JSON.fields);
		def category = EntityType.getListAsCategory(entityType)
		def result = null;
		try{
			def attributes = projectService.getAttributes(entityType)?.attributeCode
			def assetTypes=EntityType.list
			fields.each{
				project[it.label]=it.id
			}
			if(!project.validate() || !project.save(flush:true)){
				def etext = "Project customs unable to Update "+GormUtil.allErrorsString( project )
				log.error( etext )
				result = ServiceResults.fail(etext) as JSON
			} else {
				def values = KeyValue.getAll(project, category, null)
				def keysMap = [:]
				if (values != null) {
					values.each{ v -> keysMap[v.key] = v }
				}
				attributes.each{ k ->
					def keyMap = keysMap[k]
					if (!keyMap) {
						keyMap = new KeyValue( project:project ,category:category, key:k, value:helpText.("$k"))
					}
					keyMap.value = helpText.("$k")
					if(!keyMap.validate() || !keyMap.save(flush:true)){
						def etext = "tooltipsUpdate Unable to create HelpText"+GormUtil.allErrorsString( keyMap )
						log.error( etext )
						result = ServiceResults.fail(etext) as JSON
					}
				}
				if (result == null) {
					result = ServiceResults.success() as JSON	
				}
			}
		} catch(Exception ex){
			log.error "An error occurred : ${ex}", ex
			result = ServiceResults.fail() as JSON
		}
		render result;
	}
	
	/**
	 *This action is used to get Key,values of Help Text and append to asset cruds.
	 *@param : entityType type of entity.
	 *@return : json data.
	 */
	def retrieveTooltips() {
		def returnMap =[:]
		def entityType = EntityType.getKeyByText(params.type)
		def project = securityService.getUserCurrentProject()
		returnMap = assetEntityService.retrieveTooltips(entityType, project)
		render returnMap as JSON
	}
	
	def tmLinkableUrl (){
		def errMsg = null
		try{
			def linkableUrl = params["linkableUrl"]
			def isLinkableUrl = HtmlUtil.isMarkupURL(linkableUrl)
			if(!isLinkableUrl){
				errMsg = "The format of the linkable URL is invalid."
			}
		}catch(e){
			e.printStackTrace()
			errMsg = "There's been an error validating the Linkable Url."
		}
		if(errMsg){
			ServiceResults.respondWithError(response, errMsg)
		}else{
			ServiceResults.respondWithSuccess(response, [])
		}
	}
}
