package specs.Projects.ETLScripts

import geb.spock.GebReportingSpec
import pages.Projects.ETLScripts.*
import spock.lang.Stepwise
import pages.Login.LoginPage
import pages.Login.MenuPage

/**
 * This script deletes excess number of ETL Scripts
 * @ingrid
 */

@Stepwise
class ETLScriptsCleanUpSpec extends GebReportingSpec{

    def testKey
    static testCount
    static baseName = "E2E DS"
    static maxAllowed=3
    static maxNumberToBeDeleted=5
    static success=true

    def setupSpec() {
        testCount = 0
        to LoginPage
        login()
        at MenuPage
        projectsModule.goToETLScripts()
        at ETLScriptsPage
    }

    def setup() {
        testCount++
    }

    def cleanup() {
        String sCount = String.format("%03d", testCount)
        println "cleanup(): ${testKey} #${sCount} ${specificationContext.currentIteration.name} "
    }

    def deleteETLScripts(){
        if(dsTableRows.size()==0){
            //message validation
            return noRecordsMessage.displayed
        }else{
            def count = 0
            while (count<maxNumberToBeDeleted && dsTableRows.size()>maxAllowed){
                //execution speed needs to be reduced in order to avoid attempting to delete the same script twice
                commonsModule.waitForLoader 7 //needed to allow for elements to be displayed as expected
                deleteByPosition(0)
                count = count + 1
                commonsModule.waitForLoader 7 //needed to allow time for loader
            }
            if (count==maxNumberToBeDeleted){
                return dsTableRows.size()>3
            }else{
                return dsTableRows.size()<=3
            }
        }
    }

    def "1. The user deletes ETLScripts"() {
        given: 'The User is on the ETLScripts Page'
            at ETLScriptsPage
            filterByName(baseName)
        when: 'The user deletes ETLScripts'
            def validateResult = deleteETLScripts()
        then: 'The operation is successful'
            validateResult==success

    }
}
