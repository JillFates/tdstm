import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import net.transitionmanager.exception.InvalidParamException
import com.tdsops.tm.enums.ControlType

class ControlTagLib {

	static String namespace = 'tds'

	// TODO : determine what the CSS class should be for the controls
	// This is the CSS class name to be assigned to all of the control types (presently not being used)
	static final String CONTROL_CSS_CLASS = 'tm-input-control'
	static final String SELECT_REQUIRED_PROMPT = 'Select...'
	static final String MISSING_OPTION_WARNING = 'INVALID'
	static final int MAX_STRING_LENGTH = 255
	static final String EMPTY_IMP_CRIT_FIELD_CSS_CLASS = " highField"
	static final List TOOLTIP_DATA_PLACEMENT_VALUES = ["top", "bottom", "left", "right"]
	static final String MAX_VALIDATION_MESSAGE = 'Value exceeds the maximum {max} characters.'
	static final String EXACTLY_MIN_MAX_VALIDATION_MESSAGE = 'Value must be exactly {min} character(s).'
	static final String BETWEEN_MIN_MAX_VALIDATION_MESSAGE = 'Value must be between {min} and {max} characters.'

	/**
	 * Used for wrapping UI elements when no other ControlTag applies.
	 * This tag deals with adding the tooltip.
	 * @param field - fieldSpec
	 * @param dataPlacement - tooltip placement (optional)
	 * @param class - css class
	 */
	def tooltipSpan = {attrs, body ->
		Map field = attrs.field ?: [:]
		String cssClass = attrs["class"]
		out << "<span "
		out << tooltipAttrib(field, attrs.tooltipDataPlacement)
		out << attribute("class", cssClass)
		out << " >\n"
		out << body()
		out << "\n </span>\n"
	}

	/**
	 * Used to render the LABEL used for an input field
	 * @param field - the Field Specification (Map)
	 */
	def inputLabel = { Map attrs ->
		Map fieldSpec = attrs.field ?: [:]
		if (!fieldSpec) {
			throw new InvalidParamException('<tds:inputLabel> tag requires field=fieldSpec Map')
		}
		StringBuilder sb = new StringBuilder("\n")

		// Build the TD element
		// <td class="label assetName C" nowrap="nowrap">
		sb.append('<td class="label ')
		// TODO : Determine if the fieldName is used in the LABEL class attribute
		sb.append(fieldSpec.field)
		if (fieldSpec.imp) {
			String imp = fieldSpec.imp
			sb.append(" ${imp}")
			// Determines if the imp is I)mportant or C)ritical
			if (imp == "Y" || imp == "G") {
				// Checks if the value for the input was given
				if (attrs.containsKey("value")) {
					// If the value for the input is empty, the label will be red.
					if (attrs.value == null || StringUtil.isBlank(attrs.value.toString())) {
						sb.append(EMPTY_IMP_CRIT_FIELD_CSS_CLASS)
					}
				}
			}
		}
		sb.append('" nowrap="nowrap">')
		sb.append("\n")

		// Build the LABEL element
		// <label for="assetName"><span data-toggle="popover" data-trigger="hover" data-content="Some tip">Name</span></label>
		sb.append('<label for="')
		sb.append(fieldSpec.field)
		sb.append('"')
		sb.append(' >')
		sb.append('<span ')
		sb.append(tooltipAttrib(fieldSpec))
		sb.append(' >')
		sb.append(HtmlUtil.escape(fieldSpec.label))
		if (attrs.containsKey("labelSuffix")){
            sb.append(HtmlUtil.escape(attrs.labelSuffix))
        }
		sb.append('</span>')
		if (fieldSpec.constraints.required) {
			sb.append('<span style="color: red;">*</span>')
		}
		sb.append('</label>')

		// Close out the TD
		sb.append("\n</td>")
		out << sb.toString()
	}

	/**
	 * Creates the cell with the value of a field for displaying in show views.
	 */
	def labelForShowField = { Map attrs ->
		Map fieldSpec = attrs.field ?: [:]
        def fieldValue = attrs.value ?: ""
        def suffixFieldValue = attrs.valueSuffix ?: ""
		StringBuilder sb = new StringBuilder("\n")
		sb.append("<td class='valueNW ${fieldSpec.imp}'>")
		sb.append("<span ")
		sb.append(tooltipAttrib(fieldSpec, attrs.tooltipDataPlacement))
		sb.append(" >")
        sb.append(HtmlUtil.escape( fieldValue + suffixFieldValue ) )
		sb.append("</span>")
		sb.append("</td>")
		out << sb.toString()
	}

