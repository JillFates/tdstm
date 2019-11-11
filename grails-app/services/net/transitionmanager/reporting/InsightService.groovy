package net.transitionmanager.reporting

import com.tdsops.tm.enums.domain.AssetClass
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import net.transitionmanager.asset.AssetDependency
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.project.Project
import net.transitionmanager.service.ServiceMethods
import net.transitionmanager.tag.Tag
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

/**
 * Methods for getting data for the Insight dashboard.
 */
@CompileStatic
@Transactional(readOnly = true)
class InsightService implements ServiceMethods {
	NamedParameterJdbcTemplate namedParameterJdbcTemplate

	/**
	 * Gets the top count of assets by vendor, limited to the max number of rows.
	 *
	 * @param project The project to get the assets by vendor for.
	 * @param max The max amount of rows to return.
	 *
	 * @return A List of Maps containing the vendor name, and the count of assets.
	 */
	List<Map> assetsByVendor(Project project, Integer max) {
		AssetEntity.executeQuery('''
			SELECT new MAP(m.name as name, COUNT(*) as count)
			FROM AssetEntity a
			JOIN a.manufacturer as m
			WHERE a.project = :project
			GROUP BY m.name
			ORDER BY COUNT(*) desc, m.name  desc
		''', [
			project: project
		], [max: max])
	}

	/**
	 * Gets top  List count dependencies by vendor, limited to the max number of rows.
	 *
	 * @param project The project to get the dependencies by vendor for.
	 * @param max The max amount of rows to return.
	 *
	 * @return A list of Maps containing the vendor name and the count of dependencies.
	 */
	List<Map> dependenciesByVendor(Project project, Integer max) {
		AssetDependency.executeQuery('''
			SELECT new MAP(m.name as name, COUNT(*) as count)
			FROM AssetDependency ad
			JOIN ad.asset as a
			JOIN a.manufacturer as m
			WHERE a.project = :project
			GROUP BY m.name
			ORDER BY COUNT(*) desc, m.name  desc
		''', [
			project: project
		], [max: max])
	}


	/**
	 * Gets top  List of tags and there count, limited to the max number of rows.
	 *
	 * @param projectThe project to get the top tags for.
	 * @param max The max amount of rows to return.
	 *
	 * @return a List of maps, containing the tag name, and its count.
	 */
	List<Map> topTags(Project project, Integer max) {
		Tag.executeQuery('''
			SELECT  new MAP(t.id as id, t.name as name, count(ta.id) as count)
			FROM Tag t
			JOIN t.tagAssets as ta
			WHERE t.project = :project
			GROUP BY t.name
			ORDER BY count(*) desc
		''', [
			project: project
		], [max: max])
	}

	/**
	 * Gets a count of assets that have a dependencies below the low range, between the high and low range, and above the high range.
	 *
	 * @param project The project to get the applications grouped by dependencies for.
	 * @param lowRange The low range o dependencies to group by.
	 * @param highRange The high range o dependencies to group by.
	 *
	 * @return a List of maps, containing The number of assets that have less that the low range, between the low and high range, and above the high rage of dependencies.
	 */
	List<Map> applicationsGroupedByDependencies(Project project, Integer lowRange, Integer highRange) {
		namedParameterJdbcTemplate.queryForList("""
				SELECT '>$lowRange' AS level, count(*) AS count
				FROM (
					SELECT ae.asset_entity_id, count(distinct ad.asset_dependency_id) + count(distinct ad2.asset_dependency_id)
					FROM asset_entity ae
					LEFT OUTER JOIN asset_dependency ad ON ad.asset_id = ae.asset_entity_id
					LEFT OUTER JOIN asset_dependency ad2 ON ad2.dependent_id = ae.asset_entity_id
					WHERE ae.project_id = :projectId and asset_class = '${AssetClass.APPLICATION.name()}'
					GROUP BY ae.asset_entity_id
					HAVING count(distinct ad.asset_dependency_id) + count(distinct ad2.asset_dependency_id) <$lowRange
				) AS count

				UNION

				SELECT '$lowRange>$highRange' AS level, count(*) AS count
				FROM (
					SELECT ae.asset_entity_id, count(distinct ad.asset_dependency_id) + count(distinct ad2.asset_dependency_id)
					FROM asset_entity ae
					LEFT OUTER JOIN asset_dependency ad ON ad.asset_id = ae.asset_entity_id
					LEFT OUTER JOIN asset_dependency ad2 ON ad2.dependent_id = ae.asset_entity_id
					WHERE ae.project_id = :projectId and asset_class = '${AssetClass.APPLICATION.name()}'
					GROUP BY ae.asset_entity_id
					HAVING (count(distinct ad.asset_dependency_id) + count(distinct ad2.asset_dependency_id)) >=$lowRange AND ( count(distinct ad.asset_dependency_id) + count(distinct ad2.asset_dependency_id)) <$highRange
				) AS count

				UNION

				SELECT '$highRange+' AS level, count(*) AS count
				FROM (
					SELECT  ae.asset_entity_id, count(distinct ad.asset_dependency_id) + count(distinct ad2.asset_dependency_id)
					FROM asset_entity ae
					LEFT OUTER JOIN asset_dependency ad ON ad.asset_id = ae.asset_entity_id
					LEFT OUTER JOIN asset_dependency ad2 ON ad2.dependent_id = ae.asset_entity_id
					WHERE ae.project_id = :projectId and asset_class = '${AssetClass.APPLICATION.name()}'
					GROUP BY ae.asset_entity_id
					HAVING (count(distinct ad.asset_dependency_id) + count(distinct ad2.asset_dependency_id)) >$highRange
				) AS count;
		""", [
			projectId: project.id
		]) as List<Map>
	}
}
