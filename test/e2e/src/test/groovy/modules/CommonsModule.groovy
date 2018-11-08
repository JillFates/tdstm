package modules

import geb.Module
import geb.waiting.WaitTimeoutException
import geb.Browser
import utils.CommonActions

class CommonsModule extends Module {

    static content = {
        modalDialog {$('div#tdsUiDialog')}
        prompDialog {$('div#tdsUiPrompt')}
        prompDialogButton {prompDialog.find("button")}
        modalDialogButton {modalDialog.find("button")}
        confirmationAlertMessage {prompDialog.find(".box-body p")}
        deleteAlertNoButton {prompDialog.find("button", text: contains("No"))}
        deleteAlertYesButton {prompDialog.find("button", text: contains("Yes"))}
        kendoDateFilter { $('kendo-popup td[role=gridcell]')}
        loadingIndicator { $('.loading-indicator')}
        kendoGridPaginationContainer { $('kendo-pager')}
        kendoGridPaginationButtons { kendoGridPaginationContainer.find("a.k-link")}
        kendoSelectPaginationOptions { kendoGridPaginationContainer.find("kendo-pager-page-sizes select option")}
        kendoDropdownList { $("kendo-popup.k-animation-container  kendo-list")}
        kendoMultiselectTagsListOptions { kendoDropdownList.find("div.asset-tag-selector-single-item")}
        kendoMultiselectSelectedList { $("#asset-tag-selector-component kendo-taglist li div")}
        kendoDropdownListOptions { kendoDropdownList.find("li.k-item")}
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

    def waitForDialogModalDisplayed(){
        waitFor{modalDialog.jquery.attr("class").contains("in")}
    }

    def clickOnButtonDialogModalByText(text){
        waitFor{modalDialogButton.find{it.text().contains(text)}.click()}
        waitForPromptModalHidden()
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

    def getConfirmationAlertMessageText(){
        waitFor{confirmationAlertMessage.displayed}
        confirmationAlertMessage.text()
    }

    def verifyConfirmationPrompDialogMessage(text){
        getConfirmationAlertMessageText().contains text
    }

    def blockCookbookLoadingIndicator(){
        loadingIndicator.jquery.attr("style", "display: none !important")
    }

    def clickOnKendoPaginationButtonByText(text){
        goToElement kendoGridPaginationButtons.find{it.@title.contains(text)}
        waitFor{kendoGridPaginationButtons.find{it.@title.contains(text)}.click()}
        waitForLoader 2
    }

    /**
     * Clicks on kendo grid pagination arrow to go to page based on passed text
     * @param: target = options: "first", "last", "next" or "previous"
     * @param: timesToClick = default set to 1
     * @author: Sebastian Bigatton
     */
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
        browser.driver.executeScript("arguments[0].scrollIntoView(true);", element.singleElement())
    }

    def isListOfElements(selector){
        selector.size() > 1
    }

    /**
    * Verifies element is displayed in page or not
    * @param: selector. Eg: $('div.something')
    * @author: Sebastian Bigatton
    */
    def verifyElementDisplayed(selector){
        try {
            waitFor(0.5){selector.displayed}
        } catch (WaitTimeoutException e){
            false
        }
    }

    def clickToOpenKendoDropdownMultiselect(){
        js.('$(".component-action-open").click()')
    }

    /**
     * Selects random tags from common kendo tags multiselect dropdown component in the application
     * found by given name and quantity or just one if quantity is not specified
     * or filter by given text returns only 1 selector. Eg: text = QAE2E can return more than one tag
     * containing that text
     * @param: text = some text to search into existing tag list name
     * @param: numberOfTagsToBeSelected = number of tags to select, default 1 if not set
     * @author: Sebastian Bigatton
     */
    def selectRandomKendoMultiselectTagOptionByText(text, numberOfTagsToBeSelected = 1){
        clickToOpenKendoDropdownMultiselect()
        def options = waitFor{kendoMultiselectTagsListOptions.findAll {it.text().contains(text)}}
        def option
        if (options && numberOfTagsToBeSelected > 1){ // select random from filtered options by given text and quantity
            option = CommonActions.getRandomOptions options, numberOfTagsToBeSelected
        } else { // select any from filtered options by given text
            option = CommonActions.getRandomOption options
        }
        option.click()
    }

    /**
     * Returns a list of tag names (1..n tags) from common kendo tags list component displayed
     * in the application.
     * @author: Sebastian Bigatton
     */
    def getSelectedTagsFromKendoMultiselect(){
        def selectedTagsList = []
        if (isListOfElements(kendoMultiselectSelectedList)){ // add every element text displayed
            kendoMultiselectSelectedList.each {
                selectedTagsList.add it.text.trim()
            }
        } else { // add the only element text displayed
            selectedTagsList.add kendoMultiselectSelectedList.text().trim()
        }
        selectedTagsList
    }

    def selectKendoDropdownOptionByText(text){
        def option = waitFor{kendoDropdownListOptions.find {it.text().contains(text)}}
        option.click()
    }
}