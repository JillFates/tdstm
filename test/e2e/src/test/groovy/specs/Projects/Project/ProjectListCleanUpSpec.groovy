package specs.Projects.Project

import geb.spock.GebReportingSpec
import pages.Projects.Project.ProjectDetailsPage
import pages.Projects.Project.ProjectListPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise
import geb.error.RequiredPageContentNotPresent

/**
 * This class represents the new generic asset details
 * page
 * @author Sebastian Bigatton
 */

@Stepwise
class ProjectListCleanUpSpec extends GebReportingSpec {
    def testKey
    static testCount
    static baseName = "QAE2E"
    static maxNumberOfProjects = 1
    static maxNumberOfProjectsToBeDeleted = 2 // Example: will delete 2 actives and 2 completed
    static projectName = "TM-Demo"

    def setupSpec() {
        testCount = 0
        to LoginPage
        login(4,5) // test needs to be done by e2e_projects_user
        at MenuPage
        projectsModule.goToProjectsActive()
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def deleteProject(){
        clickOnProjectByName baseName
        at ProjectDetailsPage
        clickOnDeleteButtonAndConfirm()
        at ProjectListPage
        verifyDeletedMessage()
    }

    def deleteProjects(isCompleted = false){
        def count = 0
        while (verifyRowsDisplayed() && getListedProjectsSize() > maxNumberOfProjects){
            count = count + 1
            if (count > maxNumberOfProjectsToBeDeleted) {
                break
            }
            deleteProject()
            if (isCompleted){
                clickOnCompletedProjectsButton()
            }
            filterByName baseName
        }
        true // return true to avoid condition fails
    }

    def "1. The User deletes Projects starting with QAE2E for clean up purposes"() {
        given: 'The User is on the Project List Page'
            at ProjectListPage
        when: 'The user filters by project name starting with QAE2E'
            filterByName baseName
        then: 'The user deletes active projects if there are'
            deleteProjects()
        when: 'The user clicks on Show completed projects button'
            clickOnCompletedProjectsButton()
        and: 'The user filters by project name starting with QAE2E'
            filterByName baseName
        then: 'The user deletes completed projects if there are'
            deleteProjects(true)
    }

    def "2. Workaround to select a Project for next build"() {
        when: 'The user clicks on Active Projects button'
            clickOnActiveProjectsButton()
        and: 'The user selects a project'
            filterByName projectName
        and: 'The user clicks on project'
            clickOnProjectByName projectName
        then: 'The project should be selected'
            projectsModule.assertProjectName projectName
    }
}