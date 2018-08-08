package pages.Assets
import geb.Page

class NewAssetDetailsPage extends Page{

    static at = {
        waitFor {adModalWindow.displayed}
        adModalEditBtn.text().trim() == "Edit"
        adModalCloseBtn
    }

    static content = {
        adModalWindow                   (wait:true) { $(class:"tds-angular-component-content")}
        adModalTitle                    { adModalWindow.find("span#ui-id-7", class:"ui-dialog-title")}
        adModalPlanningContainer { adModalWindow.find(".dialog .planning-application-table")}
        // TODO Following items fetch by data-content cannot be located as self (Label and Value have the same properties)
        adModalAppName                  { adModalPlanningContainer.find("tr:nth-child(1) td span")[2]}
        adModalDescription              { adModalWindow.find("span","data-content":"Description")}
        adModalSME1                     { adModalWindow.find("span","data-content":"SME1")}
        adModalSME2                     { adModalWindow.find("span","data-content":"SME2")}
        adModalAppOwner                 { adModalWindow.find("span","data-content":"App Owner")}
        adModalBundle                   { adModalWindow.find("span","data-content":"Bundle")}
        adModalPlanStatus               { adModalWindow.find("span","data-content":"Plan Status")}
        adModalAssetName                {$('td.label.assetName.O').next()}
        adModalLastUpdated              {$(".last-updated")}

        adModalSuppColTitles            (required:false) { adModalWindow.find("tr#deps td div",0).find("table thead tr th")}
        adModalSuppList                 (required:false) { adModalWindow.find("tr#deps td div",0).find("table tbody tr")}
        adModalIsDepColTitles           (required:false) { adModalWindow.find("tr#deps td div",1).find("table thead tr th")}
        adModalIsDepList                (required:false) { adModalWindow.find("tr#deps td div",1).find("table tbody tr")}

        //TODO following butttons have no ID to reference them
        adModalEditBtn                  { adModalWindow.find("button",class:"btn btn-primary pull-left")}
        adModalAddTaskBtn               { adModalWindow.find("button", "onclick":contains("createIssue('${adModalAppName.text().trim()}',''"))}
        adModalAddCommentBtn            { adModalWindow.find("button", "onclick":contains("createIssue('${adModalAppName.text().trim()}','comment'"))}
        adModalArchGraphBtn             { adModalWindow.find("button", name:"_action_Delete")}
        adModalCloseBtn                 { adModalWindow.find("button", class:"btn btn-default pull-right")}//ui-button-icon-primary ui-icon

    }

    def validateDataIsPresent(List rowData, List dataDisplayed){
        boolean success=true
        def str =""
        try {
            rowData.each{
                it-> if (it!="" ){
                    str=it
                    if (!dataDisplayed.contains(it)){
                        success = false
                    }else{
                        println str
                    }
                }
            }
        } catch(Exception e1) {
            success = false
        }
        success
    }

    def getContent(){
        def screenData =$(".valueNW")
        def screenText=[]
        screenText.add($('td.label.assetName.O').next().text())
        screenData.each{
            it-> screenText.add(it.text().trim())
        }
        screenText.add(getLastUpdated())
        screenText
    }

    def getLastUpdated(){
        adModalLastUpdated.text().split(" ")[2]+ (" ")+adModalLastUpdated.text().split(" ")[3]+ (" ")+adModalLastUpdated.text().split(" ")[4]
    }

    def getName(){
        adModalAssetName.text()
    }

    def closeDetailsModal(){
        waitFor {adModalCloseBtn.click()}
        waitFor {!adModalWindow.displayed}
    }



    def getApplicationName(){
        adModalAppName.text().trim()
    }
}
