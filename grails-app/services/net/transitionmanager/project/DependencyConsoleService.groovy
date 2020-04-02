package net.transitionmanager.project

import com.tdsops.tm.asset.graph.AssetGraph
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.AssetDependencyStatus
import com.tdsops.tm.enums.domain.AssetEntityPlanStatus
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdsops.tm.enums.domain.ValidationType
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WebUtil
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import net.transitionmanager.asset.AssetDependency
import net.transitionmanager.asset.AssetEntityService
import net.transitionmanager.asset.AssetOptions
import net.transitionmanager.asset.AssetOptionsService
import net.transitionmanager.asset.AssetType
import net.transitionmanager.common.ProgressService
import net.transitionmanager.person.UserPreferenceService
import net.transitionmanager.security.SecurityService
import net.transitionmanager.service.ServiceMethods
import net.transitionmanager.tag.TagService
import org.springframework.jdbc.core.JdbcTemplate

/**
 * A class to handle getting data form the dependency console.
 */
@Transactional
class DependencyConsoleService implements ServiceMethods {

	AssetEntityService    assetEntityService
	JdbcTemplate          jdbcTemplate
	UserPreferenceService userPreferenceService
	TagService            tagService
	AssetOptionsService   assetOptionsService
	SecurityService       securityService
	ProgressService       progressService

	/**
	 * Performs an analysis of the interdependencies of assets for a project and creates assetDependencyBundle records appropriately. It will
	 * find all assets assigned to bundles that which are set to be used for planning, sorting the assets so that those with the most dependency
	 * relationships are processed first.
	 * @param projectId : Related project
	 * @param connectionTypes : filter for asset types
	 * @param statusTypes : filter for status types
	 * @param isChecked : check if should use the new criteria
	 * @param progressKey : progress key
	 * @return String message information
	 */
	def generateDependencyGroups(projectId, connectionTypes, statusTypes, isChecked, userLoginName, progressKey) {

		String sqlTime = TimeUtil.formatDateTimeAsGMT(TimeUtil.nowGMT(), TimeUtil.FORMAT_DATE_TIME_14)
		Project project = Project.get(projectId)

		// Get array of the valid status and connection types to check against in the inner loop
		def statusList = statusTypes.replaceAll(', ', ',').replaceAll("'", '').tokenize(',')
		def connectionList = connectionTypes.replaceAll(', ', ',').replaceAll("'", '').tokenize(',')

		// User previous setting if exists else set to empty
		def depCriteriaMap = project.depConsoleCriteria ? JSON.parse(project.depConsoleCriteria) : [:]
		if (isChecked == "1") {
			depCriteriaMap = [statusTypes: statusList, connectionTypes: connectionList]
		}
		depCriteriaMap.modifiedBy = userLoginName
		depCriteriaMap.modifiedDate = TimeUtil.nowGMT().getTime()
		project.depConsoleCriteria = depCriteriaMap as JSON
		project.save(flush: true)

		// Find all move bundles that are flagged for Planning in the project and then get all assets in those bundles
		List<Long> moveBundleIds = MoveBundle.findAllByUseForPlanningAndProject(true, project).id
		String moveBundleText = GormUtil.asCommaDelimitedString(moveBundleIds)
		List<String> moveBundleIdStrings = moveBundleIds*.toString()

		def errMsg

		if (moveBundleText) {
			def started = new Date()
			progressService.update(progressKey, 10I, ProgressService.STARTED, "Search assets and dependencies")

			def results = searchForAssetDependencies(moveBundleText, connectionTypes, statusTypes)

			log.info 'Dependency groups generation - Search assets and dependencies time {}', TimeUtil.elapsed(started)
			started = new Date()
			progressService.update(progressKey, 20I, ProgressService.STARTED, "Load asset results")

			def graph = new AssetGraph()
			graph.loadFromResults(results)

			log.info 'Dependency groups generation - Load results time {}', TimeUtil.elapsed(started)
			started = new Date()
			progressService.update(progressKey, 30I, ProgressService.STARTED, "Clean dependencies")

			cleanDependencyGroupsStatus(projectId)

			log.info 'Dependency groups generation - Clean dependencies time {}', TimeUtil.elapsed(started)
			started = new Date()
			progressService.update(progressKey, 40I, ProgressService.STARTED, "Group dependencies")

			def groups = graph.groupByDependencies(statusList, connectionList, moveBundleIdStrings)
			groups.sort { a, b -> b.size() <=> a.size() }


			log.info 'Dependency groups generation - Group dependencies time {}', TimeUtil.elapsed(started)
			started = new Date()
			progressService.update(progressKey, 70I, ProgressService.STARTED, "Save dependencies")

			saveDependencyGroups(project, groups, sqlTime)

			log.info 'Dependency groups generation - Save dependencies time {}', TimeUtil.elapsed(started)
			started = new Date()
			progressService.update(progressKey, 80I, ProgressService.STARTED, "Add straggles assets")

			// Last step is to put all the straggler assets that were not grouped into group 0
			addStragglerDepsToGroupZero(projectId, moveBundleText, sqlTime)

			log.info 'Dependency groups generation - Add straggles time {}', TimeUtil.elapsed(started)
			started = new Date()
			progressService.update(progressKey, 90I, ProgressService.STARTED, "Finishing")

			graph.destroy()

			log.info 'Dependency groups generation - Destroy graph time {}', TimeUtil.elapsed(started)
			started = new Date()
			progressService.update(progressKey, 100I, ProgressService.COMPLETED, "Finished")

		} else {
			errMsg = "Please associate appropriate assets to one or more 'Planning' bundles before continuing."
		}
	}

