package modules

import geb.Module

class CommonsModule extends Module {

    static content = {

    }

    def waitForLoader(){
        waitFor{$('#main-loader')}
        waitFor{!$('#main-loader')}
    }

}