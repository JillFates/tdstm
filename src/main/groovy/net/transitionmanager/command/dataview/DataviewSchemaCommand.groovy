package net.transitionmanager.command.dataview


import net.transitionmanager.command.CommandObject
import net.transitionmanager.command.dataview.DataviewSchemaColumnCommand

/**
 * The command object that represents top level of the Dataview Schema
 */

class DataviewSchemaCommand implements CommandObject {
	List<String> domains
	Map sort
	List<DataviewSchemaColumnCommand> columns = []

	static constraints = {
		domains validator: { value, object ->
			List validDomains = ['common', 'application', 'device', 'database', 'storage']
			if ( (value - validDomains).size() == 0) {
				return true
			}
			return ['dataview.validate.schemaDomain', validDomains]
		}
	}
}
