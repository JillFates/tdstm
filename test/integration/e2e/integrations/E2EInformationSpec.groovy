package e2e.integrations

import net.transitionmanager.domain.Project
import net.transitionmanager.service.ProjectService
import spock.lang.Specification
import test.helper.ProjectTestHelper

class E2EInformationSpec extends Specification {
	// Set transactional false to persist at database when spec finishes
	static transactional = false

	// IOC
	ProjectService projectService
	private ProjectTestHelper projectHelper = new ProjectTestHelper()
	private Project project

	// E2E Data
	Map<String, String> e2eProjectData = [
			"projectName": "E2E Project",
			"projectCode": "E2E Project",
			"projectDesc": "E2E Project for automation purposes",
			"projectClient": "E2E Project Client",
			"projectCompany": "E2E Project Company"
	]

	void setup(){
		project = projectHelper.createProject(e2eProjectData)
	}

	void "Setup E2E Project data"() {
		expect:
			project.projectCode == e2eProjectData.projectCode
	}
}