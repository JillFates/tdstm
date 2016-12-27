import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.License
import net.transitionmanager.domain.Project
import net.transitionmanager.service.EmptyResultException
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.UnauthorizedException
import net.transitionmanager.service.LicenseAdminService

import net.transitionmanager.controller.ServiceResults

/**
 * Created by octavio on 11/14/16.
 */
@Secured('isAuthenticated()')
@Slf4j
@Slf4j(value='logger', category='grails.app.controllers.WsLicenseController')
class WsLicenseController implements ControllerMethods {
	LicenseAdminService licenseAdminService
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
		License lic
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

	def getLicenseRequestHash(){
		def id = params.id
		License lic
		if(id) {
			lic = License.get(id)
		}

		if(lic) {
			renderSuccessJson(lic.toEncodedMessage())
		}else{
			response.status = 404 //Not Found
			render "${id} not found."
		}
	}

	def deleteLicense(){
		def id = params.id
		def lic
		if(id) {
			lic = License.get(id)
		}

		if(lic) {
			lic.delete()
			renderSuccessJson("Successful Deleted")
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
				lic.owner = securityService.loadCurrentPerson().company
				lic.requestDate = new Date()
				lic.status = License.Status.PENDING
				lic.method = License.Method.MAX_SERVERS
				lic.installationNum = licenseAdminService.getInstallationId()
				lic.hostName = licenseAdminService.hostName
				lic.websitename = licenseAdminService.FQDN
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
					lic.installationNum = "${lic.installationNum}|${project.name}|${client.name}"
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

	def loadLicense(){
		try{
			def json = request.JSON
			log.info("license ID: {}", params.id)
			def lic = License.get(params.id)

			if(lic){
				/*
				if(lic.hash){
					response.status = 302 //Found
					render "${id} already has a license"
				}else {*/
					lic.hash = json.hash

					licenseAdminService.load(lic.hash)
					def lico = licenseAdminService.useLicense()

					lic.status = License.Status.ACTIVE

					if (lic.save()) {
						renderSuccessJson("Ok")
					} else {
						throw new Exception("Error while creating License Request")
					}
				//}
			}else{
				response.status = 404 //Not Found
				render "${params.id} not found."
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
