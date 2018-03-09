package specs.Projects

import geb.spock.GebReportingSpec
import jodd.util.RandomString
import pages.Admin.StaffCreationPage
import pages.Admin.StaffListPage
import pages.Admin.UserCreationPage
import pages.Admin.UserDetailsPage
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
    def "Go to Project List"() {
        testKey = "TM-XXXX"
        given:
        at MenuPage
        when:
        menuModule.goToProjectsActive()
        then:
        at ProjectListPage
     }

    def "Open Create Project Page"() {
        testKey = "TM-XXXX"
        given:
        at ProjectListPage
        when:
        waitFor {createProjectBtn.click()}
        then:
        at ProjectCreationPage
    }

    def "Create Project"() {
        testKey = "TM-XXXX"
        given:
        at ProjectCreationPage
        when:
        waitFor {pcClientSelector.click()}
        waitFor {pcClientItem.click()}
        pcProjectCode = projName
        pcProjectName = projName
        pcDescription = projDesc
        pcComment     = projComment
        pcCompletionDate  = projCompDate
        waitFor {pcSaveBtn.click()}
        then:
        at ProjectDetailsPage
        waitFor {pdPageMessage.text().contains(projName + " was created")}
    }

    def "Go to Project List for search new project"() {
        testKey = "TM-XXXX"
        given:
        at MenuPage
        when:
        menuModule.goToProjectsActive()
        then:
        at ProjectListPage
    }

    def "Filter by Project Name" () {
        testKey = "TM-XXXX"
        given:
        at ProjectListPage
        when:
        waitFor { projectNameFilter.click() }
        projectNameFilter  = projName
        then:
        waitFor{$("td", "role": "gridcell", "aria-describedby": "projectGridIdGrid_projectCode").find("a").text() == projName}
    }
}