package net.transitionmanager.asset

import net.transitionmanager.dataview.FieldSpec
import org.hibernate.type.DateType
import org.hibernate.type.LongType
import org.hibernate.type.StringType
import org.hibernate.type.TimestampType
import spock.lang.Specification
import spock.lang.Unroll

import java.sql.Timestamp

class FieldSpecSpec extends Specification {

	private Integer setUdfProperty(String fieldName) {
		return (fieldName.startsWith('custom') ? 1 : 0)
	}

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
				"udf"              : setUdfProperty(field)
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
				"udf"              : setUdfProperty(field)
			]

		expect: 'can check if FieldSpec is custom or not'
			new FieldSpec(fieldSpecMap).getHibernateType() == hibernateType

		where:
			control    | field       | label             | precision | separator | allowNegative || hibernateType
			'String'   | 'assetName' | 'Name'            | null      | false     | false         || StringType.INSTANCE.name
			'Number'   | 'custom10'  | 'My Cutsom Field' | 2         | true      | false         || FieldSpec.CAST_BIG_DECIMAL_FUNCTION
			'Number'   | 'custom10'  | 'My Cutsom Field' | 0         | false     | false         || LongType.INSTANCE.name
			'Number'   | 'custom10'  | 'My Cutsom Field' | null      | false     | false         || LongType.INSTANCE.name
			'Date'     | 'custom10'  | 'My Cutsom Field' | null      | false     | false         || DateType.INSTANCE.name
			'DateTime' | 'custom10'  | 'My Cutsom Field' | null      | false     | false         || TimestampType.INSTANCE.name
	}

	@Unroll
	void 'test can return class type for a custom field spec with control #control'() {

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
			new FieldSpec(fieldSpecMap).getClassType() == classType

		where:
			control    | field       | label             | precision | separator | allowNegative || classType
			'String'   | 'assetName' | 'Name'            | null      | false     | false         || String
			'Number'   | 'custom10'  | 'My Cutsom Field' | 2         | true      | false         || BigDecimal
			'Number'   | 'custom10'  | 'My Cutsom Field' | 0         | false     | false         || Long
			'Number'   | 'custom10'  | 'My Cutsom Field' | null      | false     | false         || Long
			'Date'     | 'custom10'  | 'My Cutsom Field' | null      | false     | false         || Date
			'DateTime' | 'custom10'  | 'My Cutsom Field' | null      | false     | false         || Timestamp
	}

	@Unroll
	void 'test can return cast HQL sentence for a custom field spec with control #control'() {

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
				"udf"              : setUdfProperty(field)
			]

		expect: 'can check if FieldSpec is custom or not'
			new FieldSpec(fieldSpecMap).getHibernateCastSentence('A.custom10') == castSentence

		where:
			control    | field       | label             | precision | separator | allowNegative || castSentence
			'String'   | 'assetName' | 'Name'            | null      | false     | false         || "cast(A.custom10 as $StringType.INSTANCE.name)"
			'Number'   | 'custom10'  | 'My Cutsom Field' | 2         | true      | false         || "$FieldSpec.CAST_BIG_DECIMAL_FUNCTION(A.custom10, 2)"
			'Number'   | 'custom10'  | 'My Cutsom Field' | 0         | false     | false         || "cast_long(A.custom10)"
			'Number'   | 'custom10'  | 'My Cutsom Field' | null      | false     | false         || "cast_long(A.custom10)"
			'Date'     | 'custom10'  | 'My Cutsom Field' | null      | false     | false         || "cast_date_time(A.custom10, '%Y-%m-%d')"
			'DateTime' | 'custom10'  | 'My Cutsom Field' | null      | false     | false         || "cast_date_time(A.custom10, '%Y-%m-%dT%TZ')"
	}

}
