import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.SecurityRole
import com.tdsops.tm.enums.domain.ViewSaveAsOptionEnum
import com.tdssrc.grails.JsonUtil
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.command.dataview.DataviewUserParamsCommand
import net.transitionmanager.exception.DomainUpdateException
import net.transitionmanager.imports.Dataview
import net.transitionmanager.imports.DataviewService
import net.transitionmanager.person.Person
import net.transitionmanager.person.PersonService
import net.transitionmanager.project.Project
import net.transitionmanager.project.ProjectService
import net.transitionmanager.security.SecurityService
import net.transitionmanager.security.UserLogin
import org.apache.commons.lang3.RandomStringUtils
import org.grails.web.json.JSONObject
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Ignore


import java.security.InvalidParameterException

@Integration
@Rollback
@Ignore		// TODO : JPM 2/20202 : Remove the @Ignore TM-17042
class DataviewServiceIntegrationSpec extends Specification {

	@Shared
	SecurityService securityService
	@Shared
	ProjectService projectService
	@Shared
	PersonService personService
	@Shared
	DataviewService dataviewService
	test.helper.ProjectTestHelper projectTestHelper
	test.helper.PersonTestHelper  personHelper
	static Person person
	static Project project
	static UserLogin user

	void setup() {
		projectTestHelper = new test.helper.ProjectTestHelper()
		personHelper      = new test.helper.PersonTestHelper()
		project = projectTestHelper.createProject()

		person = personHelper.createStaff(projectService.getOwner(project))
		assert person

		user = personHelper.createUserLoginWithRoles(person, ["${SecurityRole.ROLE_ADMIN}"])
		assert user
		assert user.username

		// logs the admin user into the system
		securityService.assumeUserIdentity(user.username, false)

		assert securityService.isLoggedIn()

		personService.addToProjectSecured(project, person)
	}

	void '1. test create dataview without project throws exception'() {
		setup:
			dataviewService.securityService = [hasPermission: { return true },
											   loadCurrentPerson: {return person}] as SecurityService
			JSONObject dataviewJson = createDataview(null)
			dataviewJson.isSystem = false
			dataviewJson.saveAsOption = 'MY_VIEW'
		when: 'creating a dataview with an invalid project'
			dataviewService.create(person, null, dataviewJson)
		then:
			DomainUpdateException e = thrown()
			e.message ==~ /.*Property project of class net.transitionmanager.imports.Dataview cannot be null.*/
	}

	void '2. test create dataview with duplicate name within same project throws exception'() {
		setup:
			dataviewService.securityService = [hasPermission: { return true },
											   loadCurrentPerson: {return person}] as SecurityService
			JSONObject dataviewJson = createDataview(null)
			dataviewService.create(person, project, dataviewJson)
		when: 'creating a second dataview with same name and project'
			dataviewService.create(project, person, dataviewJson)
		then: 'throws domain update exception'
			DomainUpdateException e = thrown()
			e.message ==~ /.*Property name with value \[.+\] must be unique.*/
	}

	void '4. Test that unique name validation with duplicate name within same project returns false'() {
		setup:
			dataviewService.securityService = [hasPermission: { return true },
											   loadCurrentPerson: {return person}] as SecurityService
			JSONObject dataviewJson = createDataview('my dataview name')
			dataviewService.create(person, project, dataviewJson)
		when: 'a dataview with same name and project is tested'
			boolean result = dataviewService.validateUniqueName('my dataview name', null, project)
		then: 'the validation returns false'
			result == false
	}

	void '5. test unique name validation with duplicate name for a different project returns true'() {
		setup:
			Project anotherProject = projectTestHelper.createProject()
			dataviewService.securityService = [hasPermission: { return true },
											   loadCurrentPerson: {return person}] as SecurityService
			JSONObject dataviewJson = createDataview('my dataview name')
			dataviewService.create(person, project, dataviewJson)
		when: 'validate if a dataview with same name for a different project can be created'
			boolean result = dataviewService.validateUniqueName('my dataview name', null, anotherProject)
		then: 'the validation returns true'
			result == true
	}

