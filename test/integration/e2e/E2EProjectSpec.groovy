package e2e

import com.tds.asset.Application
import com.tds.asset.AssetComment
import com.tdssrc.grails.JsonUtil
import net.transitionmanager.domain.DataScript
import net.transitionmanager.domain.Dataview
import net.transitionmanager.domain.License
import net.transitionmanager.domain.LicensedClient
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.PartyGroup
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import net.transitionmanager.domain.Recipe
import net.transitionmanager.domain.Setting
import net.transitionmanager.domain.Tag
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.service.LicenseAdminService
import net.transitionmanager.service.LicenseManagerService
import net.transitionmanager.service.PartyRelationshipService
import net.transitionmanager.service.ProjectService
import org.codehaus.groovy.grails.web.json.JSONObject
import spock.lang.Specification
import test.helper.ApplicationTestHelper
import test.helper.AssetCommentTestHelper
import test.helper.DataScriptTestHelper
import test.helper.DataviewTestHelper
import test.helper.MoveBundleTestHelper
import test.helper.MoveEventTestHelper
import test.helper.ProjectTestHelper
import test.helper.PersonTestHelper
import test.helper.ProviderTestHelper
import test.helper.RecipeTestHelper
import test.helper.SettingTestHelper
import test.helper.TagTestHelper
import org.apache.commons.lang.RandomStringUtils

class E2EProjectSpec extends Specification {
	// Set transactional false to persist at database when spec finishes
	static transactional = false

	// IOC
	ProjectService projectService
	PartyRelationshipService partyRelationshipService
	LicenseAdminService licenseAdminService
	LicenseManagerService licenseManagerService
	LicensedClient licensedClient
	private static List<String> browsersInParallel = ["chrome", "firefox"]
	private ProjectTestHelper projectHelper = new ProjectTestHelper()
	private PersonTestHelper personHelper = new PersonTestHelper()
	private MoveEventTestHelper eventHelper = new MoveEventTestHelper()
	private MoveBundleTestHelper bundleHelper = new MoveBundleTestHelper()
	private ProviderTestHelper providerHelper = new ProviderTestHelper()
	private DataScriptTestHelper etlScriptHelper = new DataScriptTestHelper()
	private DataviewTestHelper dataviewHelper = new DataviewTestHelper()
	private TagTestHelper tagHelper = new TagTestHelper()
	private ApplicationTestHelper appHelper = new ApplicationTestHelper()
	private RecipeTestHelper recipeHelper = new RecipeTestHelper()
	private AssetCommentTestHelper taskHelper = new AssetCommentTestHelper()
	private SettingTestHelper settingHelper = new SettingTestHelper()
	private Project project
	private List<Project> projectsToBeDeleted = []
	private UserLogin userLogin1
	private List<Person> staffsFiltering = []
	private List<UserLogin> usersFiltering = []
	private MoveEvent buildoutEvent
	private MoveEvent m1PhysicalEvent
	private MoveBundle buildoutBundle
	private Provider projectProvider
	private List<Provider> providersToBeDeleted1 = []
	private List<Provider> providersToBeDeleted2 = []
	private List<Provider> providersToBeEdited = []
	private List<DataScript> etlToBeTransformedWithPastedData = []
	private List<DataScript> etlToBeEdited = []
	private List<DataScript> etlToBeSearched = []
	private static numberOfViews = 11
	private List<Tag> tagToBeEdited = []
	private List<Tag> tagToBeDeleted = []
	private List<Tag> tagsForAssets = []
	private static numberOfAllAssetsApplications = 50
	private List<Application> applicationsToBeEdited = []
	private List<Application> applicationsToBeDeleted = []
	private List<Recipe> recipesToBeEdited = []
	private List<Recipe> recipesToBeDeleted = []
	private List<Recipe> recipesTaskGeneration = []
	private List<Recipe> recipesHistory1 = []
	private List<Recipe> recipesHistory2 = []
	private List<PartyGroup> companiesToBeEdited = []
	private List<PartyGroup> companiesToBeDeleted = []
	private List<AssetComment> tasksToBeEdited = []
	private List<AssetComment> tasksToBeDeleted = []
	private List<JSONObject> customFieldToBeDeleted1 = []
	private List<JSONObject> customFieldToBeDeleted2 = []
	JSONObject originalData
	JSONObject dataFile
	private Date now = new Date()

