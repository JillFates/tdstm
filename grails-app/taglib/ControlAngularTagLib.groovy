import com.tdssrc.grails.HtmlUtil
import net.transitionmanager.asset.AssetEntity
import groovy.json.JsonOutput
import org.apache.commons.lang.StringEscapeUtils
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import net.transitionmanager.exception.InvalidParamException
import com.tdsops.tm.enums.ControlType

/**
 * This TabLib is a clone of the ControlTagLib. The original lib was used to render the various data elements and input controls for the
 * asset CRUD forms. This lib was created to support the rendering of the asset CRUD forms that will render the forms as html templates
 * for Angular (e.g. adding ng tags to the templates).
 */
class ControlAngularTagLib {

	static String namespace = 'tdsAngular'

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
			throw new InvalidParamException('<tdsAngular:inputLabel> tag requires field=fieldSpec Map')
		}
		StringBuilder sb = new StringBuilder("\n")
		def imp = fieldSpec.imp;
		// Build the LABEL element
		// <label for="assetName"><span data-toggle="popover" data-trigger="hover" data-content="Some tip">Name</span></label>
		sb.append('<label for="')
		sb.append(fieldSpec.field)
		sb.append('"')
		sb.append(' class="')
		sb.append(imp)
		sb.append('">')
		sb.append('<span ')
		sb.append(tooltipAttrib(fieldSpec))
		sb.append(' >')
		sb.append(HtmlUtil.escape(fieldSpec.label))
		sb.append('</span>')
		if (fieldSpec.constraints.required) {
			sb.append('<span style="color: red;">*</span>')
		}
		sb.append('</label>')

		out << sb.toString()
	}

	/**
	 * Creates the cell with the value of a field for displaying in show views.
	 */
	def labelForShowField = { Map attrs ->
		Map fieldSpec = attrs.field ?: [:]
		def fieldValue = attrs.value ?: ""
		StringBuilder sb = new StringBuilder("\n")
		sb.append("<td class='valueNW ${fieldSpec.imp}'>")
		sb.append("<span>")
		sb.append(fieldValue)
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
	 * @param ngModel - The String representation of the Model on Angular
	 * @param size - used to define the HTML size attribute on controls (optional)
	 * @param tabIndex - the tab offset (optional)
	 * @param tabOffset - used to offset the tabIndex values (used by the custom fields)
	 * @param min - used to specify minimum allowed value (used by the number fields)
	 * @param blankOptionListText - Text used to represent a blank option list text (optional)
	 * @example <tds:inputControl field="${fieldSpec} value="${domain.value}" ngmodel="model.asset.assetName" tabOffset="400"/>
	 */
	def inputControl = { Map attrs ->
		String customIndex = attrs.customIndex

		// The field Specifications
		Map fieldSpec = attrs.field ?: [:]
		if (!fieldSpec) {
			throw new InvalidParamException('<tdsAngular:inputControl> tag requires field=fieldSpec Map')
		}

		// The value that the control should be set to (optional)
		String value = ( attrs.value ?: '' )
		value = (value == null ? '' : value)

		Integer size = NumberUtil.toInteger(attrs.size,null)

		// Get tabIndex from attrib tabIndex, tabindex or tabOffset which if passed will override
		// the order specified in the fieldSpec.
		String tabIndex = ( attrs.tabIndex ?: (attrs.tabindex ?: null))
		String tabOffset = (attrs.tabOffset ?: (attrs.taboffset ?: null ))
		String min = (attrs.min ?: (attrs.min ?: "" ))
		String blankOptionListText = (attrs.blankOptionListText ?: (attrs.blankOptionListText ?: null ))
		String tabIndexInput = tabIndex ? tabIndex : calculateTabIndexNumber(fieldSpec, tabIndex, tabOffset)

		String placeholder = attrs.placeholder ?: ''
		boolean isRequired = fieldSpec.constraints?.required
		switch (fieldSpec.control) {
			case ControlType.LIST.toString():
			case ControlType.IN_LIST.toString():
			case ControlType.OPTIONS_ENVIRONMENT.toString():
			case ControlType.OPTIONS_PRIORITY.toString():
				out << renderSelectListInput(fieldSpec, value, attrs.ngmodel, customIndex, tabIndex, tabOffset, size, null, blankOptionListText)
				break
			case ControlType.YES_NO.toString():
				out << renderYesNoInput(fieldSpec, value, attrs.ngmodel, tabIndex, tabOffset, size, null, blankOptionListText)
				break
			case ControlType.NUMBER.toString():
				out << renderNumberInput(fieldSpec, attrs.ngmodel, tabIndexInput)
				break
			case ControlType.DATE.toString():
				out << renderDateInput(fieldSpec, attrs.ngmodel, tabIndexInput)
				break
			case ControlType.DATETIME.toString():
				out << renderDateTimeInput(fieldSpec, attrs.ngmodel, tabIndexInput)
				break
			case ControlType.STRING.toString():
			default:
				out << renderStringInput(fieldSpec, value, attrs.ngmodel, tabIndex, tabOffset, size, null, placeholder)
		}
	}

    private String transformNumberFormat(Map fieldSpec) {
        String outputFormat = fieldSpec.constraints.format
        if (fieldSpec.constraints.precision > 0) {
            outputFormat = outputFormat.replace("\'","\\'")
        }
        outputFormat
    }

	/**
	 * Used to render the value for any of the supported custom fields in the Asset Show Views.
	 * @param field - the field spec Map
	 * @param value - the current or default value to populate the control with (optional)
	 * @param ngModel - The String representation of the Model on Angular
	 * @param size - used to define the HTML size attribute on controls (optional)
	 * @param tabIndex - the tab offset (optional)
	 * @param tabOffset - used to offset the tabIndex values (used by the custom fields)
	 * @param min - used to specify minimum allowed value (used by the number fields)
	 * @example <tds:showValue field="${fieldSpec} value="${domain.value}" ngmodel="model.asset.assetName" tabOffset="400"/>
	 */
	def showValue = { Map attrs ->

		// The field Specifications
		Map fieldSpec = attrs.field
		if (!fieldSpec) {
			throw new InvalidParamException('<tdsAngular:inputControl> tag requires field=fieldSpec Map')
		}
		// The value that the control should be set to (optional)
		String value = ( attrs.value ?: '' )

		Integer size = NumberUtil.toInteger(attrs.size,null)

		// Get tabIndex from attrib tabIndex, tabindex or tabOffset which if passed will override
		// the order specified in the fieldSpec.
		String tabIndex = attrs.tabIndex
		String tabOffset = attrs.tabOffset
		String min = attrs.min
		String placeholder = attrs.placeholder ?: ''

		switch (fieldSpec.control) {
			case ControlType.LIST.toString():
				out << value  // render value as it is
				break
			case ControlType.YES_NO.toString():
				out << value  // render value as it is
				break
			case ControlType.DATE.toString():
				out << "{{ '$value' | tdsDate: userDateFormat }}"
				break
			case ControlType.DATETIME.toString():
				out << "{{ '$value' | tdsDateTime: userTimeZone }}"
				break
			case ControlType.NUMBER.toString():
				out << "{{ ${value ? value : '\'\''} | tdsNumber: '${transformNumberFormat(fieldSpec)}' }}"
				break
			case ControlType.STRING.toString():
			default: // call textAsLink
				out << tds.textAsLink(text: value, target: "_new")
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
		out << '<div class="clr-form-control">'
		out << inputLabel(attrs)
		out << inputControl(attrs)
		out << "</div>"
	}

	/**
	 * Used to determinate if the Field should be or not Highlighted
     * FE Equivalent: asset-common-helper.ts
	 * @param fieldSpec - the field spec Map
	 * @param asset - the domain object
	 * @param fieldName - the field
	 * @param domainField - the field on the domain Object (optional)
	 */
	def highlightedField = { Map attrs ->
		Map fieldSpec = attrs.fieldSpec ?: [:]
		AssetEntity domainObject = attrs.asset ?: [:]
		String fieldName = ( attrs.fieldName ?: '' )
		String domainField = attrs.domainField

		if (!fieldSpec || !domainObject || !fieldName) {
			throw new InvalidParamException('<tdsAngular:highlightedField> tag requires fieldSpec=standardField Map and asset=domainObject and fieldName')
		}

		def value = domainField && domainField != null ? domainObject[domainField] : domainObject[fieldName]

        def isImportantClass = 'YG'.contains(fieldSpec[fieldName].imp.toUpperCase())
		boolean hasValue = (value != '' && value != null) || value?.trim()

        out << (isImportantClass && !hasValue)
	}

	/**
	 * Generates a SELECT HTML control based on the field specification and the
	 * parameters passed to the method.
	 * @param fieldSpec - the map of field specifications
	 * @param value - the value to set the control to (optional)
	 * @param tabIndex - the tab order used to override the fieldSpec.order (optional)
	 * @param tooltipDataPlacement - the tooltip data placement value used to override the default placement (optional)
	 * @param blankOptionListText - Text used to represent a blank text (optional)
	 * @return the SELECT Component HTML
	 */
	private String renderSelectListInput(
			Map fieldSpec,
			String value,
			String ngmodel,
			String customIndex,
			String tabIndex,
			String tabOffset,
			Integer size,
			String tooltipDataPlacement,
			String blankOptionListText)
	{
		StringBuilder sb = new StringBuilder('<kendo-dropdownlist ')
		sb.append('#' + 'field' + fieldSpec.field + '="ngModel"')
		sb.append(' [(ngModel)]="'+ ngmodel +'" ')
		sb.append(' [valuePrimitive]="true" ')

		boolean isRequiredField = fieldSpec.constraints?.required || fieldSpec.field == 'environment'
		if (! isRequiredField) {
			sb.append(' [defaultItem]="\'\'" ')
		}

		sb.append(commonAttributes(fieldSpec, value, tabIndex, tabOffset, size, tooltipDataPlacement))

		// StandardFieldSpecs are by propertyName and customs are ordinal position
		if (fieldSpec.udf == 0) {
			sb.append(" [data]='model.standardFieldSpecs.${fieldSpec.field}.constraints.values' ")
		} else {
			sb.append(" [data]='model.customs[${customIndex}].constraints.values' ")
		}

		sb.append('>')

		sb.append('</kendo-dropdownlist>')
		sb.append(renderRequiredLabel(fieldSpec))

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
	private String renderStringInput(Map fieldSpec, String value, String ngmodel, String tabIndex, String tabOffset, Integer size, String tooltipDataPlacement, String placeholder) {
		'<input clrInput #' + 'field' + fieldSpec.field + '="ngModel" [(ngModel)]="'+ ngmodel +'" ' +
			attribute('type', 'text') + attribute('placeholder', placeholder) +
			commonAttributes(fieldSpec, value, tabIndex, tabOffset, size, tooltipDataPlacement) + '/>' +
			renderRequiredLabel(fieldSpec)
	}

	/**
	 * Generates a String HTML label used to show a required missing field warning
	 * @param fieldSpec - the map of field specifications
	 * @return the INPUT Component HTML
	 */
	private String renderRequiredLabel(Map fieldSpec) {
		if (fieldSpec?.constraints?.required) {
			def field = 'field' + fieldSpec.field
			def fieldLabel = HtmlUtil.escape(fieldSpec.label)

			return "<div class=\"error\" *ngIf=\"form && (form.submitted && ${field} && !${field}.valid) || " +
					" (${field}.dirty && !${field}.valid)\">* ${fieldLabel} is required</div>"
		}
		return ''
	}

	/**
	 * Generates a SELECT HTML control with Yes/No options
	 * @param fieldSpec - the map of field specifications
	 * @param value - the value to set the control to
	 * @param tabIndex - the tab order used to override the fieldSpec.order (optional)
	 * @param tooltipDataPlacement - the tooltip data placement value used to override the default placement (optional)
	 * @param blankOptionListText - Text used to represent a blank option list text (optional)
	 * @return the INPUT Component HTML
	 */
	private String renderYesNoInput(Map fieldSpec, String value, String ngmodel, String tabIndex, String tabOffset, Integer size, String tooltipDataPlacement, String blankOptionListText) {
		List options = []
		List valid = ['Yes', 'No']

		StringBuilder sb = new StringBuilder('<kendo-dropdownlist ')
		sb.append(commonAttributes(fieldSpec, value, tabIndex, tabOffset, size, tooltipDataPlacement))
		sb.append(' #' + 'field' + fieldSpec.field + '="ngModel"')
		sb.append(' [(ngModel)]="'+ ngmodel +'" ')
		sb.append(' [valuePrimitive]="true" ')
		sb.append(' [textField]="\'text\'" [valueField]="\'value\'" ')

		List<Object> stringList = new ArrayList<Object>();


		if (fieldSpec.constraints?.required) {
			// Add a Select... option at top if the field is required
			stringList.add([ 'value' : '', 'text': SELECT_REQUIRED_PROMPT])
		} else {
			// Put a blank entry in to allow the user to unset a field
			stringList.add([ 'value' : '', 'text': StringUtil.isBlank(blankOptionListText) ? '' : blankOptionListText])
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
			stringList.add([ 'value' : value, 'text': warning ])
		}

		stringList.add([ 'value' : 'Yes', 'text': 'Yes' ])
		stringList.add([ 'value' : 'No', 'text': 'No' ])

		// Iterate over the fieldSpec option values to create each of the options
		for (option in options) {
			stringList.add(selectOption(option[0], value, option[1]))
		}

		sb.append(" [data]=' " + JsonOutput.toJson(stringList) + "' ")

		sb.append('>')

		sb.append('</kendo-dropdownlist>')
		sb.append(renderRequiredLabel(fieldSpec))

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
		def ti = calculateTabIndexNumber(fieldSpec, tabIndex, tabOffset)
		return " tabindex=\"$ti\""
	}

	/**
	 * Returns the  tabIndex number attribute based on the field specification
	 * @param field - the Field specification object
	 * @param tabIndex - the tabindex of the field that if supplied overrides the setting in field spec order property
	 * @param tabOffset - a value that if supplied is added to the tabindex (used by Custom Fields presently)
	 * @param attributeName - The name of the tabindex property (default: tabindex)
	 * @return The tabIndex number
	 */
	private String calculateTabIndexNumber(Map fieldSpec, String tabIndex=null, String tabOffset=null) {
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
			return ti
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
			attrib += StringEscapeUtils.escapeHtml(value)
		} else if (defValue != null) {
			attrib += StringEscapeUtils.escapeHtml(defValue)
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
	private Map<String, String> selectOption(String option='', String value='', String label='') {
		if (option==null) option = ''
		if (value==null) value = ''
		if (label==null) label = ''

		boolean labelBlank = StringUtil.isBlank(label)
		String escapedvalue = StringEscapeUtils.escapeHtml(option)
		String text = (labelBlank ? option : label)
		return ["value": "$escapedvalue", "text": "$text"]
	}

	/**
	 * Generates a String HTML to render the angular control used to represent numbers
	 * parameters passed to the method.
	 * @param fieldSpec - the map of field specifications
	 * @param ngmodel - angular variable holding the value
	 * @param tabIndex - the tab order used to override the fieldSpec.order (optional)
	 * @return the Angular Tag Component HTML
	 */
	private String renderNumberInput(Map fieldSpec, String ngmodel, String tabIndex) {
		def name = fieldSpec.field
		def allowNegative = fieldSpec?.constraints?.allowNegative
		def precision = fieldSpec?.constraints?.precision
		def separator = fieldSpec?.constraints?.separator
		def min = fieldSpec?.constraints?.minRange
		def max = fieldSpec?.constraints?.maxRange
		def required = fieldSpec?.constraints?.required != 0

		StringBuilder control = new StringBuilder("")
		control.append("<tds-number-control ")
		control.append("  #field${fieldSpec.field}='ngModel' ")
		control.append("  [(ngModel)]=\"$ngmodel\" ")
		control.append("  name=\"$name\" ")
		control.append("  [allowNegative]=\"$allowNegative\"")
		control.append("  [precision]=\"$precision\"")
		control.append("  [separator]=\"$separator\"")
		control.append("  [min]=\"$min\"")
		control.append("  [max]=\"$max\"")
		control.append("  [required]=\"$required\" ")
		control.append("  [tabindex]=\"$tabIndex\" ")
		control.append("  [format]=\"'${transformNumberFormat(fieldSpec)}'\">")
		control.append("</tds-number-control>")
		control.append(renderCustomValidationErrors(fieldSpec))

		return control.toString()
	}

	/**
	 * Generates a String HTML to render the angular control used to represent dates
	 * parameters passed to the method.
	 * @param fieldSpec - the map of field specifications
	 * @param ngmodel - angular variable holding the value
	 * @param tabIndex - the tab order used to override the fieldSpec.order (optional)
	 * @return the Angular Tag Component HTML
	 */
	private String renderDateInput(Map fieldSpec, String ngmodel, String tabIndex) {
		def name = fieldSpec.field
		def required = fieldSpec?.constraints?.required != 0

		StringBuilder control = new StringBuilder("")
		control.append("<tds-date-control ")
		control.append("  #field${fieldSpec.field}='ngModel' ")
		control.append("  [(ngModel)]=\"$ngmodel\" ")
		control.append("  [value]=\"$ngmodel\" ")
		control.append("  name=\"$name\" ")
		control.append("  [required]=\"$required\" ")
		control.append("  [tabindex]=\"$tabIndex\"> ")
		control.append("</tds-date-control>")
		control.append(renderCustomValidationErrors(fieldSpec))

		return control.toString()
	}

	/**
	 * Generates a String HTML to render the angular control used to represent datetime
	 * parameters passed to the method.
	 * @param fieldSpec - the map of field specifications
	 * @param ngmodel - angular variable holding the value
	 * @param tabIndex - the tab order used to override the fieldSpec.order (optional)
	 * @return the Angular Tag Component HTML
	 */
	private String renderDateTimeInput(Map fieldSpec, String ngmodel, String tabIndex) {
		def name = fieldSpec.field
		def required = fieldSpec?.constraints?.required != 0

		StringBuilder control = new StringBuilder("")
		control.append("<tds-datetime-control ")
		control.append("  #field${fieldSpec.field}='ngModel' ")
		control.append("  [(ngModel)]=\"$ngmodel\" ")
		control.append("  [value]=\"$ngmodel\" ")
		control.append("  name=\"$name\" ")
		control.append("  [required]=\"$required\" ")
		control.append("  [tabindex]=\"$tabIndex\"> ")
		control.append("</tds-datetime-control>")
		control.append(renderCustomValidationErrors(fieldSpec))

		return control.toString()
	}

	/**
	 * Generates a String HTML to render the control used to show field validation errors
	 * @param fieldSpec - the map of field specifications
	 * @return the HTML tag to represent the angular control with the properties loaded
	 */
	private String renderCustomValidationErrors(Map fieldSpec) {
		def field = 'field' + fieldSpec?.field
		def controlLabel = HtmlUtil.escape(fieldSpec.label)

		StringBuilder control = new StringBuilder("")
		control.append("<tds-custom-validation-errors ")
		control.append("   [errors]='${field}.errors' ")
		control.append("   [submitted]='form && form.submitted' ")
		control.append("   [valid]='${field}.valid' ")
		control.append("   [touched]='${field}.touched' ")
		control.append("   [dirty]='${field}.dirty' >")
		control.append("${controlLabel}")
		control.append("</tds-custom-validation-errors>")

		return control.toString()
	}
}
