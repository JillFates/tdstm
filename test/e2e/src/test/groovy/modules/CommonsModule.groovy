package modules

import geb.Module
import geb.waiting.WaitTimeoutException

class CommonsModule extends Module {

    static content = {
        modalDialog {$('div#tdsUiDialog')}
    }

    def waitForLoader(Integer secondsToWait = null) {
        try {
            // try to wait loader icon is displayed then gone loading page content
            // there are big pages where lot of information is loaded
            if(secondsToWait) {
                waitFor(secondsToWait) { $('#main-loader') }
                waitFor(secondsToWait) { !$('#main-loader') }
            } else {
                waitFor { $('#main-loader') }
                waitFor { !$('#main-loader') }
            }
        } catch (WaitTimeoutException e) {
            // nothing to do here, in case server manage fast the page information
            // and the loader icon is not detected, just prevent test fails
        }

    }

    def waitForLoadingMessage() {
        try {
            // Try and wait that the loading message on the grid is displayed and then gone so it loads the page content.
            // There are big pages where a lot of information is loaded
            waitFor { $('div#load_applicationIdGrid').displayed }
            waitFor { !$('div#load_applicationIdGrid').displayed }
        } catch (WaitTimeoutException e) {
            // Nothing to do here, in case the server quickly manages the page info
            // and the loading message on the grid isn't detected, then prevent that the test fails
        }
    }

    def waitForGlobalProgressBarModal(){
        waitFor{$('div#globalProgressBar')}
        waitFor{!$('div#globalProgressBar')}
    }

    def waitForEtlScriptsModalHidden(){
        waitFor{!modalDialog.jquery.attr("class").contains("in")}
    }

    def waitForTaskModal() {
        waitFor { !$('div.modal-task') }
    }
}