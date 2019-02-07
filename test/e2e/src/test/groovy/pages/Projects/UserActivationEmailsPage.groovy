package pages.Projects

import geb.Page
import modules.ProjectsMenuModule

class UserActivationEmailsPage extends Page {

    static at = {
        modaltitle.text() == "User Activation Emails"
        customMessage.text() == "Welcome to TransitionManager."
        sendActivationEmail.value() == "Send Activation Emails"
    }

    static content = {
        modaltitle { $("section", class:"content-header").find("h1")}
        customMessage {$("textarea", name:"customMessage")}
        sendActivationEmail {$("input" , type:"submit" , value:"Send Activation Emails")}
        projectsModule { module ProjectsMenuModule}

    }


}
