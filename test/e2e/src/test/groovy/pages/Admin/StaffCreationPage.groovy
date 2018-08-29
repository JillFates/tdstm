package pages.Admin
import geb.Page
import modules.AdminModule

class StaffCreationPage extends Page {

    static at = {
        scModalTitle.text() == "Create Staff"
        scModalSaveBtn.value() == "Save"
        scModalCancelBtn.value() == "Cancel"
        scModalCloseBtn
    }

    static content = {

        scModalWindow(wait:true)        { $("div", "role":"dialog", "aria-describedby":"createStaffDialog")}
        scModalTitle                    { scModalWindow.find("span#ui-id-1", class:"ui-dialog-title")}
        scModalCompanySelector          { scModalWindow.find("select#companyId")}
        scModalStaffTypeSelector        { scModalWindow.find("select#staffType")}
        scModalActiveSelector           { scModalWindow.find("select#active")}
        scModalFirstName                { scModalWindow.find("input#firstName")}
        scModalMiddleName               { scModalWindow.find("input#middleName")}
        scModalLastName                 { scModalWindow.find("input#lastName")}
        scModalAddTeam                  { scModalWindow.find("span", "style":"cursor: pointer;", "onclick":"addFunctionsCreate()")}
        scModalTeamSelector (required: false, wait:true) { scModalWindow.find("select#functionId")}
        scModalSaveBtn                  { scModalWindow.find("input", class:"save")}
        scModalCancelBtn                { scModalWindow.find("input#cancelBId")}
        scModalCloseBtn                 { scModalWindow.find("button", "class":"ui-dialog-titlebar-close")}
        scEmail                         {$("#email")}
    }
}
