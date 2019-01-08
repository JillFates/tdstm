package test.helper.mock

import net.transitionmanager.domain.PartyGroup
import net.transitionmanager.domain.Project
import org.quartz.DateBuilder

import static org.quartz.DateBuilder.newDate

class ProjectMock {
	/**
	 * This Creates a Mock Project used in Unit Test
	 * @return a MockProject
	 */
	static Project create() {
		String projectName = 'projectName'
		String projectDescription = 'description'
		long projectId = 123
		String projectCode = 'projectCode'
		String projectClientName = 'projectClientName'
		long projectClientId = 321
		int completionDateYear = 2100
		int completionDateMonth = DateBuilder.DECEMBER
		int completionDateDay = 15
		Date completionDate = newDate()
				  .inYear(completionDateYear)
				  .inMonth(completionDateMonth)
				  .onDay(completionDateDay)
				  .build()
		completionDate.clearTime()

		Project project = new Project(
				  name: projectName, projectCode: projectCode,
				  completionDate: completionDate, description: projectDescription,
				  client: new PartyGroup(name: projectClientName)
		)

		project.id = projectId
		project.client.id = projectClientId

		return project
	}
}
