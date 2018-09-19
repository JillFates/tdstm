package specs.Projects.Project

import geb.spock.GebReportingSpec
import utils.CommonActions
import pages.Projects.Project.Project.ProjectCreationPage
import pages.Projects.Project.Project.ProjectDetailsPage
import pages.Projects.Project.Project.ProjectListPage
import pages.Projects.Tags.TagsPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise
import geb.error.RequiredPageContentNotPresent

@Stepwise
class ProjectCreationDefaultTagsSpec extends GebReportingSpec {
    def testKey
    static testCount
    static randStr = CommonActions.getRandomString()
    static baseName = "QAE2E"
    static projName = baseName +" "+ randStr
    static licensedProjectName = "TM-Demo"
    static projectInfo = [
            "projName": projName,
            "projDesc": "Description of the project "+ projName +" created by QA E2E Geb Scripts",
            "projComment": "Comment for project "+ projName +" created by QA E2E Geb Scripts",
            "projCompDate": ((new Date()) + 3).format("MM/dd/yyyy")

    ]
    static GDPR_Tag = ["name": "GDPR", "description": "General Data Protection Regulation Compliance", "color": "#eaf2d9"]
    static HIPPA_Tag = ["name": "HIPPA", "description": "Health Insurance Portability and Accountability Act Compliance", "color": "#eaf2d9"]
    static PCI_Tag = ["name": "PCI", "description": "Payment Card Industry Data Security Standard Compliance", "color": "#eaf2d9"]
    static SOX_Tag = ["name": "SOX", "description": "Sarbanes–Oxley Act Compliance", "color": "#eaf2d9"]

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        projectsModule.goToProjectsActive()
        at ProjectListPage
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "1. Open Create Project Page"() {
        given: 'The User is on the Project List Page'
            at ProjectListPage
        when: 'The User clicks on Create Project button'
            clickOnCreateButton()
        then: 'Project Creation Page is displayed'
            at ProjectCreationPage
    }

    def "2. Create Project and certify header name"() {
        given: 'The User is on the Project Create Page'
            at ProjectCreationPage
        when: 'The User fills all required fields'
            fillInFields projectInfo
        and: 'The user clicks on Save button'
            clickOnSaveButton()
        then: 'Project Details Page is displayed'
            at ProjectDetailsPage
        and: 'Message saying project created is displayed'
            waitForProjectCreatedMessage projName
        and: 'Project name is displayed in menu bar'
            projectsModule.assertProjectName projName
    }

    def "3. The User navigates to Tags Page and verifies default tags created"(){
        given: 'The User is in Menu'
            at MenuPage
        when: 'The User clicks on Project > Tags'
            projectsModule.goToTagsPage()
        then: 'Manage Tags Page should be displayed'
            at TagsPage
        and: 'Certify four default tags were created'
            getGridRowsSize() == 4
    }

    def "4. The User verifies default tags info displayed"(){
        given: 'The User is in Manage Tags page'
            at TagsPage
        when: 'The User filters by GDPR'
            filterByName GDPR_Tag.name
        then: 'User verifies row info displayed is correct'
            getTagNameText() == GDPR_Tag.name
            getTagDescriptionText() == GDPR_Tag.description
            getTagColorHexText() == GDPR_Tag.color
        when: 'The User filters by HIPPA'
            filterByName HIPPA_Tag.name
        then: 'The User verifies row info displayed is correct'
            getTagNameText() == HIPPA_Tag.name
            getTagDescriptionText() == HIPPA_Tag.description
            getTagColorHexText() == HIPPA_Tag.color
        when: 'User filters by PCI'
            filterByName PCI_Tag.name
        then: 'The User verifies row info displayed is correct'
            getTagNameText() == PCI_Tag.name
            getTagDescriptionText() == PCI_Tag.description
            getTagColorHexText() == PCI_Tag.color
        when: 'The User filters by SOX'
            filterByName SOX_Tag.name
        then: 'The User verifies row info displayed is correct'
            getTagNameText() == SOX_Tag.name
            getTagDescriptionText() == SOX_Tag.description
            getTagColorHexText() == SOX_Tag.color
    }

    def "5. Workaround to switch to a licensed Project"() {
        given: 'The user is in Menu'
            at MenuPage
        when: 'The user searches for the selected project'
            projectsModule.assertProjectName projName
        then: 'A licensed project should be selected if license is requested'
            def displayed = false
            try {
                // Check if license icon is displayed
                projectsModule.projectLicenseIcon.displayed
                // Select a licensed project
                projectsModule.goToProjectsActive()
                at ProjectListPage
                filterByName licensedProjectName
                clickOnProjectByName licensedProjectName
                at MenuPage
                waitFor {projectsModule.projectName}
                projectsModule.assertProjectName licensedProjectName
                // Recheck, if its present assertion will fail, otherwise we are OK
                displayed = projectsModule.projectLicenseIcon.displayed
            } catch (RequiredPageContentNotPresent e) {
                // Try failed because license icon is not present so a Licensed project is selected now
                displayed = false
            }
            !displayed
    }
}