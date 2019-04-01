import com.tdsops.tm.enums.domain.ProjectSortProperty
import com.tdsops.tm.enums.domain.ProjectStatus
import com.tdsops.tm.enums.domain.SortOrder
import grails.test.mixin.TestFor
import net.transitionmanager.party.PartyGroup
import net.transitionmanager.project.Project
import net.transitionmanager.security.UserLogin
import net.transitionmanager.project.WsProjectController
import net.transitionmanager.project.ProjectService
import net.transitionmanager.security.SecurityService
import org.quartz.DateBuilder
import test.AbstractUnitSpec

import static org.quartz.DateBuilder.newDate

@TestFor(WsProjectController)
class WsProjectControllerSpec extends AbstractUnitSpec {

	void testUserProjects() {
		given:
			boolean called = false
			ProjectStatus statusExpected = ProjectStatus.ACTIVE
			int maxRowsExpected = 1_000_000_000
			int currentPageExpected = 42
			ProjectSortProperty sortOnExpected = ProjectSortProperty.PROJECT_CODE
			SortOrder sortOrderExpected = SortOrder.DESC
			boolean showAllProjPermExpected = true

			String projectName = 'projectName'
			String projectDescription = 'description'
			long projectId = 123
			String projectCode = 'projectCode'
			String projectStatusExpected = 'active'
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

			controller.securityService = new SecurityService() {
				boolean hasPermission(String permission) {
					assert permission == 'ProjectShowAll'
					showAllProjPermExpected
				}
			}

			controller.projectService = new ProjectService() {
				List<Project> getUserProjects(
					Boolean showAllProjPerm = false,
					ProjectStatus projectStatus = ProjectStatus.ACTIVE,
					Map searchParams = [:],
					UserLogin userLogin = null)
				{
					assert showAllProjPerm == showAllProjPermExpected
					assert projectStatus == statusExpected
					assert searchParams.size() == 4
					assert searchParams.maxRows == maxRowsExpected.toString()
					assert searchParams.currentPage == currentPageExpected.toString()
					assert searchParams.sortOn == sortOnExpected
					assert searchParams.sortOrder == sortOrderExpected
					assert !userLogin

					called = true

					Project project = new Project(name: projectName, projectCode: 'projectCode',
							completionDate: completionDate, description: projectDescription,
							client: new PartyGroup(name: projectClientName))
					project.id = projectId
					project.client.id = projectClientId
					[project]
				}
			}

		when:
			controller.params.status = statusExpected.value
			controller.params.maxRows = maxRowsExpected.toString()
			controller.params.currentPage = currentPageExpected.toString()
			controller.params.sortOn = sortOnExpected.value
			controller.params.sortOrder = sortOrderExpected.value
			controller.userProjects()
		then:
			called
			assertSuccessJson controller.response

		when:
			def projects = controller.response.json?.data?.projects
		then:
			projects
			projects.size() == 1
			projects[0].clientId == projectClientId
			projects[0].projectCode == projectCode
			projects[0].clientName == projectClientName
			projects[0].name == projectName
			projects[0].description == projectDescription
			projects[0].id == projectId
			projects[0].status == projectStatusExpected
			projects[0].completionDate.startsWith("$completionDateYear-$completionDateMonth-${completionDateDay}T")
	}
}