	/**
	 * Performs a search for all the assets and dependencies that match the parameters.
	 * @param moveBundleText : bundle ids to analyze
	 * @param connectionTypes : filter for asset types on connection
	 * @param statusTypes : filter for status types
	 * @return List of records
	 */
	private def searchForAssetDependencies(moveBundleText, connectionTypes, statusTypes) {
		// First we need the list of assets that belongs to planning bundles - See TM-10261
		String assetIdsSubQuery = "select a.asset_entity_id from asset_entity a where a.move_bundle_id in ( ${moveBundleText} )";

		String filterOptionsCriteria = " AND ad.asset_id != ad.dependent_id "
		filterOptionsCriteria += connectionTypes ? "AND ad.type in (${connectionTypes}) " : ""
		filterOptionsCriteria += statusTypes ? "AND ad.status in (${statusTypes}) " : ""


		// Query to fetch dependent asset list with dependency type and status and move bundle list with use for planning .
		String queryForAssets = "SELECT pa.asset_entity_id as assetId, ad.asset_id as assetDepFromId, ad.dependent_id as assetDepToId, " +
								"pa.move_bundle_id as moveBundleId, ad.status as status, ad.type as type, pa.asset_type as assetType " +
								"FROM asset_dependency ad " +
								"JOIN asset_entity pa ON pa.asset_entity_id=ad.asset_id " +
								"WHERE ad.asset_id in ( " + assetIdsSubQuery + "  ) " +
								"AND ad.dependent_id IN ( " + assetIdsSubQuery + " ) " +  // < TM-10261
								filterOptionsCriteria +
								"UNION " +
								"SELECT sa.asset_entity_id as assetId, ad.asset_id as assetDepFromId, ad.dependent_id as assetDepToId, " +
								"sa.move_bundle_id as moveBundleId, ad.status as status, ad.type as type, sa.asset_type as assetType " +
								"FROM asset_dependency ad " +
								"JOIN asset_entity sa ON sa.asset_entity_id=ad.dependent_id " +
								"WHERE ad.asset_id in ( " + assetIdsSubQuery + "  ) " +
								"AND ad.dependent_id IN ( " + assetIdsSubQuery + " ) " +  // < TM-10261
								filterOptionsCriteria +
								"ORDER BY assetId DESC  "

		log.info 'SQL used to find assets: {}', queryForAssets
		return jdbcTemplate.queryForList(queryForAssets)
	}

	/**
	 * Clean all dependency groups for the specified project
	 * @param projectId : related project
	 */
	private void cleanDependencyGroupsStatus(projectId) {
		// Deleting previously generated dependency bundle table .
		jdbcTemplate.execute("DELETE FROM asset_dependency_bundle where project_id = $projectId")

		// Reset hibernate session since we just cleared out the data directly
		GormUtil.flushAndClearSession()
	}

