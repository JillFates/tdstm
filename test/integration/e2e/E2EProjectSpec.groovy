package e2e

import net.transitionmanager.domain.DataScript
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.service.ProjectService
import org.codehaus.groovy.grails.web.json.JSONObject
import spock.lang.Specification
import test.helper.DataScriptTestHelper
import test.helper.MoveBundleTestHelper
import test.helper.MoveEventTestHelper
import test.helper.ProjectTestHelper
import test.helper.PersonTestHelper
import test.helper.ProviderTestHelper

class E2EProjectSpec extends Specification {
	// Set transactional false to persist at database when spec finishes
	static transactional = false

	// IOC
	ProjectService projectService
	private ProjectTestHelper projectHelper = new ProjectTestHelper()
	private PersonTestHelper personHelper = new PersonTestHelper()
	private MoveEventTestHelper eventHelper = new MoveEventTestHelper()
	private MoveBundleTestHelper bundleHelper = new MoveBundleTestHelper()
	private ProviderTestHelper providerHelper = new ProviderTestHelper()
	private DataScriptTestHelper etlScriptHelper = new DataScriptTestHelper()
	private Project project
	private Project projectToBeDeleted
	private UserLogin userLogin1
	private UserLogin userLogin2
	private UserLogin userLogin3
	private UserLogin userLogin4
	private MoveEvent buildoutEvent
	private MoveEvent m1PhysicalEvent
	private MoveBundle buildoutBundle
	private MoveBundle m1PhyBundle
	private Provider projectProvider
	private Provider providerToBeDeleted1
	private Provider providerToBeDeleted2
	private Provider providerToBeEdited
	private DataScript etlToBeTransformedWithPastedData
	private DataScript etlToBeEdited
	private DataScript etlToBeSearched
	JSONObject dataFile

	private JSONObject getJsonObjectFromFile(){
		String jsonText = this.getClass().getResource("E2EProjectData.json").text
		return new JSONObject(jsonText)
	}

	void setup(){
		dataFile = getJsonObjectFromFile()
		project = projectHelper.createProject(dataFile.e2eProjectData)
		projectToBeDeleted = projectHelper.createProject(dataFile.projectToBeDeletedData)
		userLogin1 = personHelper.createPersonWithLoginAndRoles(dataFile.userData1, project)
		userLogin2 = personHelper.createPersonWithLoginAndRoles(dataFile.userData2, project)
		userLogin3 = personHelper.createPersonWithLoginAndRoles(dataFile.userData3, project)
		userLogin4 = personHelper.createPersonWithLoginAndRoles(dataFile.userData4, project)
		buildoutEvent = eventHelper.createMoveEvent(project, dataFile.eventName1)
		m1PhysicalEvent = eventHelper.createMoveEvent(project, dataFile.eventName2)
		buildoutBundle = bundleHelper.createBundle(dataFile.bundleName1, project, buildoutEvent)
		m1PhyBundle = bundleHelper.createBundle(dataFile.bundleName2, project, m1PhysicalEvent)
		projectProvider = providerHelper.createProvider(project, dataFile.projectProvider)
		providerToBeDeleted1 = providerHelper.createProvider(project, dataFile.providerToBeDeleted1)
		providerToBeDeleted2 = providerHelper.createProvider(project, dataFile.providerToBeDeleted2)
		providerToBeEdited = providerHelper.createProvider(project, dataFile.providerToBeEdited)
		etlToBeTransformedWithPastedData = etlScriptHelper.createDataScript(project, projectProvider, userLogin1.person, "", dataFile.etlToBeTransformedWithPastedData)
		etlToBeEdited = etlScriptHelper.createDataScript(project, projectProvider, userLogin1.person, "", dataFile.etlToBeEdited)
		etlToBeSearched = etlScriptHelper.createDataScript(project, projectProvider, userLogin1.person, "", dataFile.etlToBeSearched)
	}

	void "Setup E2E Project data"() {
		expect:
			Project.findWhere([projectCode: dataFile.e2eProjectData.projectCode]) != null
			Project.findWhere([projectCode: dataFile.projectToBeDeletedData.projectCode]) != null
			UserLogin.findWhere([username: dataFile.userData1.email]) != null
			UserLogin.findWhere([username: dataFile.userData2.email]) != null
			UserLogin.findWhere([username: dataFile.userData3.email]) != null
			UserLogin.findWhere([username: dataFile.userData4.email]) != null
			MoveEvent.findWhere([name: dataFile.eventName1, project: project]) != null
			MoveEvent.findWhere([name: dataFile.eventName2, project: project]) != null
			MoveBundle.findWhere([name: dataFile.bundleName1, project: project]) != null
			MoveBundle.findWhere([name: dataFile.bundleName2, project: project]) != null
			Provider.findWhere([name: dataFile.projectProvider.name, project: project]) != null
			Provider.findWhere([name: dataFile.providerToBeDeleted1.name, project: project]) != null
			Provider.findWhere([name: dataFile.providerToBeDeleted2.name, project: project]) != null
			Provider.findWhere([name: dataFile.providerToBeEdited.name, project: project]) != null
			DataScript.findWhere([name: dataFile.etlToBeTransformedWithPastedData.name, project: project]) != null
			DataScript.findWhere([name: dataFile.etlToBeEdited.name, project: project]) != null
			DataScript.findWhere([name: dataFile.etlToBeSearched.name, project: project]) != null
	}
}