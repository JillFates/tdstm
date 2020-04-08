package net.transitionmanager.command.model

import grails.testing.web.controllers.ControllerUnitTest
import net.transitionmanager.command.ModelCommand
import net.transitionmanager.model.ModelController
import spock.lang.Specification
import spock.lang.Unroll

import static com.tdssrc.grails.TimeUtil.parseISO8601Date

class ModelCommandSpec extends Specification implements ControllerUnitTest<ModelController> {

    @Unroll
    void 'test a populated CreateModelCommandObject with property date:#date bound is valid:#valid)'() {

        setup: 'a JSON input from UI'
            request.json = buildJSONModelWith(date)

        and: 'command is populated with JSON content'
            ModelCommand modelCommand = controller.populateCommandObject(ModelCommand, false)

        expect: 'command is valid'
            modelCommand.validate() == valid

        and: 'endOfLifeDate field was correctly bound'
            modelCommand.endOfLifeDate == boundDate

        and: 'the amount of errors is correctly counted'
            modelCommand.errors.errorCount == errorCount
            modelCommand.errors.getFieldError('endOfLifeDate')?.defaultMessage == errorMesage

        where:
            date                     | boundDate                                  | errorCount | errorMesage                        || valid
            "2027-10-22 00:00:00'Z'" | parseISO8601Date("2027-10-22 00:00:00'Z'") | 0          | null                               || true
            '05-15-2027'             | null                                       | 1          | 'Unparseable date: "05-15-2027"'   || false
            'Invalid Date'           | null                                       | 1          | 'Unparseable date: "Invalid Date"' || false
            null                     | null                                       | 1          | 'Unparseable date: "null"'         || false
    }

    @Unroll
    void 'test can populate CreateModelCommandObject binding properties powerType:#powerType, powerNameplate:#powerNameplate, powerDesign:#powerDesign, powerUse:#powerUse)'() {

        setup: 'a JSON input from UI'
            request.json = buildJSONModelWith(powerType, powerNameplate, powerDesign, powerUse)

        and: 'command is populated with JSON content'
            ModelCommand modelCommand = controller.populateCommandObject(ModelCommand, false)

        expect: 'command is valid'
            modelCommand.validate() == valid

        and: 'power type field and its related fields were correctly bound'
            modelCommand.powerNameplate == powerNameplateBound
            modelCommand.powerDesign == powerDesignBound
            modelCommand.powerUse == powerUseBound

        where:
            powerType | powerNameplate | powerNameplateBound | powerDesign | powerDesignBound | powerUse | powerUseBound || valid
            null      | null           | null                | null        | null             | null     | null          || false
            'Watts'   | null           | null                | null        | null             | null     | null          || false
            'Watts'   | '1.0'          | 1.0f                | '1.0'       | 1.0f             | '1.0'    | 1.0f          || true
            'Amps'    | '1.0'          | 1.0f                | '1.0'       | 1.0f             | '1.0'    | 1.0f          || true
            'Invalid' | '1.0'          | 1.0f                | '1.0'       | 1.0f             | '1.0'    | 1.0f          || false
    }

    private String buildJSONModelWith(String powerType, String powerNameplate, String powerDesign, String powerUse) {
        return buildJSONModelWith("2027-10-22 00:00:00'Z'", powerType, powerNameplate, powerDesign, powerUse)
    }

    private String buildJSONModelWith(String date) {
        return buildJSONModelWith(date, 'Watts', '0.0', '0.0', '0.0')
    }
    /**
     * Build JSON model in String format using parameters
     */
    private String buildJSONModelWith(String date, String powerType, String powerNameplate, String powerDesign, String powerUse) {
        return """
        {
          "modelName": "TM-16131 Model",
          "manufacturer": {
            "id": "748"
          },
          "assetType": "Power",
          "usize": 1,
          "weight": 123.456,
          "productLine": "Product Line Variable",
          "endOfLifeDate": "$date",
          "cpuType": "CPU Type Value",
          "cpuCount": 8,
          "powerType": "$powerType",
          "powerNameplate": $powerNameplate,
          "powerDesign": $powerDesign,
          "powerUse": $powerUse,
          "roomObject": true,
          "height": 12.34,
          "width": 56.78,
          "depth": 90.12,
          "layoutStyle": "Layout Style value",
          "endOfLifeStatus": "End Of Life Status Value",
          "modelFamily": "Model Family Value",
          "memorySize": 2050.45,
          "storageSize": 2048.23,
          "description": "A quick note",
          "sourceTDS": 1,
          "sourceURL": "www.tds.com",
          "modelStatus": "full",
          "modelConnectors": [
            {
              "connector": 1,
              "status": "missing",
              "type": "Serial",
              "label": "Connector I",
              "labelPosition": "Bottom",
              "connectorPosX": "12.34",
              "connectorPosY": "21.43"
            }
          ],
          "removedConnectors": [],
          "connectorCount": 1,
          "akaChanges": {
            "deleted": "",
            "edited": [],
            "added": []
          }
        }
    """.trim().stripIndent()
    }
}
