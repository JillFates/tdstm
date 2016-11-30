import com.github.icedrake.jsmaz.Smaz
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import groovy.json.JsonBuilder
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.License
import net.transitionmanager.domain.Project
import net.transitionmanager.service.EmptyResultException
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.UnauthorizedException
import net.transitionmanager.service.license.LicenseService
import org.apache.commons.codec.binary.Base64

/**
 * Created by octavio on 11/14/16.
 */
@Secured('isAuthenticated()')
@Slf4j
@Slf4j(value='logger', category='grails.app.controllers.WsLicenseController')
class WsLicenseController implements ControllerMethods {
	LicenseService licenseService
	ProjectService projectService
	SecurityService securityService

	/**
	 * Get the List of projects attached with the clients, this is being used to select Client as well
	 * @return
     */
	def fetchProjects(){
		def projects = projectService.getUserProjects()
		projects = projects.inject([[id:"all", name:"-- Multiple Projects --", client:[id:'', name:'']]]){ arr, p ->
			def client = p.client
			arr << [id:p.id, name:p.projectCode, client:[id:client.id, name:client.name]]
		}

		//OL: Should we render using RESTFull ¬¬
		//renderAsJson projMap
		renderSuccessJson(projects)
	}

	private renderEnum(daEnum){
		def list = daEnum.values().collect {
			[
					id:it.id,
					name:it.name()
			]
		}

		//OL: Should we render using RESTFull ¬¬
		//renderAsJson map
		renderSuccessJson(list)
	}
	def fetchEnvironments(){
		renderEnum(License.Environment)
	}

	def fetchTypes(){
		renderEnum(License.Type)
	}

	def fetchStatus(){
		renderEnum(License.Status)
	}

	def fetchMethods(){
		renderEnum(License.Method)
	}

	/* list the licenses */
	def getLicenses(){
		renderSuccessJson(License.findAll()*.toJsonMap())
	}

	def getLicense(){
		def id = params.id
		def lic
		if(id) {
			lic = License.get(id)
		}

		if(lic) {
			renderSuccessJson(lic.toJsonMap())
		}else{
			response.status = 404 //Not Found
			render "${id} not found."
		}

	}

	/**
	 * Where should I check for the valid parameters?
	 * @return
	 */
	def generateRequest(){
		try {
			def json = request.JSON
			License lic
			if(params.id){
				lic = License.get(json.id)
			}else{
				lic = new License()
				lic.requestDate = new Date()
				lic.status = License.Status.PENDING
				lic.method = License.Method.MAX_SERVERS
				lic.instalationNum = licenseService.getInstalationId()
			}

			lic.email = json.email
			lic.environment = License.Environment.forId(json.environmentId)
			lic.project = json.projectId
			lic.requestNote = json.requestNote

			if(lic.project != "all"){
				lic.type = License.Type.SINGLE_PROJECT
				def project = Project.get(lic.project)
				if(project !=  null) {
					def client = project.client
					lic.instalationNum = "${lic.instalationNum}|${project.name}|${client.name}"
				}else{
					lic.errors.rejectValue("project", "Project (id:${lic.project}) not found")
				}
			}else{
				lic.type = License.Type.MULTI_PROJECT
			}

			if (!lic.hasErrors() && lic.save(flush:true)) {
				log.error("OLB: NDA: {}", lic.hasErrors())
				renderSuccessJson(id:lic.id, body:lic.toEncodedMessage())
			}else{
				lic.errors.each {
					log.error("lic error: {}", it)
				}
				throw new Exception("Error while creating License Request")
			}
		} catch (e) {
			log.error("Da error", e)
			preHandleException e
		}

	}

	/*** HELPER *************************/
	/* I believe that this should be on the trait  ¬¬ */
	private void preHandleException(Exception e, boolean includeException = false) {
		if (e instanceof UnauthorizedException) {
			if (includeException) {
				ServiceResults.forbidden(response, e)
			}
			else {
				ServiceResults.forbidden(response)
			}
		}
		else if (e instanceof IllegalArgumentException) {
			if (includeException) {
				ServiceResults.internalError(response, logger, e)
			}
			else {
				ServiceResults.forbidden(response)
			}
		}
		else if (e instanceof EmptyResultException) {
			if (includeException) {
				ServiceResults.internalError(response, logger, e)
			}
			else {
				ServiceResults.methodFailure(response)
			}
		}
		else if (e instanceof ValidationException) {
			render(ServiceResults.errorsInValidation(e.errors) as JSON)
		}
		else {
			ServiceResults.internalError(response, logger, e)
		}
	}
}
