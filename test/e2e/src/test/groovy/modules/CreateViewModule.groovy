package modules

import geb.Module

class CreateViewModule extends Module {

    static at = {
        println(">>> in create view module ***")
        waitFor {crtViewPageWindow.displayed}
        println(">>> title")
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
        println(">>> checking Application")
        applicationOption.click()
    }
    def clickNext(){
        println(">>> clicking next")
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
        println("clicking preview")
        previewBtn.click()
    }
    def filter(){
        println("number of elements found")
        println(previewGrid.count())
    }
    def searchFieldName(){
        println("filtering field name")
        searchField="bundle"
        filterBtn.click()
    }
    def clearSearch(){
        clearSearchBtn.click()
    }
    def filterFields(String value){
        println("filtering selected")
        fieldsFilter="Selected"
    }
    def clickSave(){
        saveBtn.click()
    }


}
