package pages.Admin
import geb.Page

class UserCreationPage extends Page {

    static at = {
        ucPageTitle.text().trim() == "Create UserLogin"
        ucSaveBtn.value() == "Save"
    }

    static content = {
        ucPageTitle             { $("section", class:"content-header").find("h1")}
        pageMessage (required: false, wait:true) { $("div", class:"message")}
        ucPageForm              { $("form", id:"createUserForm")}
        ucUsername              { ucPageForm.find("input#username")}
        ucEmail                 { ucPageForm.find("input#emailInputId")}
        ucPassword              { ucPageForm.find("input#passwordId")}
        ucConfirmPassword       { ucPageForm.find("input#confirmPasswordId")}
        ucAdminRoleCB           { ucPageForm.find("input#role_ADMIN")}
        ucActiveSelector        { ucPageForm.find("select#active")}
        ucProjectSelector       { ucPageForm.find("select#projectId")}
        ucSaveBtn               { $("input", class:"save")}
    }
}
