package pages.Admin
import geb.Page
import modules.AdminModule

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
        ucClientAdmiRoleCB      { ucPageForm.find("input#role_CLIENT_ADMIN")}
        ucClientMgrRoleCB       { ucPageForm.find("input#role_CLIENT_MGR")}
        ucEditorRoleCB          { ucPageForm.find("input#role_EDITOR")}
        ucSupervisorRoleCB      { ucPageForm.find("input#role_SUPERVISOR")}
        ucUserRoleCB            { ucPageForm.find("input#role_USER")}

        ucActiveSelector        { ucPageForm.find("select#active")}
        ucProjectSelector       { ucPageForm.find("select#projectId")}
        ucSaveBtn               { $("input", class:"save")}
    }
    /**
     * This method selects up to six random roles for the user
     * @author ingrid
     */
    def selectRandomRoles(){
        def availableRoles =[ucAdminRoleCB,ucClientAdmiRoleCB,ucClientMgrRoleCB,ucEditorRoleCB,
                         ucSupervisorRoleCB,ucUserRoleCB]
        //number of roles that will be assigned to the user
        def numRoles =Math.abs(new Random().nextInt() % availableRoles.size())+1
        for(int i = 0;i<numRoles+1;i++) {
            def num =Math.abs(new Random().nextInt() % availableRoles.size())//random position in the availableRoles list
            if (availableRoles[num].value()==false){
                availableRoles[num].click()
            }
        }
    }
}
