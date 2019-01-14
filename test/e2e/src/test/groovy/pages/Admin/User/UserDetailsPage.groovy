package pages.Admin.User

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
        udPageTitle { $("section", class:"content-header").find("h1")}
        pageMessage (required: false, wait:true) { $("div", class:"message")}
        adminModule { module AdminModule}
        udButtonsForm { $("form", action:"/tdstm/userLogin/list")}
        udEditBtn { udButtonsForm.find("input", class:"edit")}
        udDeleteBtn { udButtonsForm.find("input", class:"delete")}
        udPassResetBtn { udButtonsForm.find("input#resetPassword")}
        fullName {$("tbody")[0].find("a")}
        unlockButtonID {$("#unlockButtonId")}
        userCompany {$(".value")[0].text()}
        userName {$(".value")[2].text()}
        email {$(".value")[3].text()}
        createdDate {$(".value")[18].text()}
        lastModified {$(".value")[19].text()}
        lockedOutUntil {$(".value")[9]}
        unlockUserLoginUsername {$("#unlockUserDialog").find("ul").find("li")[0]}
        unlockUserLoginConfirmBtn {$(class:"ui-dialog-buttonset").find("button")[0]}
        unlockUserLoginCancelBtn {$(class:"ui-dialog-buttonset").find("button")[1]}
    }

    def clickOnDeleteButtonAndConfirm(){
        withConfirm(true){waitFor{udDeleteBtn.click()}}
    }
    /**
     * Validates the page contains the original details entered when creating the user
     * @param originalDetails
     * @author ingrid
     */
    def validateUserDetails(List originalDetails){
        //originalDetails = [userCompany,firstName,middleName,lastName,userName,userEmail]
        List dataDisplayed=[userCompany,fullName.text().split(" ")[0],fullName.text().split(" ")[1],fullName.text().split(" ")[2],
                       userName,email]
        def success=false
        originalDetails.each { data ->
            if(dataDisplayed.find {it.contains(data)}){
                success = true
            }
            assert success, "$data was not found in the details page."
        }
        assert createdDate== lastModified, "Created and Last Modified date did not match"
        true
    }

    def verifyUnlockButtonDisplayed(){
        unlockButtonID.displayed
    }

    def verifyUsernameLocked(username){
        unlockUserLoginUsername.text().contains(username)
    }

    def verifyNotLockedOut(){
        lockedOutUntil.text().contains("Not Locked Out")
    }

}
