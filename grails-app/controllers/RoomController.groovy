import com.tds.asset.AssetCableMap
import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.HtmlUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.command.RoomCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.Model
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Rack
import net.transitionmanager.domain.Room
import net.transitionmanager.security.Permission
import net.transitionmanager.service.AssetEntityService
import net.transitionmanager.service.ControllerService
import net.transitionmanager.service.RackService
import net.transitionmanager.service.RoomService
import net.transitionmanager.service.TaskService
import net.transitionmanager.service.UserPreferenceService

@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class RoomController implements ControllerMethods {

	static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
	static defaultAction = 'list'

	AssetEntityService assetEntityService
	ControllerService controllerService
	RackService rackService
	RoomService roomService
	TaskService taskService
	UserPreferenceService userPreferenceService

	@HasPermission(Permission.RackMenuView)
	def list() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		// Not sure what is being stored into the user session...
		def rackIds = session.getAttribute("RACK_ID")

		params.max = params.int('max', 100)

		def model = [:]

		def rooms = Room.findAll("FROM Room WHERE project =:project order by location, roomName", [project: project])
		def roomId = userPreferenceService.getPreference(PREF.CURR_ROOM)
		Room room = new Room()
		def entities = assetEntityService.entityInfo(project)
		def moveBundleList = MoveBundle.findAllByProject(project,[sort:'name'])

		model = [
			applications: entities.applications,
			dbs: entities.dbs,
			dependencyStatus: entities.dependencyStatus,
			dependencyType: entities.dependencyType,
			files: entities.files,
			filterRackId: rackIds,
			moveBundleList: moveBundleList,
			networks: entities.networks,
			projectId: project.id,
			roomId: roomId,
			roomInstance: room,
			roomInstanceList: rooms,
			roomInstanceTotal: rooms.size(),
			servers: entities.servers,
			staffRoles: taskService.getRolesForStaff(),
			viewType: params.viewType
		]

		return model
	}

	@HasPermission(Permission.RoomCreate)
	def create() {
		[roomInstance: new Room(params)]
	}

	@HasPermission(Permission.RoomCreate)
	def save() {
		RoomCommand roomCommand = populateCommandObject(RoomCommand)
		flash.message = roomService.save(roomCommand)
		redirect(action: "list", params: [viewType: "list"])
	}

	@HasPermission(Permission.RoomView)
	def show() {

		session.removeAttribute("RACK_ID")
		Room room = Room.get(params.id)
		userPreferenceService.setPreference(PREF.CURR_ROOM, room?.id)
		if (!room) {
			userPreferenceService.removePreference(PREF.CURR_ROOM)
			flash.message = "Current Room not found"
			redirect(action: "list", params:[viewType : "list"])
			return
		}

		def auditView
		if (params.containsKey("auditView")) {
			auditView = params.auditView
			userPreferenceService.setPreference(PREF.AUDIT_VIEW, params.auditView)
		} else {
			auditView = session.AUDIT_VIEW ?: userPreferenceService.getPreference(PREF.AUDIT_VIEW) ?: 0
		}

		Project project = securityService.userCurrentProject
		List<Room> rooms = Room.findAllByProject(project, [sort: 'roomName', order: 'asc'])
		def moveBundleList = []
		def moveBundleId = params.moveBundleId
		def racksList = []
		def racks = []
		List<Object[]> idAndNamePairs = MoveBundle.executeQuery(
			'SELECT id, name from MoveBundle m where project=:project',
			[project :project])

		LinkedList bundleLists = new LinkedList(idAndNamePairs)
		bundleLists.addFirst(['taskReady', 'Active Tasks'])
		def statusList = [:]
		if (moveBundleId && !moveBundleId.contains("all") && !moveBundleId.contains("taskReady")) {
				userPreferenceService.removePreference(PREF.HIGHLIGHT_TASKS)
				moveBundleList = MoveBundle.getAll(moveBundleId.split(",")*.toLong()).findAll()
				moveBundleList.each { moveBundle->
					moveBundle.sourceRacks.findAll { it.roomId == room?.id }.each { sourceRack->
						if (!racksList.contains(sourceRack)) {
							racksList.add(sourceRack)
						}
					}
					moveBundle.targetRacks.findAll { it.roomId == room?.id }.each { targetRack->
						if (!racksList.contains(targetRack)) {
							racksList.add(targetRack)
						}
					}
				}
		} else if (moveBundleId?.contains("all")) {
			userPreferenceService.removePreference(PREF.HIGHLIGHT_TASKS)
			racksList = Rack.findAllByRoom(room)
			moveBundleList = [id: 'all']
		} else if (userPreferenceService.getPreference(PREF.HIGHLIGHT_TASKS) || moveBundleId?.contains("taskReady")) {
			def roomAssets = room.sourceAssets + room.targetAssets
			Set assetsByStatus = AssetComment.findAllByAssetEntityInListAndStatusInList(roomAssets,
								[AssetCommentStatus.STARTED, AssetCommentStatus.READY, AssetCommentStatus.HOLD]).assetEntity
			racks = assetsByStatus.rackSource + assetsByStatus.rackTarget
			racks.removeAll([null])
			racks.each {
				def statuses = AssetComment.findAllByAssetEntityInListOrAssetEntityInList(it.sourceAssets, it.targetAssets)?.status
				statusList[it.id] = statuses.contains("Hold") ? "task_hold" : statuses.contains("Started") ? "task_started" : "task_ready"
			}
			userPreferenceService.setPreference(PREF.HIGHLIGHT_TASKS, moveBundleId)
			moveBundleId = 'taskReady'
		}

		def browserTestiPad = request.getHeader("User-Agent").toLowerCase().contains("ipad") ?:
		                      request.getHeader("User-Agent").toLowerCase().contains("mobile")

		[roomInstance: room, roomInstanceList: rooms, moveBundleList: moveBundleList, project: project,
		 racksList: racksList, source: params.source, target: params.target, projectId: project.id,
		 capacityView: params.capView, capacityType: params.capType ?: 'Remaining', auditPref: auditView,
		 browserTestiPad: browserTestiPad, statusList: statusList, bundleList: bundleLists, moveBundleId: moveBundleId]
	}

	@HasPermission(Permission.RoomEdit)
	def edit() {
		Room room = Room.get(params.id)
		if (!room) {
			flash.message = "Current Room not found"
			redirect(action: "list")
			return
		}

		List<Rack> racks = Rack.findAllByRoom(room, [sort: "tag"])
		def prefVal = userPreferenceService.getPreference(PREF.ROOM_TABLE_SHOW_ALL) ?: 'FALSE'
		List<Model> models = Model.findAllByRoomObjectAndAssetType(true, 'Rack')
		def newRacks = []
		for (int i = 50000 ; i<50051; i++) {
			newRacks << i
		}

		[roomInstance: room, rackInstanceList: racks, newRacks: newRacks, modelList: models, prefVal: prefVal,
		 draggableRack: userPreferenceService.getPreference(PREF.DRAGGABLE_RACK),
		 defaultRackModel: rackService.getDefaultRackModel()]
	}

	/**
	 * Provide layout to create/edit rack.
	 * @param id:Rack id
	 * @return room layout
	 */
	@HasPermission(Permission.RoomEdit)
	def roomObject() {
		def rack = Rack.get(params.id) ?: new Rack()
		render(template: "roomObject", model: [rack: rack, rackId: params.id])
	}

	/**
	 * Update Room and Rack information
	 */
	@HasPermission(Permission.RoomEdit)
	def update() {
		Project project = controllerService.getProjectForPage(this)
		if (! project) return

		def powerType = userPreferenceService.getPreference(PREF.CURR_POWER_TYPE)
		def roomId = params.id
		List rackIds = request.getParameterValues("rackId")

		def msg
		try {
			msg = roomService.updateRoomAndRacksInfo(project, roomId, rackIds, params, powerType)

			// Set user preference for Show All (TODO - this should NOT be here)
			userPreferenceService.setPreference(PREF.ROOM_TABLE_SHOW_ALL, params.showAll == '1')

		} catch (e) {
			e.printStackTrace()
			log.info "Calling roomService.updateRoomAndRacksInfo() failed - $e.message\n${ExceptionUtil.stackTraceToString(e,60)}"
			msg = 'An unexpected error occurred during the update'
		}

		flash.error = msg
		chain(action: "show", id: params.id)
	}

	/**
	 * Used to delete one or more rooms that do not have associated assets assigned to the room
	 * @param checkbox_{roomId}
	 */
	@HasPermission(Permission.RoomDelete)
	def delete() {
		Project project = securityService.userCurrentProject
		def roomIds = []

		// Iterate over the params looking for the checkboxes that have been selected
		params.each { n, v ->
			if (n.startsWith('checkbox_') && params[n] == 'on') {
				roomIds << n.split(/_/)[1]
			}
		}

		if (! roomIds.size()) {
			flash.message "Please select at least one room to be deleted before clicking Delete"
		} else {
			int s = 0
			try {
				def results = roomService.deleteRoom(project, roomIds)
				s = results.size()
				if (s) {
					flash.message = "$s room${s > 1 ? 's were' : ' was'} not deleted either because ${s > 1 ? 'they were' : 'it was'} associated with devices or not found"
				}
			} catch (e) {
				flash.message = "The selected room${s > 1 ? 's were' : ' was'} not deleted due to: $e.message"
			}
		}

		redirect(action: "list", params:[viewType : "list"])
	}

	/**
	 * Verify if rack has associated with any assets before deleting it.
	 */
	@HasPermission(Permission.RoomEdit)
	def verifyRackAssociatedRecords() {
		AssetEntity assetEntity
		Rack rack = Rack.get(params.rackId)
		if (rack) {
			assetEntity = AssetEntity.findByRackSourceOrRackTarget(rack, rack)
		}

		if (!assetEntity) {
			assetEntity = []
		}

		render assetEntity as JSON
	}

	/**
	 * Verify if Room has associated with any assets before deleting it.
	 */
	@HasPermission(Permission.RoomEdit)
	def verifyRoomAssociatedRecords() {
		Room room = Room.get(params.roomId)
		def associatedRecords

		if (room) {
			associatedRecords = AssetEntity.findByRoomSourceOrRoomTarget(room, room) ?: Rack.findByRoom(room)
		}

		if (!associatedRecords) {
			associatedRecords = []
		}

		render associatedRecords as JSON
	}

	/**
	 *  Return Power details as string to show at room layout.
	 */
	@HasPermission(Permission.RoomView)
	def retrieveRackPowerData() {
		def room = Room.read(params.roomId)
		def racks = Rack.findAllByRoom(room)
		def location = room.source == 1 ? "source" : "target"
		def powerType = userPreferenceService.getPreference(PREF.CURR_POWER_TYPE)
		def totalPower = 0
		def totalSpace = 0
		def spaceUsed = 0
		def powerUsed = 0
		def moveBundles = MoveBundle.findAllByProject(room.project)
		List bundleIds = request.getParameterValues("moveBundleId")
		if (bundleIds && !bundleIds.contains("all") && params.otherBundle != "on") {
			moveBundles = MoveBundle.getAll(bundleId*.toLong()).findAll()
		}
		
		racks.each { obj ->
			totalPower += obj.powerA + obj.powerB + obj.powerC
			totalSpace += obj.rackType == 'Rack' ? (obj.model?.usize ?: 42) : 0
			def assetsInRack = location == "source" ? AssetEntity.findAllByRackSource(obj, [sort:'sourceRackPosition']) :
			                                          AssetEntity.findAllByRackTarget(obj, [sort:'targetRackPosition'])
			assetsInRack.findAll{ it.assetType != 'Blade' }.each { assetEntity ->
				spaceUsed += assetEntity?.model?.usize ? assetEntity?.model?.usize : 1
				def powerConnectors = AssetCableMap.executeQuery(
					'FROM AssetCableMap WHERE assetFromPort.type=? AND assetFrom=?', ["Power", assetEntity])
				def powerConnectorsAssigned = powerConnectors.size()
				def rackPower = assetEntity.model?.powerDesign ? assetEntity.model?.powerDesign : 0
				if (powerConnectorsAssigned) {
					def powerUseForConnector = rackPower ? rackPower / powerConnectorsAssigned : 0
					powerConnectors.each { cables ->
						powerUsed += powerUseForConnector
					}
				}
			}
		}
		
		if (params.capacityType != "Used") {
			spaceUsed = totalSpace - spaceUsed
			powerUsed = totalPower - powerUsed
		}
		
		powerUsed = convertPower(powerUsed, powerType)
		totalPower = convertPower(totalPower, powerType)
		def powerA = 0
		def powerB = 0
		def powerC = 0
		def powerX = 0
		def rackPowerA = 0
		def rackPowerB = 0
		def rackPowerC = 0
		def spaceString = ""
		def powerString = powerType == "Watts" ? "W" : "Amps"
		def thisRackUsedSpace = 0
		def rackId = params.rackId
		def rack
		def totalPowerInRack = 0
		def unassignedPowerInRack = 0
		if (rackId) {
			rack = Rack.get(rackId)
			def assets = AssetEntity.findAllByRackSource(rack)
			if (rack.source != 1) {
				assets = AssetEntity.findAllByRackTarget(rack)
			}
			int thisRackTotalSpace = rack.model?.usize ?: 42
			
			def assetsInRack = location == "source" ? AssetEntity.findAllByRackSource(rack, [sort: 'sourceRackPosition']) :
			                                          AssetEntity.findAllByRackTarget(rack, [sort: 'targetRackPosition'])
			def assetPos = 0
			def prevUsize = 0
			assetsInRack.findAll{ it.assetType != 'Blade' }.each { assetEntity ->
				// Calculating current assets's position .
				def posResult = retrieveRackPosDetails(assetEntity, assetPos, prevUsize, location, rack)
				thisRackUsedSpace += posResult.assetUsize
				
				// Assigning rack position to keep track on upcoming asset's position in loop .
				assetPos = posResult.assetUsize > 0 ? posResult.currAssetPos : assetPos
				prevUsize = posResult.thisUsize > 0 ? posResult.thisUsize : prevUsize
			}
			spaceString = params.capacityType != "Used" ? (thisRackTotalSpace - thisRackUsedSpace) +
				" remaining of " + thisRackTotalSpace + " RU" :
				thisRackUsedSpace + " used of " + thisRackTotalSpace + " RU"
			assets.each { asset->
				def assetPowerCabling = AssetCableMap.executeQuery(
					'FROM AssetCableMap cap WHERE cap.assetFromPort.type = ? AND cap.assetFrom = ?',["Power",asset])
				def powerConnectors = assetPowerCabling.size()
				def powerConnectorsAssigned = assetPowerCabling.findAll{it.toPower != null && it.toPower != '' }.size()
				
				def powerDesign = asset.model?.powerDesign ? asset.model?.powerDesign : 0
				totalPowerInRack = powerDesign + totalPowerInRack
				assetPowerCabling.each {
					if (it.toPower==null) {
						unassignedPowerInRack = powerDesign/powerConnectors + unassignedPowerInRack
					}
				}
				if (powerConnectorsAssigned) {
					def powerUseForConnector = powerDesign ? (powerDesign / powerConnectors) : 0
					assetPowerCabling.each { cables ->
						if (cables.toPower) {
							switch(cables.toPower) {
								case "A": powerA += powerUseForConnector; break
								case "B": powerB += powerUseForConnector; break
								case "C": powerC += powerUseForConnector; break
							}
						}
					}
				}
				powerX = unassignedPowerInRack
			}
			
			powerA = convertPower(powerA, powerType)
			powerB = convertPower(powerB, powerType)
			powerC = convertPower(powerC, powerType)
			powerX = convertPower(powerX, powerType)
			
			rackPowerA = convertPower(rack.powerA, powerType)
			rackPowerB = convertPower(rack.powerB, powerType)
			rackPowerC = convertPower(rack.powerC, powerType)
		}
		
		def redTBD = false
		if ((powerA +powerB+ powerC+ powerX) > (rackPowerA+ rackPowerB + rackPowerC)) {
			redTBD = true
		}
		def op="""<table style='width:300px;padding:0px;border:0px;'>
			<tr>
				<td class='powertable_H'>Room <br />Totals: </td>
				<td colspan=2 class='powertable_H' >Space: (RU)<br />$spaceUsed / $totalSpace</td>
				<td colspan=2 class='powertable_H' >Power ($powerString):<br />$powerUsed / $totalPower</td>
			</tr>
			<tr><td class='powertable_L'>&nbsp;</td></tr>"""
		if (rack) {
			op += """
					<tr>
						<td colspan=2 class='powertable_L'><b>Rack : ${HtmlUtil.escape(rack?.tag)}</b></td>
						<td colspan=3 class='powertable_L' nowrap>$spaceString</td>
					</tr>
					<tr>
						<td class='powertable_L'>Power ($powerString)</td>
						<td style='background:${powerA > rackPowerA ? 'red' : ''};' class='powertable_R'>A</td>
						<td style='background:${powerB > rackPowerB ? 'red' : ''};' class='powertable_R'>B</td>
						<td style='background:${powerC > rackPowerC ? 'red' : ''};' class='powertable_R'>C</td>
						<td style='background:${redTBD ? 'red':''};' class='powertable_R'>TBD</td>
					</tr>
					<tr>
						<td class='powertable_R'>&nbsp;In Rack:</td><td class='powertable_R'>$rackPowerA</td>
						<td class='powertable_R'>$rackPowerB</td><td class='powertable_R'>$rackPowerC</td>
						<td class='powertable_R'>&nbsp;</td></tr><tr><td class='powertable_R'>&nbsp;Used:</td>
						<td class='powertable_R'>$powerA</td><td class='powertable_R'>$powerB</td><td class='powertable_R'>$powerC</td>
						<td class='powertable_R'>$powerX</td>
					</tr></table>"""
		} else {
			op += "</table>"
		}
		
		render  op
	}
	
	/**
	 *  Return assets list as html row format to assign racks
	 */
	@HasPermission(Permission.RoomView)
	def retrieveAssetsListToAddRack() {
		def order = params.order ?: 'asc'
		Project project = securityService.userCurrentProject
		def source = params.source
		def sort = params.sort ?: 'assetName'
		def assign = params.assign
		def excludeAssetType = ['Blade', 'Application', 'Database', 'Files', 'VM', 'Virtual', 'Virtual Machine']
		def query = "from AssetEntity where project =:project and assetType not in (:excludeAssetType) and assetClass =:assetClass "
		if (assign != 'all') {
			query += ' and (rack' + (source == '1' ? 'Source' : 'Target') + ') is null'
		}
		order = order == 'asc' ? 'desc' : 'asc'
		query += " order by $sort $order"
		def entities = AssetEntity.findAll(query,[project:project, excludeAssetType:excludeAssetType, assetClass: AssetClass.DEVICE ])
		
		def html = new StringBuilder()
		html.append("""
			<div class="dialog" >
			<table id="listDiv">
			<thead>
				<tr>
					<th class="sortable ${sort=='assetName' ? 'sorted '+order :''}"><a href="javascript:listDialog('$assign', 'assetName','$order','$source','$params.rack','$params.roomName','$params.location','$params.position')">Asset Name</a></th>
					<th class="sortable ${sort=='assetTag' ? 'sorted '+order :''}"><a href="javascript:listDialog('$assign', 'assetTag','$order','$source','$params.rack','$params.roomName','$params.location','$params.position')">Asset Tag</a></th>
					<th class="sortable ${sort=='model' ? 'sorted '+order :''}"><a href="javascript:listDialog('$assign', 'model','$order','$source','$params.rack','$params.roomName','$params.location','$params.position')">Model</a></th>
				</tr>
			</thead>
			<tbody class="tbody" >
			""")
		if (entities) {
			entities.eachWithIndex{ obj, i ->
				html.append("""<tr class="${i % 2 == 0 ? 'odd' : 'even'}" onclick="EntityCrud.showAssetEditView('$obj.assetClass',$obj.id,'$source','$params.rack','$params.roomName','$params.location','$params.position', false, closeAssignAssetListDialog);">
					<td>$obj.assetName</td>
					<td>$obj.assetTag</td>
					<td>${obj.model ? obj.model.modelName : ''}</td>
				</tr>""")
			}
		} else {
			html.append("<tr><td colspan='3' class='no_records'>No records found</td></tr>")
		}
		html.append("</tbody></table></div>")
		
		render html.toString()
	}
	
	/**
	 *  Return blades list as html row format to assign blade chassis
	 */
	@HasPermission(Permission.RoomView)
	def retrieveBladeAssetsListToAddRack() {
		def source = params.source
		
		String middle = ''
		if (params.assign == 'assign') {
			middle = (source == '1' ? 'source' : 'target') + 'Chassis is null and '
		}
		String hql = 'from AssetEntity where ' + middle + "project=:projectId and assetType = 'Blade'"
		List<AssetEntity> entities = AssetEntity.executeQuery(hql, [projectId: securityService.userCurrentProjectId])
		
		def html = new StringBuilder()
		
		def bladeAsset = AssetEntity.get(params.blade)
		def bladeAssetId = bladeAsset?.id
		def bundleId = bladeAsset?.moveBundleId
		html.append("""
			<div class="dialog" >
			<table id="listDiv">
			<tbody class="tbody" >
			""")
		if (entities) {
			entities.eachWithIndex{ obj, i ->
				html.append("""<tr class="${i % 2 == 0 ? 'odd' : 'even'}" onclick="editBladeDialog('$obj.assetClass',$obj.id,'$source','$bladeAssetId','$params.roomName','$params.location','$params.position')">
											<td>$obj.assetName</td>
											<td>$obj.assetTag</td>
											<td>${obj.model ? obj.model.modelName : ''}</td>
										</tr>""")
			}
		} else {
			html.append("<tr><td colspan='3' class='no_records'>No records found</td></tr>")
		}
		html.append("</tbody></table></div>")
		
		render html.toString()
	}
	
	/**
	 * Room Capacity Scaling
	 */
	@HasPermission(Permission.RoomView)
	def retrieveCapacityView() {
		def capacityData = [:]
		def room = Room.read(params.roomId)
		def racks = Rack.findAllByRoomAndRackType(room,"Rack")
		def capacityView = params.capacityView
		def capacityType = params.capacityType
		def rackData = [:]
		def maxU = 42
		def maxPower = 1
		def location = room?.source == 1 ? "source" : "target"
		def rackCountMap = [:]
		racks.each { rack ->
			def rackUsize = rack.model?.usize ?:42
			def rackPower = rack.powerA + rack.powerB + rack.powerC
			if (rackPower && maxPower < rackPower) {
				maxPower = rackPower
			}

			def assetsInRack = location == "source" ? AssetEntity.findAllByRackSource(rack, [sort:'sourceRackPosition']) :
					AssetEntity.findAllByRackTarget(rack, [sort:'targetRackPosition'])
			def usedRacks = 0
			def powerUsed = 0
			def assetPos = 0 // a flag to determine the previous asset's  rack position .
			def prevUsize = 0 // a flag to determine the previous asset's  usize .
			assetsInRack.findAll{ it.assetType != 'Blade' }.each { assetEntity ->

			def posResult = retrieveRackPosDetails(assetEntity, assetPos, prevUsize, location, rack)
				usedRacks += posResult.assetUsize

				def powerConnectors = AssetCableMap.findAll("FROM AssetCableMap cap WHERE cap.toPower is not null AND cap.assetFromPort.type = ? AND cap.assetFrom = ? ",["Power",assetEntity])
				def powerConnectorsAssigned = powerConnectors.size()
				def totalPower = assetEntity.model?.powerDesign ? assetEntity.model?.powerDesign : 0
				if (powerConnectorsAssigned) {
					def powerUseForConnector = totalPower ? totalPower / powerConnectorsAssigned : 0
					powerConnectors.each { cables ->
						powerUsed += powerUseForConnector
					}
				}
				// Assigning rack position to keep track on upcoming asset's position in loop .
				assetPos = posResult.assetUsize > 0 ? posResult.currAssetPos : assetPos
				prevUsize = posResult.thisUsize > 0 ? posResult.thisUsize : prevUsize
			}
			switch(capacityView) {
				case "Space":
					if (capacityType != "Used") {
						usedRacks = rackUsize - usedRacks
						String value
						if (usedRacks <= Math.round(maxU*0.2)) {
							value = "rack_cap100"
						} else if (usedRacks <= Math.round(maxU*0.32)) {
							value = "rack_cap80"
						} else if (usedRacks <= Math.round(maxU*0.44)) {
							value = "rack_cap68"
						} else if (usedRacks <= Math.round(maxU*0.56)) {
							value = "rack_cap56"
						} else if (usedRacks <= Math.round(maxU*0.68)) {
							value = "rack_cap44"
						} else if (usedRacks <=  Math.round(maxU*0.80)) {
							value = "rack_cap32"
						} else {
							value = "rack_cap20"
						}
						rackData[rack.id.toString()] = value
						rackCountMap["rack_" + rack.id] = usedRacks
					} else {
						String value
						if (usedRacks <= Math.round(maxU*0.2)) {
							value = "rack_cap20"
						} else if (usedRacks <= Math.round(maxU*0.32)) {
							value = "rack_cap32"
						} else if (usedRacks <= Math.round(maxU*0.44)) {
							value = "rack_cap44"
						} else if (usedRacks <= Math.round(maxU*0.56)) {
							value = "rack_cap56"
						} else if (usedRacks <= Math.round(maxU*0.68)) {
							value = "rack_cap68"
						} else if (usedRacks <= Math.round(maxU*0.80)) {
							value = "rack_cap80"
						} else {
							value = "rack_cap100"
						}
						rackData[rack.id.toString()] = value
						rackCountMap["rack_" + rack.id] = usedRacks
					}
					break
			  case "Power":
				 if (capacityType != "Used") {
					powerUsed = rackPower - powerUsed
					int value
					if (powerUsed <= Math.round(rackPower*0.2)) {
						value = 100
					}
					else if (powerUsed <= Math.round(rackPower*0.32)) {
						value = 80
					}
					else if (powerUsed <= Math.round(rackPower*0.44)) {
						value = 68
					}
					else if (powerUsed <= Math.round(rackPower*0.56)) {
						value = 56
					}
					else if (powerUsed <= Math.round(rackPower*0.68)) {
						value = 44
					}
					else if (powerUsed <= Math.round(rackPower*0.80)) {
						value = 32
					}
					else {
						value = 20
					}
					rackData[rack.id.toString()] = 'rack_cap' + value
				}
				else {
						if (powerUsed <= Math.round(rackPower*0.2)) {
							rackData[rack.id.toString()] = "rack_cap20"
						}else if (powerUsed <= Math.round(rackPower*0.32)) {
							rackData[rack.id.toString()] = "rack_cap32"
						}else if (powerUsed <= Math.round(rackPower*0.44)) {
							rackData[rack.id.toString()] = "rack_cap44"
						}else if (powerUsed <= Math.round(rackPower*0.56)) {
							rackData[rack.id.toString()] = "rack_cap56"
						}else if (powerUsed <= Math.round(rackPower*0.68)) {
							rackData[rack.id.toString()] = "rack_cap68"
						}else if (powerUsed <= Math.round(rackPower*0.80)) {
							rackData[rack.id.toString()] = "rack_cap80"
						}else{
							rackData[rack.id.toString()] = "rack_cap100"
						}
					}
					break
			}
		}

		// Implement the Scale display
		capacityData.rackData = rackData
		capacityData.racks = racks.id
		capacityData.rackCountMap = rackCountMap

		def powerType = userPreferenceService.getPreference(PREF.CURR_POWER_TYPE) ?: "Watts"
		maxPower = convertPower(maxPower, powerType)
		// Added switch to use if we use other capacityViews
		switch(capacityView) {
			case "Space":
				if (capacityType != "Used") {
					capacityData.view = [
							cap20:"${Math.round(maxU*0.80)}+ RU",
							cap32:"${Math.round(maxU*0.68)}+ RU",
							cap44:"${Math.round(maxU*0.56)}+ RU",
							cap56:"${Math.round(maxU*0.44)}+ RU",
							cap68:"${Math.round(maxU*0.32)}+ RU",
							cap80:"${Math.round(maxU*0.20)}+ RU",
							cap100:"< ${Math.round(maxU*0.2)} RU"
						]
				} else {
					capacityData.view = [
							cap20:"< ${Math.round(maxU*0.2)} RU",
							cap32:"${Math.round(maxU*0.20)}+ RU",
							cap44:"${Math.round(maxU*0.32)}+ RU",
							cap56:"${Math.round(maxU*0.44)}+ RU",
							cap68:"${Math.round(maxU*0.56)}+ RU",
							cap80:"${Math.round(maxU*0.68)}+ RU",
							cap100:"${Math.round(maxU*0.8)}+ RU"
						]
				}
				break
			case "Power":
				if (capacityType != "Used") {
					capacityData.view = [
							cap20:"${Math.round(maxPower*0.80)}+ $powerType",
							cap32:"${Math.round(maxPower*0.68)}+ $powerType",
							cap44:"${Math.round(maxPower*0.56)}+ $powerType",
							cap56:"${Math.round(maxPower*0.44)}+ $powerType",
							cap68:"${Math.round(maxPower*0.32)}+ $powerType",
							cap80:"${Math.round(maxPower*0.20)}+ $powerType",
							cap100:"< ${Math.round(maxPower*0.2)} $powerType"
						]
				} else {
					capacityData.view = [
							cap20:"< ${Math.round(maxPower*0.2)} $powerType",
							cap32:"${Math.round(maxPower*0.20)}+ $powerType",
							cap44:"${Math.round(maxPower*0.32)}+ $powerType",
							cap56:"${Math.round(maxPower*0.44)}+ $powerType",
							cap68:"${Math.round(maxPower*0.56)}+ $powerType",
							cap80:"${Math.round(maxPower*0.68)}+ $powerType",
							cap100:"${Math.round(maxPower*0.8)}+ $powerType"
						]
				}
				break
		}
		if (capacityData.view) {
			render capacityData as JSON
		} else {
			render "None"
		}
	}

	@HasPermission(Permission.RoomEdit)
	def setDraggableRackPref() {
		userPreferenceService.setPreference(PREF.DRAGGABLE_RACK, params.prefVal)
		render 'success'
	}

	/**
	 * This method is used to fetch all the assets By position , rack and location .
	 * @param rack - rack instance could be either source rack or target rak
	 * @param location - location of rack could be either 'source' and 'target'
	 * @param rackPos - the position of rack where we are requesting this method to fetch the assets
	 * @return - list of assets at requested position
	 */
	private List<AssetEntity> retrieveAssetsAtPosByRackAndLoc(rack, location, assetEntity) {
		location == "source" ? AssetEntity.findAllBySourceRackPositionAndRackSource(assetEntity.sourceRackPosition, rack) :
		                       AssetEntity.findAllByTargetRackPositionAndRackTarget(assetEntity.targetRackPosition, rack)
	}

	/**
	 * Get information of assets and there relevant pos., and usize to count rack used count .
	 * @param assetEntity : instance of assetEntity .
	 * @param assetPos : previous asset rack position .
	 * @param prevUsize : previous asset's model usize .
	 * @param location : location of rack could be source or target .
	 * @param rack : instance of rack .
	 * @return : a map contains info like current and previous asset's usize and like current and previous asset's rack position .
	 */
	private Map retrieveRackPosDetails(assetEntity, assetPos, prevUsize, location, rack) {
		def currAssetPos = location == "source" ? assetEntity.sourceRackPosition : assetEntity.targetRackPosition
		def assetUsize = 0  // Initialized  to 0 to take the count of usize 0 if assets are overlapping.
		// if assets are not overLapping then it should go inside condition to calculate asset's max usize.
		def thisUsize
		if (assetPos != currAssetPos &&  currAssetPos > (assetPos + prevUsize - 1)) { // if assets are not overlapping
			// fetching  all assets  of current rack position .
			def assetsAtPos = retrieveAssetsAtPosByRackAndLoc(rack, location, assetEntity)
			// Assigning usize of that assets which has max usize .
			thisUsize = assetsAtPos.model?.usize.sort().reverse()[0] ?: 1
			assetUsize = thisUsize
		} else if (currAssetPos > assetPos && currAssetPos < (assetPos+prevUsize)) { // If assets are overlapping
			def assetsAtPos = retrieveAssetsAtPosByRackAndLoc(rack, location, assetEntity)
			//Assigning usize of that assets which has max usize .
			thisUsize = assetsAtPos.model?.usize.sort().reverse()[0] ?: 1
			if ((assetPos + prevUsize) < (currAssetPos + thisUsize)) {
				assetUsize =  thisUsize - ((assetPos + prevUsize) - (currAssetPos))
			} else {
				thisUsize = thisUsize > prevUsize ? thisUsize : prevUsize
			}
		}
		[thisUsize: thisUsize, prevUsize: prevUsize, assetUsize: assetUsize, currAssetPos: currAssetPos]
	}
}
