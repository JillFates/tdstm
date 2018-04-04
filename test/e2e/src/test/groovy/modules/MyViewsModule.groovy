package modules

import geb.Module

class MyViewsModule extends Module {

    static at = {
        createViewButton.text().trim() =="Create"
    }
    static content = {
        viewsContainer       { $( "div", class:"content body")}
        createViewButton    {viewsContainer.find("button", text:containsWord("Create"))}
    }
    def clickCreateView(){
        createViewButton.click()
    }
}
