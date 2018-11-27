package net.transitionmanager.dataview

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.hibernate.type.DateType
import org.hibernate.type.LongType
import org.hibernate.type.StringType
import org.hibernate.type.TimestampType

import java.sql.Timestamp

/**
 * Manage Field Spec definition using the following JSON structure saved in Database
 * <pre>
 *	{
 *		"control": "String",
 *       "default": "",
 *       "field": "custom10",
 *       "imp": "N",
 *       "label": "Custom10",
 *       "order": 110,
 *       "shared": 0,
 *       "show": 1,
 *       "tip": "",
 *       "udf": 1,
 *			{ "constraint: {
 *				"maxRange": 100,
 *         		"minRange": 0,
 *         		"precision": 2,
 *         		"separator": true,
 *         		"allowNegative": true
 *         		"required": 0
 *			}
 *	}
 *</pre>
 */
@CompileStatic
class FieldSpec {

	public static final CAST_BIG_DECIMAL_FUNCTION = 'cast_big_decimal'

	String control
	Object defaultValue
	String field
	String label
	/*
	 * Identifies if the field is a custom field
	 */
	Boolean isUserDefinedField

	// Constraints field section
	Integer max
	Integer min
	Integer precision
	Boolean separator
	Boolean allowNegative
	Integer required

	@CompileStatic(TypeCheckingMode.SKIP)
	FieldSpec(Map fieldSpecMap) {
		this.control = fieldSpecMap.control
		this.defaultValue = fieldSpecMap."default"
		this.field = fieldSpecMap.field
		this.label = fieldSpecMap.label
		this.isUserDefinedField = fieldSpecMap.udf == 1
		this.max = fieldSpecMap.constraints.max
		this.min = fieldSpecMap.constraints.min
		this.precision = fieldSpecMap.constraints.precision
		this.separator = fieldSpecMap.constraints.separator
		this.allowNegative = fieldSpecMap.constraints.allowNegative
		this.required = fieldSpecMap.constraints.required
	}

	/**
	 *
	 * @return
	 */
	Boolean isCustom() {
		return isUserDefinedField
	}
	/**
	 * A field spec is numeric when control == 'Number'
	 * @return true if control == 'Number' otherwise it returns false
	 */
	Boolean isNumeric() {
		return this.control == 'Number'
	}

	/**
	 * <h2>Primitive Types</h2>
	 * <table class="table table-bordered" style="text-align:center;">
	 * <tbody><tr>
	 * <th style="width:25%;text-align:center;">Mapping type</th>
	 * <th style="text-align:center;">Java type</th>
	 * <th style="width:30%;text-align:center;">ANSI SQL Type</th>
	 * </tr>
	 * <tr>
	 * <td>integer</td>
	 * <td>int or java.lang.Integer</td>
	 * <td>INTEGER</td>
	 * </tr>
	 * <tr>
	 * <td>long</td>
	 * <td>long or java.lang.Long</td>
	 * <td>BIGINT</td>
	 * </tr>
	 * <tr>
	 * <td>short</td>
	 * <td>short or java.lang.Short</td>
	 * <td>SMALLINT</td>
	 * </tr>
	 * <tr>
	 * <td>float</td>
	 * <td>float or java.lang.Float</td>
	 * <td>FLOAT</td>
	 * </tr>
	 * <tr>
	 * <td>double</td>
	 * <td>double or java.lang.Double</td>
	 * <td>DOUBLE</td>
	 * </tr>
	 * <tr>
	 * <td>big_decimal</td>
	 * <td>java.math.BigDecimal</td>
	 * <td>NUMERIC</td>
	 * </tr>
	 * <tr>
	 * <td>character</td>
	 * <td>java.lang.String</td>
	 * <td>CHAR(1)</td>
	 * </tr>
	 * <tr>
	 * <td>string</td>
	 * <td>java.lang.String</td>
	 * <td>VARCHAR</td>
	 * </tr>
	 * <tr>
	 * <td>byte</td>
	 * <td>byte or java.lang.Byte</td>
	 * <td>TINYINT</td>
	 * </tr>
	 * <tr>
	 * <td>boolean</td>
	 * <td>boolean or java.lang.Boolean</td>
	 * <td>BIT</td>
	 * </tr>
	 * <tr>
	 * <td>yes/no</td>
	 * <td>boolean or java.lang.Boolean</td>
	 * <td>CHAR(1) ('Y' or 'N')</td>
	 * </tr>
	 * <tr>
	 * <td>true/false</td>
	 * <td>boolean or java.lang.Boolean</td>
	 * <td>CHAR(1) ('T' or 'F')</td>
	 * </tr>
	 * </tbody></table>
	 *
	 * <h2>Date and Time Types</h2>
	 * <table class="table table-bordered" style="text-align:center;">
	 * <tbody><tr>
	 * <th style="width:25%;text-align:center;">Mapping type</th>
	 * <th style="text-align:center;">Java type</th>
	 * <th style="width:25%;text-align:center;">ANSI SQL Type</th>
	 * </tr>
	 * <tr>
	 * <td>date</td>
	 * <td>java.util.Date or java.sql.Date</td>
	 * <td>DATE</td>
	 * </tr>
	 * <tr>
	 * <td>time</td>
	 * <td>java.util.Date or java.sql.Time</td>
	 * <td>TIME</td>
	 * </tr>
	 * <tr>
	 * <td>timestamp</td>
	 * <td>java.util.Date or java.sql.Timestamp</td>
	 * <td>TIMESTAMP</td>
	 * </tr>
	 * <tr>
	 * <td>calendar</td>
	 * <td>java.util.Calendar</td>
	 * <td>TIMESTAMP</td>
	 * </tr>
	 * <tr>
	 * <td>calendar_date</td>
	 * <td>java.util.Calendar</td>
	 * <td>DATE</td>
	 * </tr>
	 * </tbody></table>
	 *
	 * <h2>Binary and Large Object Types</h2>
	 *
	 * <table class="table table-bordered" style="text-align:center;">
	 * <tbody><tr>
	 * <th style="width:25%;text-align:center;">Mapping type</th>
	 * <th style="text-align:center;">Java type</th>
	 * <th style="width:25%;text-align:center;">ANSI SQL Type</th>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align:middle;">binary</td>
	 * <td style="vertical-align:middle;">byte[]</td>
	 * <td>VARBINARY (or BLOB)</td>
	 * </tr>
	 * <tr>
	 * <td>text</td>
	 * <td>java.lang.String</td>
	 * <td>CLOB</td>
	 * </tr>
	 * <tr>
	 * <td style="vertical-align:middle;">serializable</td>
	 * <td>any Java class that implements java.io.Serializable</td>
	 * <td>VARBINARY (or BLOB)</td>
	 * </tr>
	 * <tr>
	 * <td>clob</td>
	 * <td>java.sql.Clob</td>
	 * <td>CLOB</td>
	 * </tr>
	 * <tr>
	 * <td>blob</td>
	 * <td>java.sql.Blob</td>
	 * <td>BLOB</td>
	 * </tr>
	 * </tbody></table>
	 *
	 *
	 * @return
	 * @see https://www.tutorialspoint.com/hibernate/hibernate_mapping_types.htm
	 */
	String getHibernateType() {
		return mapHibernateCastType[control](this)
	}

