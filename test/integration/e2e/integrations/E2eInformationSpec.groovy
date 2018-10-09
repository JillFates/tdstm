package e2e.integrations

import net.transitionmanager.domain.Project
import net.transitionmanager.service.ProjectService
import spock.lang.Specification
import test.helper.E2eInformationHelper

class E2eInformationSpec extends Specification {
	// Set transactional false to persist at database when spec finishes
	static transactional = false

	// IOC
	ProjectService projectService
	private E2eInformationHelper e2eHelper = new E2eInformationHelper()
	private Project project

	// E2E Data
	def e2eProjectData = [
			"projectName": "E2E Project",
			"projectCode": "E2E Project",
			"projectDesc": "E2E Project for automation purposes",
			"projectClient": "E2E Project Client",
			"projectCompany": "E2E Project Company"
	]

	void setup(){
		project = e2eHelper.createProject(e2eProjectData)
	}

	void "Setup E2E Project data"() {
		expect:
			project.projectCode == e2eProjectData.projectCode
	}
}