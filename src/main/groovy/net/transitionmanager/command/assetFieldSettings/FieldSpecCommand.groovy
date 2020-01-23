package net.transitionmanager.command.assetFieldSettings

import com.tdsops.tm.enums.ControlType
import net.transitionmanager.command.CommandObject

/**
 * The command object that represents all of the properties of an individual field in the asset field specs
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
