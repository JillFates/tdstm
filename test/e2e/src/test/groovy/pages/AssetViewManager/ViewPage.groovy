package pages.AssetViewManager

import geb.Page
import modules.CommonsModule
import modules.CreateViewModule

class ViewPage extends Page{

    static at = {
        waitFor {view.displayed}
    }
    static content = {
        view (wait:true) { $("section","class":"page-asset-explorer-config")}
        sectionHeaderTitle { $("section.content-header h1")}
        clearBtn {$("button", id:"btnClear")}
        exportViewButton {$("button", id:"btnExport")}
        exportModalContainer {$('#tdsUiDialog')}
        fileNameField {exportModalContainer.find("input", id: "fileName")}
        exportModalButton {exportModalContainer.find("button span", class: "fa-download")}
        cancelModalButton {exportModalContainer.find("button span", class: "glyphicon-ban-circle")}
        commonsModule { module CommonsModule }
        createViewModule {module CreateViewModule}
        viewMgrBreadCrumb {$('a.font-weight-bold')}
        //starOn {$("fa text-yellow fa-star")}
        starOff {$(".fa.text-yellow.fa-star-o")}
        gearBtn {$(".fa-cog")}
        justPlanningCheck(required:false)  { $("input", type: "checkbox" , name: "justPlanning") }
        itemsPerPage(required:false) {$("kendo-pager" , class:"k-pager-wrap k-grid-pager k-widget").find("kendo-pager-page-sizes", class:"k-pager-info k-label")}
        selectAllChecks(required:false) {$("input" , type:"checkbox" , name:"selectAll")}
        itemNumberDesc(required:false) {$("kendo-pager-info" , class:"k-pager-info k-label")}
        nextPageButton(required:false) {$("kendo-pager" , class:"k-pager-wrap k-grid-pager k-widget").find("kendo-pager-next-buttons").find("a", class:"k-link k-pager-nav" , title:"Go to the next page").find("span", class:"k-icon k-i-arrow-e")}
        leftTableElements(required:false) {$("div" , class:"k-grid-content-locked element-height-100-per-i" , role:"presentation")}
        allItemsCheckbox(required:false) {$("label",class:"selectall-checkbox-column text-center").find("input",type:"checkbox", class:"ng-untouched ng-pristine ng-valid")}
        firstElementName(required:false) {$("div", class:"k-grid-content-locked element-height-100-per-i").find("div", role:"presentation").find("table",class:"k-grid-table").find("tbody",role:"presentation").find("tr")[0].find("td")[1]}
        firstElementAssetClass(required:false) {$("div", class:"k-grid-content-locked element-height-100-per-i").find("div", role:"presentation").find("table",class:"k-grid-table").find("tbody",role:"presentation").find("tr")[0].find("td")[2]}
        nameFilter(required:false) {$("div", class:"k-grid-header-locked").find("thead").find("tr","aria-rowindex":"2").find("td","aria-colindex":"2").find("div").find("input",type:"text")}
        nameFilterXicon(required:false) {$("div", class:"k-grid-header-locked").find("thead").find("tr","aria-rowindex":"2").find("td","aria-colindex":"2").find("div").find("span")}
        assetClassFilter(required:false) {$("div", class:"k-grid-header-locked").find("thead").find("tr","aria-rowindex":"2").find("td","aria-colindex":"3").find("div").find("input",type:"text")}
        assetClassFilterXicon(required:false) {$("div", class:"k-grid-header-locked").find("thead").find("tr","aria-rowindex":"2").find("td","aria-colindex":"3").find("div").find("span")}
        nameColumn(required:false) {$("div", class:"k-grid-header-locked").find("thead").find("tr","aria-rowindex":"1").find("th","aria-colindex":"2")}
        descColumn(required:false) {$("div", class:"k-grid-header-wrap").find("thead").find("tr","aria-rowindex":"1").find("th","aria-colindex":"4")}
        refreshBtn {$("div", class:"kendo-grid-toolbar__refresh-btn btnReload").find("span", class:"glyphicon glyphicon-refresh")}
        assetNames {$(".asset-detail-name-column")}
        rows {$("[kendogridtablebody]")[1]}
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
        gearBtn.click()
    }

    def nextPage(){
        interact{
            moveToElement nextPageButton
        }
        waitFor{nextPageButton.click()}
        waitFor {view.displayed}
        return true
    }

    /*
    Verifies if any element from a given list contains the "TBD" text.
    If you check the "Just Planning" checkbox, then no elements with TBD bundles should be displayed.
    This method is used for that purpose.
    Parameter: dropdownItems(set on the AllPagesSpec page).
     */
    def searchTBD (dropdownItems) {
        def splitItemsString = itemNumberDesc.text().split(" ")
        def stringSize = splitItemsString.size()
        def numberItems = splitItemsString[stringSize-2]
        float numberItemsFloat = Math.ceil(numberItems as Float)
        def amountOfIterations = Math.ceil(numberItemsFloat/dropdownItems)
        def flag=false
        for(int j =1 ; j<=amountOfIterations ; j++ ){
            waitFor {view.displayed}
            waitFor{leftTableElements.displayed}
            //We create a list with the elements on the Bundle column every time the table is refreshed
            def bundleData = $("div" , class:"k-grid-content k-virtual-content").find("table" , class:"k-grid-table" , style:"transform: translateY(0px); width: 1699px;").find("tbody", role:"presentation").find("tr").find("aria-colindex":"8")
            for(int i=0; i<bundleData.size();i++){
                if(bundleData[i].text().equals("TBD")){
                    flag=true
                    break
                }
            }
            if(j!=amountOfIterations)
                nextPage()
        }
        return flag
    }

    def checkedItems(){
        def flag = true
        for(int i=0;i<allItemsCheckbox.size();i++){
            if(allItemsCheckbox[i].value()==false){
                flag=false
                break
            }
        }
        return flag
    }

    def checkJustPlanning(){
        if(justPlanningCheck.value()==false)
            waitFor{justPlanningCheck.click()}
    }

    def checkAllItems(){
        if(selectAllChecks.value()==false)
            waitFor{selectAllChecks.click()}
    }
}