	private JSONObject createDataview(String name) {
		Map<String, ?> dataviewMap = [
			'name'    : name == null ? RandomStringUtils.randomAlphabetic(10) : name,
			'schema'  : ['key': 'value'],
			'isShared': false,
			'isSystem': false
		]

		String dataviewJson = JsonUtil.convertMapToJsonString(dataviewMap)
		return JsonUtil.parseJson(dataviewJson)
	}

	void '6. test getAssetIdsHql'() {
		setup: 'given a project, a dataview, and a DataviewUserParamsCommand'
			dataviewService.securityService = [
				hasPermission        : { return true },
				getUserCurrentProject: { return project },
				loadCurrentPerson    : { return person },
				isLoggedIn           : { return true },
				getCurrentPersonId   : { return person.id }] as SecurityService

			JSONObject dataviewJson1 = createDataview(null)
			Dataview dataview = dataviewService.create(person, project, dataviewJson1)
			dataview.reportSchema = '{"key":"value"}'

			DataviewUserParamsCommand dataviewUserParamsCommand = [
				sortDomain  : 'common',
				sortProperty: 'id',
				filters     : ['domains': ['device']]
			] as DataviewUserParamsCommand

		when: 'calling getAssetIdHQL'
			Map hql = dataviewService.getAssetIdsHql(project, dataview.id, dataviewUserParamsCommand)

		then: 'a map is returned, with the hql query, and its parameters.'
			hql.query.stripIndent() == '''
				SELECT AE.id
				FROM AssetEntity AE

				WHERE AE.project = :project AND AE.assetClass in (:assetClasses)
				group by AE.id
			'''.stripIndent()

			hql.params == [project: project, assetClasses: [AssetClass.DEVICE]]
	}

	void '7. test create dataview to throw saveMyViewInDefaultProject exception'() {
		setup:
			dataviewService.securityService = [hasPermission: { return true }] as SecurityService
			JSONObject dataviewJson = createDataview(null)
			dataviewJson.isSystem = true
			dataviewJson.saveAsOption = ViewSaveAsOptionEnum.MY_VIEW.name()
		when: 'creating a dataview for system view as my view'
			dataviewService.create(person, project , dataviewJson)
		then:
			InvalidParameterException e = thrown()
			e.message ==~ /.*My Views are not allowed to be saved into the Default project.*/
	}

	void '8. test create dataview to throw overrideForSelfInDefaultProject exception'() {
		setup:
			dataviewService.securityService = [hasPermission: { return true }] as SecurityService
			JSONObject dataviewJson = createDataview(null)
			dataviewJson.isSystem = true
			dataviewJson.saveAsOption = ViewSaveAsOptionEnum.OVERRIDE_FOR_ME.name()
		when: 'creating a dataview for system view as override for me'
			dataviewService.create(person, project , dataviewJson)
		then:
			InvalidParameterException e = thrown()
			e.message ==~ /.*Overriding system views for self is not allowed in the Default project.*/
	}

	void '9. test create dataview to throw overrideGlobalPermission exception'() {
		setup:
			dataviewService.securityService = [hasPermission: { return false }] as SecurityService
			JSONObject dataviewJson = createDataview(null)
			dataviewJson.isSystem = true
			dataviewJson.saveAsOption = ViewSaveAsOptionEnum.OVERRIDE_FOR_ALL.name()
		when: 'creating a dataview for system view as override for all'
			dataviewService.create(person, project , dataviewJson)
		then:
			InvalidParameterException e = thrown()
			e.message ==~ /.*You do not have the necessary permission to save override views for the Default project.*/
	}

	void '10. test create dataview to throw createPermission exception'() {
		setup:
			dataviewService.securityService = [hasPermission: { return false }] as SecurityService
			JSONObject dataviewJson = createDataview(null)
			dataviewJson.isSystem = false
			dataviewJson.saveAsOption = ViewSaveAsOptionEnum.OVERRIDE_FOR_ALL.name()
		when: 'creating a dataview for non-system view as my view'
			dataviewService.create(person, project , dataviewJson)
		then:
			InvalidParameterException e = thrown()
			e.message ==~ /.*You do not have the necessary permission to save override views for all users.*/
	}

