package net.transitionmanager.command

/**
 *
 */
class DataviewUserFilterParamsCommand implements CommandObject {

	/**
	 * List of domains defined in {@code AssetClass.domainAssetTypeList}
	 */
	List<String> domains
	List<DataviewUserFilterColumnParamsCommand> columns
	/**
	 * List of named filters parse and split it up by comma character
	 */
	List<DataviewExtraFilterParamsCommand> extra

	static constraints = {
		domains nullable: false
		columns nullable: false
		extra nullable: true, validator: { val, obj ->
			if (val) {
				return val.every { (it as DataviewExtraFilterParamsCommand).validate() }
			}
			return true
		}
	}
}
