package pages.Assets.AssetViews

import geb.Page
import modules.CommonsModule
import modules.CreateViewModule
import utils.CommonActions
import modules.AssetsModule
import geb.waiting.WaitTimeoutException

class ViewPage extends Page{

    static at = {
        waitFor {view.displayed}
        createButton.displayed
        exportViewButton.displayed
    }
    static content = {
        view (wait:true) { $("section","class":"page-asset-explorer-config")}
        sectionHeaderTitle { $("section.content-header h1")}
        clearBtn {$("button", id:"btnClear")}
        createButton {$("button[type=button]", text: "Create")}
        bulkChangeButton {$('#btnBulkChange')}
        exportViewButton {$("button", id:"btnExport")}
        exportModalContainer {$('#tdsUiDialog')}
        fileNameField {exportModalContainer.find("input", id: "fileName")}
        exportModalButton {exportModalContainer.find("button span", class: "fa-download")}
        cancelModalButton {exportModalContainer.find("button span", class: "glyphicon-ban-circle")}
        commonsModule { module CommonsModule }
        assetsModule { module AssetsModule }
        createViewModule {module CreateViewModule}
        viewMgrBreadCrumb {$('a.font-weight-bold')}
        //starOn {$("fa text-yellow fa-star")}
        starOff {$(".fa.text-yellow.fa-star-o")}
        gearBtn {$(".fa-cog")}
        justPlanningCheck(required:false)  { $("input", type: "checkbox" , name: "justPlanning") }
        itemsPerPage(required:false) {$("kendo-pager" , class:"k-pager-wrap k-grid-pager k-widget").find("kendo-pager-page-sizes", class:"k-pager-info k-label")}
        selectAllChecks(required:false) {$("[name=assetsSelector] input")}
        itemNumberDesc(required:false) {$("kendo-pager-info" , class:"k-pager-info k-label")}
        nextPageButton(required:false) {$("kendo-pager" , class:"k-pager-wrap k-grid-pager k-widget").find("kendo-pager-next-buttons").find("a", class:"k-link k-pager-nav" , title:"Go to the next page").find("span", class:"k-icon k-i-arrow-e")}
        leftTableElements(required:false) {$("div" , class:"k-grid-content-locked element-height-100-per-i" , role:"presentation")}
        allItemsCheckbox(wait: true) {$("label",class:"selectall-checkbox-column").find("input",type:"checkbox")}
        firstElementName(required:false) {$("div", class:"k-grid-content-locked element-height-100-per-i").find("div", role:"presentation").find("table",class:"k-grid-table").find("tbody",role:"presentation").find("tr")[0].find("td")[1]}
        firstElementAssetClass(required:false) {$("div", class:"k-grid-content-locked element-height-100-per-i").find("div", role:"presentation").find("table",class:"k-grid-table").find("tbody",role:"presentation").find("tr")[0].find("td")[2]}
        nameFilter {$('div.k-grid-header-wrap').find("td[kendogridfiltercell] input", "ng-reflect-name": "common.assetName")}
        nameFilterXicon { nameFilter.next("span.component-action-clear-filter")}
        assetClassFilter {$('div.k-grid-header-wrap').find("td[kendogridfiltercell] input", "ng-reflect-name": "common.assetClass")}
        assetClassFilterXicon { assetClassFilter.next("span.component-action-clear-filter")}
        descriptionFilter { $('div.k-grid-header-wrap').find("td[kendogridfiltercell] input", "ng-reflect-name": "common.description")}
        descriptionFilterXicon { descriptionFilter.next("span.component-action-clear-filter")}
        environmentFilter { $('div.k-grid-header-wrap').find("td[kendogridfiltercell] input", "ng-reflect-name": "common.environment")}
        environmentFilterXicon { environmentFilter.next("span.component-action-clear-filter")}
        allFilterXIcons { $('td[kendogridfiltercell] span.component-action-clear-filter')}
        nameColumn(required:false) { gridHeader.find("thead tr th label.text-delimited", text: contains("Name")).parent("a.k-link")}
        descColumn(required:false) { gridHeader.find("thead tr th label.text-delimited", text: contains("Description")).parent("a.k-link")}
        refreshBtn {$("div", class:"component-action-reload").find("span", class:"glyphicon-refresh")}
        assetNames {$(".asset-detail-name-column")}
        rows {$("[kendogridtablebody]")[1]}
        noRecords {$("div.grid-message label")}
        assetsDisplayedInPager {$("kendo-pager-info")}
        paginationSizes {$("kendo-pager-page-sizes select")}
        selectedAssets {$('.selected-assets')}
        gridHeader {$(".k-grid-header")}
        editAssetButtons { $("button", title: "Edit Asset")}
    }

