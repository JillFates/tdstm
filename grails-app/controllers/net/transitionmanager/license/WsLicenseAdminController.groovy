package net.transitionmanager.license

import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.ProjectStatus
import grails.core.GrailsApplication
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.party.PartyGroup
import net.transitionmanager.project.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.project.ProjectService
/**
 * Created by octavio on 11/14/16.
 */
@Secured('isAuthenticated()')
class WsLicenseAdminController implements ControllerMethods {
	LicenseCommonService licenseCommonService
	LicenseAdminService licenseAdminService
	ProjectService projectService
	GrailsApplication grailsApplication

	def managerActive(){
		renderSuccessJson(licenseCommonService.isManagerEnabled())
	}

	/**
	 * Get the List of projects attached with the clients, this is being used to select Client as well
	 * TODO: Do we need a list of projects in the Manager? What for? the Project is a String as far as I know
	 * @return
     */
	def fetchProjects(){
		def searchParams = [:]
		searchParams.sortOn = 'name'
		List<Project> projects = projectService.getUserProjects(securityService.hasPermission(Permission.ProjectShowAll), ProjectStatus.ACTIVE, searchParams)

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
		renderSuccessJson(License.findAll()*.toJsonMap(grailsApplication))
	}

	@HasPermission(Permission.LicenseView)
	def getLicense(String id){
		License lic
		if (id) {
			lic = License.get(id)
		}

		if (lic) {
			renderSuccessJson(lic.toJsonMap(grailsApplication))
		} else {
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

		if (hash) {
			renderSuccessJson(hash)
		} else {
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
        if (licenseAdminService.deleteLicense(id)) {
            renderSuccessJson("Successful Deleted")
        } else {
            sendNotFound()
        }
    }

	/**
	 * generate a license request
	 * @return
	 */
	@HasPermission(Permission.LicenseAdministration)
    def generateRequest() {
		def json = request.JSON

		String licenseUid = json.id
		def owner = securityService.loadCurrentPerson().company
		def email = json.email
		String environment = json.environment
		def projectId = json.projectId  // Can be a numeric id or "all", for all projects
		String requestNote = json.requestNote

		License lic = licenseAdminService.generateRequest(licenseUid, owner, email, environment, projectId, requestNote)
		licenseAdminService.sendMailRequest(lic)
		renderSuccessJson(id:lic.id, body:lic.toEncodedMessage(grailsApplication))
    }

	/**
	 * Load a license to match against a request
	 * @return
	 */
	@HasPermission(Permission.LicenseAdministration)
	def loadLicense(){ // Apply license
		def json = request.JSON
		License lic = License.get(params.id)

		if (lic) {
			lic.hash = json.hash

			if (licenseAdminService.load(lic)) {
				renderSuccessJson("Ok")
			} else {
				throw new Exception("Error while loading the license")
			}
		} else {
			response.status = 404 //Not Found
			render "${params.id} not found."
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
		if (emailData) {
			renderSuccessJson(emailData)
		} else {
			renderFailureJson("Could not get Email Data")
		}
	}

}
