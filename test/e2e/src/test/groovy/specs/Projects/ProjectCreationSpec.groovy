package specs.Projects

import geb.spock.GebReportingSpec
import jodd.util.RandomString
import pages.Projects.ProjectCreationPage
import pages.Projects.ProjectDetailsPage
import pages.Projects.ProjectListPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise

@Stepwise
class ProjectCreationSpec extends GebReportingSpec {
    def testKey
    static testCount
    static randStr =  RandomString.getInstance().randomAlphaNumeric(3)

    //Define the names for the Staff you will Create and Edit
    static baseName = "QAE2E"
    static projName = baseName +" "+ randStr
    static projDesc = "Descrition of the project "+ projName +" created by QA E2E Geb Scripts"
    static projComment = "Comment for project "+ projName +" created by QA E2E Geb Scripts"
    static projCompDate =  ((new Date()) + 3).format("MM/dd/yyyy")
    static licensedProjectName = "TM-Demo"

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    // Enter on Project page
    def "1. Go to Project List"() {
        testKey = "TM-XXXX"
        given: 'The user navigates to Project menu'
            at MenuPage
        when: 'The user clicks on Active Projects link'
            menuModule.goToProjectsActive()
        then: 'Project List Page should be displayed'
            at ProjectListPage
     }

    def "2. Open Create Project Page"() {
        testKey = "TM-XXXX"
        given: 'The User is on the Project List Page'
            at ProjectListPage
        when: 'The User clicks on Create Project button'
            waitFor {createProjectBtn.click()}
        then: 'Project Creation Page is displayed'
            at ProjectCreationPage
    }

    def "3. Create Project"() {
        testKey = "TM-XXXX"
        given: 'The User is on the Project Create Page'
            at ProjectCreationPage
        when: 'The user fill all required fields'
            waitFor {pcClientSelector.click()}
            waitFor {pcClientItem.click()}
            pcProjectCode = projName
            pcProjectName = projName
            pcDescription = projDesc
            pcComment     = projComment
            pcCompletionDate  = projCompDate
        and: 'The user clicks on Save button'
            waitFor {pcSaveBtn.click()}
        then: 'Project Details Page is displayed'
            at ProjectDetailsPage
        and: 'Message saying project created is displayed'
            waitFor {pdPageMessage.text().contains(projName + " was created")}
    }

    def "4. Go to Project List for search new project"() {
        testKey = "TM-XXXX"
        given: 'The user navigates to Projects'
            at MenuPage
        when: 'The user clicks on Active Projects link'
            menuModule.goToProjectsActive()
        then: 'Project List Page should be displayed'
            at ProjectListPage
    }

    def "5. Filter by Project Name" () {
        testKey = "TM-XXXX"
        given: 'The user is in Project List Page'
            at ProjectListPage
        when: 'The user set project filter name'
            waitFor { projectNameFilter.click() }
            projectNameFilter = projName
        then: 'Project created should be displayed in the grid'
            waitFor{$("td", "role": "gridcell", "aria-describedby": "projectGridIdGrid_projectCode").find("a").text() == projName}
    }

    def "6. Verify whether the Project is licensed or not"() {
        testKey = "TM-XXXX"
        given: 'The user is in Menu'
            at MenuPage
        when: 'The user searches for the license Icon visible'
            menuModule.assertLicenseIconVisible()
        then: 'The user is in Project List Page'
            at ProjectListPage
        when: 'A licensed project should be selected'
            waitFor {projectNameFilter.click()}
            projectNameFilter = licensedProjectName
            waitFor {projectNameGridField.find("a", text: licensedProjectName).first().click()}
        then: 'The user is in the menu'
            at MenuPage
        and: 'The project name should be the selected'
            waitFor {menuModule.projectName.isDisplayed()}
            menuModule.assertProjectName(licensedProjectName)
        and: 'The license icon is not present'
            menuModule.assertLicenseIconNotVisible()
    }
}