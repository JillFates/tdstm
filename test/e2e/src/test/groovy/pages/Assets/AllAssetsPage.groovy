package pages.Assets
import geb.Page
import modules.CommonsModule

class AllAssetsPage extends Page {

    static at = {
        title == "All Assets"
        modaltitle.text() == "All Assets"
    }

    static content = {
        modaltitle(required:false) { $("section", class:"content-header").find("h1")}
        justPlanningCheck  { $("input", type: "checkbox" , name: "justPlanning") }
        itemsPerPage {$("kendo-pager" , class:"k-pager-wrap k-grid-pager k-widget").find("kendo-pager-page-sizes", class:"k-pager-sizes k-label").find("select")}
        selectAllChecks {$("input" , type:"checkbox" , name:"selectAll")}
        itemNumberDesc {$("kendo-pager-info" , class:"k-pager-info k-label")}
        nextPageButton {$("kendo-pager" , class:"k-pager-wrap k-grid-pager k-widget").find("kendo-pager-next-buttons").find("a", class:"k-link k-pager-nav" , title:"Go to the next page").find("span", class:"k-icon k-i-arrow-e")}
        leftTableElements {$("div" , class:"k-grid-content-locked element-height-100-per-i" , role:"presentation")}
        allItemsCheckbox {$("div", class:"checkbox checkbox-grid").find("input",name:"selectAll",type:"checkbox")}
        firstElementName {$("div", class:"k-grid-content-locked element-height-100-per-i").find("div", role:"presentation").find("table",class:"k-grid-table").find("tbody",role:"presentation").find("tr")[0].find("td")[2]}
        firstElementAssetClass {$("div", class:"k-grid-content-locked element-height-100-per-i").find("div", role:"presentation").find("table",class:"k-grid-table").find("tbody",role:"presentation").find("tr")[0].find("td","aria-colindex":"3").find("span")}
        nameFilter {$("div", class:"k-grid-header-locked").find("thead").find("tr","aria-rowindex":"2").find("td","aria-colindex":"2").find("div").find("input",type:"text")}
        nameFilterXicon {$("div", class:"k-grid-header-locked").find("thead").find("tr","aria-rowindex":"2").find("td","aria-colindex":"2").find("div").find("span")}
        assetClassFilter {$("div", class:"k-grid-header-locked").find("thead").find("tr","aria-rowindex":"2").find("td","aria-colindex":"3").find("div").find("input",type:"text")}
        assetClassFilterXicon {$("div", class:"k-grid-header-locked").find("thead").find("tr","aria-rowindex":"2").find("td","aria-colindex":"3").find("div").find("span")}
        clearFiltersBtn {$("button", id:"btnClear")}
        nameColumn {$("div", class:"k-grid-header-locked").find("thead").find("tr","aria-rowindex":"1").find("th","aria-colindex":"2")}
        descColumn {$("div", class:"k-grid-header-wrap").find("thead").find("tr","aria-rowindex":"1").find("th","aria-colindex":"4")}
    }

    def nextPage(){
        interact{
            moveToElement nextPageButton
        }
        waitFor{nextPageButton.click()}
        return true
    }

    /*
    * Note: Returns false if the list does not have the string TBD
    * And true if it does.
    * It looks for the Bundle elements of a table and goes to the next pages as many times as the parameter indicates.
    * Parameter: A number
    */
    def searchTBD (amountIterations) {
        def flag=false
        for(int j =1 ; j<=amountIterations ; j++ ){
            waitFor{leftTableElements.displayed}
            //We create a list with the elements on the Bundle column every time the table is refreshed
            def bundleData = $("div" , class:"k-grid-content k-virtual-content").find("table" , class:"k-grid-table" , style:"transform: translateY(0px); width: 1699px;").find("tbody", role:"presentation").find("tr").find("td","aria-colindex":"8")
            for(int i=0; i<bundleData.size();i++){
                if(bundleData[i].text().equals("TBD")){
                    flag=true
                    break
                }
            }
            if(j!=amountIterations)
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


}
