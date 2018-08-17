import com.tds.asset.AssetEntity
import com.tdsops.common.security.spring.HasPermission
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.ProjectTeam
import net.transitionmanager.security.Permission
import net.transitionmanager.service.AssetEntityAttributeLoaderService
import net.transitionmanager.service.PartyRelationshipService
import net.transitionmanager.service.UserPreferenceService
import org.springframework.jdbc.core.JdbcTemplate

import grails.plugin.springsecurity.annotation.Secured
@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class MoveBundleAssetController implements ControllerMethods {

	static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
	static defaultAction = 'list'

	AssetEntityAttributeLoaderService assetEntityAttributeLoaderService
	JdbcTemplate jdbcTemplate
	PartyRelationshipService partyRelationshipService
	UserPreferenceService userPreferenceService

	@HasPermission(Permission.AssetView)
	def list() {
		if (!params.max) params.max = 10
		[moveBundleAssetInstanceList: AssetEntity.list(params)]
	}

	@HasPermission(Permission.AssetView)
	def show() {
		AssetEntity moveBundleAsset = AssetEntity.get(params.id)
		if (!moveBundleAsset) {
			flash.message = "MoveBundleAsset not found with id $params.id"
			redirect(action: "list")
			return
		}

		[moveBundleAssetInstance: moveBundleAsset]
	}

	@HasPermission(Permission.AssetDelete)
	def delete() {
		AssetEntity moveBundleAsset = AssetEntity.get(params.id)
		if (moveBundleAsset) {
			moveBundleAsset.delete(flush: true)
			flash.message = "MoveBundleAsset $params.id deleted"
		}
		else {
			flash.message = "MoveBundleAsset not found with id $params.id"
		}
		redirect(action: "list")
	}

	@HasPermission(Permission.AssetEdit)
	def edit() {
		def moveBundleAsset = AssetEntity.get(params.id)
		if (!moveBundleAsset) {
			flash.message = "MoveBundleAsset not found with id ${params.id}"
			redirect(action: "list")
			return
		}

		[moveBundleAssetInstance: moveBundleAsset]
	}

	@HasPermission(Permission.AssetEdit)
	def update() {
		def moveBundleAsset = AssetEntity.get(params.id)
		if (moveBundleAsset) {
			moveBundleAsset.properties = params
			if (!moveBundleAsset.hasErrors() && moveBundleAsset.save()) {
				flash.message = "MoveBundleAsset $params.id updated"
				redirect(action: "show", id: moveBundleAsset.id)
			}
			else {
				render(view: 'edit', model: [moveBundleAsset: moveBundleAsset])
			}
		}
		else {
			flash.message = "MoveBundleAsset not found with id $params.id"
			redirect(action: "edit", id: params.id)
		}
	}

	@HasPermission(Permission.AssetCreate)
	def create() {
		[moveBundleAssetInstance: new AssetEntity(params)]
	}

	@HasPermission(Permission.AssetCreate)
	def save() {
		def moveBundleAsset = new AssetEntity(params)
		if (!moveBundleAsset.hasErrors() && moveBundleAsset.save()) {
			flash.message = "MoveBundleAsset $moveBundleAsset.id created"
			redirect(action: "show", id: moveBundleAsset.id)
		}
		else {
			render(view: 'create', model: [moveBundleAssetInstance: moveBundleAsset])
		}
	}

	/*
	 *  Sort Assets By Selected Row Column
	 */
	@HasPermission(Permission.AssetView)
	def sortAssetList() {
		def rightBundleId = params.rightBundle
		def leftBundleId = params.leftBundle
		String sortField = params.sort == "lapplication" ? "application" : params.sort
		if (params.sortField == "lapplication") {
			params.sortField = "application"
		}
		def sideList = params.side
		MoveBundle rightMoveBundle
		MoveBundle leftMoveBundle
		List<MoveBundle> moveBundles
		List<AssetEntity> currentBundleAssets
		List<AssetEntity> moveBundleAssets
		String sessionSort
		String sessionSide
		String sessionOrder

		if ((sideList == "right" && params.sideField == "left") || (sideList == "left" && params.sideField == "right")) {
			session.setAttribute("sessionSort", params.sortField)
			session.setAttribute("sessionSide", params.sideField)
			session.setAttribute("sessionOrder", params.orderField)
		}

		if ((sideList == "right" && params.sideField == "right") || (sideList == "left" && params.sideField == "left")) {
			sessionSort = session.getAttribute("sessionSort")
			sessionSide = session.getAttribute("sessionSide")
			sessionOrder = session.getAttribute("sessionOrder")
		}

		String sort
		String order

		//Right Side AssetTable Sort
		if (sideList == "right") {
			rightMoveBundle = MoveBundle.get(rightBundleId)
			moveBundles = MoveBundle.findAllByProject(rightMoveBundle.project)
			currentBundleAssets = findAllAssetEntityByBundle(rightMoveBundle, sortField, params.order)

			sort = order = null
			if (leftBundleId) {
				leftMoveBundle = MoveBundle.get(leftBundleId)
				if (params.sideField == "left") {
					sort = params.sortField
					order = params.orderField
				}
				else if (params.sideField == "right") {
					if (sessionSort != null) {
						sort = sessionSort
						order = sessionOrder
					}
				}
				moveBundleAssets = findAllAssetEntityByBundle(leftMoveBundle, sort, order)
			}
			else {
				sort = order = null
				if (params.sideField == "left") {
					sort = params.sortField
					order = params.orderField
				}
				else if (params.sideField == "right") {
					if (sessionSort != null) {
						sort = sessionSort
						order = sessionOrder
					}
				}
				moveBundleAssets = findAllAssetEntityByCurrentProject(sort, order)
			}
		}
		//Left Side AssetTable Sort
		else {
			if (leftBundleId) {
				leftMoveBundle = MoveBundle.get(leftBundleId)
				moveBundleAssets = findAllAssetEntityByBundle(leftMoveBundle, sortField, params.order)
			}
			else {
				moveBundleAssets = findAllAssetEntityByCurrentProject(sortField, params.order)
			}
			rightMoveBundle = MoveBundle.get(rightBundleId)
			moveBundles = MoveBundle.findAllByProject(rightMoveBundle.project)

			sort = order = null
			if (params.sideField == "right") {
				sort = params.sortField
				order = params.orderField
			}
			else if (params.sideField == "left") {
				if (sessionSort != null) {
					sort = sessionSort
					order = sessionOrder
				}
			}
			currentBundleAssets = findAllAssetEntityByBundle(rightMoveBundle, sort, order)
		}
		render(view: 'assignAssets',
		       model: [moveBundles: moveBundles, currentBundleAssets: currentBundleAssets, sortField: params.sort,
		               moveBundleInstance: rightMoveBundle, leftBundleInstance: leftMoveBundle, orderField: params.order,
		               moveBundleAssets: moveBundleAssets, sideField: params.side])
	}

	//get teams for selected bundles.
	@HasPermission(Permission.BundleView)
	def retrieveTeamsForBundles() {
		def bundleId = params.bundleId
		List<ProjectTeam> teams
		if (bundleId) {
			teams = ProjectTeam.findAllByMoveBundle(MoveBundle.load(bundleId))
		}
		else {
			teams = ProjectTeam.executeQuery('from ProjectTeam where moveBundle.id in (select id from MoveBundle where project=?)',
					[securityService.loadUserCurrentProject()])
		}

		renderAsJson(teams.collect { [id: it.id, name: it.teamCode] })
	}

	//Get the List of Racks corresponding to Selected Bundle
	@HasPermission(Permission.BundleView)
	def retrieveRacksForBundles() {
		def assetEntityList = AssetEntity.findAllByMoveBundle(MoveBundle.load(params.bundleId))
		renderAsJson(assetEntityList.collect { [id: it.sourceRackName, name: it.sourceRackName] })
	}

	@HasPermission(Permission.RackView)
	def retrieveRackDetails() {
		Long bundleId = params.long('bundleId')
		def sourceRackList
		def targetRackList

		String queryForSourceRacks = '''
			select source_location as location, source_rack as rack, source_room as room
			from asset_entity
			where asset_type NOT IN ('VM', 'Blade')
			  and source_rack != ''
			  and source_rack is not null '''

		String queryForTargetRacks = '''
			select target_location as location, target_rack as rack, target_room as room
			from asset_entity
			where asset_type NOT IN ('VM', 'Blade')
			  and target_rack != ''
			  and target_rack is not null '''

		String sourceGroup = 'group by source_location, source_rack, source_room'
		String targetGroup = 'group by target_location, target_rack, target_room'

		if (bundleId) {
			userPreferenceService.setMoveBundleId(bundleId)
			sourceRackList = jdbcTemplate.queryForList(queryForSourceRacks + 'and move_bundle_id=? ' + sourceGroup, bundleId)
			targetRackList = jdbcTemplate.queryForList(queryForTargetRacks + 'and move_bundle_id=? ' + targetGroup, bundleId)
		}
		else {
			long projectId = securityService.userCurrentProjectId.toLong()
			sourceRackList = jdbcTemplate.queryForList(queryForSourceRacks + 'and project_id=? ' + sourceGroup, projectId)
			targetRackList = jdbcTemplate.queryForList(queryForTargetRacks + 'and project_id=? ' + targetGroup, projectId)
		}

		renderAsJson([[sourceRackList: sourceRackList, targetRackList: targetRackList]])
	}

	@HasPermission(Permission.AssetView)
	def retrieveAssetTagLabelData() {
		def moveBundleId = params.moveBundle
		def projectId = params.project
		def reportFields = []
		if (!moveBundleId || !projectId) {
			reportFields << [flashMessage: "Please Select Bundles."]
			renderAsJson reportFields
			return
		}

		def assetsQuery = new StringBuilder('''SELECT ae.asset_entity_id as id, ae.asset_name as assetName, ae.asset_tag as assetTag,
							ae.move_bundle_id as bundle, ae.asset_type as type, ae.source_blade_chassis as chassis, ae.source_rack as rack,
							ae.source_blade_position as bladePos, ae.source_rack_position as uposition
							FROM asset_entity ae WHERE ae.project_id=? ''')
		List args = [projectId as Long]
		if (moveBundleId != "all") {
			assetsQuery.append(' AND ae.move_bundle_id=?')
			args << moveBundleId as Long
		}
		assetsQuery.append('ORDER BY ae.source_rack, ae.source_rack_position DESC')
		def assetEntityList = jdbcTemplate.queryForList(assetsQuery.toString(), args as Object[])
		if (!assetEntityList) {
			reportFields << [flashMessage: "Team Members not Found for selected Teams"]
		}
		else {
			assetEntityList.each {
				if (it.type == "Blade") {
					def chassisAsset = AssetEntity.findWhere(assetTag: it.chassis, moveBundle: MoveBundle.get(it.bundle))
					def pos = it.bladePos ? "-" + it.bladePos : ""
					it.rack = chassisAsset?.sourceRackName + pos
					it.assetName = chassisAsset?.assetName
				}
				else {
					it.rack += it.uposition ? ("-" + it.uposition) : ""
				}
			}

			reportFields << assetEntityList
		}

		renderAsJson reportFields
	}

	private List<AssetEntity> findAllAssetEntityByBundle(MoveBundle moveBundle, String sort, String order) {
		AssetEntity.findAllByMoveBundle(moveBundle, [sort: sort, order: order])
	}

	private List<AssetEntity> findAllAssetEntityByCurrentProject(String sort = null, String order = null) {
		AssetEntity.findAllByProjectAndMoveBundleIsNull(
				securityService.loadUserCurrentProject(), [sort: sort, order: order])

	}
}
