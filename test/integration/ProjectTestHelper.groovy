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

		if (!company) {
			company = createCompany('Owner')
		}

		Project project = new Project()
		project.with {
			client = createClient(company)
			projectCode = RSU.randomAlphabetic(10)
			name = 'Project ' + projectCode
			description = 'Test project created by the ProjectTestHelper'
			startDate = new Date()
			completionDate = startDate + 30
			workflowCode = 'STD_PROCESS'
			timezone = Timezone.findByCode('GMT')
		}
		project.save(failOnError:true)

		// Assigning the owner to a project is done through the PartyRelationship so the project must be saved first
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
			partyType = pt
			name = (prefix ? "$prefix " : '') + RSU.randomAlphabetic(10)
		}

		company.save(failOnError:true)
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
		return client
	}

}
