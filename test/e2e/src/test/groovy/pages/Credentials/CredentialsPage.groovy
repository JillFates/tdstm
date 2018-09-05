package pages.Credentials

import geb.Page
import modules.ProjectsModule


class CredentialsPage extends Page{
    static at = {
        title == "Credentials"
        pageHeaderName.text() == "Credentials"
        createBtn.text() == "Create Credential"
    }

    static content = {
        pageHeaderName { $("section", class:"content-header").find("h1")}
        createBtn(wait:true) { $('button#btnCreate')}
        projectsModule { module ProjectsModule}
    }


}
