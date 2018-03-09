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
        applicationOption            {$("label",text:"Application")}//unable to see "checkbox" so used "label"
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
    def selectCheckBoxes(){
        assetClassCh.click()
        bundle.click()
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
    def searchFieldName(){
        searchField="bundle"
        filterBtn.click()
    }
    def clearSearch(){
        clearSearchBtn.click()
    }
    def filterFields(String value){
        fieldsFilter="Selected"
    }
    def clickSave(){
        saveBtn.click()
    }

}
