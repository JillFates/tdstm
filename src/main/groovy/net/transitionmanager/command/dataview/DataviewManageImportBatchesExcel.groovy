package net.transitionmanager.command.dataview

import net.transitionmanager.command.CommandObject

/**
 * The {@code DataviewManageImportBatchesExcel} represents the various
 * parameters that make up the properties that the user can override in the view.
 * It is defined by a nested {@code CommandObject} to refer validations of input data.
 */
class DataviewManageImportBatchesExcel implements CommandObject {
	int max
	int offset

	static constraints = {
		max nullable: false, inList: [0, 25, 50, 100, 250]
		offset nullable: true, min: 0
	}
}
