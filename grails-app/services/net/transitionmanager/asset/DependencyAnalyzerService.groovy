package net.transitionmanager.asset

import com.tdssrc.grails.NumberUtil
import net.transitionmanager.command.dependency.analyzer.FilteredAssetsCommand
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.service.ServiceMethods
import net.transitionmanager.tag.TagService
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

import java.util.regex.Pattern

/**
 * A service to support getting assets to be highlighted by in the dependency analyzer.
 */
class DependencyAnalyzerService implements ServiceMethods {
	TagService   tagService
	JdbcTemplate jdbcTemplate

	/**
	 * Gets a filtered list of assets based on the FilteredAssetsCommand object.
	 *
	 * @param filter includes the dependency Group, a personId and tagIds used for filtering.
	 * @param projectId project id to filter for multi tenancy.
	 *
	 * @return a List of maps containing the assets to be highlighted.
	 */
	List<Map> getAssets(FilteredAssetsCommand filter, String projectId) {
		Map queryParams = [projectId: projectId]

		String tagQuery = tagService.getTagsQuery(filter.tagIds, filter.tagMatch, queryParams)
		String tagJoins = tagService.getTagsJoin(filter.tagIds, filter.tagMatch)
		String dependencyQuery = getDependencyQuery(filter.depGroup, queryParams)
		String personQuery = getPersonQuery(filter.personId, queryParams)

		String query = """
			SELECT a.asset_entity_id AS assetId, a.asset_name AS assetName FROM asset_entity a
			LEFT OUTER JOIN application app ON app.app_id = a.asset_entity_id
			INNER JOIN asset_dependency_bundle adb ON adb.asset_id = a.asset_entity_id
			LEFT OUTER JOIN person p ON p.person_id IN (app.sme_id, app.sme2_id, a.app_owner_id)
			$tagJoins
			WHERE adb.project_id = :projectId $tagQuery $dependencyQuery $personQuery
			Group by a.asset_entity_id
		"""

		NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource())
		return template.queryForList(query.toString(), queryParams)
	}

	/**
	 * Gets the query string for the dependency group, and updates the query params to be used.
	 *
	 * @param dependencyGroup the dependency bundle
	 * @param queryParams the query params to be used by getAssets
	 *
	 * @return the query to filter for the dependency string
	 */
	private String getDependencyQuery(String dependencyGroup, Map queryParams) {
		if (dependencyGroup != 'onePlus') {
			dependencyGroup = NumberUtil.toPositiveLong(dependencyGroup, null)
		}

		if (dependencyGroup == 'onePlus') {
			return ' AND adb.dependency_bundle > 0'
		} else if (dependencyGroup != null) {
			queryParams.dependencyGroup = dependencyGroup
			return ' AND adb.dependency_bundle = :dependencyGroup'
		}

		return dependencyGroup ?: ''
	}

	/**
	 *Gets the query string for filtering by a person id.
	 *
	 * @param personId the person id to filter by
	 * @param queryParams the query params to be used by getAssets.
	 *
	 * @return the query to filter by a person id.
	 */
	private String getPersonQuery(Long personId, Map queryParams) {
		if (personId) {
			queryParams.personId = personId
			return ' AND p.person_id = :personId'
		}

		return ''
	}

	/**
	 * Filters a list of assets using a name filter, which could be a regex.
	 *
	 * @param nameFilter a filter string which could be a regex
	 * @param assets a list of assets to filter
	 * @param isRegex true is the name filter is a regex, false otherwise.
	 *
	 * @return a filtered list of assets based on the nameFilter passed.
	 */
	List<Map> filterAssets(String nameFilter, List<Map> assets, boolean isRegex) {
		if(!nameFilter){
			return assets*.assetId
		}

		List assetList = []
		Pattern nameRegex

		try {
			nameRegex = ~"${nameFilter}"
		} catch (e) {
			throw new InvalidParamException('The search was an invalid regex expression')
		}

		for (Map asset in assets) {
			if (isRegex && asset.assetName.matches(nameRegex)) {
				assetList << asset.assetId
			} else if (!isRegex && asset.assetName.toUpperCase().contains(nameFilter)) {
				assetList << asset.assetId
			}
		}

		return assetList
	}
}
