package specs.Projects.Project

import geb.spock.GebReportingSpec
import utils.CommonActions
import pages.Projects.Project.ProjectCreationPage
import pages.Projects.Project.ProjectEditPage
import pages.Projects.Project.ProjectDetailsPage
import pages.Projects.Project.ProjectListPage
import pages.Login.LoginPage
import pages.Login.MenuPage
import spock.lang.Stepwise

// import geb.driver.CachingDriverFactory

/**
 * This script was created as requested on TM-15572
 * @author: Ingrid
 */

@Stepwise
class ProjectEditionSpec extends GebReportingSpec {
    def testKey
    static testCount
    static randStr = CommonActions.getRandomString()

    //Define the names for the Staff you will Create and Edit
    static baseName = "QAE2E"
    static projName = baseName +" "+ randStr
    static projCode = projName
    static projDesc = "Description of the project "+ projName +" created by QA E2E Geb Scripts"
    static projComment = "Comment for project "+ projName +" created by QA E2E Geb Scripts"
    static projCompDate =  ((new Date()) + 180).format("MM/dd/yyyy")
    static licensedProjectName = "TM-Demo"

    def setupSpec() {
        // CachingDriverFactory.clearCacheAndQuitDriver()
        
        testCount = 0
        to LoginPage
        login(4,5) // test needs to be done by e2e_projects_user
        at MenuPage
        waitFor{projectsModule}
        waitFor{projectsModule.goToProjectsActive()}
        at ProjectListPage
        filterByName "QAE2E"
        if( noRecrdsAreDisplayed() ){
            createProjectBtn.click()
            at ProjectCreationPage
            waitFor {pcClientSelector.click()}
            waitFor {pcClientItem.click()}
            pcProjectCode = projName
            pcProjectName = projName
            pcDescription = projDesc
            pcComment     = projComment
            pcCompletionDate  = projCompDate
            waitFor {pcSaveBtn.click()}
            projectsModule.goToProjectsActive()
            filterByName "QAE2E"
        }else{
            projName = getFirstProjectName()
            projCode = getFirstProjectCode()

        }
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    // Enter on Project page
    def "1. Go to Project List and search for an existing project"() {
        given: 'The user is in Project List'
            at ProjectListPage
        when: 'The user clicks on a project'
            clickOnFirstListedProject()
        then: 'The user is led to Project Details page'
            at ProjectDetailsPage

    }

    def "2. User edits the project"() {
        given: 'The User in on Edit Page'
            at ProjectDetailsPage
        when: 'The User makes changes and saves them'
            clickEdit()
            at ProjectEditPage
            editProjectName()
            editProjectDescription()
            editCompletionDate(projCompDate)
            clickUpdate()
        then: 'User is led to Project Details Page'
            at ProjectDetailsPage
        and: 'Corresponding update message is displayed'
            waitFor {pdPageMessage.text().contains(projCode + " updated")}
        and: 'Changes are saved'
            waitFor{pdProjectName.text() == projName + " Edited"}

    }

    //Verifications are done, the following lines set the project back to TM-Demo
    def "3. User goes back to TM-Demo"(){
        given: 'The user goes to Project List'
            projectsModule.goToProjectsActive()
            at ProjectListPage
        when : 'The user selects TM-Demo'
            waitFor{projectNameGridField}
            filterByName "TM-Demo"
            clickOnFirstListedProject()
        then:   'The user is led to Project Details Page'
        at ProjectDetailsPage

    }

}
