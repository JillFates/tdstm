package pages.Tags

import geb.Page
import utils.CommonActions

class TagsPage extends Page{
    static at = {
        title == "Manage Tags"
        pageHeaderName.text().trim() == "Manage Tags"
        createBtn.text() == "Create Tag"
        tagsGrid.displayed
    }

    static content = {
        pageHeaderName { $("section", class:"content-header").find("h1")}
        createBtn { $('button.k-grid-add-command')}
        tagsGrid {$('div[role=grid]')}
        tagsGridRows {tagsGrid.find(".k-grid-container tr[kendogridlogicalrow]")}
        nameFilter { tagsGrid.find("td[kendogridfiltercell]", "aria-colindex": "2").find("input")}
        //First Element of the Providers Table
        firstTagName { tagsGridRows.find("td", "aria-colindex": "2")}
        firstTagDesc { tagsGridRows.find("td", "aria-colindex": "3")}
        firstTagColor { tagsGridRows.find("td", "aria-colindex": "4").find("span.tag")}
    }

    def filterByName(name){
        waitFor{nameFilter.displayed}
        nameFilter = name
    }

    def getGridRowsSize(){
        tagsGridRows.size()
    }

    def getTagNameText(){
        firstTagName.text().trim()
    }

    def getTagDescriptionText(){
        firstTagDesc.text().trim()
    }

    static commonActions = new CommonActions()

    def getTagColorHexText(){
        def rgb = firstTagColor.jquery.css("background-color")
        def r = rgb.substring(4, 7).toInteger()
        def g = rgb.substring(9, 12).toInteger()
        def b = rgb.substring(14, 17).toInteger()
        commonActions.convertRgbToHex r,g,b
    }



}