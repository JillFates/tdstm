package net.transitionmanager.asset

/**
 * <pre>
 * 	{
 *       "control": "String",
 *       "default": "",
 *       "field": "custom10",
 *       "imp": "N",
 *       "label": "Custom10",
 *       "order": 110,
 *       "shared": 0,
 *       "show": 1,
 *       "tip": "",
 *       "udf": 1,
 *     {
 *       "constraint: {
 *         "maxRange": 100,
 *         "minRange": 0,
 *         "precision": 2,
 *         "separator": true,
 *         "allowNegative": true
 *         "required": 0
 *       }
 * }
 * </pre>
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

		this.max = fieldSpecMap.max
		this.min = fieldSpecMap.min
		this.precision = fieldSpecMap.precision
		this.separator = fieldSpecMap.separator
		this.allowNegative = fieldSpecMap.allowNegative
		this.required = fieldSpecMap.required
	}

	/**
	 *
	 * @return
	 */
	Boolean isCustom(){
		return this.field.startsWith('custom')
	}

	Boolean isNumeric(){
		// TODO: add logic to define all posible numeric value
		return this.type in ['Long', 'Integer', 'Number', 'Decimal']
	}
}
