package com.tdssrc.grails

import com.tdsops.tm.enums.domain.AssetCommentStatus
import org.codehaus.groovy.grails.web.util.WebUtils
import org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib

/**
 * The HtmlUtil class contains method to generate HTML from server side e.g. Select Box
 */
class HtmlUtil {
	def static final g = new org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib()

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
			def optionClass = params.containsKey('optionClass') ? "class ='${getCssClassForStatus(it)}' ": ''
			html.append("""<option value="${key}" ${selected} ${optionClass}>${value}</option>""")
		}
		html.append('</select>')
	 
		return html.toString()
	}
	
	/**
	 * Generate action button in action.
	 * @param label - text that appears in button
	 * @param icon - CSS icon name to use
	 * @param id - the task/asset Id number used to embed into the IDs
	 * @param onClick - javascript to embed into the onclick event
	 * @return String - HTML for the button
	 */
	def public static actionButton(label, icon, id, onclick, def href='javascript:') {
		def name = label.toLowerCase().replace(' ', '').replace('.','')
		def buttonId = name + "_button_" + id
		def labelId = name + "_text_" + id
		return """<a id="${buttonId}" href="${href}" class="task_action ui-button ui-widget ui-state-default ui-corner-all ui-button-text-icon-primary task_action"
			onclick="${onclick}">
			<span class="ui-button-icon-primary ui-icon ${icon} task_icon"></span>
			<span id="${labelId}" class="ui-button-text task_button">${label}</span>
			</a>"""
	}
	
	 
	/**
	 * Used to determine the CSS class name that should be used when presenting a task, which is based on the task's status
	 * @param status
	 * @return String The appropriate CSS style or task_na if the status is invalid
	 */
	//TODO : Overriding the service method here since facing some issue to inject service class here,  need to clean up ASAP in HtmlUilt
	def public static getCssClassForStatus( status ) {
		def css = 'task_na'
		
		if (AssetCommentStatus.list.contains(status)) {
			css = "task_${status.toLowerCase()}"
		}
		// log.error "getCssClassForStatus('${status})=${css}"
		return css
	}
	
	/**
	 * Access the remote IP address from the web request or the X-Forwarded-For header content
	 * @return String The remote IP address that made the web request
	 */
 	public static String getRemoteIp() {
		def webUtils = WebUtils.retrieveGrailsWebRequest()

		//Getting the Request object
		def request = webUtils.getCurrentRequest()
		
		// Now try and figure out the IP 
		def remoteIp = request.getHeader("X-Forwarded-For")
		if (remoteIp) {
			remoteIp = "X-Forwarded-For: ${remoteIp}"
		} else {
			remoteIp = request.getRemoteAddr()
		}
		
		return remoteIp.toString()
	}

	/**
	 * Used to create a Grails formed URL using the resource tablib
	 * @param Map parameters used by the g:createLink tag
	 * @return String URL
	 */
	def public static createLink(map) {
		g.createLink(map).toString()
	}
}