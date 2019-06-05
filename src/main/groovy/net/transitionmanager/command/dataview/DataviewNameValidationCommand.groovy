package net.transitionmanager.command.dataview

import net.transitionmanager.command.CommandObject


class DataviewNameValidationCommand implements CommandObject{

	Long dataViewId
	String name

	static constraints = {
		dataViewId nullable: true
		name nullable: false, blank: false
	}
}