	void '11. test create dataview to throw createOverridePermission exception'() {
		setup:
			dataviewService.securityService = [hasPermission: { return false }] as SecurityService
			JSONObject dataviewJson = createDataview(null)
			dataviewJson.isSystem = false
			dataviewJson.saveAsOption = ViewSaveAsOptionEnum.OVERRIDE_FOR_ALL.name()
		when: 'creating a dataview for non-system view as override for me'
			dataviewService.create(person, project , dataviewJson)
		then:
			InvalidParameterException e = thrown()
			e.message ==~ /.*You do not have the necessary permission to save override views.*/
	}

	void '12. test create dataview to throw overrideAllUsers exception'() {
		setup:
			dataviewService.securityService = [hasPermission: { return false }] as SecurityService
			JSONObject dataviewJson = createDataview(null)
			dataviewJson.isSystem = false
			dataviewJson.saveAsOption = ViewSaveAsOptionEnum.OVERRIDE_FOR_ALL.name()
		when: 'creating a dataview for non-system view as override for all'
			dataviewService.create(person, project , dataviewJson)
		then:
			InvalidParameterException e = thrown()
			e.message ==~ /.*You do not have the necessary permission to save override views for all users.*/
	}

	void '13. test creating override dataview'() {
		setup:
			dataviewService.securityService = [
					hasPermission: { return true },
					getUserCurrentProject: { return project },
					loadCurrentPerson    : { return person },
					isLoggedIn           : { return true },
					getCurrentPersonId   : { return person.id }
			] as SecurityService

			JSONObject dataviewJson = createDataview(null)
			dataviewJson.isSystem = false
			dataviewJson.saveAsOption = ViewSaveAsOptionEnum.OVERRIDE_FOR_ME.name()
			dataviewJson.overridesView = 1
		when: 'creating a valid override dataview'
			dataviewService.create(person, project, dataviewJson)
		then:
			Dataview.findAll()
	}

	def '14. No Overrides of System View'() {
		setup:
			String allAssetViewName = 'All Assets'
		when: 'service is called to get a DataView'
			Dataview dv = dataviewService.fetch(1)
		then: 'Since there is no overriden view we get the default original View'
			dv.name == allAssetViewName
	}

	def '15. System wide override of System View'() {
		setup:
			String overridenSystemWideViewName = 'Overriden All Assets System-Wide Scope'
			Project defaultProject = Project.defaultProject
			dataviewService.securityService = [
					hasPermission        : { return true },
					getUserCurrentProject: { return project },
					loadCurrentPerson    : { return person },
					isLoggedIn           : { return true },
					getCurrentPersonId   : { return person.id }
			] as SecurityService
			Dataview originalSystemView = dataviewService.fetch(1)
			Dataview.saveAll(
					new Dataview(
							name: overridenSystemWideViewName,
							overridesView: originalSystemView,
							project: defaultProject,
							isSystem: true,
							isShared: true,
							reportSchema: '{/*SOME VIEW OBJECT DEF*/}'
					)
			)
		when: 'service is called to get a DataView'
			Dataview dv = dataviewService.fetch(originalSystemView.id)
		then: 'since there is a Wide system view we retrieve that one'
			dv.name == overridenSystemWideViewName
	}

