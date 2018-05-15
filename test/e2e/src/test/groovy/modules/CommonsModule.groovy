package modules

import geb.Module
import geb.error.RequiredPageContentNotPresent

class CommonsModule extends Module {

    static content = {

    }

    def waitForLoader() {
        try {
            // try to wait loader icon is displayed then gone loading page content
            // there are big pages where lot of information is loaded
            waitFor { $('#main-loader') }
            waitFor { !$('#main-loader') }
        } catch (RequiredPageContentNotPresent e) {
            // nothing to do here, in case server manage fast the page information
            // and the loader icon is not detected, just prevent test fails
        }
    }

    def waitForGlobalProgressBarModal(){
        waitFor{$('div#globalProgressBar')}
        waitFor{!$('div#globalProgressBar')}
    }
}