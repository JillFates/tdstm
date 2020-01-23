package net.transitionmanager.command.dataview

import com.tdsops.tm.enums.ControlType
import net.transitionmanager.command.CommandObject

/**
 * The command object that represents top level of the Dataview Schema
 */

class DataviewSchemaColumnCommand implements CommandObject {
	/*
	 * property constraints is mapped to columnConstraints because of the 'static constraints' name conflict
	 */
	Map columnConstraints
	ControlType control
	/*
	 * property default is mapped to defaultValue because default is a reserved word
	 */
	String defaultValue
	String field
	String imp
	String label
	Integer order
	Integer shared
	Integer show
	String tip
	Integer udf
	List<String> bulkChangeActions

	void setDefault(String value) {
		defaultValue = value
	}

	/**
	 * Used to convert the control string to the Enum appropriately
	 * @param c
	 */
	void setControl(String c) {
		control = ControlType.asEnum(c)
	}

	/**
	 * Used to set the json.constraints to columnnConstraints due to the conflict with the static constraints property
	 * @param constraints
	 */
	void setConstraints(Map constraints) {
		columnConstraints = constraints
	}

	static constraints = {
		defaultValue nullable: true

		bulkChangeActions validator: { value, object ->
			List validActions = ['clear', 'replace']
			if ( (value - validActions).size() == 0) {
				return true
			}
			return ['dataview.validate.schemaBulkChangeActions', validActions]
		}
	}
}