	/**
	 * <p>Returns the correct groovy class for a field Spec to be used in filtering rows.</p>
	 *
	 * @see net.transitionmanager.search.FieldSearchData
	 * @return a {@code Class < ? >} type instance
	 */
	Class<?> getClassType() {
		return mapClassType[control](this)
	}

	/**
	 * <p>Creates the custom cast sentence for FieldSpecs to be used in HQL queries</p>
	 * <p>It calculates Hibernate type, and prepares cast defintion using standard hibernate cast function + type
	 * or a custom one using custom_big_decimal.</p>
	 *
	 * @param property
	 * @return a HSQL cast sentence to be used in DataviewService
	 * @see com.tdsops.common.sql.BigDecimalSQLFunction
	 * @see net.transitionmanager.service.DataviewService#propertyFor(java.util.Map)
	 */
	String getHibernateCastSentence(String property) {

		String castSentence
		String hibernateType = getHibernateType()

		switch (control) {
			case 'Number':
				if (hibernateType == CAST_BIG_DECIMAL_FUNCTION) {
					castSentence = "$CAST_BIG_DECIMAL_FUNCTION($property, $precision)"
				} else {
					castSentence = "cast($property as $hibernateType)"
				}
				break
			default:
				castSentence = "cast($property as $hibernateType)"
				break
		}

		return castSentence
	}

	/**
	 * <p>This Map determines the class type to be used in FieldSearchData.</p>
	 * If key used in this Map is 'Number', it is necessary to define, by precision,
	 * if field spec belongs to a Long or a Decimal number<br>
	 * <p>It creates Number class based on FieldSpec constraints definition</p>
	 * <pre>
	 * 	"constraints": {
	 * 		"maxRange": 100,
	 *      "minRange": 0,
	 *      "precision": 2,
	 *      "separator": true,
	 *      "allowNegative": true,
	 *      "required": 0
	 *  }
	 * </pre>
	 * Then It can returns this:
	 * <pre>
	 *     return mapClassType[control](new FieldSpec(control: 'Date')) == java.util.Date
	 *     return mapClassType[control](new FieldSpec(control: 'DateTime')) == java.sql.Timestamp
	 *     return mapClassType[control](new FieldSpec(control: 'Number', precision: 0)) == java.lang.Long
	 *     return mapClassType[control](new FieldSpec(control: 'Number', precision: 2)) == java.math.BigDecimal
	 * </pre>
	 * * @see FieldSpec#getClassType()
	 */
	static Map<String, Closure<Object>> mapClassType = [
		'Number'  : { FieldSpec fieldSpec -> fieldSpec.precision > 0 ? BigDecimal : Long },
		'Date'    : { FieldSpec fieldSpec -> Date },
		'DateTime': { FieldSpec fieldSpec -> Timestamp },
	].withDefault { String key -> { FieldSpec fieldSpec -> String } }.asImmutable()

	/**
	 * <p>This Map determines hibernate cast type for FieldSearchData.</p>
	 * If key used in this Map is Number, it is necessary to define, defined by precision,
	 * if field spec is going to user a custom cast function ({@code CAST_BIG_DECIMAL_FUNCTION}) or a simple cast hibernate type
	 *
	 * @see FieldSpec#getHibernateType()
	 * @see com.tdsops.common.sql.BigDecimalSQLFunction
	 */
	static Map<String, Closure<Object>> mapHibernateCastType = [
		'Number'  : { FieldSpec fieldSpec -> fieldSpec.precision > 0 ? CAST_BIG_DECIMAL_FUNCTION : LongType.INSTANCE.name },
		'Date'    : { FieldSpec fieldSpec -> DateType.INSTANCE.name },
		'DateTime': { FieldSpec fieldSpec -> TimestampType.INSTANCE.name },
	].withDefault { String key ->
		{ FieldSpec fieldSpec -> StringType.INSTANCE.name }
	}.asImmutable()
}
