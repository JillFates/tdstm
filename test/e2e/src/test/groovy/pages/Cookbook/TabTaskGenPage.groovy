package pages.Cookbook

import geb.Page

class TabTaskGenPage extends Page {

    static at = {

//TODO check the recipe name first on the list
//      tskGTabText.contains("Select appropriate context to generate tasks using the ${recipeName} recipe:"
        tskGTabEventSelectorLabel == "Event:"
        tskGTabAutoPubTaskCboxLabel == "Automatically publish tasks"
        tskGTabGenUsingWipCBoxLabel == "Generate using WIP recipe"
        tskGTabGenerateTasksBtn.text() == "Generate Tasks"
    }

    static content = {

        tskGTab                             { $("li", heading: "Task Generation").find("a") }
        tskGTabText                         { $("div", "ui-view": "taskBatchStart").find("p").text() }
        tskGTabEventSelectorLabel           { $("label", for: "eventSelect").text() }
        tskGTabEventSelector(wait:true)     { $("select#eventSelect") }
        tskGTabBundleSelector               { $("select#bundleSelect") }
        tskGTabSetDefaultLink(wait:true)    { $('a#setDefaultContext') }
        tskGTabClearDefaultLink             { $('a#clearDefaultContext') }
        tskGTabAutoPubTaskCboxLabel         { $("label", for: "autoPublishTasks").text() }
        tskGTabAutoPubTaskCBox              { $('input#autoPublishTasks') }
        tskGTabGenUsingWipCBoxLabel         { $("label", for: "generateUsingWIP").text() }
        tskGTabGenUsingWipCBox              { $('input#generateUsingWIP') }
        tskGTabGenerateTasksBtn(wait:true)  { $("a#generateTask") }
    }
}
