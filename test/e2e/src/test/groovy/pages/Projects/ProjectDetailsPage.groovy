package pages.Projects

import geb.Page

class ProjectDetailsPage extends Page {

    static at = {
        pdHeaderTitle.text().trim() == "Project Details"
        // TODO project table have no references to its items
        pdEditBtn.value() == "Edit"
        pdDeleteBtn.value() == "Delete"
        pdFieldSetBtn.value() == "Field Settings"
    }

    static content = {
        pdHeaderTitle               { $("section", class:"content-header").find("h1") }
        pdProjDetTable              { $("table",class:"show-project-table")}
        pdPageMessage (required: false, wait:true) { $("div", class:"message")}  //TODO add ID reference to message div
        // TODO project table have no references to its items
        pdEditBtn                   { $("input", type:"submit",class:"edit", name:"_action_Edit")}
        pdDeleteBtn                 { $("input", type:"submit",class:"delete", name:"_action_Delete")}
        pdFieldSetBtn               { $("input", type:"button",class:"show")}

    }
}