    def getRandomAssetDataAndClickOnIt(){
        //waitFor asset details to be displayed
        commonsModule.waitForLoader(5)
        def dataList = []
        def assetIndex =Math.abs(Math.min(new Random().nextInt(10),new Random().nextInt() % assetNames.size()))
        def assetName =assetNames[assetIndex].text()
        dataList.add(assetName)
        def rowData =getRowData(assetIndex)
        dataList.addAll(rowData)
        interact {
            moveToElement(assetNames[assetIndex])
        }
        assetNames[assetIndex].click()
        dataList
    }
    /**
     * Clicks on THE FIRST asset with that name
     * @param name
     * @return
     */
    def openAssetByName(name){
        nameFilter = name
        // verify exact match and no other was found with same name
        // otherwise we can click in other view than is required
        def links = assetNames.findAll { it.text() == name }
        waitFor{ links[0].click() }
    }

    def openFirstAssetDisplayed(){
        waitFor{ assetNames[0].click() }
    }

    def clickOnEditButtonForFirstAssetDisplayed(){
        waitFor{editAssetButtons[0].click()}
    }

    def openRandomAssetDisplayed(){
        waitFor{ assetNames[0].displayed }
        def asset = CommonActions.getRandomOption(assetNames)
        commonsModule.goToElement asset
        asset.click()
    }

    /**
     * saves the text of a row in a list so it can be validated later
     * @param rowIndex
     */
    def getRowData(int rowIndex){
        def assetRowDataDisplayed = rows.find("tr")[rowIndex].find("td")
        def assetRowData = []
        assetRowDataDisplayed.each {
            assetRowData.add(it.text())
        }
        assetRowData
    }

    def getViewName(){
        voidStars[0].parent().parent().next().text()
    }

    def verifyViewTitle(title) {
        waitFor{sectionHeaderTitle.text().trim() == title}
    }

    def waitForDisplayedModalContainer(){
        waitFor{exportModalContainer.isDisplayed()}
    }

    def clickViewManagerBreadCrumb(){
        waitFor{viewMgrBreadCrumb.click()}
    }

    def waitForHiddenModalContainer(){
        waitFor{!exportModalContainer.isDisplayed()}
    }

    def clickOnExportViewButton() {
        waitFor{exportViewButton.click()}
    }

    def clickOnExportModalButton() {
        waitFor{exportModalButton.click()}
    }

    def clickOnCancelModalButton() {
        waitFor{cancelModalButton.click()}
    }

    def setExportFileName(name) {
        fileNameField = name
    }

    def validateStarIsOff(){
        starOff.displayed
    }

    def setViewAsFavorite() {
        starOff.click()
    }

    def clickOnGear(){
        waitFor{gearBtn.click()}
    }

    /*
    Verifies if any element from a given list contains the "TBD" text.
    If you check the "Just Planning" checkbox, then no elements with TBD bundles should be displayed.
    This method is used for that purpose.
    Parameter: dropdownItems(set on the AllPagesSpec page).
     */
    def searchTBD (dropdownItems) {
        def numberItemsFloat = Math.ceil(getTotalNumberOfAssetsFromBottomPager() as Float)
        def amountOfIterations = Math.ceil(numberItemsFloat/dropdownItems)
        def flag=false
        for(int j =1 ; j<=amountOfIterations ; j++ ){
            waitFor {view.displayed}
            waitFor{leftTableElements.displayed}
            //We create a list with the elements on the Bundle column every time the table is refreshed
            def bundleData = $("div", class:"k-grid-content").find("td", "aria-colindex":"8")
            for(int i=0; i<bundleData.size();i++){
                commonsModule.goToElement bundleData[i]
                if(bundleData[i].text().equals("TBD")){
                    flag=true
                    break
                }
            }
            if(j!=amountOfIterations)
                commonsModule.goToTargetKendoGridPage "next"
        }
        flag
    }

