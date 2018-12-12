package specs.Planning.Events
/**
 * This Spec creates events without bundles.
 * @author ingrid
 */

import pages.Login.LoginPage
import pages.Login.MenuPage
import pages.Planning.Events.ListEventsPage
import pages.Planning.Events.CreateEventPage
import pages.Planning.Events.EventDetailsPage
import geb.spock.GebReportingSpec
import spock.lang.Stepwise
import utils.CommonActions

@Stepwise
class CreateEventNoBundleSpec extends GebReportingSpec {

    def testKey
    static testCount
    static baseName = "QAE2E"
    static randStr = CommonActions.getRandomString()
    static bundleData = [baseName + " " + randStr + " Planning", baseName + " bundle created by automated test",
                         "STD_PROCESS", "on"]
    static startDate = ""
    static proceed = true
    static maxNumberOfBundles = 1

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        planningModule.goToListEvents()
        at ListEventsPage

    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def "1. User has access to creation Page"() {
        given: 'The user is at Event List page'
            at ListEventsPage
        when: 'The user clicks create event'
            clickCreateEvent()
        then: 'The user is led to Event creation page'
            at CreateEventPage
     }

    def "2. User can cancel"() {
        when: 'The user clicks cancel'
            cancelEventCreation()
        then: 'The user is led to Event List page'
            at ListEventsPage
    }

    def "2. User cannot save without a name"() {
        given:'The user has completed the CreateEvent form'
            clickCreateEvent()
            at CreateEventPage
        when: 'The user attempts to save without entering a name'
            enterDescription(bundleData[1])
            saveChanges()
        then: 'The system displays an error message'
            at CreateEventPage
    }

    def "3.User is able to create an Event with no associated bundle"(){
        given: 'The user has completed the creation form'
            startDate=completeForm(bundleData)
        when: 'The user saves'
            saveChanges()
        then: 'The user is led to the Event details page'
            at EventDetailsPage
        and: 'The name is as expected'
            validateEventCreationMessage(bundleData[0])
        and: 'The name is as expected'
            validateEventName(bundleData[0])
        and: 'The description is as expected'
            validateEventDescription(bundleData[1])
        and:'Start date is as expected'
            validateEventStartDate(startDate)
        and: 'The new event is listed in Event List'
            goToEventList()
    }

    def "4. The event created is listed in event list"(){
        given: 'The user is in Event List page'
            at ListEventsPage
        when: 'The user filters the event by name'
            filterEventByName(bundleData[0])
        then: 'The event is retrieved'
            validateNameIsListed(bundleData[0])
        and: 'Description matches the one entered on creation'
            validateDescription(bundleData[1])
        and: 'Start Date matches the one entered on creation'
            validateStartDate(startDate)
        and: 'Event name is present on the top right along with project name'
            validateEventNameAlongProjName(bundleData[0])
    }

}