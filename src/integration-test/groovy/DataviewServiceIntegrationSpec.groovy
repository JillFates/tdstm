import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.JsonUtil
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.command.DataviewUserParamsCommand
import net.transitionmanager.imports.Dataview
import net.transitionmanager.person.Person
import net.transitionmanager.project.Project
import net.transitionmanager.service.DataviewService
import net.transitionmanager.exception.DomainUpdateException
import net.transitionmanager.service.SecurityService
import org.apache.commons.lang3.RandomStringUtils
import org.grails.web.json.JSONObject
import spock.lang.Ignore
import spock.lang.Specification

@Integration
@Rollback
class DataviewServiceIntegrationSpec extends Specification{

	DataviewService               dataviewService
	test.helper.ProjectTestHelper projectTestHelper = new test.helper.ProjectTestHelper()
	test.helper.PersonTestHelper  personHelper      = new test.helper.PersonTestHelper()

	void '1. test create dataview without project throws exception'() {
		setup:
			Person person = personHelper.createPerson()
			dataviewService.securityService = [hasPermission: { return true }] as SecurityService
			JSONObject dataviewJson = createDataview(null)
		when: 'creating a dataview with an invalid project'
			dataviewService.create(person, null, dataviewJson)
		then:
			DomainUpdateException e = thrown()
			e.message ==~ /.*Property project of class net.transitionmanager.domain.Dataview cannot be null.*/
	}

	void '2. test create dataview with duplicate name within same project throws exception'() {
		setup:
			Project project = projectTestHelper.createProject()
			Person person = personHelper.createPerson()
			dataviewService.securityService = [hasPermission: { return true }] as SecurityService
			JSONObject dataviewJson = createDataview(null)
			dataviewService.create(person, project, dataviewJson)
		when: 'creating a second dataview with same name and project'
			dataviewService.create(person, project, dataviewJson)
		then: 'throws domain update exception'
			DomainUpdateException e = thrown()
			e.message ==~ /.*Property name of class net.transitionmanager.domain.Dataview with value \[.+\] must be unique.*/
	}

	@Ignore
	// This test is ignored since the Dataview update does not update the name
	void '3. test update dataview with duplicate name within same project throws exception'() {
		setup:
			Project project = projectTestHelper.createProject()
			Person person = personHelper.createPerson()
			dataviewService.securityService = [
				hasPermission        : { return true },
				getUserCurrentProject: { return project },
				loadCurrentPerson    : { return person },
				isLoggedIn           : { return true },
				getCurrentPersonId   : { return person.id }] as SecurityService

			JSONObject dataviewJson1 = createDataview(null)
			JSONObject dataviewJson2 = createDataview(null)
			Dataview dataview1 = dataviewService.create(person, project, dataviewJson1)
			Dataview dataview2 = dataviewService.create(person, project, dataviewJson2)
		when: 'creating a updating dataview with name of an existing one and project'
			dataviewService.update(person, project, dataview2.id, dataviewJson1)
		then: 'throws domain update exception'
			DomainUpdateException e = thrown()
			e.message ==~ /.*Property name of class net.transitionmanager.domain.Dataview with value \[.+\] must be unique.*/
	}

	void '4. Test that unique name validation with duplicate name within same project returns false'() {
		setup:
			Project project = projectTestHelper.createProject()
			Person person = personHelper.createPerson()
			dataviewService.securityService = [hasPermission: { return true }] as SecurityService
			JSONObject dataviewJson = createDataview('my dataview name')
			dataviewService.create(person, project, dataviewJson)
		when: 'a dataview with same name and project is tested'
			boolean result = dataviewService.validateUniqueName('my dataview name', null, project)
		then: 'the validation returns false'
			result == false
	}

	void '5. test unique name validation with duplicate name for a different project returns true'() {
		setup:
			Project project = projectTestHelper.createProject()
			Project anotherProject = projectTestHelper.createProject()
			Person person = personHelper.createPerson()
			dataviewService.securityService = [hasPermission: { return true }] as SecurityService
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
			Project project = projectTestHelper.createProject()
			Person person = personHelper.createPerson()

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
}
