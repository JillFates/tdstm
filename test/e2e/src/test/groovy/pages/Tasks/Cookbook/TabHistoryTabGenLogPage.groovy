package pages.Tasks.Cookbook

import geb.Page

class TabHistoryTabGenLogPage extends Page {

    static at = {
        hisTabGenLogTab.parent(".active")
    }

    static content = {
        hisTabGenLogTab    (wait:true)     { $("li", heading: "Generation Log").find("a") }

        // TODO following item have the checkbox inside the label
        //hisTabGenLogTabExcpRadioLabel      { $("label", for:"exceptions").text() }
        hisTabGenLogTabExcpRadio           { $("input#exceptions") }
        // TODO following item have the checkbox inside the label
        //hisTabGenLogTabInfoRadioLabel      { $("label", for:"infoWarnings").text() }
        hisTabGenLogTabInfoRadio           { $("input#infoWarnings") }
        hisTabGenLogTabTxt                 { $("pre", "ng-bind-html":"taskBatchLogs") }    }
}
