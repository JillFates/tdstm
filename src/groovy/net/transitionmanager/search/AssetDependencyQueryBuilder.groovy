package net.transitionmanager.search

import com.tds.asset.AssetDependency
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.NumberUtil
import net.transitionmanager.domain.Project

class AssetDependencyQueryBuilder extends DomainQueryBuilder{

	AssetDependencyQueryBuilder(Project project, Map filterParams, Map sortingParams, Map paginationParams) {
		super(project, filterParams, sortingParams, paginationParams)
	}


	@Override
	 Class getDomain() {
		return AssetDependency
	}

	@Override
	String getDomainAlias() {
		return null
	}

	@Override
	protected List<String> getDefaultWhereConditions() {
		List<String> conditions = super.getDefaultWhereConditions()
		conditions << "ae.project = :project"
		return conditions
	}

	@Override
	Map getDefaultWhereParameters(){
		Map whereParameters = super.getDefaultWhereParameters()
		whereParameters['project'] = project
	}

	@Override
	Map<String, Map> getDomainFieldsMap() {
		return [
			assetClass: [property: 'str(ae.assetClass)', type: String],
			assetBundle: [property: 'amb.name', type: String],
			assetId: [property: 'ae.id', type: Long],
			assetName: [property: 'ae.assetName', type: String],
			assetType: [property: 'ae.assetType', type: String],
			c1: [property: 'c1', type: String],
			c2: [property: 'c2', type: String],
			c3: [property: 'c3', type: String],
			c4: [property: 'c4', type: String],
			comment: [property: 'comment', type: String],
			dependentClass: [property: 'str(ad.assetClass)', type: String],
			dependentBundle: [property: 'dmb.name', type: String],
			dependentId: [property: 'ad.id', type: Long],
			dependentName: [property: 'ad.assetName', type: String],
			dependentType: [property: 'ad.assetType', type: String],
			direction: [property: 'dataFlowDirection', type: String],
			frequency: [property: 'dataFlowFreq', type: String],
			id: [property: 'id', type: Long],
			status: [property: 'status', type: String],
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
			type: [property: 'type']
		]
	}

	@Override
	List<Map<String, String>> getJoinsList() {
		return [
			[
			    property: 'asset',
				alias: 'ae'
			],
			[
				property: 'dependent',
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
}