	def '16. Project wide override of System View'() {
		setup:
			String overridenSystemWideViewName  = 'Overriden All Assets System-Wide Scope'
			String overridenProjectWideViewName = 'Overriden All Assets Project-Wide Scope'
			Project defaultProject = Project.defaultProject
			dataviewService.securityService = [
					hasPermission        : { return true },
					getUserCurrentProject: { return project },
					loadCurrentPerson    : { return person },
					isLoggedIn           : { return true },
					getCurrentPersonId   : { return person.id }
			] as SecurityService
			Dataview originalSystemView = dataviewService.fetch(1)
			Dataview.saveAll(
					new Dataview(
							name: overridenSystemWideViewName,
							overridesView: originalSystemView,
							project: defaultProject,
							isSystem: true,
							isShared: true,
							reportSchema: '{/*SOME VIEW OBJECT DEF*/}'
					),
					new Dataview(
							name: overridenProjectWideViewName,
							overridesView: originalSystemView,
							project: project,
							isSystem: true,
							isShared: true,
							reportSchema: '{/*SOME VIEW OBJECT DEF*/}'
					),
			)
		when: 'service is called to get a DataView'
			Dataview dv = dataviewService.fetch(originalSystemView.id)
		then: 'since there is a Wide system view we retrieve that one'
			dv.name == overridenProjectWideViewName
	}

	def '17. Individual User override of System View'() {
		setup:
			String overridenSystemWideViewName  = 'Overriden All Assets System-Wide Scope'
			String overridenProjectWideViewName = 'Overriden All Assets Project-Wide Scope'
			String overridenIndividualViewName  = 'Overriden All Assets Individual Scope'
			Project defaultProject = Project.defaultProject
			dataviewService.securityService = [
					hasPermission        : { return true },
					getUserCurrentProject: { return project },
					loadCurrentPerson    : { return person },
					isLoggedIn           : { return true },
					getCurrentPersonId   : { return person.id }
			] as SecurityService
			Dataview originalSystemView = dataviewService.fetch(1)
			Dataview.saveAll(
					new Dataview(
							name: overridenSystemWideViewName,
							overridesView: originalSystemView,
							project: defaultProject,
							isSystem: true,
							isShared: true,
							reportSchema: '{/*SOME VIEW OBJECT DEF*/}'
					),
					new Dataview(
							name: overridenProjectWideViewName,
							overridesView: originalSystemView,
							project: project,
							isSystem: true,
							isShared: true,
							reportSchema: '{/*SOME VIEW OBJECT DEF*/}'
					),
					new Dataview(
							name: overridenIndividualViewName,
							overridesView: originalSystemView,
							person: person,
							project: project,
							isSystem: true,
							isShared: false,
							reportSchema: '{/*SOME VIEW OBJECT DEF*/}'
					),
			)
		when: 'service is called to get a DataView'
		Dataview dv = dataviewService.fetch(originalSystemView.id)
		then: 'since there is a Wide system view we retrieve that one'
		dv.name == overridenIndividualViewName
	}

	def '18. Ignore override of System View'() {
		setup:
		String overridenSystemWideViewName  = 'Overriden All Assets System-Wide Scope'
		String overridenProjectWideViewName = 'Overriden All Assets Project-Wide Scope'
		String overridenIndividualViewName  = 'Overriden All Assets Individual Scope'
		Project defaultProject = Project.defaultProject
		dataviewService.securityService = [
				hasPermission        : { return true },
				getUserCurrentProject: { return project },
				loadCurrentPerson    : { return person },
				isLoggedIn           : { return true },
				getCurrentPersonId   : { return person.id }
		] as SecurityService
		Dataview originalSystemView = dataviewService.fetch(1)
		Dataview.saveAll(
				new Dataview(
						name: overridenSystemWideViewName,
						overridesView: originalSystemView,
						project: defaultProject,
						isSystem: true,
						isShared: true,
						reportSchema: '{/*SOME VIEW OBJECT DEF*/}'
				),
				new Dataview(
						name: overridenProjectWideViewName,
						overridesView: originalSystemView,
						project: project,
						isSystem: true,
						isShared: true,
						reportSchema: '{/*SOME VIEW OBJECT DEF*/}'
				),
				new Dataview(
						name: overridenIndividualViewName,
						overridesView: originalSystemView,
						person: person,
						project: project,
						isSystem: true,
						isShared: false,
						reportSchema: '{/*SOME VIEW OBJECT DEF*/}'
				),
		)
		when: 'service is called to get a DataView'
		Dataview dv = dataviewService.fetch(originalSystemView.id, false)
		then: 'since there is a Wide system view we retrieve that one'
		dv.name == originalSystemView.name
	}
}
