package pages.Projects.AssetFields

import geb.Page
import modules.ProjectsMenuModule
import modules.CommonsModule
import utils.CommonActions

class AssetFieldSettingsPage extends Page {

    static at = {
        pagetitle.text() == "Asset Field Settings"
        editButton.displayed
        filterInput.displayed
        filterButton.displayed
        kendoGridContainer.displayed
    }

    static content = {
        pagetitle { $("section", class:"content-header").find("h1")}
        projectsModule { module ProjectsMenuModule}
        commonsModule { module CommonsModule }
        contentContainer { $("div.field-setting-component")}
        navTabs { contentContainer.find("ul.nav-tabs li a")}
        activeTab { navTabs.parent(".active")}
        activeTabPane { $(".tab-pane.active")}
        editButton { activeTabPane.find("#btnEdit")}
        cancelButton { activeTabPane.find("#btnCancel")}
        saveAllButton { activeTabPane.find("#btnSaveAll")}
        filterInput { activeTabPane.find("input.searchFilterInput")}
        filterButton { activeTabPane.find("#btnFilter")}
        addCustomFieldButton { activeTabPane.find("#btnAddCustom")}
        kendoGridContainer { activeTabPane.find("kendo-grid.field-settings-grid")}
        headers { kendoGridContainer.find(".k-grid-header th")}
        gridRows { kendoGridContainer.find(".k-grid-container tr[kendogridlogicalrow]")}
        firstRow { gridRows.first()}
        minSringControlLenght { $(".modal label[for=minSize]").next().find(".k-numeric-wrap input")}
        maxSringControlLenght { $(".modal label[for=maxSize]").next().find(".k-numeric-wrap input")}
        deleteButtons { gridRows.find("button span.fa-trash")}
    }

    def clickOnEditButton(){
        waitFor{editButton.click()}
        commonsModule.waitForLoader 5
    }

    def clickOnCancelButton(){
        waitFor{cancelButton.click()}
    }

    def clickOnSaveAllButton(){
        waitFor{saveAllButton.click()}
        commonsModule.waitForLoader 5
    }

    def clickOnAddCustomFieldButton(){
        waitFor{addCustomFieldButton.click()}
    }

    def filterByName(name){
        waitFor{filterInput.displayed}
        filterInput = name
        filterButton.click()
    }

    def verifyActiveTabByName(name){
        activeTab.text() == name
    }

    def verifySaveAllButtonState(isDisabled){
        saveAllButton.jquery.prop('disabled') == isDisabled
    }

    def verifyCancelButtonState(isDisabled){
        cancelButton.jquery.prop('disabled') == isDisabled
    }

    /**
     * This method finds a column by the given name (Eg: "Name") and gets the attribute which is the index
     * that then will be used to find the specific cell in the row we need to find/edit associated by the column.
     * @return the Geb cell Object in row by given index.
     * @param: rowIndex = row number displayed in grid
     * @author Sebastian Bigatton
     */
    def getFieldRowCellByIndexAndColumnName(columnName, rowIndex = 0){
        def index = headers.find{it.text().trim() == columnName}.attr("aria-colindex")
        gridRows[rowIndex].find("aria-colindex": index)
    }

    /**
     * Just verifies couple of things required by TM-12431 right after click on Add new Custom field
     * @author Sebastian Bigatton
     */
    def verifyCustomRowStatus(){
        waitFor{firstRow.find("button span.fa-trash").displayed} // verify remove button displayed

        def labelInput = getLabelInput()
        labelInput.singleElement() == js."document.activeElement" // verify label has the focus
        labelInput.hasClass("has-error") // verifies red border

        def fieldCell = getFieldRowCellByIndexAndColumnName "Field"
        def fieldInput = fieldCell.find("input")
        fieldInput.jquery.prop('disabled') == true // verify field name is disabled
        fieldInput.value().contains "custom"
        fieldInput.value() == labelInput.attr("id") // verifies field name displayed associated to label
    }

    /**
     * Need to find the cell this way because column index change when view and edit, on edit an extra column is
     * added to the grid so is not possible to get static index to identify columns
     * @return first row label input cell on edit view
     * @author Sebastian Bigatton
     */
    def getLabelInput() {
        def labelCell = getFieldRowCellByIndexAndColumnName "Label"
        labelCell.find("input")
    }

    def getLabelInputId(){
        getLabelInput().attr("id")
    }

