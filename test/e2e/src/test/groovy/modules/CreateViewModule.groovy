package modules

import geb.Module
import modules.CommonsModule

class CreateViewModule extends Module {

    static content = {
        crtViewPageWindow            (wait:true) { $("section", 	class:"content-header").find("h1")}
        crtViewTitle                 { $("section", 	class:"content-header").find("h1") }

        //>>>>fields
        //filter on field name
        searchField                  {$("input", name:"search")}
        //Modules
        commonsModule { module CommonsModule }
        //>>>>buttons
        nextBtn                      {$("button", class:"btn btn-success")}
        previewBtn                   {$("button",text:"Preview")}
        filterBtn                    {$("button",text:"Filter")}
        clearSearchBtn               {searchField.siblings()}
        saveBtn                      {$("button", text:"Save")}//#btnSave
        saveOptions                  {$("button.btn.dropdown-toggle.btn-success")}//once the button has turned green
        saveOptionsGrey              {$("button.btn.dropdown-toggle.btn-default")}
        saveAs                       {$("a",text:"Save As")}
        closeViewEdition {$("button i.fa-angle-double-down")}
        //>>>>tabs
        assetsClasses                {$("a",text:"Asset Classes")}
        fields                       {$("a",text:"Fields")}
        columns                      {$("a",text:"Columns")}
        //>>>>checkboxes
        applicationOption            {$("label",text:"Application")}

        //fields
        assetClassCh                 {$("label",text:"Asset Class")}
        bundle                       {$("label", text:"Bundle")}
        //>>>>DROP DOWNS
        fieldsFilter                 { $("select#selected") }
        tcModalEventSelector         { tcModalWindow.find("select#moveEvent")}

        //>>>>GRID
        previewGrid                  {$(class:"k-widget k-grid k-grid-lockedcolumns.find(input)")}
        fieldCollection              {$("div",id:"tab_2")}
        previewRows {$("tbody")[1]}
        firstPreviewFilter {$("td[kendogridfiltercell] div input")[0]}
        tableHeaderNames {$('th label')}

    }

    def clickSaveOptions(){
        commonsModule.waitForLoader(5)
        waitFor{saveOptions.click()}
    }

    def clickSaveAs(){
        waitFor{clickSaveOptions()}
        waitFor{saveAs.click()}
    }

    def clickSpecificCheckbox(String name){
        waitFor{$("label", title: name).click()}
    }

    def clickOnCloseViewEdition(){
        waitFor{closeViewEdition.click()}
    }

    def filterPreviewByText(String txt){
        firstPreviewFilter=txt
    }

    def selectApplication(){
        waitFor{applicationOption.click()}
    }

    def clickNext(){
        waitFor{nextBtn.click()}
    }

    def goToAssetsClasses(){
        assetsClasses.click()
    }

    def goToFields(){
        fields.click()
    }

    def goToColumns(){
        columns.click()
    }

    def clickPreview(){
        waitFor{previewBtn.click()}
    }

    def String searchFieldName(){
        def checkboxes = $("div.row input")
        def num =Math.abs(new Random().nextInt() % checkboxes.size()-4)-1
        def name =checkboxes[num].parent().text()
        searchField=name
        filterBtn.click()
        name
    }

    def clearSearch(){
        clearSearchBtn.click()
    }

    def filterFields(String value){
        fieldsFilter=value
    }

    def firstSave(){
        waitFor{saveBtn.click()}
    }
    /**
     * this one is different from the first save since we only need to wait for the loader in the
     * following save actions. Else execution might fail.
     */
    def clickSave(){
        waitFor{saveBtn.click()}
        commonsModule.waitForLoader()
    }

    def selectRandomCheckboxes(){
        def checkboxes = $("div.row input")
        def willSelect =Math.abs(new Random().nextInt() % 5)+1
        for(int i = 1;i<willSelect+1;i++) {
            //this cuts out the input at the beginning and the last 4 which are not part of the set we want
            def num =Math.abs(new Random().nextInt() % checkboxes.size()-4)
            if (checkboxes[num].value()!="on"){
                checkboxes[num].click()
            }
        }
    }

    def boolean selectedCheckboxesDisplayed(){
        boolean isChecked=true
        def selectedChecks =  $("div.nav-tabs-custom", type:"checkbox")
        for (int j=0;j<selectedChecks.size();j++){
            if(selectedChecks[j].value()=="on"){
            }else{
                isChecked= false
            }
        }
        return isChecked
    }

    def boolean unselectedCheckboxesDisplayed(){
        boolean isChecked=true
        def selectedChecks =  $("div.nav-tabs-custom", type:"checkbox")
        for (int j=0;j<selectedChecks.size();j++){
            if(selectedChecks[j].value()!="on"){
            }else{
                isChecked= false
            }
        }
        return isChecked
    }

    //validates the name (label)of the filtered checkbox
    def boolean filteredFieldMatchesDisplay(String value){
        def selectedChecks =  $("div.content.body input")
        //discards first input which is name filter field
        return selectedChecks[1].parent().text().trim().contains(value.trim())
    }

    def boolean compareColumns(){
        def selectedChecks =  $("div.content.body input")
        def cbxNames=[]
        def headerNames=[]
        /**
         * out of all the inputs saves the names of the selected ones
         * in cbxNames List
         */
        for (int i=1; i<selectedChecks.size()-4;i++){
            if (selectedChecks[i].value()=="on"){
                cbxNames.add(selectedChecks[i].parent().text())
            }
        }
        /**
         * gets all the names of the headers and saves them in
         * headerNames List
         */
        def columnHeaders=$("div.sortable-column")
        columnHeaders.each {
            headerNames.add(it.text())
        }
        // for each selected checkbox validates there is a column header name
        cbxNames.each{
            if(!it in headerNames){
                return false
            }
        }
        return  true
    }

    def boolean previewDataIsDisplayed(){
        def appRows = $("tr.application")
        return appRows!=null
    }

    /**
     * validates the basic elements in the Fields tab are displayed as a way
     * to make sure we are in the fields tab
     */
    def validateFieldsTab(){
        previewBtn.displayed
        fieldsFilter.displayed
    }

    def getListOfSelectedFields() {
        def listOfFields= []
        def checks = $("div.content.body input")
        checks.each {
            if (it.value() == "on") {
                listOfFields.add(it.parent().text())
            }
        }
        listOfFields
    }

    def expectedColumnsDisplayed(List names){
        names.each{
            tableHeaderNames.contains(it)
        }
    }

    def validateFilteredRows(String txt){
        previewRows.each{
            it.contains(txt)
        }
    }

    def verifyButtonIsDefaultWithNoChanges(){
        waitFor{saveBtn.jquery.attr("class").contains("btn-default")}
    }
}