	/**
	 * Used to render text if the fieldSpec is required
	 * @param field - the Field Specification (Map)
	 */
	def ifInputRequired = { Map attrs, body ->
		Map fieldSpec = attrs.field ?: [:]
		if (!fieldSpec) {
			throw new InvalidParamException('<tds:ifInputRequired> tag requires field=fieldSpec Map')
		}

		if (fieldSpec.constraints?.required == 1) {
			out << body()
		}
	}

	/**
	 * Used to render any of the supported custom fields input control
	 * @param field - the field spec Map
	 * @param value - the current or default value to populate the control with (optional)
	 * @param size - used to define the HTML size attribute on controls (optional)
	 * @param tabIndex - the tab offset (optional)
	 * @param tabOffset - used to offset the tabIndex values (used by the custom fields)
	 * @example <tds:inputControl field="${fieldSpec} value="${domain.value}" tabOffset="400"/>
	 */
	def inputControl = { Map attrs ->

		// The field Specifications
		Map fieldSpec = attrs.field ?: [:]
		if (!fieldSpec) {
			throw new InvalidParamException('<tds:inputControl> tag requires field=fieldSpec Map')
		}

		// The value that the control should be set to (optional)
		String value = ( attrs.value ?: '' )
		value = (value == null ? '' : value)

		Integer size = NumberUtil.toInteger(attrs.size,null)

		// Get tabIndex from attrib tabIndex, tabindex or tabOffset which if passed will override
		// the order specified in the fieldSpec.
		String tabIndex = ( attrs.tabIndex ?: (attrs.tabindex ?: null))
		String tabOffset = (attrs.tabOffset ?: (attrs.taboffset ?: null ))

		// println "attrs=${attrs.keySet()}"
		// println "tabIndex = $tabIndex; tabOffset=$tabOffset; fieldSpec.order=${fieldSpec.order}"

		// Get bootstrap tooltip data-placement from attrib tooltipDataPlacement
		// This parameter is optional to modify default tooltip positioning
		// Also checks that the value is one of the valid data-placement element values
		String tooltipDataPlacement = attrs.tooltipDataPlacement ?: null
		if (tooltipDataPlacement !=null && !TOOLTIP_DATA_PLACEMENT_VALUES.contains(tooltipDataPlacement)) {
			throw new InvalidParamException('<tds:inputControl> tag optional argument tooltipDataPlacement ' +
					'requires its value to be in ' + TOOLTIP_DATA_PLACEMENT_VALUES)
		}

		switch (fieldSpec.control) {
			case ControlType.LIST.toString():
				out << renderSelectListInput(fieldSpec, value, tabIndex, tabOffset, size, tooltipDataPlacement)
				break

			case ControlType.YES_NO.toString():
				out << renderYesNoInput(fieldSpec, value, tabIndex, tabOffset, size, tooltipDataPlacement)
				break

			case ControlType.STRING.toString():
			default:
				out << renderStringInput(fieldSpec, value, tabIndex, tabOffset, size, tooltipDataPlacement)
		}
	}

	/**
	 * Used to render the label and the value for a field in show views.
	 */
	def showLabelAndField = { Map attrs ->
		out << inputLabel(attrs)
		out << labelForShowField(attrs)

	}

	/**
	 * Used to render the label and the corresponding input in create/edit views.
	 */
	def inputLabelAndField = { Map attrs ->
		out << inputLabel(attrs)
		out << '<td>'
		out << inputControl(attrs)
		out << '</td>'
	}

