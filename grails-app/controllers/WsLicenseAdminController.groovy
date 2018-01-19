import com.tdsops.common.security.spring.HasPermission
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.License
import net.transitionmanager.domain.PartyGroup
import net.transitionmanager.domain.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.service.EmptyResultException
import net.transitionmanager.service.LicenseCommonService
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.UnauthorizedException
import net.transitionmanager.service.LicenseAdminService

import net.transitionmanager.controller.ServiceResults

/**
 * Created by octavio on 11/14/16.
 */
@Secured('isAuthenticated()')
@Slf4j(value='logger', category='grails.app.controllers.WsLicenseController')
class WsLicenseAdminController implements ControllerMethods {
	LicenseCommonService licenseCommonService
	LicenseAdminService licenseAdminService
	ProjectService projectService
	SecurityService securityService

	def managerActive(){
		renderSuccessJson(licenseCommonService.isManagerEnabled())
	}

	/**
	 * Get the List of projects attached with the clients, this is being used to select Client as well
	 * TODO: Do we need a list of projects in the Manager? What for? the Project is a String as far as I know
	 * @return
     */
	def fetchProjects(){
		List<Project> projects = projectService.getUserProjects(securityService.hasPermission(Permission.ProjectShowAll))

		List<Map> initialData = [[id:"all", name:"-- Multiple Projects --", client:[id:'', name:'']]]

		List<Map> projectsMap = projects.inject(initialData){ arr, p ->
			PartyGroup client = p.client
			arr << [id:p.id, name:p.projectCode, client:[id:client.id, name:client.name]]
		}

		renderSuccessJson(projectsMap)
	}

	//TODO: OLB Refactor into the ControllerMethods
	private renderEnum(daEnum){
		List list = daEnum.values().collect {
			it.name()
		}

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
	@HasPermission(Permission.LicenseView)
	def getLicenses(){
		renderSuccessJson(License.findAll()*.toJsonMap())
	}

	@HasPermission(Permission.LicenseView)
	def getLicense(){
		Long id = params.id
		License lic
		if(id) {
			lic = License.get(id)
		}

		if(lic) {
			renderSuccessJson(lic.toJsonMap())
		}else{
			//TODO: OLB 20170124 Change this to the AJax Approach
			response.status = 404 //Not Found
			render "${id} not found."
		}

	}

	/**
	 * get the license request body used to present the hash and other information to the user
	 * so he can request the license
	 * @return
	 */
	@HasPermission(Permission.LicenseView)
	def getLicenseRequestHash(){
		def id = params.id

		String hash = licenseAdminService.getLicenseRequestBody(id)

		if(hash) {
			renderSuccessJson(hash)
		}else{
			response.status = 404 //Not Found
			render "${id} not found."
		}
	}

	/**
	 * Delete license
	 * @param id identifier of the license to delete
	 * @return
	 */
	@HasPermission(Permission.LicenseDelete)
    def deleteLicense(String id){
        if(licenseAdminService.deleteLicense(id)) {
            renderSuccessJson("Successful Deleted")
        }else{
            sendNotFound()
        }
    }

	/**
	 * generate a license request
	 * @return
	 */
	@HasPermission(Permission.LicenseAdministration)
    def generateRequest() {

        try {

            def json = request.JSON

            String licenseUid = json.id
            def owner = securityService.loadCurrentPerson().company
            def email = json.email
            String environment = json.environment
            def projectId = json.projectId  // Can be a numeric id or "all", for all projects
            String requestNote = json.requestNote

            License lic = licenseAdminService.generateRequest(licenseUid, owner, email, environment, projectId, requestNote)

            if (lic) {
                renderSuccessJson(id:lic.id, body:lic.toEncodedMessage())
            }

        } catch (e) {
            log.error("Error", e)
            handleException e, log
        }

    }

	/**
	 * Load a license to match against a request
	 * @return
	 */
	@HasPermission(Permission.LicenseAdministration)
	def loadLicense(){ // Apply license
		try{
			def json = request.JSON
			License lic = License.get(params.id)

			if(lic){
				lic.hash = json.hash

				if (licenseAdminService.load(lic)) {
					renderSuccessJson("Ok")
				} else {
					throw new Exception("Error while loading the license")
				}
			}else{
				response.status = 404 //Not Found
				render "${params.id} not found."
			}
		} catch (e) {
			log.error(e.message)
			renderErrorJson(e.message)
		}
	}

	/**
	 * Email a license request
	 * @param id identifier of the license request to email
	 * @return
	 */
	@HasPermission(Permission.LicenseAdministration)
	def emailRequest(String id){
		if(licenseAdminService.resubmitRequest(id)){
			renderSuccessJson("Ok")
		}else{
			renderFailureJson("Email could not be sent")
		}
	}

	/**
	 * Email a license request
	 * @param id identifier of the license request to email
	 * @return
	 */
	@HasPermission(Permission.LicenseAdministration)
	def emailRequestData(String id){
		LicenseAdminService.EmailHolder emailData = licenseAdminService.emailRequestData(id)
		if(emailData){
			renderSuccessJson(emailData)
		}else{
			renderFailureJson("Could not get Email Data")
		}
	}


	/*** HELPER *************************/
	/* I believe that this should be on the trait  ¬¬ */
	private void preHandleException(Exception e, boolean includeException = false) { // TODO move to super class
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
