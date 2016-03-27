
class ProjectTestHelper {
	def projectService
	Long projectId = 2445 

	ProjectTestHelper(projectService) {
		assert (projectService instanceof ProjectService)
		this.projectService = projectService
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