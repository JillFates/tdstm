//import com.tdssrc.grails.TimeUtil
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
//import net.transitionmanager.domain.UserLogin
//import net.transitionmanager.domain.UserPreference
//import net.transitionmanager.service.UserPreferenceService
import org.apache.commons.lang.StringUtils
import spock.lang.Ignore
import test.AbstractUnitSpec
import com.tdsops.tm.enums.ControlType

@TestFor(ControlTagLib)
// @Mock([UserLogin, UserPreference])
class ControlTagLibTests extends AbstractUnitSpec {

	// The <tds:inputControl> taglet HTML mockup
	private static final String inputControlTagTemplate =
		'<tds:inputControl field="${field}" value="${value}" tabIndex="${tabIndex}" tabOffset="${tabOffset}" size="${size}"/>'

	// The <tds:ifInputRequired> taglet HTML mockup
	private static final String ifInputRequiredTagTemplateOpen  = '<tds:ifInputRequired field="${field}">'
	private static final String ifInputRequiredTagTemplateClose = '</tds:ifInputRequired>'

	private static final ControlTagLib tagLib = new ControlTagLib()

	private static Map stringFieldSpec = [
		field: 'nTrack',
		order: 42,
		control: ControlType.STRING.toString(),
		constraints: [
			required: 1,
			minSize: 0,
			maxSize: 10
		]
	]

	private static Map fieldSpec = [
		field: 'nTrack',
		order: 42,
		control: ControlType.LIST.toString(),
		constraints: [
			required: 1,
			values: ['1', '2', '3', '4']
		]
	]

	// occasionally (I haven't gotten much of a clue about what combination of factors causes this)
	// the first test will fail because something internal doesn't get correctly configured during
	// setup. in those cases having a dummy first test that doesn't use any of the configured test
	// infrastructure allows it to be 'misconfigured' but have no effect
	void 'test nothing'() {
		expect:
		'free beer'
	}


	void 'Test sizeAttrib method'() {
		expect: 'for each test the results will match'
			expected == tagLib.sizeAttrib(size)
		where:
			size 	| expected
			0		| ''
			5		| ' size="5"'
			null	| ''
			-5		| ''
	}

	void 'Test selectOption method'() {
		setup:
			Map spec = [
				order:5
			]

		expect: 'that each where example will match'
			expected == tagLib.selectOption(option, value, label)
		where:
			option  | value | label	| expected
			''		| ''	| null	| '<option value="" selected></option>'
			''		| null	| null	| '<option value="" selected></option>'
			null	| null	| null	| '<option value="" selected></option>'
			'1'		| ''	| null	| '<option value="1">1</option>'
			'1'		| '1'	| null	| '<option value="1" selected>1</option>'
			''		| '1'	| null	| '<option value=""></option>'

	}

	void 'Test tabIndexAttrib method with fieldSpec with order set'() {
		expect: 'that each where example will match'
			expected == tagLib.tabIndexAttrib(fieldSpec, tabIndex)
		where:
			tabIndex | expected
			''		| " tabindex=\"${fieldSpec.order}\""
			'2' 	| ' tabindex="2"'
	}

	void 'Test tabIndexAttrib method with fieldSpec with order NOT set'() {
		when: 'the fieldSpec.order is not set'
			Map altFieldSpec = fieldSpec + [order:0]
		then: 'the tabindex attribute should be empty'
			'' == tagLib.tabIndexAttrib(altFieldSpec)
	}

	void 'Test tabIndexAttrib method with tabOffset parameter'() {
		when: 'calling with tabOffset="1000" and default order from field spec'
			String result = tagLib.tabIndexAttrib(fieldSpec, null, '1000')
			int tabidx = 1000 + fieldSpec.order
		then: 'the tabindex attribute include the offset + fieldSpec.order value'
			" tabindex=\"$tabidx\"" == result

		when: 'calling with tabOffset="1000" and overriding the tagindex param'
			result = tagLib.tabIndexAttrib(fieldSpec, '87', '1000')
		then: 'the tabindex attribute include the offset + fieldSpec.order value'
			" tabindex=\"1087\"" == result
	}