    /**
     * Need to find the cell this way because column index change when view and edit, on edit an extra column is
     * added to the grid so is not possible to get static index to identify columns.
     * Using web element sendkeys method which is valid by GEB because above described issue
     * @return first row label input cell on edit view
     * @author Sebastian Bigatton
     */
    def setLabelName(name){
        def labelInput = getLabelInput()
        labelInput.singleElement().sendKeys name
    }

    /**
     * Sets Default Value using web element sendkeys method which is valid by GEB because of finding cell by index
     * issue explained in setLabelName
     * @author Sebastian Bigatton
     */
    def setDefaultValue(name){
        def defaultCell = getFieldRowCellByIndexAndColumnName "Default Value"
        def defaultInput = defaultCell.find("input")
        defaultInput.singleElement().sendKeys name
    }

    /**
     * Sets Tooltip Help using web element sendkeys method which is valid by GEB because of finding cell by index
     * issue explained in setLabelName
     * @author Sebastian Bigatton
     */
    def setToolTipHelp(name){
        def tooltipCell = getFieldRowCellByIndexAndColumnName "Tooltip Help"
        def tooltipTextArea = tooltipCell.find("textarea")
        tooltipTextArea.singleElement().sendKeys name
    }

    def getSelectedControlValueInEdit(){
        def controlCell = getFieldRowCellByIndexAndColumnName "Control"
        controlCell.find("select").value().substring(3) // value example: 1. String, so substring to fix
    }

    def clickOnControlWheelIcon(){
        def controlCell = getFieldRowCellByIndexAndColumnName "Control"
        controlCell.find(".open-close-dropdown-icon").click()
        commonsModule.waitForDialogModalDisplayed()
    }

    def setMinMaxStringControlLengh(min, max){
        waitFor{
            minSringControlLenght.displayed
            maxSringControlLenght.displayed
        }
        minSringControlLenght = min
        maxSringControlLenght = max
        commonsModule.clickOnButtonDialogModalByText "Ok"
    }

    def selectRandomHighlighting(){
        def highlightingCell = getFieldRowCellByIndexAndColumnName "Highlighting"
        def options = highlightingCell.find("field-settings-imp span")
        def option = CommonActions.getRandomOption options
        option.click()
        option.text() // returns random option selected to verify later
    }

    def clickOnDisplayCheckbox(){
        def displayCell = getFieldRowCellByIndexAndColumnName "Display"
        displayCell.find("input").click()
    }

    def verifyCustomFieldInfo(fieldInfo){
        getFieldRowCellByIndexAndColumnName("Field").text().trim() == fieldInfo.fieldName
        getFieldRowCellByIndexAndColumnName("Label").text().trim() == fieldInfo.label
        getFieldRowCellByIndexAndColumnName("Highlighting").text().trim() == fieldInfo.highlighting
        getFieldRowCellByIndexAndColumnName("Required").text().trim() == fieldInfo.required
        getFieldRowCellByIndexAndColumnName("Display").text().trim() == fieldInfo.display
        getFieldRowCellByIndexAndColumnName("Default Value").text().trim() == fieldInfo.defaultValue
        getFieldRowCellByIndexAndColumnName("Control").text().trim() == fieldInfo.control
        getFieldRowCellByIndexAndColumnName("Tooltip Help").text().trim() == fieldInfo.tooltip
    }

    /**
     * Deletes fields by given min number of fields to avoid deleting or stay in the system
     * @author Sebastian Bigatton
     */
    def deleteFields(minNumberOfFieldsPresent){
        def count = 0
        def cell
        while (deleteButtons.size() > minNumberOfFieldsPresent){
            cell = getFieldRowCellByIndexAndColumnName("Action", count)
            waitFor{cell.find("button span.fa-trash").click()}
            waitFor{cell.find("button span.fa-undo").displayed}
            count = count + 1
        }
        clickOnSaveAllButton()
    }

    def getGridRowsSize(){
        gridRows.size()
    }

    def bulkDelete(minNumberOfFieldsPresent) {
        if(getGridRowsSize() > minNumberOfFieldsPresent) {
            clickOnEditButton()
            deleteFields minNumberOfFieldsPresent
        }
        true // done, just return true to avoid test fails
    }

    def verifyRowsCountDisplayedByGivenNumber(fieldsCount){
        getGridRowsSize() == fieldsCount
    }
}
