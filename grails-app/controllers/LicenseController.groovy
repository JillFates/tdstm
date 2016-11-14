import com.github.icedrake.jsmaz.Smaz
import groovy.json.JsonBuilder
import net.transitionmanager.domain.License
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.license.LicenseService
import org.apache.commons.codec.binary.Base64

//Client Controller
class LicenseController {

	LicenseService licenseService
	SecurityService securityService
	ProjectService projectService

	/**
	 * forward to Manager
	 * @return
	 */
	def index() {
		redirect action:"manager"
	}

	/**
	 * Manager of all licenses
	 * @return
	 */
	def manager(){
		def licenses = License.findAll()
		[licenses:licenses]
	}

	/**
	 * Request new License
	 * @return
	 */
	def request(){
		def userLogin = securityService.getUserLogin()
		def projects = projectService.getUserProjects(userLogin, true)
		projects = projects.inject([[id:"all", projectCode:"-- Multiple Projects --", client:[id:'', label:'']]]){ arr, p ->
			def client = p.client
			arr << [id:p.id, projectCode:p.projectCode, client:[id:client.id, label:client.name]]
		}

		//def methodTypes = [[value:'server', label:'Server count', enabled:true], [value:'token', label:'Token', enabled:true], [value:'custom', label:'Custom', enabled:false]]

		def license = null
		if(params.id){
			license = License.get(params.id)
		}

		[license:license, projects:projects]
	}

	/**
	 * Where should I check for the valid parameters?
	 * @return
	 */
	def generateEnvelop(){
		License lic
		if(params.id){
			lic = License.get(params.id)
		}else{
			lic = new License()
			lic.requestDate = new Date()
			lic.instalationNum = licenseService.getInstalationId()
		}

		lic.email = params.email
		lic.environment = params.environment
		lic.project = params.project
		lic.requestNote = params.requestNote

		if(lic.project != "all"){
			def project = Project.get(lic.project)
			def client = project.client
			lic.instalationNum = "${lic.instalationNum}|${project.name}|${client.name}"
		}

		if (lic.save()) {
			redirect action:"showRequest", id:lic.id

		}else{
			lic.errors.each {
				log.error(it)
			}
			flash.message = "Error while creating Request"
		}
	}

	def showRequest(){
		def lic = License.get(params.id)

		def json = [
				id:lic.id,
				email:lic.email,
				environment:lic.environment,
				instalationNum:lic.instalationNum,
				project: lic.project,
				requestDate: lic.requestDate,
				requestNote: lic.requestNote
		]

		def jsonString = new JsonBuilder( json ).toString()

		String encodedString = new String(Base64.encodeBase64(Smaz.compress(jsonString)))

		[id:lic.id, encodedString: encodedString]
	}

}