    def checkedItems(checked = true){
        goToBulkChangeButton()
        allItemsCheckbox.each{
            commonsModule.goToElement it
            assert checked ? it.value() : !it.value() // if checked true assert value true so are checked else unchecked
        }
        true // return true to avoid failure in spoke verification, if fails it will be in above assert
    }

    def checkJustPlanning(){
        if(!justPlanningCheck.jquery.prop('checked')){
            clickOnJustPlanningCheckbox()
        }
    }

    def unCheckJustPlanning(){
        if(justPlanningCheck.jquery.prop('checked')){
            clickOnJustPlanningCheckbox()
        }
    }

    def checkAllItems(){
        def isChecked = getCheckedInputStatus(selectAllChecks)
        def isIndeterminate = getSelectIndeterminateState()
        if(!isChecked && !isIndeterminate){
            clickOnAllAssetsAndWait() // from uncheck to check all
        } else if (isIndeterminate) {
            clickOnAllAssetsAndWait(false) // from indeterminate to uncheck
            clickOnAllAssetsAndWait() // from uncheck to check all
        }
    }

    def unCheckAllItems(){
        def isChecked = getCheckedInputStatus(selectAllChecks)
        def isIndeterminate = getSelectIndeterminateState()
        if(isChecked && !isIndeterminate){
            clickOnAllAssetsAndWait() // from check all to indeterminate
            clickOnAllAssetsAndWait(false) // from indeterminate to uncheck
        } else if (isIndeterminate) {
            clickOnAllAssetsAndWait(false) // from indeterminate to uncheck
        }
    }

    def checkIndetermitateItems(){
        def isChecked = getCheckedInputStatus(selectAllChecks)
        def isIndeterminate = getSelectIndeterminateState()
        if(!isChecked && !isIndeterminate){
            clickOnAllAssetsAndWait() // from uncheck to check all
            clickOnAllAssetsAndWait() // from check all to indeterminate
        } else if (isChecked) {
            clickOnAllAssetsAndWait() // from check all to indeterminate
        }
    }

    def clickOnAllAssetsAndWait(isSelectAllMessageDisplayed = true){
        clickOnSelectAllAssets()
        if (!isSelectAllMessageDisplayed) {
            !verifySelectedAssetsTextDisplayed()
        } else {
            verifySelectedAssetsTextDisplayed()
        }

    }

    def clickOnJustPlanningCheckbox(){
        waitFor{justPlanningCheck.click()}
        commonsModule.waitForLoader 2
    }

    def clickOnSelectAllAssets(){
        waitFor{selectAllChecks.click()}
    }

    def getSelectIndeterminateState(){
        selectAllChecks.jquery.prop('indeterminate')
    }

    def goToBulkChangeButton(){
        commonsModule.goToElement bulkChangeButton // trick to avoid stale element
    }

    def waitForBulkChangeButtonDisplayed(){
        goToBulkChangeButton()
        waitFor{bulkChangeButton.displayed}
    }

    def waitForBulkChangeButtonDisabled(){
        goToBulkChangeButton()
        waitFor{bulkChangeButton.@disabled == "true"}
    }

    def filterByAssetClass(text){
        waitFor{assetClassFilter.displayed}
        assetClassFilter = text
        commonsModule.waitForLoader 2
    }

    def filterByDescription(text){
        waitFor{descriptionFilter.displayed}
        descriptionFilter = text
        commonsModule.waitForLoader 2
    }

    def filterByEnvironment(text){
        waitFor{environmentFilter.displayed}
        environmentFilter = text
        commonsModule.waitForLoader 2
    }

    def filterByName(text){
        waitFor{nameFilter.displayed}
        nameFilter = text
        commonsModule.waitForLoader 2
    }

