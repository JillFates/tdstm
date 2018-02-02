import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.EntityType
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.HtmlUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.KeyValue
import net.transitionmanager.domain.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.service.AssetEntityService
import net.transitionmanager.service.ControllerService
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.SecurityService

@Slf4j(value='logger', category='grails.app.controllers.CommonController')
@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class CommonController implements ControllerMethods {

	AssetEntityService assetEntityService
	ControllerService controllerService
	ProjectService projectService
	SecurityService securityService

	// TODO: This should be deleted
	def index() { }

	/**
	 * Initializing Help Text for a given entity type.
	 * @param : entityType type of entity.
	 * @return : Json data.
	 * TODO : REMOVE TM-6722
	 */
	@Deprecated
	@HasPermission(Permission.ProjectFieldSettingsView)
	def tooltips() {
		def entityType = request.JSON.entityType
		def keyValueMap = [:]
		def keyMap = [:]

		List<KeyValue> list = KeyValue.getAll(securityService.userCurrentProject,
			EntityType.getListAsCategory(entityType), Project.getDefaultProject())

		list.each { KeyValue kv -> keyMap[kv.key] = kv.value }

		if (keyMap) {
			keyValueMap = keyMap
		}
		else {
			projectService.getAttributes(entityType)*.attributeCode.each { String code ->
				keyValueMap[code] = keyMap[code] ?: (code.contains('custom') ? code : '')
			}
		}

		render([(entityType): keyValueMap] as JSON)
	}

	/**
	 * Updates Help Text and displays it.
	 * @param : entityType type of entity.
	 * @return success string.
	 * TODO : REMOVE TM-6722
	 */
	@Deprecated
	@HasPermission(Permission.ProjectFieldSettingsEdit)
	def tooltipsUpdate() {
		Project project = controllerService.getProjectForPage(this)
		if (! project) return

		def entityType = request.JSON.entityType
		def helpText = request.JSON.jsonString
		def category = EntityType.getListAsCategory(entityType)
		def result
		try {
			def attributes = projectService.getAttributes(entityType)?.attributeCode
			JSON.parse(request.JSON.fields).each { project[it.label] = it.id }

			saveWithWarnings project
			if (project.hasErrors()) {
				renderFailureJson('Project customs unable to Update: ' + GormUtil.allErrorsString(project))
				return
			}

			Collection<KeyValue> values = KeyValue.getAll(project, category, null)
			def keysMap = [:]
			if (values != null) {
				values.each { v -> keysMap[v.key] = v }
			}
			attributes.each { k ->
				def keyMap = keysMap[k] ?: new KeyValue(project: project, category: category, key: k, value: helpText.("$k"))
				keyMap.value = helpText.("$k")
				saveWithWarnings keyMap, true
				if (keyMap.hasErrors()) {
					renderFailureJson("tooltipsUpdate Unable to create HelpText" + GormUtil.allErrorsString(keyMap))
					return
				}
			}
			if (result == null) {
				renderSuccessJson()
				return
			}
		} catch(e) {
			logger.error "An error occurred : $e", e
			renderFailureJson()
		}
		render result
	}

	/**
	 * Get Key, values of Help Text and append to asset cruds.
	 * @param : entityType type of entity.
	 * @return : json data.
	 * TODO : REMOVE TM-6722
	 */
	@Deprecated
	@HasPermission(Permission.UserGeneralAccess)
	def retrieveTooltips() {
		renderAsJson assetEntityService.retrieveTooltips(EntityType.getKeyByText(params.type),
				securityService.userCurrentProject)
	}

	@HasPermission(Permission.UserGeneralAccess)
	def tmLinkableUrl() {
		String errMsg
		try {
			def jsonData = request.JSON
			def linkableUrl = jsonData.linkableUrl
			def isLinkableUrl = HtmlUtil.isMarkupURL(linkableUrl)
			if(!isLinkableUrl){
				errMsg = "The format of the linkable URL is invalid."
			}
		}catch(e){
			e.printStackTrace()
			errMsg = "There's been an error validating the Linkable Url."
		}
		if (errMsg) {
			renderErrorJson(errMsg)
		}else{
			renderSuccessJson([])
		}
	}
}
