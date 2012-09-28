package com.tdssrc.grails

import com.tdsops.tm.enums.domain.AssetCommentStatus

/**
 * The HtmlUtil class contains method to generate HTML from server side e.g. Select Box
 */
class HtmlUtil {
	
	/**
	 * Generate the HTML for a SELECT control based on a map of parameters
	 * @param String selectId - the CSS ID to use for the <SELECT> element
	 * @param String selectName - the name to use for the <SELECT> element
	 * @param List options - a list of Strings or Maps of data to populate the <OPTION> elements
	 * @param String optionKey - indicates the map key used in the <Map>options for the OPTION identifier (required for <Map>options)
	 * @param String optionValue - indicates the map key used in the <Map>options for the OPTION display value (required for <Map>options)
	 * @param String optionSelected - the key value of the OPTION that is the default selected value (optional)
	 * @param Map firstOption - a map containing [value,display] to display first (optional)
	 * @param String javascript - used to inject any javascript code into the <SELECT> element (optional)
	 * @param String selectClass - CSS class name to use (optional)
	 * @return String HTML selectBox
	 */
	
	def public static generateSelect(def params) {
		def optionKey = params.optionKey
		def optionValue = params.optionValue
		def optionSelected = params.optionSelected
		def selectClass = params.selectClass ? """class="${params.selectClass}" """ : ''
		def html = new StringBuffer("""<select id="${params.selectId}" name="${params.selectName}" ${selectClass} ${params.javascript ?:''}>""")
		def selected 
		if (params.firstOption){
			selected = optionSelected == params.firstOption ? 'selected="selected"' : ''			
			html.append("""<option value="${params.firstOption.value}" ${selected}>${params.firstOption.display}</option>""")
		}

		
		params.options.each() {
			def key = optionKey ? it."${optionKey}" : it
			def value = optionValue ? it."${optionValue}" : it
			selected = key == optionSelected ? 'selected="selected"' : ''
			def optionClass = params.containsKey('optionClass') ? "class ='${getCssClassForStatus(it.status)}' ": ''
			html.append("""<option value="${key}" ${selected} ${optionClass}>${value}</option>""")
		}
		html.append('</select>')
	 
		return html.toString()
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
	
	 
	/**
	 * Used to determine the CSS class name that should be used when presenting a task, which is based on the task's status
	 * @param status
	 * @return String The appropriate CSS style or task_na if the status is invalid
	 */
	//TODO : Overriding the service method here since facing some issue to inject service class here,  need to clean up ASAP” in HtmlUilt
	def public static getCssClassForStatus( status ) {
		def css = 'task_na'
		
		if (AssetCommentStatus.list.contains(status)) {
			css = "task_${status.toLowerCase()}"
		}
		// log.error "getCssClassForStatus('${status})=${css}"
		return css
	}
}