	/**
	 * Store all groups in the database
	 * @param project : related project
	 * @param groups : groups to be stored
	 */
	private void saveDependencyGroups(Project project, groups, String sqlTime) {
		int groupNum = 0
		for (group in groups) {
			int count = 0
			groupNum++

			def insertSQL = "INSERT INTO asset_dependency_bundle (asset_id, dependency_bundle, dependency_source, last_updated, project_id) VALUES "
			def first = true

			group.each { asset ->
				String dependency_source = (count++ == 0 ? "Initial" : "Dependency")
				if (!first) {
					insertSQL += ","
				}
				insertSQL += "($asset.id,$groupNum,'$dependency_source','$sqlTime',$project.id)"
				first = false
			}

			jdbcTemplate.execute(insertSQL)
		}
	}

	/**
	 * put all the straggler assets that were not grouped into group 0
	 * @param projectId : related project
	 * @param moveBundleText : bundle ids to analyze
	 */
	private void addStragglerDepsToGroupZero(projectId, moveBundleText, sqlTime) {

		def sql = """
			INSERT INTO asset_dependency_bundle (asset_id, dependency_bundle, dependency_source, last_updated, project_id)
			SELECT ae.asset_entity_id, 0, "Straggler", "$sqlTime", ae.project_id
			FROM asset_entity ae
			LEFT OUTER JOIN asset_dependency_bundle adb ON ae.asset_entity_id=adb.asset_id
			WHERE ae.project_id = $projectId # AND ae.dependency_bundle IS NULL
			AND adb.asset_id IS NULL
			AND move_bundle_id in ($moveBundleText)
		"""

		jdbcTemplate.execute(sql)
	}

	/**
	 * Generates the mapping arguments used by the dependencyConsole view
	 * 	 * @param project  the project
	 * 	 * @param moveBundleId - move bundle id to filter for bundle
	 * 	 * @return MapArray of properties
	 *
	 * @param project The project for the dependency console.
	 * @param moveBundleId move bundle id to filter for bundle
	 * @param tagIds A list of tag ids used for filtering.
	 * @param tagMatch The type of tag match to use ALL or ANY.
	 * @param isAssignedString Is the groups should be assigned only '1' for true false otherwise
	 * @param dependencyBundle *
	 * @param subsection The name of the selected tab *
	 * @param groupId The group id. *
	 * @param assetName The asset name. *
	 *
	 * @return a Map containing the lists and properties used by the dependency console.
	 */
	//TODO 4/1/2020 we might be able to delete some of the params that are no longer used...
	Map dependencyConsoleMap(
		Project project,
		Long moveBundleId,
		List<Long> tagIds,
		String tagMatch,
		String isAssignedString,
		dependencyBundle,
		String subsection = null,
		groupId = null,
		assetName = null) {

		boolean isAssigned = isAssignedString == '1'
		Date startAll = new Date()

		Map<String, List> dependencyConsole = [
			group          : [],
			application    : [],
			serversPhysical: [],
			serversVirtual : [],
			databases      : [],
			storage        : [],
			statusClass    : []
		]

		// This will hold the totals of each, element 0 is all and element 1 will be All minus group 0
		Map<String, Map> stats = [
			group          : [all: 0, grouped: 0],
			application    : [all: 0, grouped: 0],
			serversPhysical: [all: 0, grouped: 0],
			serversVirtual : [all: 0, grouped: 0],
			databases      : [all: 0, grouped: 0],
			storage        : [all: 0, grouped: 0]
		]


		String query = dependencyConsoleQuery(project, tagIds, dependencyBundle)
		List<Map<String, Object>> dependList = jdbcTemplate.queryForList(query)

		dependList = filterDependListOnTagAndBundles(dependList, tagIds, tagMatch, project, moveBundleId, dependencyBundle)
		dependList = addRemnantsIfNoExists(dependList)

		def groups = dependList.dependencyBundle

		if (dependencyBundle == null) {
			session.setAttribute('Dep_Groups', (groups as JSON).toString())
		}

		// Used by the Assignment Dialog
		List<Map> allMoveBundles = getAllMoveBundles(project)

		Map entities = assetEntityService.entityInfo(project)
		accumulateDependencyConsoleListAndStats(dependList, dependencyConsole, stats, isAssigned)
		def depGrpCrt = project.depConsoleCriteria ? JSON.parse(project.depConsoleCriteria) : [:]
		String generatedDate = TimeUtil.formatDateTime(depGrpCrt.modifiedDate)

		Map map = [
			asset            : 'apps',
			date             : generatedDate,
			dependencyType   : entities.dependencyType,
			dependencyConsole: combineStatsAndConsoleMap(dependencyConsole, stats, isAssigned),
			dependencyStatus : entities.dependencyStatus,
			assetDependency  : new AssetDependency(),
			planningBundles   : allMoveBundles.findAll { return it.useForPlanning },
			allMoveBundles   : allMoveBundles,
			planStatusOptions: assetOptionsService.findAllByType(AssetOptions.AssetOptionsType.STATUS_OPTION),
			isAssigned       : isAssigned,
			depGrpCrt        : depGrpCrt,
			compactPref      : userPreferenceService.getPreference(PREF.DEP_CONSOLE_COMPACT),
			showTabs         : subsection != null,
			tabName          : subsection,
			groupId          : groupId,
			assetName        : assetName,
			//Tags Properties
			tagIds           : tagIds,
			tagMatch         : tagMatch
		]

		log.info 'dependencyConsoleMap() : OVERALL took {}', TimeUtil.elapsed(startAll)

		return map
	}