    def clearAllAppliedFilters(){
        allFilterXIcons.each{
            it.click()
            commonsModule.waitForLoader 2
        }
    }

    def getFilterAssetClassText(){
        assetClassFilter.value()
    }

    def getFilterDescriptionText(){
        descriptionFilter.value()
    }

    def getFilterEnvironmentText(){
        environmentFilter.value()
    }

	/**
	* Method gets random assets from passed param, selects them if there are more than one asset found and returns
	* a list of names of selected assets displayed in name column (list = one or more asset names)
	* @param numberOfAssetsToBeSelected = int
	* @author Sebastian Bigatton
	*/
    def selectRandomAssetsAndGetNames(numberOfAssetsToBeSelected){
        def assetNames = []
        if (!commonsModule.isListOfElements(allItemsCheckbox)) { // if not list just click on selector
            clickOnAssetCheckbox allItemsCheckbox
            assetNames.add getAssetNameFromGivenCheckboxSelector(allItemsCheckbox)
        } else { // else get random number of assets, click on them and add names to the list
            // validates available checkboxes are more than passed number, otherwise selects displayed
            def maxNumberToSelect = allItemsCheckbox.size() < numberOfAssetsToBeSelected ? allItemsCheckbox.size() : numberOfAssetsToBeSelected
            def checkboxes = CommonActions.getRandomOptions allItemsCheckbox, maxNumberToSelect
            def checkbox
            checkboxes.each { input ->
                checkbox = allItemsCheckbox.find{it.attr("name") == input.jquery.prop("name")}
                clickOnAssetCheckbox checkbox
                assetNames.add getAssetNameFromGivenCheckboxSelector(checkbox)
            }
        }
        assetNames
    }

    def clickOnAssetCheckbox(checkboxSelector){
        goToBulkChangeButton()
        commonsModule.goToElement checkboxSelector
        gridHeader.jquery.removeClass("k-grid-dynamic-header") // set to top avoid getting inside the checkbox
        waitFor{checkboxSelector.click()}
        waitFor{getCheckedInputStatus(checkboxSelector) == true} // verify its checked
    }

    def getCheckedInputStatus(element){
        element.jquery.prop('checked') // returns true or false
    }

    def getAssetNameFromGivenCheckboxSelector(element){
        element.parent().parent().next().find("span.asset-detail-name-column").text()
    }

    def clickOnBulkChangeButton(){
        waitForBulkChangeButtonDisplayed()
        waitFor{bulkChangeButton.click()}
    }

    /**
     * Method gets a list of checked assets in page, iterates in passed names and compares them
     * with checked assets names
     * @param names = string array list with asset name(s)
     * @author Sebastian Bigatton
     */
    def verifyCheckedInputAssetsByName(names){
        def checkedAssetsInput = getAllCheckedInputs()
        def isChecked = false
        names.each{ assetName ->
            // compare and find displayed name with passed name
            if (checkedAssetsInput.find{getAssetNameFromGivenCheckboxSelector(it) == assetName}){
                isChecked = true
            }
            assert isChecked
        }
        true // to avoid spoke step fails
    }

    def getAllCheckedInputs(){
        allItemsCheckbox.findAll{getCheckedInputStatus(it) == true} // returns list checked input elements
    }

    /**
     * Method gets a checkbox name identifier and returns a map of associated asset name and checkbox name
     * @param names = string array list with asset name(s)
     * @author Sebastian Bigatton
     */
    def getAllCheckedInputNameMap(names){
        def checkBoxNameMap = [:]
        def checkbox
        names.each{ assetName ->
            // find displayed name with passed name of checked inputs
            checkbox = getAllCheckedInputs().find{getAssetNameFromGivenCheckboxSelector(it) == assetName}
            checkBoxNameMap.put(assetName, checkbox.attr("name"))
        }
        checkBoxNameMap
    }

