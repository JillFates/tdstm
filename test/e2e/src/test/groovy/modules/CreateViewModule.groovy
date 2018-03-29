package modules

import geb.Module

class CreateViewModule extends Module {

    static at = {
        waitFor {crtViewPageWindow.displayed}
    }

    static content = {
        crtViewPageWindow            (wait:true) { $("section", 	class:"content-header").find("h1")}
        crtViewTitle                 { $("section", 	class:"content-header").find("h1") }

        //>>>>fields
        //filter on field name
        searchField                  {$("input", name:"search")}

        //>>>>buttons
        nextBtn                      {$("button", class:"btn btn-success")}
        previewBtn                   {$("button",text:"Preview")}
        filterBtn                    {$("button",text:"Filter")}
        clearSearchBtn               {searchField.siblings()}
        saveBtn                      {$("button", text:"Save")}
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

    }

    def selectApplication(){
        applicationOption.click()
    }
    def clickNext(){
        nextBtn.click()
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
        previewBtn.click()
    }
    def filter(){
        println(previewGrid.count())
    }
    def String searchFieldName(){
        def checkboxes = $("div.content.body input")
        def num =Math.abs(new Random().nextInt() % checkboxes.size()-4)-1
        def name =checkboxes[num].parent().text()
        searchField=name
        filterBtn.click()
        return name
    }
    def clearSearch(){
        clearSearchBtn.click()
    }
    def filterFields(String value){
        fieldsFilter=value
    }
    def clickSave(){
        saveBtn.click()
    }
    def selectRandomCheckboxes(){
        def checkboxes = $("div.content.body input")
        def willSelect =Math.abs(new Random().nextInt() % 5)+1
        for(int i = 1;i<willSelect+1;i++) {
            println("for: i= "+i)
            //this cuts out the input at the begiNning and the last 4 which are not part of the set we want
            def num =Math.abs(new Random().nextInt() % checkboxes.size()-4)
            println ">>>>  num"+ num
            println ">>>>>>>  CLICKING"
            if (checkboxes[num].value()!="on"){
                checkboxes[num].click()
            }

        }
    }
    def boolean selectedCheckboxesDisplayed(){
        boolean isChecked=true
        def selectedChecks =  $("div.content.body inputy")
        for (int j=10;j<selectedChecks.size()-5;j++){
            if(selectedChecks[j].value()=="on"){
            }else{
                isChecked= false
            }
        }
        return isChecked
    }
    def boolean unselectedCheckboxesDisplayed(){
        boolean isChecked=true
        def selectedChecks =  $("div.content.body input")
        for (int j=10;j<selectedChecks.size()-5;j++){
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
}
