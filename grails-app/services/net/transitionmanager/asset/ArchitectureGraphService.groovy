package net.transitionmanager.asset

import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.AssetDependencyStatus
import com.tdsops.tm.enums.domain.UserPreferenceEnum
import grails.converters.JSON
import grails.util.Environment
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.TailRecursive
import net.transitionmanager.model.Model
import net.transitionmanager.person.UserPreferenceService
import net.transitionmanager.project.Project
import net.transitionmanager.service.ServiceMethods
import org.apache.commons.lang.StringEscapeUtils
import org.hibernate.criterion.CriteriaSpecification

/**
 * A service to building up the architecture graphs.
 */
@CompileStatic
class ArchitectureGraphService implements ServiceMethods {
	DeviceService         deviceService
	AssetEntityService    assetEntityService
	UserPreferenceService userPreferenceService

	/**
	 * The asset properties to use in projections of queries.
	 */
	static final List<String> assetProperties = [
		'id',
		'model',
		'assetClass',
		'assetType',
		'criticality',
		'assetName'
	]

	/**
	 * The asset dependency properties and aliases, to use in projections of queries
	 */
	static final List<LinkedHashMap<String, String>> dependencyProperties = [
		[name: 'id', alias: 'id'],
		[name: 'status', alias: 'status'],
		[name: 'asset.id', alias: 'assetId'],
		[name: 'dependent.id', alias: 'dependentId'],
		[name: 'isFuture', alias: 'isFuture'],
		[name: 'isStatusResolved', alias: 'isStatusResolved']
	]

	/**
	 * Gets the preferences for the Architecture graph.
	 *
	 * @param assetId The asset id, which is nullable
	 * @param levelsUp the number of levels up from an asset, which defaults to zero.
	 * @param levelsDown The number of levels down from an asset with defaults to three.
	 * @param currentProject The current project used to look up the asset.
	 *
	 * @return A map of preferences for the architecture graph.
	 */
	Map getPreferences(Long assetId, Integer levelsUp, Integer levelsDown, Project currentProject) {
		String assetName

		if (assetId) {
			assetName = get(AssetEntity, assetId, currentProject).assetName
		}

		Map defaultPrefs = [
			levelsUp    : '0',
			levelsDown  : '3',
			showCycles  : true,
			appLbl      : true,
			labelOffset : '2',
			assetClasses: 'ALL'

		]

		String graphPrefs = userPreferenceService.getPreference(UserPreferenceEnum.ARCH_GRAPH)
		Map prefsObject = graphPrefs ? (Map) JSON.parse(graphPrefs) : defaultPrefs
		List assetClassesForSelect = [[id: 'ALL', value: 'All Classes']]
		assetClassesForSelect.addAll(AssetClass.classOptions.collect {
			[id: it.key, value: it.value]
		})

		return [
			assetId               : assetId,
			assetName             : assetName,
			levelsUp              : levelsUp,
			levelsDown            : levelsDown,
			assetClassesForSelect : assetClassesForSelect,
			dependencyStatus      : assetEntityService.getDependencyStatuses(),
			dependencyType        : assetEntityService.getDependencyTypes(),
			assetTypes            : AssetEntityService.ASSET_TYPE_NAME_MAP,
			defaultPrefs          : defaultPrefs as JSON,
			graphPrefs            : prefsObject,
			assetClassesForSelect2: AssetClass.classOptionsDefinitions
		]
	}

	/**
	 * Build the architecture model map to be rendered to the view.
	 *
	 * @param rootAsset The root asset node of the graph
	 * @param assetId The id of the asset, that is the root of the graph.
	 * @param levelsUp The numbers of levels up, for the graph.
	 * @param levelsDown The number of levels down for the graph.
	 * @param mode the mode of the graph if 'assetId' then buildArchitectureGraph is called
	 *
	 * @return A map representing the architecture graph to be rendered in the view.
	 */
	Map architectureGraphModel(AssetEntity rootAsset, Integer levelsUp, Integer levelsDown, String mode) {

		Set assetsList = [] as Set
		Set dependencyList = [] as Set

		if (rootAsset && mode == "assetId") {
			buildArchitectureGraph([rootAsset.id], levelsDown + 1, assetsList, dependencyList)
			buildArchitectureGraph([rootAsset.id], levelsUp, assetsList, dependencyList, false)
		}

		dependencyList.addAll extraDependencies(assetsList, dependencyList)

		List<Map> graphNodes = createGraphNodes(assetsList, rootAsset)
		List<Map> graphLinks = createGraphLinks(dependencyList, graphNodes)

		addLinksToNodes(graphLinks, graphNodes)

		// maps asset type names to simpler versions
		Map assetTypes = AssetEntityService.ASSET_TYPE_NAME_MAP

		return [
			nodes         : graphNodes,
			links         : graphLinks,
			assetId       : rootAsset?.id,
			levelsUp      : levelsUp,
			levelsDown    : levelsDown,
			assetTypes    : assetTypes,
			assetTypesJson: assetTypes,
			environment   : Environment.current.name
		]
	}

