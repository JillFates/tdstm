package net.transitionmanager.asset

import spock.lang.Specification
import spock.lang.Unroll

class FieldSpecSpec extends Specification {

	void 'test if a field spec is custom'() {

		setup: 'a field spec definition from database'
			Map fieldSpecMap = [
				"bulkChangeActions": [],
				"constraints"      : [
					"required": 1
				],
				"control"          : control,
				"default"          : "",
				"field"            : field,
				"imp"              : "Y",
				"label"            : label,
				"order"            : 1,
				"shared"           : 0,
				"show"             : 1,
				"tip"              : "",
				"udf"              : 0
			]

		expect: 'can check if FieldSpec is custom or not'
			new FieldSpec(fieldSpecMap).isCustom() == isCustom

		where:
			control    | field       | label             || isCustom
			'String'   | 'assetName' | 'Name'            || false
			'Number'   | 'custom10' || 'My Cutsom Field' || true
			'Date'     | 'custom10' || 'My Cutsom Field' || true
			'DateTime' | 'custom10' || 'My Cutsom Field' || true
	}

	@Unroll
	void 'test can return hibernate type for a custom field spec with control #control'() {

		setup: 'a field spec definition from database'
			Map fieldSpecMap = [
				"bulkChangeActions": [],
				"constraints"      : [
					"maxRange"     : 100,
					"minRange"     : 0,
					"precision"    : precision,
					"separator"    : separator,
					"allowNegative": allowNegative,
					"required"     : 0
				],
				"control"          : control,
				"default"          : "",
				"field"            : field,
				"imp"              : "Y",
				"label"            : label,
				"order"            : 1,
				"shared"           : 0,
				"show"             : 1,
				"tip"              : "",
				"udf"              : 0
			]

		expect: 'can check if FieldSpec is custom or not'
			new FieldSpec(fieldSpecMap).getHibernateType() == hibernateType

		where:
			control    | field       | label             | precision | separator | allowNegative || hibernateType
			'String'   | 'assetName' | 'Name'            | null      | false     | false         || ''
			'Number'   | 'custom10'  | 'My Cutsom Field' | 2         | true      | false         || 'big_decimal'
			'Number'   | 'custom10'  | 'My Cutsom Field' | 0         | false     | false         || 'long'
			'Number'   | 'custom10'  | 'My Cutsom Field' | null      | false     | false         || 'long'
			'Date'     | 'custom10'  | 'My Cutsom Field' | null      | false     | false         || 'date'
			'DateTime' | 'custom10'  | 'My Cutsom Field' | null      | false     | false         || 'timestamp'
	}
}
