/**
 * ProjectTestHelper is a helper class that can be used by the test cases to fetch, create and do other 
 * helpful data preparation necessary to be used by the integration tests. The intent of these helper classes 
 * is to do the heavy lifting for the ITs so that they an focus on the good stuff.
 * 
 * These helpers should not rely on any pre-existing data and will generate anything that is necessary. At least
 * that's the idea...
 */

import com.tdsops.common.grails.ApplicationContextHolder


class ProjectTestHelper {
	def projectService
	Long projectId = 2445 

	ProjectTestHelper() {
		projectService = ApplicationContextHolder.getService('projectService')	
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

}