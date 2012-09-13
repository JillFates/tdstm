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
	
	def public static genHtmlSelect(def paramMap){
		def jsEvent = paramMap.jsEvent ? paramMap.jsEvent : ""
		def select = new StringBuffer("<select id=\"${paramMap.selectId}\" name=\"${paramMap.selectName}\"  ${jsEvent}>")
		if(paramMap.noSelectionString){
			select.append("<option value=\"${paramMap.noSelectionString.key}\">${paramMap.noSelectionString.value}</option>")
		}
		paramMap.from.each(){
			def key = paramMap.optionKey ? it."${paramMap.optionKey}" : it
			def value = paramMap.optionValue ? it."${paramMap.optionValue}" : it
			def selected = key == paramMap.selection ? 'selected="selected"' : ''
			select.append("<option value=\"${key}\" ${selected} >${value}</option>")
		}
		select.append('</select>')
	 
		return  select
	}
	
	/**
	 * Generate action button in action.
	 * @param : obj as object , label as td's label, function as jsEvent
	 * @return String	HTML td
	 */
	def public static genActionButton(obj, label, function, tdId){
		return """<td id="${tdId}" width="8%" nowrap="nowrap"><a class=" task_action ui-button ui-widget ui-state-default ui-corner-all ui-button-text-icon-primary task_action"
					onclick="${function}">
					<span class="ui-button-icon-primary ui-icon ui-icon-check task_icon"></span>
					<span class="ui-button-text task_button">${label}</span>
					</a></td> 
				"""
	}
}
