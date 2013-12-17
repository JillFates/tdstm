import grails.converters.JSON

import org.hibernate.SessionFactory
import org.apache.commons.lang.math.NumberUtils 

import com.tds.asset.AssetCableMap
import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetCommentStatus

class RoomController {

	def userPreferenceService
	def securityService
	def taskService
	def assetEntityService
	SessionFactory sessionFactory
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
		def rackIds = session.getAttribute("RACK_ID")
        params.max = Math.min(params.max ? params.int('max') : 100, 100)
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		if (projectId) {
			log.info "projectId=${projectId} class=${projectId.class}"
			def project = Project.findById( projectId )
			def roomInstanceList = Room.findAll("FROM Room WHERE project =:project order by location, roomName", [project:project])
			def roomId = getSession().getAttribute( "CURR_ROOM" )?.CURR_ROOM
			def roomInstance = new Room()
			def entities = assetEntityService.entityInfo( project )
			def moveBundleList = MoveBundle.findAllByProject(project,[sort:'name'])
			[roomInstanceList: roomInstanceList, roomInstanceTotal: roomInstanceList.size(), 
			 projectId:projectId, roomId:roomId, viewType:params.viewType, roomInstance:roomInstance, servers : entities.servers, networks : entities.networks,
					applications : entities.applications, dbs : entities.dbs, files : entities.files ,filterRackId:rackIds, staffRoles:taskService.getRolesForStaff(),
					dependencyType:entities.dependencyType, dependencyStatus:entities.dependencyStatus, moveBundleList:moveBundleList]
		} else {
			flash.message = 'You must have a project selected before using this feature'
			redirect(controller: "project", action: "list",params:[viewType : "list"])
		}
    }

    def create = {
        def roomInstance = new Room()
        roomInstance.properties = params
        return [roomInstance: roomInstance]
    }

    def save = {
        def roomInstance = new Room(params)
        if (roomInstance.save(flush: true)) {
            flash.message = "Room : ${roomInstance.roomName} is created"
            redirect(action: "list", params:[viewType : "list"])
        }
        else {
			flash.message = "Room : ${roomInstance.roomName} is not created"
            redirect(action: "list",params:[viewType : "list"])
        }
    }

    def show = {
		session.removeAttribute("RACK_ID")
        def roomInstance = Room.get(params.id)
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		userPreferenceService.setPreference( "CURR_ROOM", "${roomInstance?.id}" )
		def browserTestiPad = request.getHeader("User-Agent").toLowerCase().contains("ipad") ?:request.getHeader("User-Agent").toLowerCase().contains("mobile")
        if (!roomInstance) {
			userPreferenceService.removePreference("CURR_ROOM")
            flash.message = "Current Room not found"
            redirect(action: "list", params:[viewType : "list"])
        }
        else {
			def auditView 
			if(params.containsKey("auditView")){
				auditView = params.auditView
				userPreferenceService.setPreference("AUDIT_VIEW", params.auditView)
			} else {
				auditView = session.AUDIT_VIEW?.AUDIT_VIEW ?:(userPreferenceService.getPreference("AUDIT_VIEW")?:0 )
			}
			def project = Project.findById( projectId )
			def roomInstanceList = Room.findAllByProject( project, [sort:"roomName",order:'asc'])
			def moveBundleList = []
			def moveBundleId = params.moveBundleId
			def racksList = []
			def racks = []
			def moveBundlesMap = MoveBundle.executeQuery("SELECT m.id as bundleId , m.name from MoveBundle m \
					where m.project =:project",[project :project])
			
			LinkedList bundleLists = new LinkedList(moveBundlesMap);
			bundleLists.addFirst(['taskReady', 'Active Tasks'])
			def statusList = [:]
			if(moveBundleId && !moveBundleId.contains("all") && !moveBundleId.contains("taskReady")){
					userPreferenceService.removePreference("highlightTasks")
					def bundles = moveBundleId.split(",").collect{id-> Long.parseLong(id) }
					moveBundleList = MoveBundle.findAllByIdInList(bundles)
					moveBundleList.each{ moveBundle->
						moveBundle.sourceRacks.findAll{it.room?.id == roomInstance?.id}.each{ sourceRack->
							if( !racksList.contains( sourceRack ) )
								racksList.add( sourceRack )
						}
						moveBundle.targetRacks.findAll{it.room?.id == roomInstance?.id}.each{ targetRack->
							if( !racksList.contains( targetRack ) )
								racksList.add( targetRack )
						}
					}
			} else if ( moveBundleId?.contains("all") ) {
				userPreferenceService.removePreference("highlightTasks")
				racksList = Rack.findAllByRoom(roomInstance)
				moveBundleList = [id:'all']
			} else if( userPreferenceService.getPreference("highlightTasks") || moveBundleId?.contains("taskReady") ) {
					def roomAssets =  roomInstance.sourceAssets + roomInstance.targetAssets
					Set assetsByStatus = AssetComment.findAllByAssetEntityInListAndStatusInList(roomAssets,
										[AssetCommentStatus.STARTED, AssetCommentStatus.READY, AssetCommentStatus.HOLD]).assetEntity
				    racks = assetsByStatus.rackSource +  assetsByStatus.rackTarget
					racks.removeAll([null])
					racks.each{
						def statuses = AssetComment.findAllByAssetEntityInListOrAssetEntityInList(it.sourceAssets, it.targetAssets)?.status 
						def statusCss = statuses.contains("Hold") ? "task_hold" : (statuses.contains("Started") ? "task_started" : "task_ready")
						statusList << [(it.id) : statusCss]
					}
					userPreferenceService.setPreference("highlightTasks", moveBundleId)
					moveBundleId = 'taskReady'
			}
			
            [roomInstance: roomInstance, roomInstanceList:roomInstanceList, moveBundleList:moveBundleList, project:project,
			 racksList: racksList, source:params.source, target:params.target, projectId : projectId, capacityView:params.capView, capacityType:params.capType?:'Remaining',
			 auditPref:auditView, browserTestiPad:browserTestiPad, statusList:statusList, bundleList:bundleLists, moveBundleId:moveBundleId]
        }
    }

    def edit = {
        def roomInstance = Room.get(params.id)
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		def project = Project.findById( projectId )
		def rackInstanceList = Rack.findAllByRoom(roomInstance , [sort:"tag"])
		def prefVal = userPreferenceService.getPreference("roomTableShowAll")?: 'FALSE'
		def modelList = Model.findAllByRoomObject(true);		
		def newRacks = []
		for(int i = 50000 ; i<50051; i++ ){
			newRacks << i
		}
        if (!roomInstance) {
        	flash.message = "Current Room not found"
            redirect(action: "list")
        }
        else {
			def draggableRack = session.getAttribute( "DraggableRack" )?.DraggableRack
			[roomInstance: roomInstance, rackInstanceList:rackInstanceList, newRacks : newRacks, 
				modelList:modelList, draggableRack:draggableRack, prefVal:prefVal]
        }
		
    }

	/**
	 * Provide layout to create/edit rack.
	 * @param id:Rack id
	 * @return room layout 
	 */
	def roomObject = {
		def rack = Rack.get(params.id)
		if(!rack) rack = new Rack()
		render (template:"roomObject", model:[rack:rack, rackId:params.id])
	}

    def update = {
        def roomInstance = Room.get(params.id)
		def powerType = session.getAttribute('CURR_POWER_TYPE')?.CURR_POWER_TYPE
		List rackIds = request.getParameterValues("rackId")
		userPreferenceService.setPreference("roomTableShowAll", params.showAll=='1' ? 'TRUE' : 'FALSE')
		def source = params.addTargetRoom == "on" ? 0 : 1
        if (roomInstance) {
    		roomInstance.roomName = params.roomName
			roomInstance.location = params.location
			roomInstance.roomWidth = params.roomWidth ? NumberUtils.toDouble(params.roomWidth,0).round() : null
			roomInstance.roomDepth = params.roomDepth ? NumberUtils.toDouble(params.roomDepth,0).round() : null
			roomInstance.address = params.address
			roomInstance.city = params.city
			roomInstance.stateProv = params.stateProv
			roomInstance.postalCode = params.postalCode
			roomInstance.country = params.country
			roomInstance.source = source
			
            if (!roomInstance.hasErrors() && roomInstance.save(flush: true)) {
				
                def racks = Rack.findAllByRoom( roomInstance )
				racks.each{rack->
                	if(rackIds?.contains(rack.id.toString())){
						rack.tag = params["tag_"+rack.id]
	                	rack.roomX = params["roomX_"+rack.id] ? NumberUtils.toDouble(params["roomX_"+rack.id],0).round() :0 
	                	rack.roomY = params["roomY_"+rack.id] ? NumberUtils.toDouble(params["roomY_"+rack.id],0).round() :0
	                	rack.powerA = params["powerA_"+rack.id] ? NumberUtils.toDouble(params["powerA_"+rack.id],0).round() : 0
						rack.powerB = params["powerB_"+rack.id] ? NumberUtils.toDouble(params["powerB_"+rack.id],0).round() : 0
						rack.powerC = params["powerC_"+rack.id] ? NumberUtils.toDouble(params["powerC_"+rack.id],0).round() : 0
						if(powerType != "Watts"){
							rack.powerA = Math.round(rack.powerA * 120)
							rack.powerB = Math.round(rack.powerB * 120)
							rack.powerC = Math.round(rack.powerC * 120)
						}
						rack.rackType = params["rackType_"+rack.id]
						rack.front = params["front_"+rack.id]
                        def model = params["model_"+rack.id] != "null" ?  Model.get(params["model_"+rack.id]) : null
						rack.manufacturer = model?.manufacturer
						rack.model = model
	                	rack.save(flush:true)
                	} else {
						AssetEntity.executeUpdate("Update AssetEntity set rackSource = null where rackSource = ${rack.id}")
						AssetEntity.executeUpdate("Update AssetEntity set rackTarget = null where rackTarget = ${rack.id}")
						rack.delete(flush:true)
					}
                }
				rackIds.each{id->
					if(id < params.rackCount ){
						def rack = Rack.get( id )
						if( !rack ){
							def newRack = Rack.findOrCreateWhere(source:source, 'project.id':roomInstance.project.id, location:roomInstance.location, 'room.id':roomInstance?.id, tag:params["tag_"+id])
							if(newRack){
								newRack.roomX = params["roomX_"+id] ? NumberUtils.toDouble(params["roomX_"+id],0).round() :0
								newRack.roomY = params["roomY_"+id] ? NumberUtils.toDouble(params["roomY_"+id],0).round() :0
								newRack.powerA = params["powerA_"+id] ? NumberUtils.toDouble(params["powerA_"+id],0).round() : 0
								newRack.powerB = (params["powerB_"+id]) ? NumberUtils.toDouble(params["powerB_"+id],0).round() : 0
								newRack.powerC = (params["powerC_"+id]) ? NumberUtils.toDouble(params["powerC_"+id],0).round() : 0
                                def model = params["model_"+id] != "null" ?  Model.get(params["model_"+id]) : null
                                newRack.manufacturer = model?.manufacturer
                                newRack.model = model
								if(powerType != "Watts"){
									newRack.powerA = Math.round(newRack.powerA * 120)
									newRack.powerB = Math.round(newRack.powerB * 120)
									newRack.powerC = Math.round(newRack.powerC * 120)
								}
								newRack.rackType = params["rackType_"+id]
								newRack.front = params["front_"+id]
								newRack.save(flush:true)
							}
						}
					}
				}
            }
        }
    	// Update the Assests which are pointing to Room and Rack
		def racks = Rack.findAllByRoom( roomInstance )
		racks.each{rack->
			rack.sourceAssets.each{assetEntity ->
				assetEntity.sourceRack = rack.tag
				assetEntity.sourceRoom = roomInstance.roomName
				assetEntity.sourceLocation =  roomInstance.location
				assetEntity.save()
	        }
			rack.targetAssets.each{assetEntity ->
				assetEntity.targetRack = rack.tag
				assetEntity.targetRoom = roomInstance.roomName
				assetEntity.targetLocation =  roomInstance.location
				assetEntity.save()
	        }
    	}
		flush()
    	redirect(action: "show", id: params.id)
    }
	
	private void flush() {
		println "inside flush"
		def hbSession = sessionFactory.currentSession
		hbSession.flush()
		hbSession.clear()
	}
	
    def delete = {
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		def project = Project.get(projectId)
		def roomInstanceList = Room.findAllByProject( project )
		def skippingRooms = []
		roomInstanceList.each {roomInstance->
			def isSelected = params["checkbox_"+roomInstance.id]
			if (isSelected == "on") {
				try {
					if(AssetEntity.findByRoomSource(roomInstance) || AssetEntity.findByRoomTarget(roomInstance) || Rack.findByRoom(roomInstance)){
						skippingRooms << roomInstance.roomName
					} else {
						roomInstance.racks*.delete()
						roomInstance.delete(flush: true)
					}
					/**
					AssetEntity.executeUpdate("Update AssetEntity set roomSource = null where roomSource = ${roomInstance.id}")
					AssetEntity.executeUpdate("Update AssetEntity set roomTarget = null where roomTarget = ${roomInstance.id}")
					
					def rackQuery = "select r.id from Rack r where r.room = ${roomInstance.id}"
					AssetEntity.executeUpdate("Update AssetEntity set rackSource = null where rackSource in (${rackQuery})")
					AssetEntity.executeUpdate("Update AssetEntity set rackTarget = null where rackTarget in (${rackQuery})")
					Rack.executeUpdate("delete from Rack where room = ${roomInstance.id}")
					
					*/
				}
				catch (org.springframework.dao.DataIntegrityViolationException e) {
					flash.message = "Error occured while deleting Rack : ${roomInstance.roomName}"
				}
			}
		}
		if(skippingRooms.size() > 0){
			flash.message = "Rooms : ${skippingRooms} are not deleted as they were associated with AssetEntity or Rack"
		}
		userPreferenceService.removePreference("CURR_ROOM")
		redirect(action: "list", params:[viewType : "list"])
    }
	
	/**
	 *  Verify if rack has associated with any assets before deleting it.
	 */
	def verifyRackAssociatedRecords = {
		def id = params.rackId
		def rack = Rack.get(id)
		def assetEntity
		if(rack){
			assetEntity = AssetEntity.findByRackSourceOrRackTarget(rack,rack)
		} 
		if(!assetEntity)
			assetEntity = []
		render assetEntity as JSON
	}
	/**
	*  Verify if Room has associated with any assets before deleting it.
	*/
   def verifyRoomAssociatedRecords = {
       def id = params.roomId
	   def room = Room.get(id)
	   def associatedRecords
	   
	   if(room){
		   associatedRecords = AssetEntity.findByRoomSourceOrRoomTarget(room,room)
		   if(!associatedRecords)
		   associatedRecords = Rack.findByRoom(room)
	   }
	   if(!associatedRecords)
	   		associatedRecords = []
	   render associatedRecords as JSON
   }
	/**
	 *  Merge Racks and delete the selected Room and Racks
	 */
   def mergeRoom = {
	   def sourceRoomId = params.sourceRoom
	   def targetRoomId = params.targetRoom
	   if(sourceRoomId && targetRoomId){
		   def sourceRoom = Room.get(sourceRoomId)
		   def targetRoom = Room.get(targetRoomId)
		   updateAssetEntityToMergeRooms(sourceRoom,targetRoom)
		   updateRackToMergeRooms(sourceRoom,targetRoom)
		   sourceRoom.delete(flush:true)
		   redirect(action: "list", params:[viewType : "list"])
		   flash.message = "Room:${sourceRoom} is merged to Room: ${targetRoom} successfully."
	   } else {
	   	   redirect(action: "list", params:[viewType : "list"])
	   }
   }
   def updateAssetEntityToMergeRooms(sourceRoom,targetRoom){
	   AssetEntity.executeUpdate("Update AssetEntity set sourceLocation = '${targetRoom.location}', sourceRoom = '${targetRoom.roomName}', roomSource = ${targetRoom.id} where roomSource = ${sourceRoom.id}")
	   AssetEntity.executeUpdate("Update AssetEntity set targetLocation = '${targetRoom.location}', targetRoom = '${targetRoom.roomName}', roomTarget = ${targetRoom.id} where roomTarget = ${sourceRoom.id}")
   }
   def updateRackToMergeRooms(sourceRoom,targetRoom){
	   def sourceRoomRacks = Rack.findAllByRoom( sourceRoom )
	   sourceRoomRacks.each{ sourceRack ->
		   def rackTarget = Rack.findOrCreateWhere(source:sourceRack.source, 'project.id':sourceRack.project.id, location:sourceRack.location, 'room.id':targetRoom?.id, tag:sourceRack.tag)
		   // Update all assets with this rack info to point to this rack
		   if(!rackTarget){
			   println "Unable to create rack: ${rackTarget.errors}"
			   AssetEntity.executeUpdate("Update AssetEntity set rackSource = null where rackSource = ${sourceRack.id}")
			   AssetEntity.executeUpdate("Update AssetEntity set rackTarget = null where rackTarget = ${sourceRack.id}")
		   } else {
			   AssetEntity.executeUpdate("Update AssetEntity set rackSource = ${rackTarget.id} where rackSource = ${sourceRack.id}")
			   AssetEntity.executeUpdate("Update AssetEntity set rackTarget = ${rackTarget.id} where rackTarget = ${sourceRack.id}")
		   }
		   sourceRack.delete(flush:true)
	   }
   }
   /**
    *  Return Power details as string to show at room layout.
    */
   def getRackPowerData = {
	   def room = Room.read(params.roomId)
	   def racks = Rack.findAllByRoom(room)
	   def location = room.source == 1 ? "source" : "target"
	   def powerType = session.getAttribute('CURR_POWER_TYPE')?.CURR_POWER_TYPE
	   def totalPower = 0
	   def totalSpace = 0
	   def spaceUsed = 0
	   def powerUsed = 0
	   def moveBundles = MoveBundle.findAllByProject( room.project )
	   List bundleId = request.getParameterValues("moveBundleId")
	   if((bundleId && !bundleId.contains("all")) && params.otherBundle != "on"){
		   def moveBundleId = bundleId.collect{id->Long.parseLong(id)}
		   moveBundles = MoveBundle.findAllByIdInList(moveBundleId)
	   }
	   racks.each{ obj->
		   totalPower += obj.powerA + obj.powerB + obj.powerC
		   totalSpace += obj.rackType == 'Rack' ? (obj.model?.usize ?: 42) : 0
		   def assetsInRack = location == "source" ? AssetEntity.findAllByRackSource(obj, [sort:'sourceRackPosition']) :
		   	 AssetEntity.findAllByRackTarget(obj, [sort:'targetRackPosition'])
		   assetsInRack.findAll{ it.assetType != 'Blade' }.each{ assetEntity ->
			   spaceUsed += assetEntity?.model?.usize ? assetEntity?.model?.usize : 1
			   def powerConnectors = AssetCableMap.findAll("FROM AssetCableMap cap WHERE cap.assetFromPort.type = ? AND cap.assetFrom = ? ",["Power",assetEntity])
			   def powerConnectorsAssigned = powerConnectors.size()
			   def rackPower = assetEntity.model?.powerDesign ? assetEntity.model?.powerDesign : 0
			   if(powerConnectorsAssigned){
				   def powerUseForConnector = rackPower ? rackPower / powerConnectorsAssigned : 0
				   powerConnectors.each{ cables ->
					   powerUsed += powerUseForConnector
				   }
			   }
		   }
	   }
	   if(params.capacityType != "Used") {
		   spaceUsed = totalSpace - spaceUsed
		   powerUsed = totalPower - powerUsed
	   }
	   powerUsed = powerType != "Watts" ?  powerUsed ? (powerUsed / 120).toFloat().round(1) : 0.0 : powerUsed ? Math.round(powerUsed):0
	   totalPower = powerType != "Watts" ?  totalPower ? (totalPower / 120).toFloat().round(1) : 0.0 : totalPower ? Math.round(totalPower):0
	   def powerA = 0
	   def powerB = 0
	   def powerC = 0
	   def powerX = 0
	   def rackPowerA = 0
	   def rackPowerB = 0
	   def rackPowerC = 0
	   def spaceString = ""
	   def powerString = "W"
	   if(powerType != "Watts"){
		powerString = "Amps"
	   }
	   def thisRackUsedSpace = 0
	   def thisRackTotalSpace = 42
	   def rackId = params.rackId
	   def rack
	   def totalPowerInRack = 0;
	   def unassignedPowerInRack = 0;
	   if(rackId){
		   rack = Rack.get(rackId)
		   def assets = AssetEntity.findAllByRackSource( rack )
		   if(rack.source != 1){
				   assets = AssetEntity.findAllByRackTarget( rack )
		   }
		   thisRackTotalSpace = rack.model?.usize ?: 42
		   
		   def assetsInRack = location == "source" ? AssetEntity.findAllByRackSource(rack, [sort:'sourceRackPosition']) : 
		   		AssetEntity.findAllByRackTarget(rack, [sort:'targetRackPosition'])
		   def assetPos = 0
		   def prevUsize = 0
		   assetsInRack.findAll{ it.assetType != 'Blade' }.each{ assetEntity ->
			   // Calculating current assets's position .
			   def posResult = getRackPosDetails(assetEntity, assetPos, prevUsize, location, rack)
			   thisRackUsedSpace += posResult.assetUsize
			   
			   // Assigning rack position to keep track on upcoming asset's position in loop .
			   assetPos = posResult.assetUsize > 0 ? posResult.currAssetPos : assetPos
			   prevUsize = posResult.thisUsize > 0 ? posResult.thisUsize : prevUsize
		   }
		   spaceString = params.capacityType != "Used" ? (thisRackTotalSpace-thisRackUsedSpace)+" remaining of "+thisRackTotalSpace+" RU" : thisRackUsedSpace+" used of "+thisRackTotalSpace+" RU"
		   assets.each{ asset->
			   def assetPowerCabling = AssetCableMap.findAll("FROM AssetCableMap cap WHERE cap.assetFromPort.type = ? AND cap.assetFrom = ? ",["Power",asset])
			   def powerConnectors = assetPowerCabling.size()
			   def powerConnectorsAssigned = assetPowerCabling.findAll{it.toPower != null && it.toPower != '' }.size()
			   
			   def powerDesign = asset.model?.powerDesign ? asset.model?.powerDesign : 0
			   totalPowerInRack = powerDesign + totalPowerInRack
			   assetPowerCabling.each{
				   if(it.toPower==null){
					   unassignedPowerInRack = powerDesign/powerConnectors + unassignedPowerInRack
					}
				}
			   if(powerConnectorsAssigned){
				   def powerUseForConnector = powerDesign ? (powerDesign / powerConnectors) : 0
				   assetPowerCabling.each{ cables ->
					   if(cables.toPower){
						   switch(cables.toPower){
							   case "A": powerA += powerUseForConnector
							   break;
							   case "B": powerB += powerUseForConnector
							   break;
							   case "C": powerC += powerUseForConnector
							   break;
						   }
					   }
				   }
			  } 
			   		powerX = unassignedPowerInRack
			  
		   }
		   powerA = powerType != "Watts" ?  powerA ? (powerA / 120).toFloat().round(1) : 0.0 : powerA ? Math.round(powerA):0
		   powerB = powerType != "Watts" ?  powerB ? (powerB / 120).toFloat().round(1) : 0.0 : powerB ? Math.round(powerB):0
		   powerC = powerType != "Watts" ?  powerC ? (powerC / 120).toFloat().round(1) : 0.0 : powerC ? Math.round(powerC):0
		   powerX = powerType != "Watts" ?  powerX ? (powerX / 120).toFloat().round(1) : 0.0 : powerX ? Math.round(powerX):0
		   
		   rackPowerA = powerType != "Watts" ? rack.powerA ? (rack.powerA / 120).toFloat().round(1) : 0.0 : rack.powerA ? Math.round(rack.powerA) : 0
		   rackPowerB = powerType != "Watts" ? rack.powerB ? (rack.powerB / 120).toFloat().round(1) : 0.0 : rack.powerB ? Math.round(rack.powerB) : 0
		   rackPowerC = powerType != "Watts" ? rack.powerC ? (rack.powerC / 120).toFloat().round(1) : 0.0 : rack.powerC ? Math.round(rack.powerC) : 0
			
	   }
	  
	   
	   def redTBD = false
	   if((powerA +powerB+ powerC+ powerX) > (rackPowerA+ rackPowerB + rackPowerC )){
		   redTBD = true
	   }
	   def op="""<table style='width:300px;padding:0px;border:0px;'>
		   <tr>
		   		<td class='powertable_H'>Room <br />Totals: </td>
		   		<td colspan=2 class='powertable_H' >Space: (RU)<br />$spaceUsed / $totalSpace</td>
		   		<td colspan=2 class='powertable_H' >Power (${powerString}):<br />$powerUsed / $totalPower</td>
		   </tr>
		   <tr><td class='powertable_L'>&nbsp;</td></tr>"""
	   if(rack){
		   op += """
				   <tr>
				   		<td colspan=2 class='powertable_L'><b>Rack : ${rack?.tag ?:""}</b></td>
				   		<td colspan=3 class='powertable_L' nowrap>${spaceString}</td>
				   </tr>
				   <tr>
				   	   <td class='powertable_L'>Power (${powerString})</td>
					   <td style='background:${ powerA > rackPowerA ? 'red':''};' class='powertable_R'>A</td>
					   <td style='background:${ powerB > rackPowerB ? 'red':''};' class='powertable_R'>B</td>
					   <td style='background:${ powerC > rackPowerC ? 'red':''};' class='powertable_R'>C</td>
					   <td style='background:${redTBD ? 'red':''};' class='powertable_R'>TBD</td>
				   </tr>
				   <tr>
					   <td class='powertable_R'>&nbsp;In Rack:</td><td class='powertable_R'>${rackPowerA}</td>
					   <td class='powertable_R'>${rackPowerB}</td><td class='powertable_R'>${rackPowerC}</td>
					   <td class='powertable_R'>&nbsp;</td></tr><tr><td class='powertable_R'>&nbsp;Used:</td>
					   <td class='powertable_R'>${powerA}</td><td class='powertable_R'>${powerB}</td><td class='powertable_R'>${powerC}</td>
					   <td class='powertable_R'>${powerX}</td>
				   </tr></table>"""
	   } else {
	   	op += "</table>"
	   }
		   
		   
		render  op
   }
   /**
    *  Return assets list as html row format to assign racks
    */
   def getAssetsListToAddRack = {
	   def order = params.order ? params.order : 'asc'
	   def project = securityService.getUserCurrentProject()
	   def projectId = project.id
	   def source = params.source
	   def assetEntityList = null
	   def sort = params.sort ?  params.sort : 'assetName'
	   def assign = params.assign
	   def excludeAssetType = ['Blade', 'Application', 'Database', 'Files', 'VM', 'Virtual', 'Virtual Machine']
	   def query = "from AssetEntity where project =:projcet and assetType not in (:excludeAssetType) "
	   if(assign != 'all') {
		   query += " and (${source == '1' ? 'rackSource' : 'rackTarget'}) is null"
	   }
	   order = order == 'asc' ? 'desc' : 'asc'
	   query += " order by ${sort} ${order}"
	   assetEntityList = AssetEntity.findAll(query,[projcet:project, excludeAssetType:excludeAssetType ])
	   
	   def stringToReturn = new StringBuffer()
	   stringToReturn.append("""<thead>
									<tr>
									<th class="sortable ${sort=='assetName' ? 'sorted '+order :''}"><a href="javascript:listDialog('${assign}', 'assetName','${order}','${source}','${params.rack}','${params.roomName}','${params.location}','${params.position}')">Asset Name</a></th>
									<th class="sortable ${sort=='assetTag' ? 'sorted '+order :''}"><a href="javascript:listDialog('${assign}', 'assetTag','${order}','${source}','${params.rack}','${params.roomName}','${params.location}','${params.position}')">Asset Tag</a></th>
									<th class="sortable ${sort=='model' ? 'sorted '+order :''}"><a href="javascript:listDialog('${assign}', 'model','${order}','${source}','${params.rack}','${params.roomName}','${params.location}','${params.position}')">Model</a></th>
									</tr>
								</thead>
								<tbody class="tbody" >
								</tbody>""")
	   if(assetEntityList.size() > 0){
		   assetEntityList.eachWithIndex{ obj, i ->
			   stringToReturn.append("""<tr class="${(i % 2) == 0 ? 'odd' : 'even'}" onclick="editEntity( 'rack','${obj.assetType}',${obj.id},'${source}','${params.rack}','${params.roomName}','${params.location}','${params.position}')">
											<td>${obj.assetName}</td>
											<td>${obj.assetTag}</td>
											<td>${obj.model ? obj.model.modelName : ''}</td>
										</tr>""")
		   }
	   } else {
	   		stringToReturn.append("<tr><td colspan='3' class='no_records'>No records found</td></tr>")
	   }
	   render stringToReturn
   }
   /**
    *  Return blades list as html row format to assign blade chassis
    */
   def getBladeAssetsListToAddRack = {
	   def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
	   def source = params.source
	   def assetEntityList = null
	   if(params.assign == 'assign'){
		   if(source == '1'){
				   assetEntityList = AssetEntity.findAll("from AssetEntity where sourceBladeChassis is null and project = ${projectId} and assetType = 'Blade'")
		   } else {
				   assetEntityList = AssetEntity.findAll("from AssetEntity where targetBladeChassis is null and project = ${projectId} and assetType = 'Blade'")
		   }
	   } else {
		   if(source == '1'){
				   assetEntityList = AssetEntity.findAll("from AssetEntity where project = ${projectId} and assetType = 'Blade'")
		   } else {
				   assetEntityList = AssetEntity.findAll("from AssetEntity where project = ${projectId} and assetType = 'Blade'")
		   }
	   }
	   def stringToReturn = new StringBuffer()
	   def bundleId = AssetEntity.findByAssetTag(params.blade)?.moveBundle?.id
	   if(assetEntityList.size() > 0){
		   assetEntityList.eachWithIndex{ obj, i ->
			   stringToReturn.append("""<tr class="${(i % 2) == 0 ? 'odd' : 'even'}" onclick="editBladeDialog( ${obj.id},'${source}','${params.blade}','${params.position}',${bundleId})">
											<td>${obj.assetName}</td>
											<td>${obj.assetTag}</td>
											<td>${obj.model ? obj.model.modelName : ''}</td>
										</tr>""")
		   }
	   } else {
			   stringToReturn.append("<tr><td colspan='3' class='no_records'>No records found</td></tr>")
	   }
	   render stringToReturn
   }
	/**
	 * Room Capacity Scaling
	 */
	def getCapacityView = {
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
		racks.each{rack->
			def rackUsize = rack.model?.usize ?:42
			def rackPower = rack.powerA + rack.powerB + rack.powerC
			if( rackPower && maxPower < rackPower ){
				maxPower = rackPower
			}
			
			def assetsInRack = location == "source" ? AssetEntity.findAllByRackSource(rack, [sort:'sourceRackPosition']) : 
					AssetEntity.findAllByRackTarget(rack, [sort:'targetRackPosition'])
			def usedRacks = 0
			def powerUsed = 0
			def assetPos = 0 // a flag to determine the previous asset's  rack position .
			def prevUsize = 0 // a flag to determine the previous asset's  usize . 
			assetsInRack.findAll{ it.assetType != 'Blade' }.each{ assetEntity ->

            def posResult = getRackPosDetails(assetEntity, assetPos, prevUsize, location, rack)
			    usedRacks += posResult.assetUsize
				
				def powerConnectors = AssetCableMap.findAll("FROM AssetCableMap cap WHERE cap.toPower is not null AND cap.assetFromPort.type = ? AND cap.assetFrom = ? ",["Power",assetEntity])
				def powerConnectorsAssigned = powerConnectors.size()
				def totalPower = assetEntity.model?.powerDesign ? assetEntity.model?.powerDesign : 0
				if(powerConnectorsAssigned){
					def powerUseForConnector = totalPower ? totalPower / powerConnectorsAssigned : 0
					powerConnectors.each{ cables ->
						powerUsed += powerUseForConnector
					}
				}
				// Assigning rack position to keep track on upcoming asset's position in loop .
				assetPos = posResult.assetUsize > 0 ? posResult.currAssetPos : assetPos
				prevUsize = posResult.thisUsize > 0 ? posResult.thisUsize : prevUsize
			}
			switch(capacityView){
				case "Space":
					if(capacityType != "Used"){
						usedRacks = rackUsize - usedRacks
						if(usedRacks <= Math.round(maxU*0.2)){
							rackData["${rack.id}"] = "rack_cap100"
						}else if(usedRacks <= Math.round(maxU*0.32)){
							rackData["${rack.id}"] = "rack_cap80"
						}else if(usedRacks <= Math.round(maxU*0.44)){
							rackData["${rack.id}"] = "rack_cap68"
						}else if(usedRacks <= Math.round(maxU*0.56)){
							rackData["${rack.id}"] = "rack_cap56"
						}else if(usedRacks <= Math.round(maxU*0.68)){
							rackData["${rack.id}"] = "rack_cap44"
						}else if(usedRacks <=  Math.round(maxU*0.80)){
							rackData["${rack.id}"] = "rack_cap32"
						}else{
						    rackData["${rack.id}"] = "rack_cap20"
						}
						rackCountMap << [("rack_"+rack.id) : usedRacks]
						
					}else{
						if(usedRacks <= Math.round(maxU*0.2)){
							rackData["${rack.id}"] = "rack_cap20"
						}else if(usedRacks <= Math.round(maxU*0.32)){
							rackData["${rack.id}"] = "rack_cap32"
						}else if(usedRacks <= Math.round(maxU*0.44)){
							rackData["${rack.id}"] = "rack_cap44"
						}else if(usedRacks <= Math.round(maxU*0.56)){
							rackData["${rack.id}"] = "rack_cap56"
						}else if(usedRacks <= Math.round(maxU*0.68)){
							rackData["${rack.id}"] = "rack_cap68"
						}else if(usedRacks <= Math.round(maxU*0.80)){
							rackData["${rack.id}"] = "rack_cap80"
						}else{
						    rackData["${rack.id}"] = "rack_cap100"
						}
						rackCountMap << [("rack_"+rack.id) : usedRacks]
					}
					break;
			  case "Power":
				     if(capacityType != "Used"){
						powerUsed = rackPower - powerUsed
						if(powerUsed <= Math.round(rackPower*0.2) ){
							rackData["${rack.id}"] = "rack_cap100"
						}else if (powerUsed  <= Math.round(rackPower*0.32) ){
						    rackData["${rack.id}"] = "rack_cap80"
						}else if (powerUsed <= Math.round(rackPower*0.44) ){
						    rackData["${rack.id}"] = "rack_cap68"
						}else if (powerUsed <= Math.round(rackPower*0.56) ){
						    rackData["${rack.id}"] = "rack_cap56"
						}else if (powerUsed <= Math.round(rackPower*0.68) ){
						    rackData["${rack.id}"] = "rack_cap44"
						}else if (powerUsed <= Math.round(rackPower*0.80) ){
						    rackData["${rack.id}"] = "rack_cap32"
						}else{
						    rackData["${rack.id}"] = "rack_cap20"
					    }
					}else{
						if(powerUsed <= Math.round(rackPower*0.2) ){
							rackData["${rack.id}"] = "rack_cap20"
						}else if (powerUsed <= Math.round(rackPower*0.32) ){
						    rackData["${rack.id}"] = "rack_cap32"
						}else if (powerUsed <= Math.round(rackPower*0.44) ){
						    rackData["${rack.id}"] = "rack_cap44"
						}else if (powerUsed <= Math.round(rackPower*0.56) ){
						    rackData["${rack.id}"] = "rack_cap56"
						}else if (powerUsed <= Math.round(rackPower*0.68) ){
						    rackData["${rack.id}"] = "rack_cap68"
						}else if (powerUsed <= Math.round(rackPower*0.80) ){
						    rackData["${rack.id}"] = "rack_cap80"
						}else{
						    rackData["${rack.id}"] = "rack_cap100"
					    }
					}
					break;
			}
		}
		
		// Implement the Scale display  
		capacityData.rackData = rackData
		capacityData.racks = racks.id
		capacityData.rackCountMap = rackCountMap
		
		def powerType = session.getAttribute('CURR_POWER_TYPE')?.CURR_POWER_TYPE
		powerType = powerType ?: "Watts"
		maxPower = powerType != "Watts" ? Math.round(maxPower / 120) : maxPower
		// Added switch to use if we use other capacityViews 
		switch(capacityView){
			case "Space":
				if(capacityType != "Used"){
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
				break;
			case "Power":
				if(capacityType != "Used"){
					capacityData.view = [
							cap20:"${Math.round(maxPower*0.80)}+ ${powerType}",
							cap32:"${Math.round(maxPower*0.68)}+ ${powerType}",
							cap44:"${Math.round(maxPower*0.56)}+ ${powerType}",
							cap56:"${Math.round(maxPower*0.44)}+ ${powerType}",
							cap68:"${Math.round(maxPower*0.32)}+ ${powerType}",
							cap80:"${Math.round(maxPower*0.20)}+ ${powerType}",
							cap100:"< ${Math.round(maxPower*0.2)} ${powerType}"
						]
				} else {
					capacityData.view = [
							cap20:"< ${Math.round(maxPower*0.2)} ${powerType}",
							cap32:"${Math.round(maxPower*0.20)}+ ${powerType}",
							cap44:"${Math.round(maxPower*0.32)}+ ${powerType}",
							cap56:"${Math.round(maxPower*0.44)}+ ${powerType}",
							cap68:"${Math.round(maxPower*0.56)}+ ${powerType}",
							cap80:"${Math.round(maxPower*0.68)}+ ${powerType}",
							cap100:"${Math.round(maxPower*0.8)}+ ${powerType}"
						]
				}
				break;
		}
		if(capacityData.view){
			render capacityData as JSON
		} else {
			render "None"
		}
	}
	
	def setDraggableRackPref = {
		def prefVal = params.prefVal
		userPreferenceService.setPreference("DraggableRack",prefVal)
		render 'success'
	}
	
	/**
	 * This method is used to fetch all the assets By position , rack and location .
	 * @param rack - rack instance could be either source rack or target rak
	 * @param location - location of rack could be either 'source' and 'target'
	 * @param rackPos - the position of rack where we are requesting this method to fetch the assets
	 * @return - list of assets at requested position
	 */ 
    def getAssetsAtPosByRackAndLoc(rack, location, assetEntity) {
		def assetsAtThisPos = location == "source" ? AssetEntity.findAllBySourceRackPositionAndRackSource(assetEntity.sourceRackPosition, rack) :
			AssetEntity.findAllByTargetRackPositionAndRackTarget(assetEntity.targetRackPosition , rack)
	
	    return assetsAtThisPos
	}
   
   /**
	 * This method is used to get information of assets and there relevant pos., and usize to count rack used count .
	 * @param assetEntity : instance of assetEntity .
	 * @param assetPos : previous asset rack position .
	 * @param prevUsize : previous asset's model usize .
	 * @param location : location of rack could be source or target .
	 * @param rack : instance of rack .
	 * @return : a map contains info like current and previous asset's usize and like current and previous asset's rack position .
	 */
	def getRackPosDetails(assetEntity, assetPos, prevUsize, location, rack){
		def currAssetPos = location == "source" ? assetEntity.sourceRackPosition : assetEntity.targetRackPosition
		def assetUsize = 0  // Initialized  to 0 to take the count of usize 0 if assets are overlapping.
		// if assets are not overLapping then it should go inside condition to calculate asset's max usize.
		def thisUsize
		if( assetPos != currAssetPos &&  currAssetPos > (assetPos+prevUsize-1) ){ // if assets are not overlapping
			// fetching  all assets  of current rack position .
			def assetsAtPos = getAssetsAtPosByRackAndLoc(rack, location, assetEntity)
			// Assigning usize of that assets which has max usize .
			thisUsize = assetsAtPos.model?.usize.sort().reverse()[0]?:1
			assetUsize = thisUsize
		} else if (currAssetPos > assetPos && currAssetPos < (assetPos+prevUsize)){ // If assets are overlapping
				def assetsAtPos = getAssetsAtPosByRackAndLoc(rack, location, assetEntity)
				//Assigning usize of that assets which has max usize .
				thisUsize = assetsAtPos.model?.usize.sort().reverse()[0]?:1
				if((assetPos + prevUsize) < (currAssetPos + thisUsize)){
					assetUsize =  thisUsize - ((assetPos + prevUsize ) - (currAssetPos))
				} else {
					thisUsize = thisUsize > prevUsize ? thisUsize : prevUsize
				}
		}
		return [thisUsize:thisUsize, prevUsize:prevUsize, assetUsize:assetUsize, currAssetPos:currAssetPos]
	}
}