import com.tdsops.tm.enums.domain.ProjectStatus
import net.transitionmanager.domain.Person
import net.transitionmanager.service.PersonService
import net.transitionmanager.service.ProjectService
import spock.lang.Specification

class ProjectServiceTests extends Specification {

	// IOC
	ProjectService projectService
	PersonService personService

	// Initialized by setup()
	private ProjectTestHelper projectHelper = new ProjectTestHelper()
	private PersonTestHelper personHelper = new PersonTestHelper()
	private Project project

	void setup() {
		project = projectHelper.getProject()
	}

	void "1. Test the getStaff "() {
		// Get a list of staff for a project
		when:
		List staff = projectService.getStaff(project)

		then:
		staff
		def numOfStaff = staff.size()
		numOfStaff > 0

		// Find a subset of the staff (assuming that there are PMs on the project)
		when:
		staff = projectService.getStaff(project, 'PROJ_MGR')

		then:
		staff
		staff.size() < numOfStaff
	}

	void "2. Test the getProjectManagers "() {
		// Get a list of PMs
		when:
		List pms = projectService.getProjectManagers(project)

		then:
		pms
		def numOfPms = pms.size()
		numOfPms > 0

		// Disable one of the PMs on the project and then refetch the list which should have one less now
		when:
		def staff = pms[0]
		staff.disable()
		assert staff.save()
		pms = projectService.getProjectManagers(project)

		then:
		(numOfPms > 1 && (pms.size() == (numOfPms - 1))) || (numOfPms == 1 && !pms)
	}

	void "3. Test getProjectsWherePersonIsStaff "() {
		// Try looking up Active projects
		when:
		Person adminPerson = personHelper.getAdminPerson()

		// Default is ProjectStatus.ACTIVE
		List projects = projectService.getProjectsWherePersonIsStaff(adminPerson)
		int activeCount = projects?.size()

		then:
		projects
		activeCount > 0

		// Try looking up Completed projects
		when:
		projects = projectService.getProjectsWherePersonIsStaff(adminPerson, ProjectStatus.COMPLETED)
		int completedCount = projects?.size()

		then:
		projects
		completedCount > 0

		// Try looking up ALL projects
		when:
		projects = projectService.getProjectsWherePersonIsStaff(adminPerson, ProjectStatus.ANY)
		int allCount = projects?.size()

		then:
		projects
		allCount > 0
		allCount == (activeCount + completedCount)

		// Check a new person that shouldn't have any projects
		when:
		Person newPerson = personHelper.createPerson(adminPerson, project.client)
		projects = projectService.getProjectsWherePersonIsStaff(newPerson, ProjectStatus.ANY)

		then:
		projects
		projects.size() == 0

		// Now assign the person to the project with a team
	}

	void "4. Test getProjectsWhereClient "() {
		// Get a list of ALL projects for the client
		when:
		def company = project.client
		assert company
		List projectList = projectService.getProjectsWhereClient(company, ProjectStatus.ANY)
		int allProjects = projectList?.size()

		then:
		projectList
		allProjects > 0
		// The project we started with should be in the list
		projectList.find { it.id == project.id }

		// The total of the ACTIVE and COMPLETED projects should equal that of ANY
		when:
		projectList = projectService.getProjectsWhereClient(company, ProjectStatus.ACTIVE)
		int activeProjects = projectList?.size()
		projectList = projectService.getProjectsWhereClient(company, ProjectStatus.COMPLETED)
		int completedProjects = projectList?.size()

		then:
		allProjects == (activeProjects + completedProjects)
	}

	void "5. Test access by users to projects "() {
		when:
		Person adminPerson = personHelper.getAdminPerson()
		Person person = personHelper.createPerson(adminPerson, project.client, project)

		then:
		person
		personService.hasAccessToProject(person, project)
		projectService.getUserProjects(false, ProjectStatus.ANY, [personId: person.id]).size() == 1
		projectService.getUserProjects(false, ProjectStatus.ACTIVE, [personId: person.id]).size() == 1
		projectService.getUserProjects(false, ProjectStatus.COMPLETED, [personId: person.id]).size() == 0
	}

	void "6. Test defaultAccountExpirationDate"() {
		when:
		Date compDate = new Date() + 45
		Project project = new Project()

		then:
		projectService.defaultAccountExpirationDate(project) > compDate

		when:
		project.completionDate = compDate

		then:
		projectService.defaultAccountExpirationDate(project) == compDate
	}
}
