import com.tdssrc.grails.JsonUtil
import grails.test.spock.IntegrationSpec
import net.transitionmanager.domain.Dataview
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.service.DataviewService
import net.transitionmanager.service.DomainUpdateException
import net.transitionmanager.service.SecurityService
import org.apache.commons.lang3.RandomStringUtils
import org.codehaus.groovy.grails.web.json.JSONObject
import spock.lang.Ignore

class DataviewServiceIntegrationSpec extends IntegrationSpec {

	DataviewService dataviewService
	test.helper.ProjectTestHelper projectTestHelper = new test.helper.ProjectTestHelper()
	test.helper.PersonTestHelper personHelper = new test.helper.PersonTestHelper()

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
					hasPermission: { return true },
					getUserCurrentProject: { return project },
					loadCurrentPerson: { return person },
					isLoggedIn: { return true },
					getCurrentPersonId: { return person.id }] as SecurityService

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

	private JSONObject createDataview(String name) {
		Map<String, ?> dataviewMap = [
		        'name': name == null ? RandomStringUtils.randomAlphabetic(10) : name,
				'schema': ['key': 'value'],
				'isShared': false,
				'isSystem': false
		]

		String dataviewJson = JsonUtil.convertMapToJsonString(dataviewMap)
		return JsonUtil.parseJson(dataviewJson)
	}
}
