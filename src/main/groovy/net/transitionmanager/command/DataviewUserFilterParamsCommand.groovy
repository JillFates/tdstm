package net.transitionmanager.command

/**
 *
 */
class DataviewUserFilterParamsCommand implements CommandObject {

	/**
	 *
	 */
	List<String> domains
	List<DataviewUserFilterColumnParamsCommand> columns
	String named
	List<String> namedFilterList
	List<DataviewExtraFilterParamsCommand> extra

	static constraints = {
		domains nullable: false
		columns nullable: false//, validator: DataviewUserParamsCommand.subValidator
		named nullable: true, validator: { val, obj ->
			if (val) {
				obj.namedFilterList = val.split(',').toList()
			}
			return true
		}
		namedFilterList nullable: true
		extra nullable: true, validator: { val, obj ->
			if (val) {
				return val.every { it.validate() }
			}
			return true
		}
	}
}
