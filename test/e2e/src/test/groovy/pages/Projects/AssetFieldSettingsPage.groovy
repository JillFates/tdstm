package pages.Projects

import geb.Page
import modules.ProjectsModule
import modules.CommonsModule

class AssetFieldSettingsPage extends Page {

    static at = {
        modaltitle.text() == "Asset Field Settings"

    }

    static content = {
        modaltitle { $("section", class:"content-header").find("h1")}
        projectsModule { module ProjectsModule}
        commonsModule { module CommonsModule }
    }


}
