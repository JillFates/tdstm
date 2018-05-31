package modules

import geb.Module
import geb.waiting.WaitTimeoutException

class CommonsModule extends Module {

    static content = {

    }

    def waitForLoader() {
        try {
            // Try and wait that the loading message on the grid is displayed and then gone so it loads the page content.
            // There are big pages where a lot of information is loaded
            waitFor { $('#main-loader') }
            waitFor { !$('#main-loader') }
        } catch (WaitTimeoutException e) {
            // Nothing to do here, in case the server quickly manages the page info
            // and the loading message on the grid isn't detected, then prevent that the test fails
            true
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
            true
        }
    }

    def waitForGlobalProgressBarModal(){
        waitFor{$('div#globalProgressBar')}
        waitFor{!$('div#globalProgressBar')}
    }
}