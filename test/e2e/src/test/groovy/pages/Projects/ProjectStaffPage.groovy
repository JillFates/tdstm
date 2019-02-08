package pages.Projects

import geb.Page
import modules.ProjectsMenuModule

class ProjectStaffPage extends Page {

    static at = {
        modaltitle.text() == "Project Staff"
        team.text() == "Team"
        onlyClientStaff.text() == "Only Client Staff"
        onlyAssignedLabel.text() == "Only Assigned"
        projectLabel.text() == "Project"
    }

    static content = {
        modaltitle { $("section", class:"content-header").find("h1")}
        team { $('b#teamLabel')}
        onlyClientStaff { $('b#onlyClientStaffLabel')}
        onlyAssignedLabel { $('b#onlyAssignedLabel')}
        projectLabel { $('b#projectLabel')}
        projectsModule { module ProjectsMenuModule}

    }


}
