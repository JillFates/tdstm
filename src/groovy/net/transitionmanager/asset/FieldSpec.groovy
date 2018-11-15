package net.transitionmanager.asset

import java.sql.Timestamp

/**
 * <pre>
 *{*       "control": "String",
 *       "default": "",
 *       "field": "custom10",
 *       "imp": "N",
 *       "label": "Custom10",
 *       "order": 110,
 *       "shared": 0,
 *       "show": 1,
 *       "tip": "",
 *       "udf": 1,
 *{* "constraint: {* "maxRange": 100,
 *         "minRange": 0,
 *         "precision": 2,
 *         "separator": true,
 *         "allowNegative": true
 *         "required": 0
 *}*}* </pre>
 */
class FieldSpec {

	/**
	 * Used for contro field
	 */
	String type
	Object defaultValue
	String field
	String label

	// Constraints field section
	Integer max
	Integer min
	Integer precision
	Boolean separator
	Boolean allowNegative
	Integer required

	FieldSpec(Map fieldSpecMap) {
		this.type = fieldSpecMap.control
		this.defaultValue = fieldSpecMap."default"
		this.field = fieldSpecMap.field
		this.label = fieldSpecMap.label

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
		return this.field.startsWith('custom')
	}

	Boolean isNumeric() {
		// TODO: add logic to define all posible numeric value
		return this.type in ['Long', 'Integer', 'Number', 'Decimal']
	}

	/**
	 * <pre>
	 * 	"constraints": {* 		"maxRange": 100,
	 *      "minRange": 0,
	 *      "precision": 2,
	 *      "separator": true,
	 *      "allowNegative": true,
	 *      "required": 0
	 *},
	 *  </pre>
	 * @return
	 */
	String buildNumericHibernateType() {
		if (separator && precision) {
			return 'float'
		} else {
			return 'long'
		}
	}

	Class<?> buildNumericClass() {
		if (separator && precision) {
			return Float
		} else {
			return Long
		}
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

		String hibernateType = ''

		switch (type) {
			case 'Number':
			case 'Decimal':
			case 'Integer':
			case 'Long':
				hibernateType = buildNumericHibernateType()
				break
			case 'Date':
				hibernateType = 'date'
				break
			case 'DateTime':
				hibernateType = 'timestamp'
				break
			case 'String':
				hibernateType = ''
				break
		}

		return hibernateType
	}

	Class<?> getClassType() {

		Class<?> classType = null

		switch (type) {
			case 'Number':
			case 'Decimal':
				classType = buildNumericClass()
				break
			case 'Date':
				classType = Date
				break
			case 'DateTime':
				classType = Timestamp
				break
			case 'String':
				classType = String
				break
		}

		return classType
	}
}
