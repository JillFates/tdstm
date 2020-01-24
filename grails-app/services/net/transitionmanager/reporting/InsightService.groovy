package net.transitionmanager.reporting

import com.tdsops.tm.enums.domain.AssetClass
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
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
	 * Gets the top count of assets and dependencies  by provider, limited to the max number of rows.
	 *
	 * @param project The project to get the assets by vendor for.
	 * @param max The max amount of rows to return.
	 *
	 * @return A List of Maps containing the provider name, the count of assets and the count of dependencies.
	 */
	List<Map> assetsAndDependenciesByProvider(Project project, Integer max) {
		namedParameterJdbcTemplate.queryForList('''
			SELECT 
				p.name AS Name,
				SUM(CASE WHEN ib.domain_class_name = 'Dependency' THEN 1 ELSE 0 END) AS Dependencies, 
				SUM(CASE WHEN ib.domain_class_name <> 'Dependency' THEN 1 ELSE 0 END) AS Assets 
			FROM import_batch ib
			JOIN import_batch_record ibr ON ibr.import_batch_id = ib.import_batch_id
			JOIN provider p ON p.provider_id = ib.provider_id
			WHERE ibr.status = 'COMPLETED' AND 
				ib.project_id = :project AND
				ib.provider_id is not null
			GROUP BY ib.provider_id
			ORDER BY count(*) desc, p.name desc
			Limit :max
		''', [
			project: project.id,
			max    : max
		]) as List<Map>
	}


	/**
	 * Gets top  List of tags and there count, limited to the max number of rows.
	 *
	 * @param project The project to get the top tags for.
	 * @param max The max amount of rows to return.
	 *
	 * @return a List of maps, containing the tag name, and its count.
	 */
	List<Map> topTags(Project project, Integer max) {
		Tag.executeQuery('''
			SELECT  new MAP(t.id as Id, t.name as Name, count(ta.id) as Count)
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
	 * Gets a list of the count of assets broken down by os and environment.
	 *
	 * @param project The project to get the assets for
	 *
	 * @return A list contains the count of assets, by os and environment.
	 */
	List<Map> assetsByOsAndEnvironment(Project project) {
		namedParameterJdbcTemplate.queryForList('''
			SELECT 
				count(*) AS 'Count',
				ae.os as OS,
				ae.environment AS Environment
			FROM asset_entity ae
			WHERE ae.project_id = :project
			GROUP BY 
				ae.os, 
				ae.environment
			ORDER BY 
				os, 
				environment, 
				count(*)
		''', [
			project: project.id
		]) as List<Map>
	}

	/**
	 * Gets the count of devices per event.
	 *
	 * @param project The project to get the devices for by event.
	 * @param max The max amount of rows to return.
	 *
	 * @return A list of maps containing the event name and the count of devices for that event.
	 */
	List<Map> devicesByEvent(Project project, Integer max) {
		namedParameterJdbcTemplate.queryForList('''
			SELECT 
				me.name AS Name,
				count(a.asset_entity_id) as Devices
			FROM move_event me
			JOIN asset_comment ac on ac.move_event_id = me.move_event_id
			JOIN asset_entity a on a.asset_entity_id = ac.asset_entity_id
			WHERE a.asset_class = 'DEVICE' AND a.project_id = :project
			GROUP by me.name
			ORDER BY count(a.asset_entity_id) desc
			Limit :max
		''', [
			project: project.id,
			max    : max
		]) as List<Map>
	}

	/**
	 * Gets assets imported by type and provider including the total, processed, pending and error rows.
	 *
	 * @param project The project to get assets for by provider and asset type.
	 *
	 * @return A list of Maps containing the asset type, the provider, the total count, count of the process rows, count of the pending rows
	 * and a count of the error rows.
	 */
	List<Map> AssetsByProviderAndAssetType(Project project) {
		namedParameterJdbcTemplate.queryForList('''
			SELECT 
				ib.domain_class_name AS 'Asset Type',
				p.name AS 'Provider',
				COUNT(*) AS Total,
				SUM(CASE WHEN ibr.status = 'COMPLETED' THEN 1 ELSE 0 END) AS Processed,
				SUM(CASE WHEN ibr.status <> 'COMPLETED' THEN 1 ELSE 0 END) AS Pending,
				SUM(CASE WHEN ibr.error_count > 0 THEN 1 ELSE 0 END) AS Errors
			FROM import_batch ib
			JOIN provider p ON p.provider_id = ib.provider_id
			JOIN import_batch_record ibr ON ibr.import_batch_id = ib.import_batch_id
			WHERE 
				ib.project_id = :project and 
				ib.domain_class_name  in('Dependency','Application', 'Database', 'Device', 'Storage')
			GROUP by
				ib.domain_class_name,
				p.name
			ORDER by ib.project_id,
				p.name,
				ib.domain_class_name,
				ib.status;
		''', [
			project: project.id,
		]) as List<Map>
	}

	/**
	 * Gets a count of assets that have a dependencies below the low range, between the high and low range, above the high range, and Orphaned dependencies.
	 *
	 * @param project The project to get the applications grouped by dependencies for.
	 * @param lowRange The low range o dependencies to group by.
	 * @param highRange The high range o dependencies to group by.
	 *
	 * @return a List of maps, containing The number of assets that have less that the low range, between the low and high range,
	 * above the high rage of dependencies, and Orphaned dependencies.
	 */
	List<Map> applicationsGroupedByDependencies(Project project, Integer lowRange, Integer highRange) {
		namedParameterJdbcTemplate.queryForList("""
				SELECT 'Orphaned' AS level, count(*) AS count
				FROM (
					SELECT ae.asset_entity_id, count(distinct ad.asset_dependency_id) + count(distinct ad2.asset_dependency_id)
					FROM asset_entity ae
					LEFT OUTER JOIN asset_dependency ad ON ad.asset_id = ae.asset_entity_id
					LEFT OUTER JOIN asset_dependency ad2 ON ad2.dependent_id = ae.asset_entity_id
					WHERE ae.project_id = :projectId and asset_class = '${AssetClass.APPLICATION.name()}'
					GROUP BY ae.asset_entity_id
					HAVING count(distinct ad.asset_dependency_id) + count(distinct ad2.asset_dependency_id) = 0
				) AS count

				UNION 

				SELECT '1-${lowRange - 1} dependencies' AS level, count(*) AS count
				FROM (
					SELECT ae.asset_entity_id, count(distinct ad.asset_dependency_id) + count(distinct ad2.asset_dependency_id)
					FROM asset_entity ae
					LEFT OUTER JOIN asset_dependency ad ON ad.asset_id = ae.asset_entity_id
					LEFT OUTER JOIN asset_dependency ad2 ON ad2.dependent_id = ae.asset_entity_id
					WHERE ae.project_id = :projectId and asset_class = '${AssetClass.APPLICATION.name()}'
					GROUP BY ae.asset_entity_id
					HAVING count(distinct ad.asset_dependency_id) + count(distinct ad2.asset_dependency_id) <$lowRange AND 
					count(distinct ad.asset_dependency_id) + count(distinct ad2.asset_dependency_id) > 0 
				) AS count

				UNION

				SELECT '$lowRange-$highRange dependencies' AS level, count(*) AS count
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

				SELECT '${highRange + 1}+ dependencies' AS level, count(*) AS count
				FROM (
					SELECT  ae.asset_entity_id , count(distinct ad.asset_dependency_id) + count(distinct ad2.asset_dependency_id)
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