	/**
	 * Generates a SELECT HTML control based on the field specification and the
	 * parameters passed to the method.
	 * @param fieldSpec - the map of field specifications
	 * @param value - the value to set the control to (optional)
	 * @param tabIndex - the tab order used to override the fieldSpec.order (optional)
	 * @param tooltipDataPlacement - the tooltip data placement value used to override the default placement (optional)
	 * @return the SELECT Component HTML
	 */
	private String renderSelectListInput(Map fieldSpec, String value, String tabIndex, String tabOffset, Integer size, String tooltipDataPlacement) {
		List options = fieldSpec.constraints?.values

		StringBuilder sb = new StringBuilder('<select')
		sb.append(commonAttributes(fieldSpec, value, tabIndex, tabOffset, size, tooltipDataPlacement))
		sb.append('>')

		// Add a Select... option at top if the field is required
		// <option value="" selected>Select...</option>
		boolean isRequiredField = fieldSpec.constraints?.required
		if (isRequiredField) {
			sb.append(selectOption('', value, SELECT_REQUIRED_PROMPT))
		} else {
			// Add a blank option so users can unset a value
			sb.append(selectOption('', value))
		}

		// Check to see if there is some legacy value that doesn't match the select option values.
		// If there no match then it will render the option with a warning. This will give the
		// user a visual indicator that there is an issue. The form submission should error thereby
		// not allowing the user to save until the proper value is selected.
		//
		// <option value="BadData" selected>BadData (INVALID)</option>
		boolean isBlankValue = StringUtil.isBlank(value);
		if (( ! isBlankValue && ! options.contains(value)) ) {
			String warning = "$value ($MISSING_OPTION_WARNING)"
			sb.append(selectOption(value, value, warning))
		}

		// Iterate over the fieldSpec option values to create each of the options
		for (option in options) {
		    if( ! StringUtil.isBlank(option) ) {
		        sb.append(selectOption(option, value))
		    }
		}

		sb.append('</select>')

		sb.toString()
	}

	/**
	 * Generates a String INPUT HTML control based on the field specification and the
	 * parameters passed to the method.
	 * @param fieldSpec - the map of field specifications
	 * @param value - the value to set the control to
	 * @param tabIndex - the tab order used to override the fieldSpec.order (optional)
	 * @param tooltipDataPlacement - the tooltip data placement value used to override the default placement (optional)
	 * @return the INPUT Component HTML
	 */
	private String renderStringInput(Map fieldSpec, String value, String tabIndex, String tabOffset, Integer size, String tooltipDataPlacement) {
		'<input' +
		attribute('type', 'text') +
		commonAttributes(fieldSpec, value, tabIndex, tabOffset, size, tooltipDataPlacement) +
		'/>'
	}

	/**
	 * Generates a SELECT HTML control with Yes/No options
	 * @param fieldSpec - the map of field specifications
	 * @param value - the value to set the control to
	 * @param tabIndex - the tab order used to override the fieldSpec.order (optional)
	 * @param tooltipDataPlacement - the tooltip data placement value used to override the default placement (optional)
	 * @return the INPUT Component HTML
	 */
	private String renderYesNoInput(Map fieldSpec, String value, String tabIndex, String tabOffset, Integer size, String tooltipDataPlacement) {
		List options = []
		List valid = ['Yes', 'No']

		StringBuilder sb = new StringBuilder('<select')
		sb.append(commonAttributes(fieldSpec, value, tabIndex, tabOffset, size, tooltipDataPlacement))
		sb.append(' style="width: 80px;"')
		sb.append('>')

		if (fieldSpec.constraints?.required) {
			// Add a Select... option at top if the field is required
			options << ['', SELECT_REQUIRED_PROMPT]
		} else {
			// Put a blank entry in to allow the user to unset a field
			options << ['', '']
			valid << ''
		}

		// Check to see if there is some legacy value that doesn't match the select option values.
		// If there no match then it will render the option with a warning. This will give the
		// user a visual indicator that there is an issue. The form submission should error thereby
		// not allowing the user to save until the proper value is selected.
		//
		// <option value="BadData" selected>BadData (INVALID)</option>
		boolean isBlankValue = StringUtil.isBlank(value);
		if ( ! isBlankValue && ! valid.contains(value) ) {
			String warning = "$value ($MISSING_OPTION_WARNING)"
			options << [value, warning]
		}

		options << ['Yes', 'Yes']
		options << ['No', 'No']

		// Iterate over the fieldSpec option values to create each of the options
		for (option in options) {
			sb.append(selectOption(option[0], value, option[1]))
		}

		sb.append('</select>')

		sb.toString()
	}

	/**
	 * Used to render the common control attributes that all controls will have
	 * @param fieldSpec - the map of field specifications
	 * @param value - the value to set the control to (optional)
	 * @param tabIndex - the tab order used to override the fieldSpec.order (optional)
	 * @param tooltipDataPlacement - the tooltip data placement value used to override the default placement (optional)
	 * @return the attributes generated in HTML format
	 */
	private String commonAttributes(Map fieldSpec, String value=null, String tabIndex=null, String tabOffset=null, Integer size, String tooltipDataPlacement=null ) {
		idAttrib(fieldSpec) +
		nameAttrib(fieldSpec) +
		valueAttrib(fieldSpec, value) +
		tabIndexAttrib(fieldSpec, tabIndex, tabOffset) +
		classAttrib(fieldSpec) +
		sizeAttrib(size) +
		tooltipAttrib(fieldSpec, tooltipDataPlacement) +
		constraintsAttrib(fieldSpec) +
		dataLabelAttrib(fieldSpec)
	}

