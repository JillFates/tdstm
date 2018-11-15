package pages.Assets.AssetViews

import geb.Page
import modules.CommonsModule

class AssetClonePage extends Page {

    static at = {
        asclModalWindow.displayed
        asclModalTitle.text().trim().startsWith("Clone")
        asclModalAssetCloneNameLbl.text().trim()  == "New asset name"
        asclModalIncludeDepLbl.text().trim()      == "Include Dependencies"
        asclModalCloneEditBtn.text().trim()       == "Clone & Edit"
        asclModalCloneBtn.text().trim()           == "Clone"
        asclModalCancelBtn.text().trim()          == "Cancel"
    }

    static content = {
        asclModalWindow { $("div#asset-clone-component")}
        asclModalTitle { asclModalWindow.find(".modal-title")}
        asclModalAssetCloneNameLbl { asclModalWindow.find("label", for:"newAssetName")}
        asclModalAssetCloneName { asclModalWindow.find("input#newAssetName")}
        asclModalErrorMsg { asclModalWindow.find("span.asset-unique-name")}
        asclModalViewAssetMsg { asclModalWindow.find("span", class:"open-asset-detail-btn")}
        asclModalIncludeDepLbl { asclModalWindow.find( "label", for:"includeDependencies")}
        asclModalIncludeDepCB { asclModalWindow.find("input#includeDependencies")}
        asclModalCloneEditBtn { asclModalWindow.find("button span.glyphicon-edit").parent()}
        asclModalCloneBtn { asclModalWindow.find("button span.glyphicon-duplicate").parent()}
        asclModalCancelBtn { asclModalWindow.find("button span.glyphicon-ban-circle").parent()}
        asclModalCloseBtn { asclModalWindow.find("button.close")}

        commonsModule { module CommonsModule}

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
    }

    def clickOnCloseInConfirmationDialog(){
        waitFor {asclModalDialogCancelbtn.click()}
        waitFor {!asclModalDialog.displayed}
    }

    def closeModal(){
        waitFor {asclModalCancelBtn.click()}
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
