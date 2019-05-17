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
	 * named parameter from UI
	 * <pre>{named: 'server,toValidate'} </pre>
	 */
	String named
	/**
	 * List of named filters parse and split it up by comma character
	 */
	List<String> namedFilterList
	List<DataviewExtraFilterParamsCommand> extra

	static constraints = {
		domains nullable: false
		columns nullable: false
		named nullable: true, validator: { val, obj ->
			if (val) {
				obj.namedFilterList = val.split(',').toList()
			}
			return true
		}
		namedFilterList nullable: true
		extra nullable: true, validator: { val, obj ->
			if (val) {
				return val.every { (it as DataviewExtraFilterParamsCommand).validate() }
			}
			return true
		}
	}
}
