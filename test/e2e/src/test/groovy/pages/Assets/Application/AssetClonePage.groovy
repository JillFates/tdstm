package pages.Assets.Application
import geb.Page

class AssetClonePage extends Page {

    static at = {
        waitFor {asclModalWindow.displayed}
        asclModalTitle.text().trim().startsWith("Clone")
        asclModalAssetCloneNameLbl.text().trim()  == "New asset name:"
        asclModalIncludeDepLbl.text().trim()      == "Include Dependencies:"
        asclModalCloneEditBtn.text().trim()       == "Clone & Edit"
        asclModalCloneBtn.text().trim()           == "Clone"
        asclModalCancelBtn.text().trim()          == "Cancel"
    }

    static content = {
        asclModalWindow             (wait:true) { $("div","aria-describedby":"cloneEntityView", "aria-labelledby":"ui-id-5")}
        asclModalTitle              { asclModalWindow.find("span#ui-id-5")}
        asclModalAssetCloneNameLbl  { asclModalWindow.find( "label", for:"newAssetName")}
        asclModalAssetCloneName     { asclModalWindow.find("input#newAssetName")}
        asclModalErrorMsg           { asclModalWindow.find("span", class:"lbl-error-text")}
        asclModalViewAssetMsg       { asclModalWindow.find("span", class:"open-asset-detail-btn")}
        asclModalIncludeDepLbl      { asclModalWindow.find( "label", for:"includeDependencies")}
        asclModalIncludeDepCB       { asclModalWindow.find("input#includeDependencies")}
        asclModalCloneEditBtn       { asclModalWindow.find("button", action:"edit")}
        asclModalCloneBtn           { asclModalWindow.find("button", action:"clone")}
        asclModalCancelBtn          { asclModalWindow.find("button", class:"btn btn-default close-clone-entity")}
        asclModalCloseBtn           { asclModalWindow.find("button", class:"ui-dialog-titlebar-close")}

        // TODO modal dialog its not unique. Located by CSS
        asclModalDialog             (wait:true) { $("div","aria-describedby":"confirmationDialogOnClone","aria-labelledby":"ui-id-14")}
        asclModalDialogTitle        { asclModalDialog.find("span#ui-id-14") }
        asclModalDialogText         { asclModalDialog.find("div#confirmationDialogOnClone").find("div",class:"box-body")}
        asclModalDialogConfirmBtn   { asclModalDialog.find("button",class:"btn btn-primary pull-left accept-confirmation-btn")}
        asclModalDialogCancelbtn    { asclModalDialog.find("button",class:"btn btn-default pull-right cancel-confirmation-btn")}
    }

    def getModalTitle(){
        waitFor { asclModalTitle.text().trim() }
    }

    def getValidationModalLegend(){
        asclModalErrorMsg.text().trim()
    }

    def getModalInputNameValue(){
        asclModalAssetCloneName.value()
    }

    def clickOnCloneButton(){
        waitFor{asclModalCloneBtn.click()}
        waitFor {asclModalDialog.displayed}
    }

    def clickOnCloseInConfirmationDialog(){
        waitFor {asclModalDialogCancelbtn.click()}
        waitFor {!asclModalDialog.displayed}
    }

    def closeModal(){
        waitFor {asclModalCancelBtn.click()}
        waitFor {!asclModalWindow.displayed}
    }

    def clickOnConfirmInConfirmationDialog(){
        waitFor {asclModalDialogConfirmBtn.click()}
    }

    def getConfirmationDialogTitle(){
        asclModalDialogTitle.text().trim()
    }

    def getConfirmationDialogBodyText(){
        asclModalDialogText.text().trim()
    }
}
