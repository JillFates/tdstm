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

	def "Test the getProjectManagersByProject"() {
		when:
			List pms = projectService.getProjectManagersByProject()

		then:
			pms != null
			pms?.size() > 0

			def numOfPms = pms.size()
//			pms[0].

	}

}