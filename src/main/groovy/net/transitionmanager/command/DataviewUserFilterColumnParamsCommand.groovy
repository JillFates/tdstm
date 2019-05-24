package net.transitionmanager.command

import com.tdsops.tm.enums.domain.AssetClass

/**
 * <p>It represents a column part in the dataview filters used in the client side to filter a dataview content.</p>
 * <pre>
 * "columns": [
 * 		["domain": "common", "edit": false, "filter": "", "label": "Name", "locked": true, "property": "assetName", "width": 220],
 * 	  ...
 * </pre>
 * @see {@code DataviewUserParamsCommand}
 * @see {@code DataviewUserFilterParamsCommand}
 */
class DataviewUserFilterColumnParamsCommand implements CommandObject {

	String domain
	String edit
	String filter
	String label
	String locked
	String property
	String width

	static constraints = {
		domain nullable: false, inList: ['common', 'application', 'database', 'device', 'storage']
		edit nullable: false
		filter nullable: false
		label nullable: false
		locked nullable: false
		property nullable: false
		width nullable: false
	}
}
