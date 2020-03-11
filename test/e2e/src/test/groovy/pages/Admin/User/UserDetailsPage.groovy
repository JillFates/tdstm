package pages.Admin.User

import geb.Page
import modules.AdminModule
import modules.CommonsModule

class UserDetailsPage extends Page {

    static at = {
        udPageTitle.text().trim() == "UserLogin"
        udEditBtn.value() == "Edit"
        udDeleteBtn.value() == "Delete"
        udPassResetBtn.value() == "Send Password Reset"
    }

    static content = {
        udPageTitle { $("section", class:"content-header").find("h2")}
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
        createdDate {$(".value")[19].text()}
        lastModified {$(".value")[20].text()}
        lockedOutUntil {$("div", class:"ng-scope").find("tbody").find("tr")[9].find("td", "nowrap":"nowrap")}
        unlockUserLoginUsername {$("#unlockUserDialog").find("ul").find("li")[0]}
        unlockUserLoginConfirmBtn {$(class:"ui-dialog-buttonset").find("button")[0]}
        unlockUserLoginCancelBtn {$(class:"ui-dialog-buttonset").find("button")[1]}
        commonsModule { module CommonsModule }
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
        assert createdDate == lastModified, "Created and Last Modified date did not match"
        true
    }

    def verifyUnlockButtonDisplayed(){
        unlockButtonID.displayed
    }

    def verifyUsernameLocked(username){
        unlockUserLoginUsername.text().contains(username)
    }

    /**
     *The unlock user modal is displayed on top of the /tdstm/userLogin/show/userID page,
     * when this modal is closed, the page is NOT reloaded.
     * This means that sometimes the value on the 'Locked Out Until:' field is not updated
     * as fast as the code is run.
     * For this reason I ask if it already says 'Not locked Out", and if it doesn't
     * I reload the page AGAIN.
     * I know this is in theory unnecessary but we're forced to do it due to the slowness of the server.
     * @author: Alvaro Navarro
     */
    def verifyNotLockedOut(){
        waitFor(30){lockedOutUntil.displayed}
        if((lockedOutUntil.text().contains("Not Locked Out")))
            true
        else
        {
            driver.navigate().refresh() //Done twice because sometimes 1 time doesn't load the updated info fast enough
            driver.navigate().refresh()
            waitFor(30){lockedOutUntil.text().contains("Not Locked Out")}
        }
    }

}
