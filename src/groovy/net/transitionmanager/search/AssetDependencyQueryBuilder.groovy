package net.transitionmanager.search

import com.tds.asset.AssetDependency
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.NumberUtil
import net.transitionmanager.domain.Project

class AssetDependencyQueryBuilder extends DomainQueryBuilder{

	/**
	 * Constructor for the builder for AssetDependencies.
	 * @param project
	 * @param filterParams
	 * @param sortingParams
	 * @param paginationParams
	 */
	AssetDependencyQueryBuilder(Project project, Map filterParams, Map sortingParams, Map paginationParams) {
		super(project, filterParams, sortingParams, paginationParams)
	}


	/**
	 * Return the AssetDependency class, as it's the domain being queried.
	 * @return
	 */
	@Override
	 Class getDomainClass() {
		return AssetDependency
	}

	/**
	 * Return the alias that is going to be used for the main domain.
	 * @return
	 */
	@Override
	String getDomainAlias() {
		return 'dep'
	}

	/**
	 * Return the default condition for the query, which is the project of the asset.
	 * @return
	 */
	@Override
	protected List<String> getDefaultWhereConditions() {
		List<String> conditions = super.getDefaultWhereConditions()
		conditions << "ae.project = :project"
		return conditions
	}

	/**
	 * Return the parameters for the default condition, which is the user's current project.
	 * @return
	 */
	@Override
	Map getDefaultWhereParameters(){
		Map whereParameters = super.getDefaultWhereParameters()
		whereParameters['project'] = project
		return whereParameters
	}

	/**
	 * This map contains all the fields that can be filtered and selected in the query.
	 * @return
	 */
	@Override
	Map<String, Map> getDomainFieldsMap() {
		return [
			assetClass: [property: 'str(ae.assetClass)', type: String],
			assetBundle: [property: 'amb.name', type: String],
			assetId: [property: 'ae.id', type: Long],
			assetName: [property: 'ae.assetName', type: String],
			assetType: [property: 'ae.assetType', type: String],
			c1: [property: 'dep.c1', type: String],
			c2: [property: 'dep.c2', type: String],
			c3: [property: 'dep.c3', type: String],
			c4: [property: 'dep.c4', type: String],
			comment: [property: 'dep.comment', type: String],
			dependentClass: [property: 'str(ad.assetClass)', type: String],
			dependentBundle: [property: 'dmb.name', type: String],
			dependentId: [property: 'ad.id', type: Long],
			dependentName: [property: 'ad.assetName', type: String],
			dependentType: [property: 'ad.assetType', type: String],
			direction: [property: 'dep.dataFlowDirection', type: String],
			frequency: [property: 'dep.dataFlowFreq', type: String],
			id: [property: 'dep.id', type: Long],
			status: [property: 'dep.status', type: String],
			tag_asset: [
				property: """
				CONCAT(
					'[',
					if(
						TA_A.id,
						group_concat(
							json_object('id', TA_A.id, 'tagId', T_A.id, 'name', T_A.name, 'description', T_A.description, 'color', T_A.color)
						),
						''
					),
					']'
				)""",
				whereProperty: 'ae.id',
				manyToManyQueries: [
					AND : { String filter ->
						List<String> numbers = filter.split('&')
						List<Long> tagList = NumberUtil.toPositiveLongList(numbers)
						Long listSize = tagList.size() // Assign to long to avoid 'Integer cannot be casted to Long' error.
						return [
							query: "SELECT asset.id FROM TagAsset WHERE tag.id in (:tagList) GROUP BY asset.id HAVING count(*) = :tagListSize",
							params: [
								tagList: tagList,
								tagListSize: listSize
							]
						]
					},
					OR: { String filter ->
						List<String> numbers = filter.split('\\|')
						List<Long> tagList = NumberUtil.toPositiveLongList(numbers)
						return [
							query: "SELECT DISTINCT(ta.asset.id) FROM TagAsset ta WHERE tag.id in (:tagList)",
							params: [tagList: tagList]
						]

					}
				]
			],
			tag_dependent: [
				property: """
				CONCAT(
					'[',
					if(
						TA_D.id,
						group_concat(
							json_object('id', TA_D.id, 'tagId', T_D.id, 'name', T_D.name, 'description', T_D.description, 'color', T_D.color)
						),
						''
					),
					']'
				)""",
				whereProperty: 'ad.id',
				manyToManyQueries: [
					AND : { String filter ->
						List<String> numbers = filter.split('&')
						List<Long> tagList = NumberUtil.toPositiveLongList(numbers)
						Long listSize = tagList.size() // Assign to long to avoid 'Integer cannot be casted to Long' error.
						return [
							query: "SELECT asset.id FROM TagAsset WHERE tag.id in (:tagList) GROUP BY asset.id HAVING count(*) = :tagListSize",
							params: [
								tagList: tagList,
								tagListSize: listSize
							]
						]
					},
					OR: { String filter ->
						List<String> numbers = filter.split('\\|')
						List<Long> tagList = NumberUtil.toPositiveLongList(numbers)
						return [
							query: "SELECT DISTINCT(ta.asset.id) FROM TagAsset ta WHERE tag.id in (:tagList)",
							params: [tagList: tagList]
						]

					}
				]
			],
			type: [property: 'dep.type']
		]
	}

	@Override
	List<Map<String, String>> getJoinsList() {
		return [
			[
			    property: 'dep.asset',
				alias: 'ae'
			],
			[
				property: 'dep.dependent',
				alias: 'ad'
			],
			[
				property: 'ae.moveBundle',
				alias: 'amb'
			],
			[
				property: 'ad.moveBundle',
				alias: 'dmb'
			],
			[
				property: 'ae.tagAssets',
				alias: 'TA_A'
			],
			[
				property: 'TA_A.tag',
				alias: 'T_A'
			],
			[
				property: 'ad.tagAssets',
				alias: 'TA_D'
			],
			[
				property: 'TA_D.tag',
				alias: 'T_D'
			]
		]
	}

	/**
	 * Return the default sorting criteria, which is the asset's name.
	 * @return
	 */
	@Override
	Map<String, String> getDefaultSorting() {
		return [
		    sortIndex: 'ae.assetName',
			sortOrder: 'asc'
		]
	}

	/**
	 * Return the 'dep.id' as the property that is going to be used for the count query.
	 * @return
	 */
	@Override
	protected String getCountProperty() {
		return "distinct dep.id"
	}

	/**
	 * Return the 'dep.id' as the property for the group by.
	 * @return
	 */
	@Override
	protected String getGroupByProperty() {
		return "dep.id"
	}
}
