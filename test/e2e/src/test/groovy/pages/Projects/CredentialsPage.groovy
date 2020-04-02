package pages.Projects

import geb.Page
import modules.ProjectsMenuModule


class CredentialsPage extends Page{
    static at = {
        title == "Credentials"
        pageHeaderName.text() == "Credentials"
        pageBreadcrumbs[0].text() == "Project"
        pageBreadcrumbs[1].text() == "Credentials"
    }

    static content = {
        pageHeaderName { $("section", class:"content-header").find("h2")}
        createBtn(wait:true) { $('button#btnCreate')}
        pageBreadcrumbs { $("ol", class:"breadcrumb-container").find("li")}
        projectsModule { module ProjectsMenuModule}
    }


}
