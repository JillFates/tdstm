package net.transitionmanager.service

import com.tds.asset.AssetEntity
import com.tdssrc.grails.NumberUtil
import grails.transaction.Transactional
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project

@Transactional
class BulkChangePersonService implements ServiceMethods {

	/**
	 * Bulk replace asset entity specified field with given numeric value
	 *
	 * @param person - new person field value
	 * @param fieldName - field name
	 * @param assetIds - list of assets to update
	 * @param assetIdsFilterQuery - additional assets query filter
	 */
	void bulkReplace(Person person, String fieldName, List<Long> assetIds = [], Map assetIdsFilterQuery = null) {
		if (!person) {
			throw new InvalidParamException('New person value cannot be null')
		}

		bulkUpdate(person, fieldName, assetIds, assetIdsFilterQuery)
	}

	/**
	 * Bulk clear asset entity specified field
	 *
	 * @param fieldName - field name
	 * @param assetIds - list of assets to update
	 * @param assetIdsFilterQuery - additional assets query filter
	 */
	void bulkClear(Person person, String fieldName, List<Long> assetIds = [], Map assetIdsFilterQuery = null) {
		bulkUpdate(null, fieldName, assetIds, assetIdsFilterQuery)
	}

	/**
	 * Parse the given person id to determine if it belongs to current project or it has access to it
	 *
	 * @param personId - a person id
	 * @param currentProject - current project
	 * @return - person if found
	 */
	String coerceBulkValue(Project currentProject, String personId) {
		if (NumberUtil.isNumber(value)) {
			return null
		}

		// get person from database by id
		Long pId = NumberUtil.toPositiveLong(personId, 0)
		Person person = Person.where { id ==  pid }

		// person was found, see if it has a user login
		if (person && person?.userLogin) {
			// obtain found person current project
			Project foundPersonProject = person.getUserLogin().getCurrentProject()
			if (foundPersonProject) {
				// if found person current project is the same
				// as the current project passed by param, them return found person
				if (currentProject.id == foundPersonProject.getId()) {
					return person
				}
			}
		}
		return null
	}

	/**
	 * Bulk update asset entity specified field with given value
	 *
	 * @param value - new value
	 * @param fieldName - field name
	 * @param assetIds - list of assets to update
	 * @param assetIdsFilterQuery - additional assets query filter
	 */
	private void bulkUpdate(Person person, String fieldName, List<Long> assetIds = [], Map assetIdsFilterQuery = null) {
		String queryForAssetIds
		String setFieldQueryPart
		Map params = [:]
		Map assetQueryParams = [:]

		if (assetIds && !assetIdsFilterQuery) {
			queryForAssetIds = ':assetIds'
			params.assetIds = assetIds
			assetQueryParams['assetIds'] = assetIds
		} else {
			queryForAssetIds = assetIdsFilterQuery.query
			params << assetIdsFilterQuery.params
			assetQueryParams = assetIdsFilterQuery.params
		}

		if (person) {
			params.value = person
			setFieldQueryPart = "SET ${fieldName} = :value"
		} else {
			setFieldQueryPart = "SET ${fieldName} = NULL"
		}

		String query = """
			UPDATE AssetEntity ${setFieldQueryPart}  
			WHERE id IN ($queryForAssetIds)
		"""

		AssetEntity.executeUpdate(query, params)
	}
}
