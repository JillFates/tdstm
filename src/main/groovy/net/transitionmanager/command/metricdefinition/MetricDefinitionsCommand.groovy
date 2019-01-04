package net.transitionmanager.command.metricdefinition

import net.transitionmanager.command.CommandObject


/**
 * A Command object to contain a list of metric definitions(JSON), and validate them.
 */

class MetricDefinitionsCommand implements CommandObject{
	List<MetricDefinitionCommand> definitions
	String metricCodes

	static constraints = {
		definitions cascade: true
		metricCodes nullable: true
	}

	/**
	 * Converts the Command Object to a map, so that it can be serialized  back to JSON
	 *
	 * @return A Map containing a list of Metric Definitions.
	 */
	Map toMap(){
		[definitions :definitions.collect{MetricDefinitionCommand definition->
			return definition.toMap()
		}]
	}

	List<String> testCodes() {
		if (metricCodes) {
			return metricCodes.split(',')*.trim()
		}

		return []
	}
}
