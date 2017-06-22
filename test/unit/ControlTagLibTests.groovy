//import com.tdssrc.grails.TimeUtil
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
//import net.transitionmanager.domain.UserLogin
//import net.transitionmanager.domain.UserPreference
//import net.transitionmanager.service.UserPreferenceService
import org.apache.commons.lang.StringUtils
import test.AbstractUnitSpec


@TestFor(ControlTagLib)
// @Mock([UserLogin, UserPreference])
class ControlTagLibTests extends AbstractUnitSpec {

	// The <tds:inputControl> taglet HTML mockup
	private static final String inputControlTagTemplate =
		'<tds:inputControl field="${field}" value="${value}" tabIndex="${tabIndex}" tabOffset="${tabOffset}"/>'

	// The <tds:ifInputRequired> taglet HTML mockup
	private static final String ifInputRequiredTagTemplateOpen  = '<tds:ifInputRequired field="${field}">'
	private static final String ifInputRequiredTagTemplateClose = '</tds:ifInputRequired>'

	private static final ControlTagLib tagLib = new ControlTagLib()

	private static Map stringFieldSpec = [
		field: 'nTrack',
		order: 42,
		control: 'String',
		constraints: [
			required: 1,
			minSize: 0,
			maxSize: 10
		]
	]

	private static Map fieldSpec = [
		field: 'nTrack',
		order: 42,
		control: 'Select List',
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
		and: 'min value should default to 1 because it is required'
			result.contains(' min="1" ')
		and: 'max value should be set based on the spec'
			result.contains(" maxlength=\"$max\"")
		and: 'the complete formatted string should be'
			" required min=\"1\" maxlength=\"$max\"" == result

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
				control: 'String',
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

	void 'Test inputControl Tag for a Select List control'() {
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
				control: 'Select List',
				constraints: [
					required: 0,
					values: ['Blue', 'Green', 'Grey', 'Red', 'Yellow']
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
		and: 'it contains 5 options'
			5 == StringUtils.countMatches(result, '<option ')

		when: 'called with an invalid value'
			result = applyTemplate(inputControlTagTemplate, [field:field, value:'bogus', tabIndex:tabIndex])
		then: 'there should be an additional option'
			6 == StringUtils.countMatches(result, '<option ')
		and: 'one of the options should have a MISSING_OPTION_WARNING message'
			result.contains(">bogus (${tagLib.MISSING_OPTION_WARNING})</option>")
	}

	/*
	void 'Test tds:convertDate tag with MIDDLE_ENDIAN'() {
		setup:
		setUserDateFormat TimeUtil.MIDDLE_ENDIAN

		when:
		setTimeZone timezone

		then: 'Test DateTime with MIDDLE_ENDIAN'
		applyTemplate(convertDateTag, [date: testDate, format: format]) == expectedValue

		where:
		timezone                            | format                | expectedValue
		'GMT'                               | TimeUtil.FORMAT_DATE  | '08/21/2012'
		'America/Argentina/Buenos_Aires'    | TimeUtil.FORMAT_DATE  | '08/21/2012'
		'America/New_York'                  | TimeUtil.FORMAT_DATE  | '08/21/2012'
	}
	*/

}
