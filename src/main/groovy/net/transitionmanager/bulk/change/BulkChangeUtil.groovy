package net.transitionmanager.bulk.change

import com.tdsops.tm.enums.domain.AssetClass
import net.transitionmanager.service.InvalidParamException

/**
 * Class for common methods used by bulk change classes.
 */
class BulkChangeUtil {

	/**
	 * Gets the query for ids to update, for bulk change, based on type.
	 *
	 * @param type the type used to get the query for.
	 * @param ids ids for the query, optional.
	 * @param idsFilterQuery the query filter map, which has the query and params.
	 * @param params params to update based on the query.
	 *
	 * @return the query to use to get the ids for bulk update.
	 */
	static String getIdsQuery(Class type, List<Long> ids = [], Map idsFilterQuery = null, Map params) {
		String queryForIds = ''

		switch (type) {
			case AssetClass.domainClassFor(AssetClass.APPLICATION):
			case AssetClass.domainClassFor(AssetClass.DATABASE):
			case AssetClass.domainClassFor(AssetClass.DEVICE):
			case AssetClass.domainClassFor(AssetClass.STORAGE):
				if (ids && !idsFilterQuery) {
					queryForIds = ':assetIds'
					params.assetIds = ids
				} else {
					queryForIds = idsFilterQuery.query
					params << idsFilterQuery.params
				}

				break
			default:
				throw new InvalidParamException("Bulk change query is not supported for ${type.simpleName}")
		}

		return queryForIds
	}
}
