package pages.Admin.Company

import geb.Page

/**
 * @author ingrid
 */
class CompanyEditionPage extends Page{

    static at = {
        title == "Edit Company"
        pageHeaderName.text() == "Edit Company"
        nameField.displayed
        saveButton.displayed
        cancelButton.displayed
    }

    static content = {
        pageHeaderName { $("section", class:"content-header").find("h1")}
        saveButton { $("input.save")}
        cancelButton { $("input.cancel")}
        nameField { $("input#name")}
        commentField { $("textarea[name=comment]")}
        partnerCheck { $("input[name=partner]")}
    }

    def editCompanyName(){
        def words = nameField.value().split(" ")
        if(nameField.value().contains("Edited")){
            if(words.last().equals("Edited")){
                nameField=nameField.value() + " 1"
            }else{
                def newIndex = words.last().toInteger() + 1
                nameField = changeIndex(words, newIndex)
            }
        }else{
            nameField=nameField.value()+ " Edited 1"
        }
        nameField.value()
    }

     def editCompanyComments(){
         def words = commentField.value().split(" ")
         if(commentField.value().contains("Edited")){
             if(words.last().equals("Edited")){
                 commentField=commentField.value() + " 1"
             }else{
                 def newIndex = words.last().toInteger() + 1
                 commentField = changeIndex(words, newIndex)
             }
         }else{
             commentField=commentField.value()+ " Edited 1"
         }
         commentField.value()
     }

    /**
     * Edits the company's name,comment and partner values.
     * @param partner
     * @return
     */
    def editCompany(){
        def editedValues =[]
        editedValues[0] = editCompanyName()
        editedValues[1] = editCompanyComments()
        partnerCheck.click()
        saveButton.click()
        editedValues
    }

    def changeIndex(wordList, newIndex){
        def fullString=""
        int i =0
        while(i<wordList.size()-1){
            fullString=fullString+wordList[i]+ " "
            i++
        }
        return fullString + newIndex
    }
}