	/**
	 * Build the architecture model map to be rendered to the view.
	 *
	 * @param graphNodes The nodes of the architecture graph.
	 * @param graphLinks The links of the architecture graph.
	 * @param assetId The id of the asset, that is the root of the graph.
	 * @param levelsUp The numbers of levels up, for the graph.
	 * @param levelsDown The number of levels down for the graph.
	 *
	 * @return A map representing the architecture graph to be rendered in the view.
	 */
	Map architectureGraphModelLegacy(List<Map> graphNodes = [], List<Map> graphLinks = [], Long assetId, Integer levelsUp, Integer levelsDown) {
		// maps asset type names to simpler versions
		Map assetTypes = AssetEntityService.ASSET_TYPE_NAME_MAP

		return [
			nodes         : graphNodes as JSON,
			links         : graphLinks as JSON,
			assetId       : assetId,
			levelsUp      : levelsUp,
			levelsDown    : levelsDown,
			assetTypes    : assetTypes,
			assetTypesJson: assetTypes as JSON,
			environment   : Environment.current.name
		]
	}

	/**
	 * Build the architecture graph based on a specific asset.
	 *
	 * @param assetIds The assetIds that make up a level of the graph to be added to the assetList.
	 * @param level The number of levels deep to produce the graph.
	 * @param assetsList The list of assets for the graph, which grows with every level added.
	 * @param dependencyList The list of dependencies, which grows with every level added.
	 *
	 * return True just to satisfy @TailRecursive, because it doesn't work with void
	 */
	@TailRecursive
	@CompileDynamic
	boolean buildArchitectureGraph(List<Long> assetIds, Integer level, Set<Map> assetsList, Set<Map> dependencyList, boolean levelsDown = true) {

		List<Map> assets = AssetEntity.createCriteria().list {
			resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
			'in'('id', assetIds)

			projections {

				assetProperties.each { String assetproperty ->
					property(assetproperty, assetproperty)
				}
			}
		}

		assetsList.addAll(assets)

		if (level > 0) {
			List<Map> dependencies = AssetDependency.createCriteria().list {
				resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)

				if(levelsDown) {
					'in'('asset.id', assetIds)
				}else {
					'in'('dependent.id', assetIds)
				}

				projections {
					dependencyProperties.each { Map dependentProperty ->
						property(dependentProperty.name, dependentProperty.alias)
					}
				}
			}

			dependencyList.addAll(dependencies)

			List assetsForNextLevel = []
			assetsForNextLevel.addAll(dependencies*.assetId)
			assetsForNextLevel.addAll(dependencies*.dependentId)

			if (!assetsForNextLevel) {
				return true
			}

			buildArchitectureGraph(assetsForNextLevel, level - 1, assetsList, dependencyList, levelsDown)
		}
	}

	/**
	 * Find any links between assets that weren't found with the DFS and add in any extra dependencies that were found
	 * TODO 08-20-2019 ask John if this is really needed I'm not sure it's actually adding anything...
	 *
	 * @param assetList The assetList used to find asset dependencies not found in the DFS of buildArchitectureGraph
	 * @param dependencyList The existing dependency list, to check adding dependencies found.
	 *
	 * @return A list, of dependencies to add not found by the DFS of buildArchitectureGraph.
	 */
	@CompileDynamic
	List<Map> extraDependencies(Set<Map> assetList, Set<Map> dependencyList) {

		return AssetDependency.createCriteria().list {
			resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)

			if (assetList) {
				and {
					'in'('asset.id', assetList*.id)
					'in'('dependent.id', assetList*.id)
				}
			}

			projections {
				dependencyProperties.each { Map dependentProperty ->
					property(dependentProperty.name, dependentProperty.alias)
				}
			}
		}.findResults { Map dependency ->
			if (!(dependency in dependencyList)) {
				return dependency
			}
			return null
		}
	}

	/**
	 * Create the nodes of the architecture graph.
	 *
	 * @param assetsList The list of assets to base the graph nodes on.
	 * @param rootAsset The root asset, used for the nodes color.
	 *
	 * @return A list of graph nodes.
	 */
	List<Map> createGraphNodes(Set<Map> assetsList, AssetEntity rootAsset) {
		String shape = 'circle'
		Integer size = 150
		String type = ''
		String assetName = ''
		String assetType = ''
		String assetClass = ''
		Map criticalitySizes = [Minor: 150, Important: 200, Major: 325, Critical: 500]

		// create a node for each asset
		return assetsList.collect { Map asset ->

			// get the type used to determine the icon used for this asset's node
			assetType = ((Model) asset.model)?.assetType ?: asset.assetType
			assetClass = asset.assetClass?.toString() ?: ''
			assetName = asset.assetName
			size = 150

			type = deviceService.getImageName(assetClass, assetType)
			if (type == AssetType.APPLICATION.toString()) {
				size = asset.criticality ? criticalitySizes[(String) asset.criticality] : 200
			}

			return [
				id        : asset.id,
				name      : StringEscapeUtils.escapeHtml(StringEscapeUtils.escapeJava(assetName)),
				type      : type,
				assetClass: asset.assetClass.toString(),
				shape     : shape,
				size      : size,
				title     : StringEscapeUtils.escapeHtml(StringEscapeUtils.escapeJava(assetName)),
				color     : asset.id == rootAsset?.id ? 'red' : 'grey',
				parents   : [],
				children  : [],
				checked   : false,
				siblings  : []
			]
		} as List<Map>
	}

	/**
	 * Create the links for the architecture graph.
	 *
	 * @param dependencyList The dependencies that the graph links are based off.
	 * @param graphNodes The graph nodes used to determine if the dependency should be added as a link.
	 *
	 * @return A list of graph links.
	 */
	List<Map> createGraphLinks(Set<Map> dependencyList, List<Map> graphNodes) {
		// Create a separate list of just the node ids to use while creating the links (this makes it much faster)
		List<Object> nodeIds = graphNodes*.id

		Integer opacity = 1

		return dependencyList.withIndex().findResults { Map dependency, Integer index ->
			boolean notApplicable = (dependency.status == AssetDependencyStatus.NA)
			boolean validated = (dependency.status == AssetDependencyStatus.VALIDATED)
			boolean questioned = (dependency.status == AssetDependencyStatus.QUESTIONED)
			boolean future = dependency.isFuture
			boolean unresolved = !dependency.isStatusResolved
			Integer sourceIndex = nodeIds.indexOf(dependency.assetId)
			Integer targetIndex = nodeIds.indexOf(dependency.dependentId)

			if (sourceIndex != -1 && targetIndex != -1) {
				return [
					id           : index,
					parentId     : dependency.assetId,
					childId      : dependency.dependentId,
					child        : targetIndex,
					parent       : sourceIndex,
					value        : 2,
					opacity      : opacity,
					redundant    : false,
					mutual       : null,
					notApplicable: notApplicable,
					future       : future,
					validated    : validated,
					questioned   : questioned,
					unresolved   : unresolved
				]
			}
		} as List<Map>
	}

	/**
	 * Set the dependency links of the nodes
	 *
	 * @param graphLinks The links used to add parents/children to the nodes.
	 * @param graphNodes The nodes which get the parents/children added to based on the links.
	 */
	void addLinksToNodes(List<Map> graphLinks, List<Map> graphNodes) {
		//
		graphLinks.each { Map link ->
			if (!link.cyclical) {
				List parents = (List) graphNodes[(Integer) link.child].parents
				parents.add(link.id)

				List children = (List) graphNodes[(Integer) link.parent].children
				children.add(link.id)
			}
		}
	}

}
