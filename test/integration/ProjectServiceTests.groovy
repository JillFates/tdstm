import com.tdsops.tm.enums.domain.ProjectStatus

import grails.test.mixin.TestFor
import spock.lang.*

class ProjectServiceTests  extends Specification {
	
	def projectService
	def personService
	def projectHelper
	def personHelper
	Project project

	def setup() {
		personHelper = new PersonTestHelper(personService)
		projectHelper = new ProjectTestHelper(projectService)
		project = projectHelper.getProject()
	}

	def "1. Test the getStaff "() {
		// Get a list of staff for a project
		when:
			List staff = projectService.getStaff(project)
		then:
			staff != null
			def numOfStaff = staff.size()
			numOfStaff > 0

		// Find a subset of the staff (assuming that there are PMs on the project)
		when:
			staff = projectService.getStaff(project, 'PROJ_MGR')
		then:
			staff != null
			staff.size() < numOfStaff
	}

	def "2. Test the getProjectManagersByProject "() {
		// Get a list of PMs
		when:
			List pms = projectService.getProjectManagersByProject(project)
		then:
			pms != null
			def numOfPms = pms.size()
			numOfPms > 0

		// Disable one of the PMs on the project and then refetch the list which should have one less now
		when:
			def staff = pms[0]
			staff.disable()
			assert staff.save()
			pms = projectService.getProjectManagersByProject(project)
		then:
			( numOfPms > 1 && ( pms.size() == (numOfPms - 1)) ) || ( numOfPms == 1 && ! pms)

	}

	def "3. Test getProjectsWherePersonIsStaff "() {
		// Try looking up Active projects
		when:
			def personHelper = new PersonTestHelper(personService)
			Person adminPerson = personHelper.getAdminPerson()

			List projects = projectService.getProjectsWherePersonIsStaff(adminPerson)
			int activeCount = projects?.size()
		then: 
			projects != null
			activeCount > 0

		// Try looking up Completed projects
		when:
			projects = projectService.getProjectsWherePersonIsStaff(adminPerson, ProjectStatus.COMPLETED)
			int completedCount = projects?.size()
		then: 
			projects != null
			completedCount > 0

		// Try looking up ALL projects
		when:
			projects = projectService.getProjectsWherePersonIsStaff(adminPerson, ProjectStatus.ANY)
			int allCount = projects?.size()
		then: 
			projects != null
			allCount > 0
			allCount == (activeCount + completedCount)

		// Check a new person that shouldn't have any projects
		when: 
			Person newPerson = personHelper.createPerson(adminPerson, project.client)
			projects = projectService.getProjectsWherePersonIsStaff(newPerson, ProjectStatus.ANY)
		then:
			projects != null
			projects.size() == 0

		// Now assign the person to the project with a team

	}

	def "4. Test getProjectsWhereClient "() {
		// Get a list of ALL projects for the client
		when:
			def company = project.client
			assert company
			List projectList = projectService.getProjectsWhereClient(company, ProjectStatus.ANY)
			int allProjects = projectList?.size()
		then:
			projectList != null
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

	def "5. Test assess by users to projects "() {
		when:
			Person adminPerson = personHelper.getAdminPerson()
			Person person = personHelper.createPerson(adminPerson, project.client, project)
		then:
			person != null
			personService.hasAccessToProject(person, project)
			projectService.getUserProjects(null, false, ProjectStatus.ANY, [personId: person.id]).size() == 1
			projectService.getUserProjects(null, false, ProjectStatus.ACTIVE, [personId: person.id]).size() == 1
			projectService.getUserProjects(null, false, ProjectStatus.COMPLETED, [personId: person.id]).size() == 0
	}


}