	void 'Test attribute method'() {
		expect: 'that each where example will match'
			expected == tagLib.attribute(name, value, defValue)
		where:
			name		| value 	| defValue	| expected
			'x'			| 'foo'		| null		| ' x="foo"'
			'x'			| 'foo'		| 'bar'		| ' x="foo"'
			'x'			| ''		| 'bar'		| ' x="bar"'
			'x'			| null		| 'bar'		| ' x="bar"'
			'x'			| null		| null		| ' x=""'
			'encode'	| '"Yes"'	| ''		| ' encode="&quot;Yes&quot;"'

	}

	void 'Test constraintsAttrib method'() {
		when: 'calling constraintsAttrib with initial test values'
			String result = tagLib.constraintsAttrib(stringFieldSpec)
			int min = stringFieldSpec.constraints.minSize
			int max = stringFieldSpec.constraints.maxSize
		then: 'the result should have required'
			result.contains(' required ')
		and: 'min pattern value should default to 1 because it is required'
			result.contains(' pattern=".{1,}" ')
		and: 'max value should be set based on the spec'
			result.contains(" maxlength=\"$max\"")
		and: 'the complete formatted string should contain'
			result.contains(" required pattern=\".{1,}\" maxlength=\"$max\"")
		and: 'the string should contain validation error messages'
			result.contains(' oninvalid="setCustomValidity(\'')

		when: 'calling constraintsAttrib with max value to large'
			Map altFS = stringFieldSpec
			altFS.constraints.maxSize = 4815162342
			result = tagLib.constraintsAttrib(altFS)
		then: 'the max attribute should be set to MAX_STRING_LENGTH'
			result.contains(" maxlength=\"${tagLib.MAX_STRING_LENGTH}\"")
	}

	void 'Test controlTag for a String control'() {
		given: 'a fieldSpec that is a String'
			Map field = [
				field: 'username',
				label: 'Username',
				tip: 'The login username to use to login with',
				udf: 0,
				shared: 0,
				imp: 'C',
				show: 1,
				order: 6,
				default: '',
				control: ControlType.STRING.toString(),
				constraints: [
					required: 1,
					minSize:1,
					maxSize:20
				]
			]
		and: 'there is a default value'
			String defValue = 'ben.dover'
		and: 'and a tabOrder of 5'
			String tabIndex='5'

		when: 'the template is applied with the parameters'
			String result = applyTemplate(inputControlTagTemplate, [field:field, value:defValue, tabIndex:tabIndex])
		then: 'a value should be returned'
			result
		and: 'it should start with <input ...'
			result.startsWith('<input type="text" ')
		and: 'name is set correctly'
			result.contains(" name=\"${field.field}\" ")
		and: 'id is set correctly'
			result.contains(" id=\"${field.field}\" ")
		and: 'class has the control style and the importance'
			result.contains(" class=\"${tagLib.CONTROL_CSS_CLASS} ${field.imp}\" ")
		and: 'value should be populated'
			result.contains(" value=\"$defValue\" ")
		and: 'tabindex should be populated'
			result.contains(" tabindex=\"$tabIndex\" ")
		and: 'it should end with >'
			result.endsWith('>')
		and: 'the size attribute should not be included by default'
			!result.contains(' size="')

		when: 'the size parameter is included'
			result = applyTemplate(inputControlTagTemplate, [field:field, value:defValue, size:'55'])
		then: 'the size attribute should be in the HTML'
			result.contains(' size="55"')
	}

	void 'Test ifInputRequired Tag'() {
		when: 'the fieldSpec is required'
			Map fs = [ field: 'test', constraints: [required:1] ]
			String content = 'Holy crap Batman, it really worked!'
			String template = ifInputRequiredTagTemplateOpen + content + ifInputRequiredTagTemplateClose
			String result = applyTemplate(template, [ field: fs ] )
		then: 'the content in the template should be returned'
			content == result

		when: 'the fieldSpec is NOT required'
			fs.constraints.required = 0
		then: 'the content in the template should be blank'
			'' == applyTemplate(template, [ field: fs ] )
	}

