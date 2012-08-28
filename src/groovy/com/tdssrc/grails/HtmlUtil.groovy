package com.tdssrc.grails

/**
 * The HtmlUtil class contains method to generate HTML from server side e.g. Select Box
 */
class HtmlUtil {
	
	/**
	 * Generate HTML selectBox for passed list
	 * @param String	selectBoxId as selectId, selectBoxName as selectName , javascript Event for select as jsEvent,
	 *   				optionKey as optionKey optionValue as optionValue , default selected value as selection, NoSelectionString as noSelectionString
	 * @param List	    list as from
	 * @return String	HTML selectBox
	 */
	
	def public static genHtmlSelect(def selectId, def selectName, def jsEvent, List from, def optionKey, def optionValue, def selection, def noSelectionString){
		def select = new StringBuffer("<select id=\"${selectId}\" name=\"${selectName}\"  ${jsEvent}>")
		if(noSelectionString){
			select.append("<option value=\"\">${noSelectionString}</option>")
		}
		from.each(){
			def key = optionKey ? it."${optionKey}" : it
			def value = optionValue ? it."${optionValue}" : it
			def selected = key == selection ? 'selected="selected"' : ''
			select.append("<option value=\"${key}\" ${selected} >${value}</option>")
		}
		select.append('</select>')
	 
		return  select
	}
}
