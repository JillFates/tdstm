package net.transitionmanager.command.dataview

import net.transitionmanager.command.CommandObject

/**
 * The command object that represents top level of the Dataview Schema
 */

class DataviewSchemaColumnCommand implements CommandObject {
	String filter = ''
	Boolean edit = true
	String domain
	String property
	String label = ''
	Boolean locked = false
	Integer width

	static constraints = {
		domain inList: ['common', 'application', 'database', 'device', 'storage']
		filter blank: true
		label blank: true
	}
}
