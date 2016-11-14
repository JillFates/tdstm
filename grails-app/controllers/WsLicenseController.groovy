import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.License
import net.transitionmanager.domain.Project
import net.transitionmanager.service.EmptyResultException
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.UnauthorizedException
import net.transitionmanager.service.license.LicenseService

/**
 * Created by octavio on 11/14/16.
 */
@Secured('isAuthenticated()')
class WsLicenseController implements ControllerMethods {
	LicenseService licenseService
	ProjectService projectService
	SecurityService securityService

	def getEnvironments(){
		def envs = License.Environment.values()

		def envMap = envs.collect {
			[
					id:it.id,
					name:it.name()
			]
		}

		renderAsJson envMap
		renderSuccessJson(environments:envMap)
	}

	def getProjects(){
		def userLogin = securityService.getUserLogin()
		def projects = projectService.getUserProjects(userLogin, true)
		projects = projects.inject([[id:"all", projectCode:"-- Multiple Projects --", client:[id:'', label:'']]]){ arr, p ->
			def client = p.client
			arr << [id:p.id, projectCode:p.projectCode, client:[id:client.id, label:client.name]]
		}

		renderSuccessJson(projects:projects)
	}

	/* list the licenses */
	def getLicenses(){
		renderSuccessJson(licenses:[])
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
				lic.instalationNum = licenseService.getInstalationId()
			}

			lic.email = json.email
			lic.environment = json.environment
			lic.project = json.project
			lic.requestNote = json.requestNote

			if(lic.project != "all"){
				def project = Project.get(lic.project)
				def client = project.client
				lic.instalationNum = "${lic.instalationNum}|${project.name}|${client.name}"
			}

			//if (lic.save()) {
			if (true){
				renderSuccessJson(id:lic.id)
			}else{
				lic.errors.each {
					log.error(it)
				}
				throw new Exception("Error while creating Request")
			}
		}
		catch (e) {
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
