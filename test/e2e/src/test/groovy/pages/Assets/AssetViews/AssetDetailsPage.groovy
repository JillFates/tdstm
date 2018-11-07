package pages.Assets.AssetViews

import geb.Page
import modules.CommonsModule

/**
 * This class represents the new generic asset details
 * page
 * @author ingrid
 */
class AssetDetailsPage extends Page{
    static at = {
        assetDetailModal.displayed
        modalTitle.text().contains("Detail")
        editButton.present
        closeButton.present
    }
    static content = {
        assetDetailModal { $("div.tds-angular-component-content")}
        modalTitle { assetDetailModal.find(".modal-title")}
        editButton { assetDetailModal.find("button span.glyphicon-pencil")}
        closeButton { assetDetailModal.find("button span.glyphicon-ban-circle")}
        adModalAssetName {$('td.label.assetName').next()}
        adModalLastUpdated {$(".last-updated")}
        tags { assetDetailModal.find("span.tag")}
        commonsModule { module CommonsModule}
        commentsContent { assetDetailModal.find('.comment-content')}
        addCommentsButton(wait: true) { commentsContent.find('button.btn-add')}
        commentsSectionTitle(wait: true) { commentsContent.find('label.task')}
        commentRows(wait: true) { commentsContent.find('kendo-grid-list tr[kendogridlogicalrow]')}
        adModalAppName                  { assetDetailModal.find("td.assetName").next()}
        adModalDescription              { assetDetailModal.find("td.description").next()}
        adModalSME1                     { assetDetailModal.find("td.sme").next()}
        adModalSME2                     { assetDetailModal.find("td.sme2").next()}
        adModalAppOwner                 { assetDetailModal.find("td.appOwner").next()}
    }

    def validateDataIsPresent(List rowData, List dataDisplayed){
        def success=false
        rowData.each { data ->
            if(data != "") {
                if(dataDisplayed.find {it.contains(data)}){
                    success = true
                }
                assert success, "$data was not found in the details page."
            }
        }
        true
    }

    def getContent(){
        def screenData =$(".valueNW")
        def screenText=[]
        screenText.add($('td.label.assetName.O').next().text())
        screenData.each{
            screenText.add(it.text().trim())
        }
        screenText.add(getLastUpdated())
        screenText
    }
    def getLastUpdated(){
        adModalLastUpdated.text().split(" ")[2]+ (" ")+adModalLastUpdated.text().split(" ")[3]+ (" ")+adModalLastUpdated.text().split(" ")[4]
    }
    def getName(){
        adModalAssetName.text()
    }

    def clickOnEditButton(){
        waitFor {editButton.click()}
    }

    def clickOnCloseButton(){
        waitFor {closeButton.click()}
    }

    def getTagNames(){
        def tagsDisplayed = []
        waitFor(5){tags.size() > 1}
        tags.each {
            tagsDisplayed.add it.text()
        }
        tagsDisplayed
    }

    def verifyTagNamesDisplayed(tagsNameList){
        def tagsDisplayed = getTagNames()
        def found = false
        tagsNameList.each { tag ->
            if (tagsDisplayed.find {it == tag}) {
                found = true
            }
            assert found, "$tag was not found in the tags list"
        }
        true // assertion is inside iteration, just prevent this break
    }

    def clickOnAddComments(){
        commonsModule.goToElement addCommentsButton
        waitFor{addCommentsButton.click()}
        commonsModule.waitForLoader(5)
    }

    def getCommentsCount(){
        commonsModule.goToElement commentsSectionTitle
        def text = commentsSectionTitle.text()
        // text should be Comments ([number]) so substring and remove "(" and ")"
        text.substring("Comments ".length(), text.length()).replace("(","").replace(")","").toInteger()
    }

    def clickOnAddedComment(index){
        commonsModule.goToElement commentRows[index]
        commentRows[index].find("td", "aria-colindex":"2").find("label").click()
    }

    def verifyAddedCommentText(comment, index){
        commonsModule.goToElement commentRows[index]
        commentRows[index].find("td", "aria-colindex":"2").find("label").text() == comment
    }

    def verifyAddedCommentCategory(category, index){
        commonsModule.goToElement commentRows[index]
        commentRows[index].find("td", "aria-colindex":"3").find("div").text() == category
    }

    def verifyCommentsCount(beforeCount){
        getCommentsCount() == beforeCount + 1
    }

    def closeDetailsModal(){
        waitFor {adModalCloseBtn.click()}
        waitFor {!adModalWindow.displayed}
    }

    def clickOnCloneButton(){
        waitFor { adModalCloneBtn.click() }
    }

    def getApplicationName(){
        adModalAppName.text().trim()
    }
}