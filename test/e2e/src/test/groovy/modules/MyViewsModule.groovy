package modules

import geb.Module

class MyViewsModule extends Module {

    static at = {
        println(">>> in my views module ***")
        createViewButton.text().trim()      =="Create"
    }

    static content = {
        viewsContainer       { $( "div", class:"content body")}
        //createViewButton    {viewsContainer.find("button", text:"Create View")}
        createViewButton    {viewsContainer.find("button", text:"Create View")}

    }


    def clickCreateView(){
        println(">> clickcing create view")
        createViewButton.click()
        println(">>>>> clicked create new view button")
    }
}
