package net.transitionmanager.service

import grails.test.mixin.TestFor
import org.grails.web.json.JSONObject
import spock.lang.Specification

@TestFor(UserPreferenceService)
class UserPreferenceServiceSpec extends Specification {

    void '01. Test depGraph validator'() {
        setup: 'Given a basic dep graph JSON structure'
            JSONObject json = [
                    "maxEdgeCount"  : "4",
                    "colorBy"       : "group",
                    "appLbl"        : "true"
            ]
        when: 'depGraphValidator is called with the JSON with different values'
            json[property]=value
            def evaluation = service.depGraphValidator(json.toString())
        then: 'the evaluation is validated for the different values'
            evaluation  == validationResult
        where:
            property            | value                 | validationResult
            'bundleConflicts'   | 'true'                | true
            'bundleConflicts'   | 'false'               | false
            'blackBackground'   | 'true'                | true
            'blackBackground'   | 'false'               | false
            'appLbl'            | 'true'                | true
            'appLbl'            | 'false'               | false
            'srvLbl'            | 'true'                | true
            'srvLbl'            | 'false'               | false
            'dbLbl'             | 'true'                | true
            'dbLbl'             | 'false'               | false
            'spLbl'             | 'true'                | true
            'spLbl'             | 'false'               | false
            'slLbl'             | 'true'                | true
            'slLbl'             | 'false'               | false
            'netLbl'            | 'true'                | true
            'netLbl'            | 'false'               | false

            'colorBy'           | 'group'               | true
            'colorBy'           | 'bundle'              | true
            'colorBy'           | 'event'               | true
            'colorBy'           | 'environment'         | true
            'colorBy'           | 'sourceLocationName'  | true
            'colorBy'           | 'targetLocationName'  | true
            'colorBy'           | 'other'               | false

            'maxEdgeCount'      | '0'                   | false
            'maxEdgeCount'      | '1'                   | true // lower-end
            'maxEdgeCount'      | '10'                  | true
            'maxEdgeCount'      | '20'                  | true // upper-end
            'maxEdgeCount'      | '21'                  | false
    }

    void '02. Test archGraph validator'() {
        setup: 'Given a basic arch graph JSON structure'
            JSONObject json = [
                    "assetClasses"  : "ALL",
                    "levelsUp"      : "0",
                    "levelsDown"    : "3",
                    "showCycles"    : "true",
                    "appLbl"        : "true"
            ]
        when: 'archGraphValidator is called with the JSON with different values'
            json[property]=value
            def evaluation = service.archGraphValidator(json.toString())
        then: 'the evaluation is validated for the different values'
            evaluation  == validationResult
        where:
            property            | value     | validationResult
            'showCycles'        | 'true'    | true
            'showCycles'        | 'false'   | false
            'blackBackground'   | 'true'    | true
            'blackBackground'   | 'false'   | false
            'appLbl'            | 'true'    | true
            'appLbl'            | 'false'   | false
            'srvLbl'            | 'true'    | true
            'srvLbl'            | 'false'   | false
            'dbLbl'             | 'true'    | true
            'dbLbl'             | 'false'   | false
            'spLbl'             | 'true'    | true
            'spLbl'             | 'false'   | false
            'slLbl'             | 'true'    | true
            'slLbl'             | 'false'   | false
            'netLbl'            | 'true'    | true
            'netLbl'            | 'false'   | false

            'levelsUp'          | '-1'      | false
            'levelsUp'          | '0'       | true // lower-end
            'levelsUp'          | '4'       | true
            'levelsUp'          | '10'      | true // upper-end
            'levelsUp'          | '11'      | false

            'levelsDown'        | '-1'      | false
            'levelsDown'        | '0'       | true // lower-end
            'levelsDown'        | '6'       | true
            'levelsDown'        | '10'      | true // upper-end
            'levelsDown'        | '12'      | false
    }

    void '03. Test legendTwistieState validator'() {

        when: 'legendTwistieStateValidator is called with different values'
        def evaluation = service.legendTwistieStateValidator(value)
        then: 'the evaluation is validated for the different values'
        evaluation  == validationResult
        where:
        value   | validationResult
        ''                  | true // empty should be ok
        'ac'                | true // Asset Classes
        'de'                | true // Dependencies
        'hb'                | true // Groups
        'other'             | false // Non-existent, should fail
        'ac,de'             | true // combinations should be ok
        'ac,de,hb'          | true
        'ac,de,hb,ac'       | false // more than 3 values should fail
    }
}