    /**
     * Method verifies input checkbox with given name attribute associated to the asset by a map [assetName: checkboxNameAttr]
     * is not displayed, this is because can be more than one asset with same name
     * @param assetMap = map [assetName: checkboxNameAttr]
     * @author Sebastian Bigatton
     */
    def verifyDeletedAssetsByCheckboxName(assetsMap){
        assetsMap.each{ assetName, checkboxName ->
            filterByName assetName
            try {
                // wait until half seconds to get no records text displayed avoiding to wait default secs because
                // if no records its possible that another asset with same name is displayed
                // by clone asset same name spec, in that case catch failure and check by checkbox id
                assert waitFor(0.5){noRecords.displayed}
            } catch (WaitTimeoutException e) {
                assert !allItemsCheckbox.find{it.attr("name") == checkboxName}
            }
            clearAllAppliedFilters()
        }
    }

    def verifyIfNoRecordsDisplayed(text){
        if (commonsModule.verifyElementDisplayed($("div.grid-message label"))) {
            verifyNoRecordsText(text)
            clearAllAppliedFilters()
        }
        true
    }

    def verifyNoRecordsText(text){
        waitFor{noRecords.text() == text}
    }

    def addColumnByName(name){
        clickOnGear()
        createViewModule.clickSpecificCheckbox name
        createViewModule.clickPreview()
        commonsModule.waitForLoader 5
        createViewModule.clickOnCloseViewEdition()
        !commonsModule.verifyElementDisplayed($('#tab_2'))
    }

    /**
     * Method gets and returns a list of tags displayed in grid associated to the asset.
     * @param checkboxName = checkbox Name Attr associated to the tag to find in asset row
     * @author Sebastian Bigatton
     */
    def getDisplayedTagsByAssetCheckbox(checkboxName){
        def displayed = []
        def checkbox = $("input[name=$checkboxName]")
        def rowIndex = checkbox.closest("tr[kendogridlogicalrow]").attr("data-kendo-grid-item-index")
        def tagsRow = checkbox.closest("kendo-grid-list")find("tr", "data-kendo-grid-item-index":rowIndex)
        def tagsDisplayedInColumn = tagsRow.find("span.tag")
        if (!commonsModule.isListOfElements(tagsDisplayedInColumn)){
            displayed.add tagsDisplayedInColumn.text()
        } else {
            tagsDisplayedInColumn.each{
                displayed.add it.text()
            }
        }
        displayed
    }

    /**
     * Method iterates thru assets map [name: checkbox name attr identifier] and tag name list
     * to verify they were successfylly saved and displayed in grid
     * @param assetsMap = [asset name: checkbox name attr identifier]
     * @param tagNames = list of tag names to verify
     * @author Sebastian Bigatton
     */
    def verifyDisplayedTagsByAsset(assetsMap, tagNames){
        assetsMap.each{
            def displayedTags
            filterByName it.key // key = assetName
            displayedTags = getDisplayedTagsByAssetCheckbox(it.value) // value = checkbox name attr identifier
            tagNames.each{
                assert displayedTags.find{it} != null
            }
        }
        true
    }

    def getTotalNumberOfAssetsFromBottomPager(){
        commonsModule.goToElement itemNumberDesc
        def pagerText = itemNumberDesc.text()
        def filteredTotal = pagerText.substring(pagerText.indexOf("of ") + 3, pagerText.indexOf(" items"))
        filteredTotal.toInteger()
    }

    def getPaginationSelectValue(){
        commonsModule.goToElement paginationSizes
        paginationSizes.value().toInteger()
    }

    def getSelectedAssetsText(){
        goToBulkChangeButton()
        selectedAssets.find("span").text() + " " + selectedAssets.find("label").text()
    }

    def verifySelectedAssetsText(selectedCount){
        goToBulkChangeButton()
        getSelectedAssetsText() == selectedCount + " Asset(s) selected"
    }

    def verifySelectedAssetsTextDisplayed(){
        goToBulkChangeButton()
        commonsModule.verifyElementDisplayed($('.selected-assets'))
    }

    def getFirstElementNameText(){
        goToBulkChangeButton()
        waitFor{firstElementName.displayed}
        firstElementName.text()
    }

    def getFirstElementClassText(){
        goToBulkChangeButton()
        waitFor{firstElementAssetClass.displayed}
        firstElementAssetClass.text()
    }

    def clickOnCreateButton(){
        createButton.click()
    }
}