package modules

import geb.Module

class CommonsModule extends Module {

    static content = {

    }

    def waitForGlobalProgressBarModal(){
        waitFor{$('div#globalProgressBar')}
        waitFor{!$('div#globalProgressBar')}
    }

}