import grails.test.mixin.TestFor
import spock.lang.*

class ProjectServiceTests  extends Specification {
	
	def projectService
	def personService

	Project project

	def setup() {
		project = Project.get(2445)	// Demo Project
		assert project
	}

	def "Test the getStaff"() {
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

	def "Test the getProjectManagersByProject"() {
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

}