	/**
	 * Accumulates the counts into a list and into a stat map.
	 *
	 * @param dependList the dependency list got get counts for.
	 * @param dependencyConsole The map to store the counts of the dependencies.
	 * @param stats The map to store the overall stat counts.
	 * @param isAssigned If the dependencies should be just for assigned groups.
	 */
	void accumulateDependencyConsoleListAndStats(List<Map<String, Object>> dependList, Map<String, List> dependencyConsole, Map<String, Map> stats, boolean isAssigned) {
		dependList.eachWithIndex { Map<String, Object> group, Integer index ->

			if (isAssigned && index == 0) {
				return
			}

			// Loop through the list to create map to be used by the view
			dependencyConsole.group << group.dependencyBundle
			dependencyConsole.application << group.appCount
			dependencyConsole.serversPhysical << group.serverCount
			dependencyConsole.serversVirtual << group.vmCount
			dependencyConsole.databases << group.dbCount
			dependencyConsole.storage << group.storageCount
			dependencyConsole.statusClass << getStatusClass(group)

			// Accumulate the totals for ALL and 1+ (aka All - 0)
			stats.application.all += group.appCount
			stats.serversPhysical.all += group.serverCount
			stats.serversVirtual.all += group.vmCount
			stats.databases.all += group.dbCount
			stats.storage.all += group.storageCount

			if (group.dependencyBundle != 0) {
				stats.application.grouped += group.appCount
				stats.serversPhysical.grouped += group.serverCount
				stats.serversVirtual.grouped += group.vmCount
				stats.databases.grouped += group.dbCount
				stats.storage.grouped += group.storageCount
			}
		}
	}

	/**
	 * Determines the css class to use for a group.
	 *
	 * @param group The group to get the css class for.
	 *
	 * @return The css class for the group.
	 */
	String getStatusClass(Map<String, Object> group) {
		String statusClass = null

		if (group.moveBundles?.contains(',') || group.needsReview > 0) {
			// Assets in multiple bundles or dependency status unknown or questioned
			statusClass = 'depGroupConflict'
		} else if (group.notBundleReady == 0 && group.statusUnassigned == group.assetCnt) {
			// If all assets are BundleReady and not fully assigned
			statusClass = 'depGroupReady'
		} else if (group.statusUnassigned == 0) {
			// Assets assigned + moved total the number of assets in the group so the group is done
			statusClass = 'depGroupDone'
		}

		return statusClass
	}

	/**
	 * A query to get all of the move bundles for a project.
	 *
	 * @param project The project to get move bundles for.
	 *
	 * @return the list of all move bundles for a project.
	 */
	List<Map> getAllMoveBundles(Project project) {
		MoveBundle.executeQuery("""
					SELECT new Map(id as id, name as name, useForPlanning as useForPlanning) 
					FROM MoveBundle 
					WHERE project =:project 
					ORDER BY name ASC""", [project: project])
	}

