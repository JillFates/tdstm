package net.transitionmanager.command



/**
 * Command Object for dealing with changes of MoveBundle in
 * dependends on /supports dropdowns.
 */

class BundleChangeCommand implements CommandObject{

	Long assetId
	Long dependencyId
	String type

	static constraints = {
		assetId nullable: false
		dependencyId nullable: false
		type nullable: false, blank: false, validator: { type ->
			return type in ['support', 'dependent']
		}
	}
}