	void 'Test YesNo inputControl Tag'() {
		given: 'a fieldSpec for a YesNo control'
			Map field = [
				field: 'active',
				label: 'Active',
				tip: 'Is the application actively used?',
				udf: 1,
				shared: 0,
				imp: 'C',
				show: 1,
				order: 6,
				default: '',
				control: ControlType.YES_NO.toString(),
				constraints: [
					required: 1
				]
			]
		when: 'the template is applied with the parameters'
			String result = applyTemplate(inputControlTagTemplate, [field:field, value:'No'])
		then: 'a value should be returned'
			result
		and: 'it should start with <input ...'
			result.startsWith('<select ')
		and: 'it should end with </select>'
			result.endsWith('</select>')
		and: 'it contains 3 options'
			3 == StringUtils.countMatches(result, '<option ')
		and: 'the No option should be selected'
			result.contains('<option value="No" selected>')
		and: 'there should be a required select option'
			result.contains("><option value=\"\">${tagLib.SELECT_REQUIRED_PROMPT}</option>")
		and: 'there should NOT be a (INVALID) option for blank value'
			! result.contains("<option value=\"\">(${tagLib.MISSING_OPTION_WARNING})</option>")

		when: 'the control is not required'
			field.constraints.required = 0
		and: 'there is a bogus value used with the YesNo control and it is not re'
			result = applyTemplate(inputControlTagTemplate, [field:field, value:'bogus'])
		then: 'there should be an empty option'
			result.contains('><option value=""></option>')
		and: 'it should contain 4 options'
			4 == StringUtils.countMatches(result, '<option ')
		and: 'one of the options should have a MISSING_OPTION_WARNING message'
			result.contains("<option value=\"bogus\" selected>bogus (${tagLib.MISSING_OPTION_WARNING})</option>")
		and: 'the size attribute should not be included by default'
			!result.contains(' size="')

		when: 'the size parameter is included'
			result = applyTemplate(inputControlTagTemplate, [field:field, value:'size', size:'55'])
		then: 'the size attribute should be in the HTML'
			result.contains(' size="55"')
	}

	void 'Test Select List inputControl Tag'() {
		given: 'a fieldSpec that is a String'
			Map field = [
				field: 'color',
				label: 'Favorite Color',
				tip: 'Select your favorite color',
				udf: 1,
				shared: 1,
				imp: 'C',
				show: 1,
				order: 6,
				default: '',
				control: ControlType.LIST.toString(),
				constraints: [
					required: 1,
					values: ['', 'Blue', 'Green', 'Grey', 'Red', 'Yellow']
				]
			]
		and: 'there is a default value'
			String defValue = field.constraints.values[1]
		and: 'and a tabOrder of 5'
			String tabIndex='5'
		when: 'the template is applied with the parameters'
			String result = applyTemplate(inputControlTagTemplate, [field:field, value:defValue, tabIndex:tabIndex])
		then: 'a value should be returned'
			result
		and: 'it should start with <select ...'
			result.startsWith('<select ')
		and: 'name is set correctly'
			result.contains(" name=\"${field.field}\" ")
		and: 'id is set correctly'
			result.contains(" id=\"${field.field}\" ")
		and: 'class has the control style and the importance'
			result.contains(" class=\"${tagLib.CONTROL_CSS_CLASS} ${field.imp}\" ")
		and: 'it should end with </select>'
			result.endsWith('</select>')
		and: 'it contains 6 options'
			6 == StringUtils.countMatches(result, '<option ')
		and: 'there should be a required select option'
			result.contains("><option value=\"\">${tagLib.SELECT_REQUIRED_PROMPT}</option>")
		and: 'there should NOT be a (INVALID) option for blank value'
			! result.contains("<option value=\"\">(${tagLib.MISSING_OPTION_WARNING})</option>")

		when: 'the control is not required'
			field.constraints.required = 0
		and: 'called with a bogus value'
			result = applyTemplate(inputControlTagTemplate, [field:field, value:'bogus', tabIndex:tabIndex])
		then: 'there should be an additional option'
			7 == StringUtils.countMatches(result, '<option ')
		and: 'there should be an empty option'
			result.contains('><option value=""></option>')
		and: 'one of the options should have a MISSING_OPTION_WARNING message'
			result.contains("<option value=\"bogus\" selected>bogus (${tagLib.MISSING_OPTION_WARNING})</option>")
		and: 'the size attribute should not be included by default'
			!result.contains(' size="')

		when: 'the size parameter is included'
			result = applyTemplate(inputControlTagTemplate, [field:field, value:'size', size:'55'])
		then: 'the size attribute should be in the HTML'
			result.contains(' size="55"')

	}