	/**
	 *
	 * @param dependList
	 * @param tagIds
	 * @param tagMatch
	 * @param project
	 * @param moveBundleId
	 * @param dependencyBundle
	 * @return
	 */
	List<Map<String, Object>> filterDependListOnTagAndBundles(
		List<Map<String, Object>> dependList,
		List<Long> tagIds,
		String tagMatch,
		Project project,
		Long moveBundleId,
		def dependencyBundle) {

		List dependListTags = []

		if (tagIds) {
			String tagsQuery = dependencyConsoleQuery(project, tagIds, dependencyBundle, true)
			dependListTags = jdbcTemplate.queryForList(tagsQuery)
		}

		if (moveBundleId || tagIds) {
			List<Long> groupTags = []
			Integer index = 0

			return dependList.findResults { result ->

				if (dependListTags && dependListTags[index].tags) {
					groupTags = dependListTags[index].tags.split(',').collect { id ->
						Long.parseLong(id)
					}
				}

				index++

				if ((moveBundleId && result.moveBundles.contains(moveBundleId as String)) ||
					(tagIds && tagMatch == 'ALL' && groupTags.containsAll(tagIds)) ||
					(tagIds && tagMatch == 'ANY' && groupTags.intersect(tagIds))) {
					return result
				}
			}
		}
		return dependList

	}

	/**
	 * Adds a remnants column to the dependency list if it doesn't already have one.
	 *
	 * @param dependList The dependency list to add a remnants column to, if it doesn't already have one.
	 *
	 * @return The updated dependency List.
	 */
	List<Map<String, Object>> addRemnantsIfNoExists(List<Map<String, Object>> dependList) {
		// TM-8535 Missing Remnants (Group 0)
		if (dependList && dependList[0].dependencyBundle != 0) {
			dependList.add(0, [
				dependencyBundle: 0,
				appCount        : 0,
				serverCount     : 0,
				vmCount         : 0,
				dbCount         : 0,
				storageCount    : 0,
				statusUnassigned: 0
			])
		}
		return dependList
	}

	/**
	 * Combines the stats inline with the list of counts per group for easy digestion by the UI.
	 *
	 * @param dependencyConsole The Map containing the lists of counts per dependency type.
	 * @param stats A map listing the total counts for all assets and grouped access by type.
	 * @param isAssigned if the counts should be filtered to us assigned groups.
	 *
	 * @return The updated map with the stats count inline.
	 */
	Map<String, List> combineStatsAndConsoleMap(Map<String, List> dependencyConsole, Map<String, Map> stats, boolean isAssigned) {
		Map<String, List> dependencyConsoleMap = dependencyConsole.clone()
		final List<String> groupNames = ['All', 'Remnants', 'Grouped']
		final List<String> assignedGroups = ['All', 'Grouped']
		final List<String> styles = [null, null, null]

		dependencyConsole.each { String key, List value ->
			List values = value.size() > 1 ? value[1..-1] : value
			if (key == 'group') {
				dependencyConsoleMap[key] = isAssigned ? assignedGroups + values : groupNames + values
			} else if (key == 'statusClass') {
				dependencyConsoleMap[key] = isAssigned ? styles[1..-1] + values : styles + values
			} else {
				if (isAssigned) {
					dependencyConsoleMap[key] = [stats[key].all, stats[key].grouped] + values
				} else {
					dependencyConsoleMap[key] = [stats[key].all, value[0], stats[key].grouped] + values
				}
			}
		}

		return dependencyConsoleMap
	}

