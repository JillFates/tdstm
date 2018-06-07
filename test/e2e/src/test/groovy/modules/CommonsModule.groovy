package modules

import geb.Module
import geb.waiting.WaitTimeoutException

class CommonsModule extends Module {

    static content = {

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

    def waitForGlobalProgressBarModal(){
        waitFor{$('div#globalProgressBar')}
        waitFor{!$('div#globalProgressBar')}
    }
}