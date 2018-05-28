package modules

import geb.Module
import geb.waiting.WaitTimeoutException

class CommonsModule extends Module {

    static content = {

    }

    def waitForLoader() {
        try {
            // try to wait loader icon is displayed then gone loading page content
            // there are big pages where lot of information is loaded
            waitFor { $('#main-loader') }
            waitFor { !$('#main-loader') }
        } catch (WaitTimeoutException e) {
            // nothing to do here, in case server manage fast the page information
            // and the loader icon is not detected, just prevent test fails
            true
        }
    }

    def waitForLoadingMessage() {
        try {
            // try to wait loading message in grid is displayed then gone loading page content
            // there are big pages where lot of information is loaded
            waitFor { $('div#load_applicationIdGrid').displayed }
            waitFor { !$('div#load_applicationIdGrid').displayed }
        } catch (WaitTimeoutException e) {
            // nothing to do here, in case server manage fast the page information
            // and the loading message in grid is not detected, just prevent test fails
            true
        }
    }

    def waitForGlobalProgressBarModal(){
        waitFor{$('div#globalProgressBar')}
        waitFor{!$('div#globalProgressBar')}
    }
}