	/**
	 * Creates one of two queries for the dependency console and returns them as a string. The two queries are one for the groups themselves
	 * and another one for the tags. The reason for this is I could either get the counts right or the tags right in one query because the
	 * query is quite complicated, and trying to do everything is one just wasn't working.
	 *
	 * @param project The project used to filter the query.
	 * @param tagIds The tag Ids used to filter the query for the tasks when includeTags is true.
	 * @param dependencyBundle The dependency  bundle used to filter the query which can be 'all', 'onePlus' or a number.
	 * @param includeTags if the tag query should be generated rather than the count query using the same filtering. This defaults to false.
	 *
	 * @return A string that contains the dependency console query for counts, or tags.
	 */
	String dependencyConsoleQuery(Project project, List<Long> tagIds, dependencyBundle, boolean includeTags = false) {
		String physicalTypes = AssetType.physicalServerTypesAsString
		String virtualTypes = AssetType.virtualServerTypesAsString
		String storageTypes = AssetType.storageTypesAsString
		String reviewCodes = AssetDependencyStatus.reviewCodesAsString
		String tagJoins = tagService.getTagsJoin(tagIds, 'ANY') //passing in any because we want the join regardless of the tag matching.
		String tagsSelect = tagService.getTagSelect(tagIds)

		String counts = """
			COUNT(distinct adb.asset_id) AS assetCnt,
			CONVERT( group_concat(distinct a.move_bundle_id) USING 'utf8') AS moveBundles,
			SUM(if(a.plan_status='$AssetEntityPlanStatus.UNASSIGNED',1,0)) AS statusUnassigned,
			SUM(if(a.validation<>'${ValidationType.PLAN_READY}',1,0)) AS notBundleReady,
			SUM(if(a.asset_class = '$AssetClass.DEVICE'
				AND if(m.model_id > -1, m.asset_type in ($physicalTypes), a.asset_type in ($physicalTypes)), 1, 0)) AS serverCount,
			SUM(if(a.asset_class = '$AssetClass.DEVICE'
				AND if(m.model_id > -1, m.asset_type in ($virtualTypes), a.asset_type in ($virtualTypes)), 1, 0)) AS vmCount,
			SUM(if((a.asset_class = '$AssetClass.STORAGE')
				OR (a.asset_class = '$AssetClass.DEVICE'
				AND if(m.model_id > -1, m.asset_type in ($storageTypes), a.asset_type in ($storageTypes))), 1, 0)) AS storageCount,
			SUM(if(a.asset_class = '$AssetClass.DATABASE', 1, 0)) AS dbCount,
			SUM(if(a.asset_class = '$AssetClass.APPLICATION', 1, 0)) AS appCount,
			COALESCE(nr.needsReview, 0) AS needsReview
		"""

		StringBuilder depSql = new StringBuilder("""SELECT
			adb.dependency_bundle AS dependencyBundle,
			${includeTags ? '' : counts}
			${includeTags ? tagsSelect : ''}

			FROM asset_dependency_bundle adb
			JOIN asset_entity a ON a.asset_entity_id=adb.asset_id
			LEFT OUTER JOIN model m ON a.model_id=m.model_id
			${includeTags ? tagJoins : ''}
			LEFT OUTER JOIN (SELECT adb.dependency_bundle, 1 AS needsReview
				FROM asset_entity ae INNER JOIN asset_dependency_bundle adb ON ae.asset_entity_id=adb.asset_id
				LEFT JOIN asset_dependency ad1 ON ad1.asset_id=ae.asset_entity_id
				LEFT JOIN asset_dependency ad2 ON ad2.dependent_id=ae.asset_entity_id
				WHERE adb.project_id=${project.id} AND (ad1.status IN (${reviewCodes}) OR ad2.status IN (${reviewCodes}))
			GROUP BY adb.dependency_bundle) nr ON nr.dependency_bundle=adb.dependency_bundle
			WHERE adb.project_id=${project.id}""")

		if (dependencyBundle) {
			List depGroups = JSON.parse((String) session.getAttribute('Dep_Groups'))

			if (dependencyBundle == 'onePlus') {
				depGroups = depGroups - [0]
			}

			if (depGroups.size() == 0) {
				depGroups = [-1]
			}

			if (dependencyBundle.isNumber()) {
				depSql.append(" AND adb.dependency_bundle = $dependencyBundle")
			} else if (dependencyBundle in ['all', 'onePlus']) {
				depSql.append(" AND adb.dependency_bundle IN (${WebUtil.listAsMultiValueString(depGroups)})")
			}
		}

		depSql.append(" GROUP BY adb.dependency_bundle ORDER BY adb.dependency_bundle ")

		depSql.toString()
	}
}
