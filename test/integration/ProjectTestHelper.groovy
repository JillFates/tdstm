import com.tdsops.common.grails.ApplicationContextHolder
import net.transitionmanager.service.PartyRelationshipService
import net.transitionmanager.service.ProjectService
import org.apache.commons.lang.RandomStringUtils as RSU

/**
 * Fetches, creates and does other helpful data preparation in the integration tests, doing the heavy lifting
 * for the ITs so that they an focus on the good stuff.
 *
 * Should not rely on any pre-existing data and will generate anything that is necessary. At least that's the idea...
 */
class ProjectTestHelper {

	static ProjectService projectService
	static PartyRelationshipService partyRelationshipService
	static final long projectId = 2445

	// Initialize
	ProjectTestHelper() {
		projectService = ApplicationContextHolder.getService('projectService', ProjectService)
		partyRelationshipService = ApplicationContextHolder.getService('partyRelationshipService', PartyRelationshipService)
	}

	/**
	 * Get the project to test with
	 * @return a project with the default id (2445)
	 */
	Project getProject() {
		Project project = Project.get(projectId)
		assert project
		return project
	}

	/**
	 * Get the first MoveEvent of a Project.
	 * @param project  the project
	 * @return the move event
	 */
	MoveEvent getFirstMoveEvent(Project project) {
		MoveEvent moveEvent = MoveEvent.findByProject(project) // Grab any one of the events
		assert moveEvent
		return moveEvent
	}

	/**
	 * Create a project
	 */
	Project createProject(PartyGroup company = null) {
		new Project(client: createCompany('Owner'),
		            projectCode: RSU.randomAlphabetic(10),
		            description: 'Test project created by the ProjectTestHelper',
		            startDate: new Date(),
		            completionDate: startDate + 30,
		            workflowCode: 'STD_PROCESS',
		            timezone: 'GMT',
		            owner: company ?: createCompany('Owner')).save(failOnError: true)
	}

	/**
	 * Create a company.
	 *
	 * @param prefix  the prefix of the company name if provided
	 * @return the company
	 */
	PartyGroup createCompany(String prefix) {
		new PartyGroup(type: PartyType.get('COMPANY'),
		               name: (prefix ? prefix + ' ' : '') + RSU.randomAlphabetic(10)).save(failOnError: true)
	}

	/**
	 * Create a company as a client and assign them as a client of the specified company.
	 * @param company  the client's owning company
	 * @return the client
	 */
	PartyGroup createClient(PartyGroup company) {
		PartyType pt = PartyType.get('COMPANY')

		PartyGroup client = createCompany('Client')
		partyRelationshipService.assignClientToCompany(client, company)
	}
}
