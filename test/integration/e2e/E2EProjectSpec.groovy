package e2e

import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.service.ProjectService
import org.codehaus.groovy.grails.web.json.JSONObject
import spock.lang.Specification
import test.helper.MoveBundleTestHelper
import test.helper.MoveEventTestHelper
import test.helper.ProjectTestHelper
import test.helper.PersonTestHelper

class E2EProjectSpec extends Specification {
	// Set transactional false to persist at database when spec finishes
	static transactional = false

	// IOC
	ProjectService projectService
	private ProjectTestHelper projectHelper = new ProjectTestHelper()
	private PersonTestHelper personHelper = new PersonTestHelper()
	private MoveEventTestHelper eventHelper = new MoveEventTestHelper()
	private MoveBundleTestHelper bundleHelper = new MoveBundleTestHelper()
	private Project project
	private UserLogin e2eUserLogin1
	private UserLogin e2eUserLogin2
	private UserLogin e2eUserLogin3
	private UserLogin e2eUserLogin4
	private MoveEvent buildoutEvent
	private MoveEvent m1PhysicalEvent
	private MoveBundle buildoutBundle
	private MoveBundle m1PhyBundle
	JSONObject dataFile

	private JSONObject getJsonObjectFromFile(){
		String jsonText = this.getClass().getResource("E2EProjectData.json").text
		return new JSONObject(jsonText)
	}

	void setup(){
		dataFile = getJsonObjectFromFile()
		project = projectHelper.createProject(dataFile.e2eProjectData)
		e2eUserLogin1 = personHelper.createPersonWithLoginAndRoles(dataFile.e2eUserData1, project)
		e2eUserLogin2 = personHelper.createPersonWithLoginAndRoles(dataFile.e2eUserData2, project)
		e2eUserLogin3 = personHelper.createPersonWithLoginAndRoles(dataFile.e2eUserData3, project)
		e2eUserLogin4 = personHelper.createPersonWithLoginAndRoles(dataFile.e2eUserData4, project)
		buildoutEvent = eventHelper.createMoveEvent(project, dataFile.e2eEventName1)
		m1PhysicalEvent = eventHelper.createMoveEvent(project, dataFile.e2eEventName2)
		buildoutBundle = bundleHelper.createBundle(dataFile.e2eBundleName1, project, buildoutEvent)
		m1PhyBundle = bundleHelper.createBundle(dataFile.e2eBundleName2, project, m1PhysicalEvent)
	}

	void "Setup E2E Project data"() {
		expect:
			Project.findWhere([projectCode: dataFile.e2eProjectData.projectCode]) != null
			UserLogin.findWhere([username: dataFile.e2eUserData1.email]) != null
			UserLogin.findWhere([username: dataFile.e2eUserData2.email]) != null
			UserLogin.findWhere([username: dataFile.e2eUserData3.email]) != null
			UserLogin.findWhere([username: dataFile.e2eUserData4.email]) != null
			MoveEvent.findWhere([name: dataFile.e2eEventName1]) != null
			MoveEvent.findWhere([name: dataFile.e2eEventName2]) != null
			MoveBundle.findWhere([name: dataFile.e2eBundleName1]) != null
			MoveBundle.findWhere([name: dataFile.e2eBundleName2]) != null
	}
}