	void setup(){
		originalData = getJsonObjectFromFile()
		project = projectHelper.createProject(originalData.e2eProjectData)
		licenseProject(project)
		userLogin1 = personHelper.createPersonWithLoginAndRoles(originalData.userData1, project)
		personHelper.createPersonWithLoginAndRoles(originalData.userData2, project)
		personHelper.createPersonWithLoginAndRoles(originalData.userData3, project)
		personHelper.createPersonWithLoginAndRoles(originalData.userData4, project)
		buildoutEvent = eventHelper.createMoveEvent(project, originalData.eventName1)
		m1PhysicalEvent = eventHelper.createMoveEvent(project, originalData.eventName2)
		buildoutBundle = bundleHelper.createBundle(originalData.bundleName1, project, buildoutEvent)
		bundleHelper.createBundle(originalData.bundleName2, project, m1PhysicalEvent)
		projectProvider = providerHelper.createProvider(project, originalData.projectProvider)
		recipeHelper.createRecipe(originalData.recipePrincipal, project, userLogin1.person)
		taskHelper.createTask(originalData.taskPrincipal, project, userLogin1.person, buildoutEvent)

		dataFile = getJsonObjectFromFile()
		browsersInParallel.each { browser ->
			projectsToBeDeleted.add(projectHelper.createProject(sanitizeProject(dataFile.projectToBeDeleted)))
			// create company and associate to current project
			PartyGroup company = projectHelper.createCompany("", "${formatToRandomValue(dataFile.companyToBeEdited.name, browser, false)}")
			companiesToBeDeleted.add(company)
			partyRelationshipService.assignClientToCompany(company, project.owner)
			// create company and associate to current project
			company = projectHelper.createCompany("", "${formatToRandomValue(dataFile.companyToBeDeleted.name, browser, false)}")
			companiesToBeEdited.add(company)
			partyRelationshipService.assignClientToCompany(company, project.owner)

			JSONObject sanitizedStaff = sanitizeStaffUserData(browser, dataFile.staffFiltering)
			staffsFiltering.add(personHelper.createStaff(project.owner, sanitizedStaff.firstName, sanitizedStaff.middleName, sanitizedStaff.lastName, sanitizedStaff.email, sanitizedStaff.userName))
			usersFiltering.add(personHelper.createPersonWithLoginAndRoles(sanitizeStaffUserData(browser, dataFile.userFiltering), project))
			createApplications(dataFile.applications, project, buildoutBundle)
			applicationsToBeEdited.add(appHelper.createApplication(sanitizeJsonObjectName(dataFile.applicationToBeEdited, browser), project, buildoutBundle))
			applicationsToBeDeleted.add(appHelper.createApplication(sanitizeJsonObjectName(dataFile.applicationToBeDeleted, browser), project, buildoutBundle))
			createCustomViews(project, userLogin1.person, getSanitizedViewObject(dataFile.customViews, dataFile.commonViewSchema, browser))
			etlToBeTransformedWithPastedData.add(etlScriptHelper.createDataScript(project, projectProvider, userLogin1.person, "", sanitizeJsonObjectName(dataFile.etlToBeTransformedWithPastedData, browser)))
			etlToBeEdited.add(etlScriptHelper.createDataScript(project, projectProvider, userLogin1.person, "", sanitizeJsonObjectName(dataFile.etlToBeEdited, browser)))
			etlToBeSearched.add(etlScriptHelper.createDataScript(project, projectProvider, userLogin1.person, "", sanitizeJsonObjectName(dataFile.etlToBeSearched, browser)))
			tagToBeEdited.add(tagHelper.createTag(project, sanitizeJsonObjectName(dataFile.tagToBeEdited, browser)))
			tagToBeDeleted.add(tagHelper.createTag(project, sanitizeJsonObjectName(dataFile.tagToBeDeleted, browser)))
			tagsForAssets.add(tagHelper.createTag(project, sanitizeJsonObjectName(dataFile.tagsForAssets, browser)))
			providersToBeDeleted1.add(providerHelper.createProvider(project, sanitizeJsonObjectName(dataFile.providerToBeDeleted1, browser)))
			providersToBeDeleted2.add(providerHelper.createProvider(project, sanitizeJsonObjectName(dataFile.providerToBeDeleted2, browser)))
			providersToBeEdited.add(providerHelper.createProvider(project, sanitizeJsonObjectName(dataFile.providerToBeEdited, browser)))
			recipesHistory1.add(recipeHelper.createRecipe(formatToRandomValue(dataFile.recipeHistory1, browser, false), project, userLogin1.person))
			recipesHistory2.add(recipeHelper.createRecipe(formatToRandomValue(dataFile.recipeHistory2, browser, false), project, userLogin1.person))
			recipesTaskGeneration.add(recipeHelper.createRecipe(formatToRandomValue(dataFile.recipeTaskGeneration, browser, false), project, userLogin1.person))
			recipesToBeDeleted.add(recipeHelper.createRecipe(formatToRandomValue(dataFile.recipeToBeDeleted, browser, false), project, userLogin1.person))
			recipesToBeEdited.add(recipeHelper.createRecipe(formatToRandomValue(dataFile.recipeToBeEdited, browser, false), project, userLogin1.person))
			tasksToBeEdited.add(taskHelper.createTask(formatToRandomValue(dataFile.taskToBeEdited, browser, false), project, userLogin1.person, buildoutEvent))
			tasksToBeDeleted.add(taskHelper.createTask(formatToRandomValue(dataFile.taskToBeDeleted, browser, false), project, userLogin1.person, buildoutEvent))
			// create 2 application custom field setting to be deleted
			JSONObject customField = sanitizeCustomField(dataFile.customFieldToBeDeleted1, browser)
			customFieldToBeDeleted1.add(customField)
			settingHelper.createCustomApplicationFieldSetting(project, customField)
			customField = sanitizeCustomField(dataFile.customFieldToBeDeleted2, browser)
			customFieldToBeDeleted2.add(customField)
			settingHelper.createCustomApplicationFieldSetting(project, customField)
			dataFile = getJsonObjectFromFile()
		}
	}

