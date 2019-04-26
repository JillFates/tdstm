package com.tdssrc.grails


import com.tdsops.tm.enums.domain.AssetCommentStatus
import grails.util.Holders
import org.apache.commons.validator.UrlValidator
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

import javax.servlet.http.HttpServletRequest
import org.apache.commons.lang.StringEscapeUtils

/**
 * The HtmlUtil class contains method to generate HTML from server side e.g. Select Box
 */
class HtmlUtil {
	//260309 @tavo_luna: Changing static call initialization to SINGLETON-LAZY loading (faster startup)
	static private ApplicationTagLib TAG_LIB = null

	static private ApplicationTagLib getTagLib(){
		if(!TAG_LIB) TAG_LIB = Holders.applicationContext.getBean('org.grails.plugins.web.taglib.ApplicationTagLib')
		return TAG_LIB
	}

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

	static generateSelect(params) {
		def optionKey = params.optionKey
		def optionValue = params.optionValue
		def optionSelected = params.optionSelected
		def selectClass = params.selectClass ? """class="${params.selectClass}" """ : ''
		def html = new StringBuilder("""<select id="${params.selectId}" name="${params.selectName}" ${selectClass} ${params.javascript ?:''}>""")
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
	static actionButton(label, icon, id, onclick, tooltipText, href='javascript:') {
		String name = label.toLowerCase().replace(' ', '').replace('.','')
		String buttonId = name + "_button_" + id
		String labelId = name + "_text_" + id
		String tooltip = tooltipText? "data-toggle='popover' data-trigger='hover' data-content='${tooltipText}'":''
		return """<a id="${buttonId}" href="${href}" class="task_action ui-button ui-widget ui-state-default ui-corner-all ui-button-text-icon-primary task_action btn_${name}"
			onclick="${onclick}">
			<span class="ui-button-icon-primary ui-icon ${icon} task_icon"></span>
			<span id="${labelId}" ${tooltip} class="ui-button-text task_button">${label}</span>
			</a>"""
	}


	/**
	 * Used to determine the CSS class name that should be used when presenting a task, which is based on the task's status
	 * @param status
	 * @return String The appropriate CSS style or task_na if the status is invalid
	 */
	//TODO : Overriding the service method here since facing some issue to inject service class here,  need to clean up ASAP in HtmlUilt
	static getCssClassForStatus( status ) {
		String css = 'task_na'

		if (AssetCommentStatus.list.contains(status)) {
			css = "task_${status.toLowerCase()}"
		}
		// log.error "getCssClassForStatus('${status})=${css}"
		return css
	}

	/**
	 * Access the remote IP address from the web request or the X-Forwarded-For header content. If the request is
	 * unavailable then 'IP.Unknown' is returned.
	 * @param request - the HttpRequest object which if null is then attempted to be looked up
	 * @return String The remote IP address that made the web request
	 */
 	static String getRemoteIp(HttpServletRequest request = null) {

 		if (!request) {
			request = ((ServletRequestAttributes)RequestContextHolder.requestAttributes)?.request
 		}

		if (!request) {
			return 'Unknown'
		}

	   String remoteIp = request.getHeader("X-Forwarded-For")
		if (!remoteIp) {
			remoteIp = request.getRemoteAddr()
		}
		return remoteIp
	}

	/**
	 * Used to create a Grails formed URL using the resource tablib
	 * @param Map parameters used by the g:createLink tag
	 * @return String URL
	 */
	static String createLink(map) {
		String link =  getTagLib().createLink(map)
		String result = ''
		if (link) {
			result = link.toString()
		} else {
			println "HtmlUtil.createLink() failed for map $map"
		}
		return result
	}


	/**
	 * Creates a link to a given resource.
	 * @param map - parameters to g:resource
	 * @return url as String
	 */
	static String createLinkToResource(map){
		String link = getTagLib().resource(map)
		String resourceUrl = ""
		if(link){
			resourceUrl = link.toString()
		}else{
			println "HtmlUtil.createLinkToResource failed for parameters: $map"
		}
		return resourceUrl
	}

	/**
	* Used to detirmine whether or not a string is in URL format
	* @param input to be checked for correct format
	* @return boolean value for whether or not the string is in URL format
	*/
	static boolean isURL(String input) {
		input = input?.trim()
		if (input) {
			String[] schemes = ['HTTP', 'http','HTTPS', 'https', 'FTP', 'ftp', 'FTPS', 'ftps', 'SMB', 'smb', 'FILE', 'file'].toArray()
			UrlValidator urlValidator = new UrlValidator(schemes)

			return urlValidator.isValid(input)
		}
		return false
	}

	static boolean isMarkupURL(String input){
		boolean  isValidURL = false
		if(input){
			def tokens = input.tokenize('|')
			String url = tokens.size() > 1 ? tokens[1] : tokens[0]
			isValidURL = isURL(url)
		}
		return isValidURL
	}

	static List parseMarkupURL(String input, String defaultLabel = ""){
		String url = ""
		String label = ""
		def result = null
		// If input isn't null
		if(input){
			// If input has a valid URL
			if(isMarkupURL(input)){
				def tokens = input.tokenize('|')
				def urlTokenPosition = 1
				if(tokens.size() > 1){
					label = tokens[0]
				}else{
					urlTokenPosition = 0
					label = defaultLabel
				}
				result = [label, tokens[urlTokenPosition]]
			}

		}
		return result
	}

	/**
	 * Used to create a Grails resource
	 * @param map of the parameters
	 * @return A string representing the resource as HTML resource
	 */
	static String resource(Map map) {
		getTagLib().resource(map).toString()
	}

	/**
	 * Used to create a UL list from an array list
	 * @param List to be converted to HTML
	 * @return The HTML of an UL list
	 */
	static String asUL(List list) {
		StringBuilder text = new StringBuilder('<ul>')
		list.each { text.append( '<li>' + it)}
		text.append('</ul>')
		return text.toString()
	}

	/**
	 * Used to construct an application URL to be used in the application
	 * @param controller - name of the controller to access
	 * @param action - optional action to add to the
	 * @param fragment - any addition content to append to the URI
	 * @param absolute - boolean flag if true will create
	 * @return The constructed URI or URL if absolute is true
	 */
	static String appUrl(String controller, String action='', String fragment='', Boolean absolute=false) {
		'/tdstm/app#' + controller + (action ?: '/' + action) + fragment
	}

	/**
	 * Used return text as safe HTML
	 * @param text
	 * @return text that has been encoded for HTML or blank if the text is NULL
	 */
	static String safe(Object text) {
		if (text) {
			return (text instanceof CharSequence) ? StringEscapeUtils.escapeHtml(text.toString()) : "$text"
		} else {
			return ''
		}
	}
}
