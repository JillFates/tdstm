/**
 * ProjectTestHelper is a helper class that can be used by the test cases to fetch, create and do other
 * helpful data preparation necessary to be used by the integration tests. The intent of these helper classes
 * is to do the heavy lifting for the ITs so that they an focus on the good stuff.
 *
 * These helpers should not rely on any pre-existing data and will generate anything that is necessary. At least
 * that's the idea...
 */

import com.tdsops.common.grails.ApplicationContextHolder
import org.apache.commons.lang.RandomStringUtils as RSU

class ProjectTestHelper {

	static def projectService
	static def partyRelationshipService
	static final Long projectId = 2445

	// Constructor - Used to initialize the class
	ProjectTestHelper() {
		projectService = ApplicationContextHolder.getService('projectService')
		partyRelationshipService = ApplicationContextHolder.getService('partyRelationshipService')
		assert (projectService instanceof ProjectService)
	}

	/**
	 * Used to get the project to test with
	 * @return a Person that has Administration privileges
	 */
	Project getProject() {
		Project project = Project.get(projectId)
		assert project
		return project
	}

	/**
	 * Used to get the first Move Event of a project
	 * @param project - the project to test with
	 * @return the move event
	 */
	MoveEvent getFirstMoveEvent(Project project) {
		List moveEvent = MoveEvent.findAllByProject(project, [max:1]) // Grab any one of the events
		assert moveEvent
		return moveEvent[0]
	}

	/**
	 * Used to create a project
	 */
	Project createProject(PartyGroup company=null) {

		Project project = new Project()
		String name = RSU.randomAlphabetic(10)
		project.with {
			client = createCompany('Owner')
			projectCode = name.toUpperCase()
			name = name
			description = 'Test project created by the ProjectTestHelper'
			startDate = new Date()
			completionDate = startDate + 30
			//client = createClient()
			workflowCode = 'STD_PROCESS'
			//timezone = TimeZone.getTimeZone("GMT")
		}

		project.save(failOnError:true)

		// Save the company that owns the project
		if (!company) {
			company = createCompany('Owner')
		}
		project.owner = company
		project.save(failOnError:true)

		return project
	}

	/**
	 * Used to create a company
	 * @param prefix - a prefix string that will prefix the company name if provided
	 * @return a freshly minted company
	 */
	PartyGroup createCompany(String prefix) {
		PartyType pt = PartyType.get('COMPANY')
		PartyGroup company = new PartyGroup()
		company.with {
			//type = pt
			name = (prefix ? "$prefix " : '') + RSU.randomAlphabetic(10)
		}

		company.save(failOnError:true)
		return company
	}

	/**
	 * Used to create a company as a client and assign them as a client of a company
	 * @param prefix - a prefix string that will prefix the company name if provided
	 * @return a freshly minted company
	 */
	PartyGroup createClient(PartyGroup company) {
		PartyType pt = PartyType.get('COMPANY')

		PartyGroup client = createCompany('Client')
		partyRelationshipService.assignClientToCompany(client, company)
	}

}