	void 'Test Tool Tip Attribute '() {
		given: 'a fieldSpec that is a String'
		Map field = [
				field      : 'color',
				label      : 'Best program language ever <!>',
				tip        : 'Select an option with a quote (")',
				udf        : 1,
				shared     : 1,
				imp        : 'C',
				show       : 1,
				order      : 1,
				default    : 'Javascript',
				control    : ControlType.LIST.toString(),
				constraints: [
						required: 1,
						values  : ['Java', 'Grails', 'Javascript']
				]
		]
		and: 'there is a default value'
			String defValue = field.constraints.values[1]
		and: 'and a tabOrder of 1'
			String tabIndex='1'
		when: 'the template is applied with the parameters'
			String result = applyTemplate(inputControlTagTemplate, [field:field, value:defValue, tabIndex:tabIndex])
		then: 'a value should be returned'
			result
		and: 'data-content should contains the tool tip'
			result.contains(' data-content="Select an option with a quote (&quot;)" ')
		and: 'it should contain the data-toggle'
			result.contains(' data-toggle="popover" ')
		and: 'it should contain the data-trigger'
			result.contains(' data-trigger="hover" ')

		when: 'the tooltip property is empty'
			field.tip = ''
			result = applyTemplate(inputControlTagTemplate, [field:field, value:defValue, tabIndex:tabIndex])
		then: 'The content should return an empty string'
			result.contains('')
	}

	void "Test InputLabel includes the correct CSS if the field is critical, or important, and it's empty." (){
		given: "a critical field"
			Map field = [
					field      : 'someField',
					label      : 'A generic field',
					tip        : 'the tip',
					udf        : 1,
					shared     : 1,
					imp        : 'Y',
					show       : 1,
					order      : 1,
					default: '',
					control: ControlType.STRING.toString(),
					constraints: [
							required: 1,
							minSize:1,
							maxSize:20
					]
			]
			String emptyImpCritClass = ControlTagLib.EMPTY_IMP_CRIT_FIELD_CSS_CLASS

		when: "generating InputLabel for an empty critical field"
			String label = tagLib.inputLabel([field:field, value: null])
		then: "the label has the appropriate CSS class"
			label.contains(emptyImpCritClass)

		when: "generating the InputLabel with some value for a critical field"
			label = tagLib.inputLabel([field:field, value: "someValue"])
		then: "the label shouldn't have the CSS class."
			!label.contains(emptyImpCritClass)

		when: "generating the InputLabel with '0' as value for a critical field"
			label = tagLib.inputLabel([field:field, value: "0"])
		then: "the label shouldn't have the CSS class"
			!label.contains(emptyImpCritClass)

		when: "generating InputLabel for an empty important field"
			field.imp = "G"
			label = tagLib.inputLabel([field:field, value: null])
		then: "the label should have the appropriate CSS class"
			label.contains(emptyImpCritClass)

		when: "generating InputLabel for an empty field that isn't critical nor important"
			field.imp = "N"
			label = tagLib.inputLabel([field:field, value: null])
		then: "the label shouldn't have the CSS class"
			!label.contains(emptyImpCritClass)

		when: "generating InputLabel for a field that isn't critical nor important"
			label = tagLib.inputLabel([field:field, value: "someValue"])
		then: "the label shouldn't have the CSS class"
			!label.contains(emptyImpCritClass)

	}
}
