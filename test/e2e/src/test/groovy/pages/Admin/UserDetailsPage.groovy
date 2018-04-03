package pages.Admin
import geb.Page
import modules.AdminModule

class UserDetailsPage extends Page {

    static at = {
        udPageTitle.text().trim() == "UserLogin"
        udEditBtn.value() == "Edit"
        udDeleteBtn.value() == "Delete"
        udPassResetBtn.value() == "Send Password Reset"
    }

    static content = {
        udPageTitle             { $("section", class:"content-header").find("h1")}
        pageMessage (required: false, wait:true) { $("div", class:"message")}

        udButtonsForm           { $("form", action:"/tdstm/userLogin/list")}
        udEditBtn               { udButtonsForm.find("input", class:"edit")}
        udDeleteBtn             { udButtonsForm.find("input", class:"delete")}
        udPassResetBtn          { udButtonsForm.find("input#resetPassword")}
    }
}