	void "Setup E2E Project data"() {
		expect:
			Project.findWhere([projectCode: originalData.e2eProjectData.projectCode]) != null
			License.findWhere([owner: project.owner, status: License.Status.ACTIVE]) != null
			UserLogin.findWhere([username: originalData.userData1.email]) != null
			UserLogin.findWhere([username: originalData.userData2.email]) != null
			UserLogin.findWhere([username: originalData.userData3.email]) != null
			UserLogin.findWhere([username: originalData.userData4.email]) != null
			MoveEvent.findWhere([name: originalData.eventName1, project: project]) != null
			MoveEvent.findWhere([name: originalData.eventName2, project: project]) != null
			MoveBundle.findWhere([name: originalData.bundleName1, project: project]) != null
			MoveBundle.findWhere([name: originalData.bundleName2, project: project]) != null
			Provider.findWhere([name: originalData.projectProvider.name, project: project]) != null
			Recipe.findWhere([name: originalData.recipePrincipal, project: project])
			AssetComment.findWhere([comment: originalData.taskPrincipal, project: project]) != null
			projectsToBeDeleted.each { project -> Project.findWhere([projectCode: project.projectCode]) != null }
			companiesToBeDeleted.each { company -> PartyGroup.findWhere([name: company.name]) != null }
			companiesToBeEdited.each { company -> PartyGroup.findWhere([name: company.name]) != null }
			staffsFiltering.each { staff -> Person.findWhere([email: staff.email]) != null }
			usersFiltering.each { user -> UserLogin.findWhere([username: user.username]) != null }
			Application.findAllWhere([project: project]).size() >= numberOfAllAssetsApplications
			applicationsToBeEdited.each { application -> Application.findWhere([assetName: application.assetName, project: project]) != null }
			applicationsToBeDeleted.each { application -> Application.findWhere([assetName: application.assetName, project: project]) != null }
			Dataview.findAllWhere([project: project, isSystem: false]).size() >= numberOfViews
			etlToBeTransformedWithPastedData.each { script -> DataScript.findWhere([name: script.name, project: project]) != null }
			etlToBeEdited.each { script -> DataScript.findWhere([name: script.name, project: project]) != null }
			etlToBeSearched.each { script -> DataScript.findWhere([name: script.name, project: project]) != null }
			tagToBeEdited.each { tag -> Tag.findWhere([name: tag.name, project: project]) != null }
			tagToBeDeleted.each { tag -> Tag.findWhere([name: tag.name, project: project]) != null }
			tagsForAssets.each { tag -> Tag.findWhere([name: tag.name, project: project]) != null }
			providersToBeDeleted1.each { provider -> Provider.findWhere([name: provider.name, project: project]) != null }
			providersToBeDeleted2.each { provider -> Provider.findWhere([name: provider.name, project: project]) != null }
			providersToBeEdited.each { provider -> Provider.findWhere([name: provider.name, project: project]) != null }
			recipesToBeEdited.each { recipe -> Recipe.findWhere([name: recipe.name, project: project]) != null }
			recipesToBeDeleted.each { recipe -> Recipe.findWhere([name: recipe.name, project: project]) != null }
			recipesTaskGeneration.each { recipe -> Recipe.findWhere([name: recipe.name, project: project]) != null }
			recipesHistory1.each { recipe -> Recipe.findWhere([name: recipe.name, project: project]) != null }
			recipesHistory2.each { recipe -> Recipe.findWhere([name: recipe.name, project: project]) != null }
			tasksToBeDeleted.each { task -> AssetComment.findWhere([comment: task.comment, project: project]) != null }
			tasksToBeEdited.each { task -> AssetComment.findWhere([comment: task.comment, project: project]) != null }
			Map jsonMap
			customFieldToBeDeleted1.each { customField ->
				jsonMap = JsonUtil.convertJsonToMap(Setting.findWhere([project: project, key: "APPLICATION"]).json)
				assert jsonMap.fields.find { it.label == customField.label } != null
			}
			customFieldToBeDeleted2.each { customField ->
				jsonMap = JsonUtil.convertJsonToMap(Setting.findWhere([project: project, key: "APPLICATION"]).json)
				assert jsonMap.fields.find { it.label == customField.label } != null
			}
	}