	/**
	 * Returns the HTML class attribute with the class for all controllers plus the
	 * importance class if included in the field specification
	 * @param field - the Field specification object
	 * @return The class attribute HTML for controls
	 */
	private String classAttrib(Map fieldSpec) {
		String c = CONTROL_CSS_CLASS
		if (fieldSpec.imp) {
			c += " ${fieldSpec.imp}"
		}
		return attribute('class', c)
	}

	/**
	 * Returns the HTML5 require and min/max appropriately for the field specification control type.
	 * Since minlength validation isn't supported as an standard on all browsers
	 * @param fieldSpec - the Field specification object
	 * @return The required attribute for controls if required otherwise blank
	 * @example   ' required pattern=".{3,}" maxlength="12"'
	 */
	private String constraintsAttrib(Map fieldSpec) {
		StringBuilder sb = new StringBuilder()

		boolean isReq = fieldSpec?.constraints?.required
		if (isReq) {
			sb.append(' required')
		}

		if (fieldSpec?.control in ['', 'String']) {
			Integer min = fieldSpec.constraints?.minSize
			Integer max = fieldSpec.constraints?.maxSize
            // println "min=$min, max=$max"
			if ((min == null || min == 0) && isReq) {
				min=1
			}
			// since minlength validation isn't supported as an standard on all browsers,
			// we need to use pattern for min length TEXT INPUT constraint.
			if (min != null && min > 0) {
				sb.append(" pattern=\".{$min,}\"")
			}

			// Make sure max is set properly
			if (max == null || max > MAX_STRING_LENGTH) {
				max = MAX_STRING_LENGTH
			}
			sb.append(" maxlength=\"$max\"")

			sb.append(validationMessagesAttrib(min, max))
		}

		sb.toString()
	}

	/**
	 * Returns the HTML attributes to show specific validation error messages for min/max constraints.
	 * @param min String min length
	 * @param max String max length
	 * @return Returns the HTML attributes to show specific validation error messages for min/max constraints.
	 */
	private String validationMessagesAttrib(Integer min, Integer max){
		StringBuilder sb = new StringBuilder()
		String validationMessage = new String()

		if (min != null && min > 0) {
			if (min == max) {
				validationMessage = EXACTLY_MIN_MAX_VALIDATION_MESSAGE.replace('{min}', min.toString())
			} else {
				validationMessage = BETWEEN_MIN_MAX_VALIDATION_MESSAGE.replace('{min}', min.toString()).replace('{max}', max.toString())
			}
		} else { // This case potenrially will never happen since html input will never let you enter a value with more than configured max. but will leave here just in case.
			validationMessage = MAX_VALIDATION_MESSAGE.replace('{max}', max.toString())
		}

		sb.append(attribute('oninvalid', 'setCustomValidity(\''+ validationMessage +'\')'))
		sb.append(attribute('oninput', 'try{setCustomValidity(\'\')}catch(e){}'))

		return sb.toString();
	}

	/**
	 * Returns the HTML class attribute with the class for all controllers plus the
	 * importance class if included in the field specification
	 * @param field - the Field specification object
	 * @return The class attribute HTML for controls
	 */
	private String dataLabelAttrib(Map fieldSpec) {
		attribute('data-label', fieldSpec.field)
	}

	/**
	 * Returns the HTML id attribute based on the field specification
	 * @param field - the Field specification object
	 * @return The id attribute HTML for controls
	 */
	private String idAttrib(Map fieldSpec) {
		return attribute('id', fieldSpec?.field)
	}

	/**
	 * Returns the HTML name attribute based on the field specification
	 * @param field - the Field specification object
	 * @return The name attribute HTML for controls
	 */
	private String nameAttrib(Map fieldSpec) {
		return attribute('name', fieldSpec?.field)
	}

