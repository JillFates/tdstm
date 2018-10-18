package e2e

import net.transitionmanager.domain.DataScript
import net.transitionmanager.domain.Dataview
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import net.transitionmanager.domain.Tag
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.service.ProjectService
import org.codehaus.groovy.grails.web.json.JSONObject
import spock.lang.Specification
import test.helper.DataScriptTestHelper
import test.helper.DataviewTestHelper
import test.helper.MoveBundleTestHelper
import test.helper.MoveEventTestHelper
import test.helper.ProjectTestHelper
import test.helper.PersonTestHelper
import test.helper.ProviderTestHelper
import test.helper.TagTestHelper

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
	private DataviewTestHelper dataviewHelper = new DataviewTestHelper()
	private TagTestHelper tagHelper = new TagTestHelper()
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
	private Dataview customView1
	private Dataview customView2
	private Dataview customView3
	private Dataview customView4
	private Dataview customView5
	private Dataview customView6
	private Dataview customView7
	private Dataview customView8
	private Dataview customView9
	private Dataview customView10
	private Dataview customView11
	private Tag tagToBeEdited
	private Tag tagToBeDeleted
	JSONObject dataFile

	private JSONObject getJsonObjectFromFile(){
		String jsonText = this.getClass().getResource("E2EProjectData.json").text
		return new JSONObject(jsonText)
	}

	private JSONObject getSanitizedViewObject(JSONObject dataViewJson, JSONObject customViewSchemaJson){
		dataViewJson.schema = customViewSchemaJson
		return dataViewJson
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
		customView1 = dataviewHelper.createDataview(project, userLogin1.person, getSanitizedViewObject(dataFile.customView1, dataFile.commonViewSchema))
		customView2 = dataviewHelper.createDataview(project, userLogin1.person, getSanitizedViewObject(dataFile.customView2, dataFile.commonViewSchema))
		customView3 = dataviewHelper.createDataview(project, userLogin1.person, getSanitizedViewObject(dataFile.customView3, dataFile.commonViewSchema))
		customView4 = dataviewHelper.createDataview(project, userLogin1.person, getSanitizedViewObject(dataFile.customView4, dataFile.commonViewSchema))
		customView5 = dataviewHelper.createDataview(project, userLogin1.person, getSanitizedViewObject(dataFile.customView5, dataFile.commonViewSchema))
		customView6 = dataviewHelper.createDataview(project, userLogin1.person, getSanitizedViewObject(dataFile.customView6, dataFile.commonViewSchema))
		customView7 = dataviewHelper.createDataview(project, userLogin1.person, getSanitizedViewObject(dataFile.customView7, dataFile.commonViewSchema))
		customView8 = dataviewHelper.createDataview(project, userLogin1.person, getSanitizedViewObject(dataFile.customView8, dataFile.commonViewSchema))
		customView9 = dataviewHelper.createDataview(project, userLogin1.person, getSanitizedViewObject(dataFile.customView9, dataFile.commonViewSchema))
		customView10 = dataviewHelper.createDataview(project, userLogin1.person, getSanitizedViewObject(dataFile.customView10, dataFile.commonViewSchema))
		customView11 = dataviewHelper.createDataview(project, userLogin1.person, getSanitizedViewObject(dataFile.customView11, dataFile.commonViewSchema))
		tagToBeEdited = tagHelper.createTag(project, dataFile.tagToBeEdited)
		tagToBeDeleted = tagHelper.createTag(project, dataFile.tagToBeDeleted)
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
			Dataview.findWhere([name: dataFile.customView1.name, project: project])
			Dataview.findWhere([name: dataFile.customView2.name, project: project])
			Dataview.findWhere([name: dataFile.customView3.name, project: project])
			Dataview.findWhere([name: dataFile.customView4.name, project: project])
			Dataview.findWhere([name: dataFile.customView5.name, project: project])
			Dataview.findWhere([name: dataFile.customView6.name, project: project])
			Dataview.findWhere([name: dataFile.customView7.name, project: project])
			Dataview.findWhere([name: dataFile.customView8.name, project: project])
			Dataview.findWhere([name: dataFile.customView9.name, project: project])
			Dataview.findWhere([name: dataFile.customView10.name, project: project])
			Dataview.findWhere([name: dataFile.customView11.name, project: project])
			Tag.findWhere([name: dataFile.tagToBeEdited.name, project: project])
			Tag.findWhere([name: dataFile.tagToBeDeleted.name, project: project])
	}
}