	private void licenseProject(Project project) {
		def currentLicense = License.findWhere([owner: project.owner])
		if (!currentLicense){
			String testEmail = 'sample@sampleEmail.com'
			String testRequestNote = 'Test request note'
			License licenseRequest = licenseAdminService.generateRequest(null, project.owner, testEmail, License.Environment.ENGINEERING.toString(), project.id, testRequestNote)
			String encodedMessage  = licenseRequest.toEncodedMessage()
			licensedClient = licenseManagerService.loadRequest(encodedMessage)
			licensedClient.type = License.Type.MULTI_PROJECT
			licensedClient.max  = 100
			licensedClient.activationDate = now
			licensedClient.expirationDate = now + 30
			licensedClient.save(flush: true)
			licenseManagerService.activate(licensedClient.id)
			String licenseKeyPending = licenseManagerService.getLicenseKey(licensedClient.id)
			License licDomain = License.get(licensedClient.id)
			licDomain.hash = licenseKeyPending
			licenseAdminService.load(licDomain)
		} else {
			currentLicense.expirationDate = now + 30
			currentLicense.save(flush: true)
		}
	}

	private JSONObject getJsonObjectFromFile(){
		String jsonText = this.getClass().getResource("E2EProjectData.json").text
		return new JSONObject(jsonText)
	}

	private JSONObject getSanitizedViewObject(JSONObject dataViewJson, JSONObject customViewSchemaJson, String browser){
		dataViewJson.name = formatToRandomValue(browser, dataViewJson.name)
		dataViewJson.schema = customViewSchemaJson
		return dataViewJson
	}

	private String getRandomString(){
		return RandomStringUtils.randomAlphabetic(5)
	}

	private String formatToRandomValue(String value, String browser = "", boolean randomRequired = true, boolean isEmail = false){
		if (isEmail) return "QAE2E${browser + getRandomString() + value}"
		else if (!randomRequired) "QAE2E ${browser} ${value}"
		else return "QAE2E ${browser} ${getRandomString()} ${value}"
	}

	private JSONObject sanitizeStaffUserData(String browser, JSONObject personInfo){
		personInfo.firstName = browser + " " + personInfo.firstName
		personInfo.middleName = browser + " " + personInfo.middleName
		personInfo.lastName = browser + " " + personInfo.lastName
		personInfo.email = browser + personInfo.email
		personInfo.userName = browser + personInfo.userName
		return personInfo
	}

	private void createCustomViews(Project project, Person person, JSONObject viewData, int toBeCreated = numberOfViews){
		int existingViews = Dataview.findAllWhere([project: project, isSystem: false]).size()
		if (existingViews < toBeCreated){
			int views = toBeCreated - existingViews
			String originalName = viewData.name
			for(int i = 0; i < views; i++){
				viewData.name = formatToRandomValue(viewData.name)
				dataviewHelper.createDataview(project, person, viewData)
				viewData.name = originalName
			}
		}
	}

	private void createApplications(JSONObject appData, Project project, MoveBundle bundle, int toBeCreated = numberOfAllAssetsApplications){
		int existingApps = Application.findAllWhere([project: project]).size()
		if (existingApps < toBeCreated){
			int apps = toBeCreated - existingApps
			String originalName = appData.name
			for(int i = 0; i < apps; i++){
				appData.name = formatToRandomValue(appData.name)
				appHelper.createApplication(appData, project, bundle)
				appData.name = originalName
			}
		}
	}

	private JSONObject sanitizeJsonObjectName(JSONObject data, String browser = "", boolean randomRequired = false){
		data.name = formatToRandomValue(data.name, browser, randomRequired)
		return data
	}

	private JSONObject sanitizeProject(JSONObject data){
		data.projectName = formatToRandomValue(data.projectName)
		data.projectCode = getRandomString() + data.projectCode
		return data
	}

	private JSONObject sanitizeCustomField(JSONObject data, String browser){
		data.label = formatToRandomValue(data.label, browser)
		data.default = data.label
		data.tip = "Tooltip for ${data.label}"
		return data
	}
}