	/**
	 * Returns the HTML tabIndex attribute based on the field specification
	 * @param field - the Field specification object
	 * @param tabIndex - the tabindex of the field that if supplied overrides the setting in field spec order property
	 * @param tabOffset - a value that if supplied is added to the tabindex (used by Custom Fields presently)
	 * @return The tabIndex attribute HTML for controls
	 */
	private String tabIndexAttrib(Map fieldSpec, String tabIndex=null, String tabOffset=null) {
		Integer ti = NumberUtil.toInteger(tabIndex, -1)
		if (ti < 1) {
			ti = fieldSpec.order
			if (ti == null) {
				ti = 1
			}
		}

		// Add tab offset to the tabindex value
		Integer to = NumberUtil.toInteger(tabOffset, -1)
		if (to > 0) {
			ti += to
		}

		if (ti > 0) {
			return " tabindex=\"$ti\""
		}
		return ''
	}

	/**
	 * Returns the HTML size attribute based on the field specification
	 * @param field - the Field specification object
	 * @return The title attribute HTML for controls
	 */
	private String sizeAttrib(Integer size) {
		String r=''
		if (size && size > 0) {
			r = " size=\"$size\""
		}
		return r
	}

	/**
	 * Returns the HTML title attribute based on the field specification
	 * @param field - the Field specification object
	 * @return The title attribute HTML for controls
	 */
	private String titleAttrib(Map field) {
		return attribute('title', field?.tip, field?.title)
	}

	/**
	 * Returns the HTML tooltip attributes based on the field specification
	 * Also checks that the tooltipDataPlacement is a valid data-placement element value on Bootstrap
	 * @param field - the Field specification object
	 * @param tooltipDataPlacement - the tooltip data placement value used to override the default placement (optional)
	 * @return The tooltip attributes HTML for controls
	 */
	private String tooltipAttrib(Map field, String tooltipDataPlacement=null ) {

		StringBuilder attrib = new StringBuilder('')
		if(field.tip) {
			if (tooltipDataPlacement && !TOOLTIP_DATA_PLACEMENT_VALUES.contains(tooltipDataPlacement)) {
				throw new InvalidParamException('<tds:inputControl> tag optional argument tooltipDataPlacement ' +
						'requires its value to be in ' + TOOLTIP_DATA_PLACEMENT_VALUES)
			}
			attrib.append(attribute('data-toggle', 'popover'))
			attrib.append(attribute('data-trigger', 'hover'))
			if (tooltipDataPlacement) {
				attrib.append(attribute('data-placement', tooltipDataPlacement))
			}
			attrib.append(attribute('data-content', field?.tip, field?.label))
		}
		return attrib.toString()
	}

	 /**
	 * Returns the HTML value attribute based on the field specification which will
	 * include the value passed in. If the value is not set then the default value from
	 * the fieldSpec will be used.
	 *
	 * Note that the fieldspec.default is NOT used by this function as it needs to be controlled
	 * externally to this logic.
	 *
	 * @param field - the Field specification object
	 * @param value - the value that the input should start with.
	 * @return The value attribute for controls populated with a value appropriately
	 */
	private String valueAttrib(Map field, String value=null) {
		return attribute('value', value)
	}

	/**
	 * Used to construct an attribute name=value HTML structure based on the parameters
	 * @param name - the name of the attribute
	 * @param value - the current value
	 * @param defValue - the value to use if value is empty and defValue is set (default null)
	 * @return the attribute string
	 */
	private String attribute(String name, String value, String defValue=null) {
		String attrib=" $name=\""

		boolean isBlank = StringUtil.isBlank(value)
		if (! isBlank) {
			attrib += HtmlUtil.escape(value)
		} else if (defValue != null) {
			attrib += HtmlUtil.escape(defValue)
		}
		attrib += '"'

		return attrib
	}

	/**
	 * Used to render an OPTION element for a SELECT control
	 * @param option - the option to use
	 * @param value - the current value to set on the select
	 * @param label - an alternate label that user sees, if null then option is used
	 */
	private String selectOption(String option='', String value='', String label='') {
		if (option==null) option = ''
		if (value==null) value = ''
		if (label==null) label = ''

		boolean labelBlank = StringUtil.isBlank(label)
		boolean selected = (value == option)

		StringBuilder opt = new StringBuilder('<option')
		opt.append(' value="')
		opt.append(HtmlUtil.escape(option))
		opt.append('"')

		if (selected) {
			opt.append(' selected')
		}

		opt.append('>')

		opt.append(labelBlank ? option : label)

		opt.append('</option>')

		return opt.toString()
	}

}
