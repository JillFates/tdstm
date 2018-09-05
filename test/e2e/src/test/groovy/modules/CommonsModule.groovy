package modules

import geb.Module
import geb.waiting.WaitTimeoutException
import geb.Browser

class CommonsModule extends Module {

    static content = {
        modalDialog {$('div#tdsUiDialog')}
        prompDialog {$('div#tdsUiPrompt')}
        prompDialogButton {prompDialog.find("button")}
        deleteAlertMessage {prompDialog.find(".box-body p")}
        deleteAlertNoButton {prompDialog.find("button", text: contains("No"))}
        deleteAlertYesButton {prompDialog.find("button", text: contains("Yes"))}
        kendoDateFilter { $('kendo-popup td[role=gridcell]')}
        loadingIndicator { $('.loading-indicator')}
        kendoGridPaginationContainer { $('kendo-pager')}
        kendoGridPaginationButtons { kendoGridPaginationContainer.find("a.k-link")}
        kendoSelectPaginationOptions { kendoGridPaginationContainer.find("kendo-pager-page-sizes select option")}
    }

    def waitForLoader(Integer secondsToWait = null) {
        try {
            // try to wait loader icon is displayed then gone loading page content
            // there are big pages where lot of information is loaded
            if(secondsToWait) {
                waitFor(secondsToWait) { $('#main-loader') }
                waitFor(secondsToWait) { !$('#main-loader') }
            } else {
                waitFor { $('#main-loader') }
                waitFor { !$('#main-loader') }
            }
        } catch (WaitTimeoutException e) {
            // nothing to do here, in case server manage fast the page information
            // and the loader icon is not detected, just prevent test fails
        }

    }

    def waitForLoadingMessage() {
        try {
            // Try and wait that the loading message on the grid is displayed and then gone so it loads the page content.
            // There are big pages where a lot of information is loaded
            waitFor { $('div.loading').displayed }
            waitFor { !$('div.loading').displayed }
        } catch (WaitTimeoutException e) {
            // Nothing to do here, in case the server quickly manages the page info
            // and the loading message on the grid isn't detected, then prevent that the test fails
        }
    }

    def waitForGlobalProgressBarModal(){
        waitFor{$('div#globalProgressBar')}
        waitFor{!$('div#globalProgressBar')}
    }

    /*
    * date: string formatted date required by kendo picker. "EEEE, MMMM d, yyyy"//Friday, June 1, 2018
    * calendarIconIndex: NOT REQUIRED if one date filter, number because is possible to have more than one inputs
    * */
    def setKendoDateFilter(date, calendarIconIndex = null){
        if (calendarIconIndex != null) {
            browser.driver.executeScript("\$('kendo-datepicker span.k-select')[$calendarIconIndex].click()")
        } else {
            browser.driver.executeScript("\$('kendo-datepicker span.k-select').click()")
        }
        waitFor{kendoDateFilter.find{it.@title.contains(date)}.click()}
    }

    def removeKendoDateFilter(calendarIconIndex = null){
        if (calendarIconIndex != null) {
            browser.driver.executeScript("\$('kendo-datepicker + span.fa-times')[$calendarIconIndex].click()")
        } else {
            browser.driver.executeScript("\$('kendo-datepicker + span.fa-times').click()")
        }
    }

    def waitForDialogModalHidden(){
        waitFor{!modalDialog.jquery.attr("class").contains("in")}
    }

    def waitForTaskModal() {
        waitFor { !$('div.modal-task') }
    }

    def waitForPromptModalHidden(){
        waitFor{!prompDialog.jquery.attr("class").contains("in")}
    }

    def waitForPromptModalDisplayed(){
        waitFor{prompDialog.jquery.attr("class").contains("in")}
    }

    def clickOnButtonPromptModalByText(text){
        waitFor{prompDialogButton.find{it.text().contains(text)}.click()}
        waitForPromptModalHidden()
    }

    def clickOnDeleteYesPromptModal(){
        waitFor{deleteAlertYesButton.click()}
        waitForPromptModalHidden()
    }

    def clickOnDeleteNoPromptModal(){
        waitFor{deleteAlertNoButton.click()}
        waitForPromptModalHidden()
    }

    def getDeleteAlertMessageText(){
        waitFor{deleteAlertMessage.displayed}
        deleteAlertMessage.text()
    }

    def verifyDeletePrompDialogMessage(text){
        getDeleteAlertMessageText().contains text
    }

    def blockCookbookLoadingIndicator(){
        loadingIndicator.jquery.attr("style", "display: none !important")
    }

    def clickOnKendoPaginationButtonByText(text){
        goToElement kendoGridPaginationButtons.find{it.@title.contains(text)}
        waitFor{kendoGridPaginationButtons.find{it.@title.contains(text)}.click()}
        waitForLoader 2
    }

    def goToTargetKendoGridPage(target, timesToClick = 1){
        def count = 0
        while (count < timesToClick){
            count = count + 1
            clickOnKendoPaginationButtonByText target.toLowerCase()
        }
    }

    def goToLastKendoGridPage(){
        clickOnKendoPaginationButtonByText "last"
    }

    def goToFirstKendoGridPage(){
        clickOnKendoPaginationButtonByText "first"
    }

    def selectRandomPaginationNumber(){
        goToElement kendoSelectPaginationOptions
        def option = CommonActions.getRandomOption kendoSelectPaginationOptions
        option.click()
        waitForLoader 2
    }

    def goToElement(element){
        interact{
            moveToElement element
        }
    }

    def isListOfElements(selector){
        selector.size() > 1
    }
}