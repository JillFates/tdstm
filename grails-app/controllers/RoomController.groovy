import grails.converters.JSON;

class RoomController {

	def userPreferenceService
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 20, 100)
		def projectId = params.projectId
    	if(projectId == null || projectId == ""){
        	projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
        }
		def project = Project.findById( projectId )
		def roomInstanceList = Room.findAllByProject( project, params )
		def roomId = getSession().getAttribute( "CURR_ROOM" )?.CURR_ROOM
		def roomInstance = new Room()
		[roomInstanceList: roomInstanceList, roomInstanceTotal: roomInstanceList.size(), 
		 projectId:projectId, roomId:roomId, viewType:params.viewType, roomInstance:roomInstance]
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
        def roomInstance = Room.get(params.id)
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		userPreferenceService.setPreference( "CURR_ROOM", "${roomInstance?.id}" )
		def project = Project.findById( projectId )
		def roomInstanceList = Room.findAllByProject( project, [sort:"roomName",order:'asc'])
		def moveBundleList = MoveBundle.findAllByProject( project )
        if (!roomInstance) {
			userPreferenceService.removePreference("CURR_ROOM")
            flash.message = "Current Room not found"
            redirect(action: "list", params:[viewType : "list"])
        }
        else {
            [roomInstance: roomInstance, roomInstanceList:roomInstanceList, moveBundleList:moveBundleList, 
			 moveBundleId:params.moveBundleId, source:params.source, target:params.target, projectId : projectId]
        }
    }

    def edit = {
        def roomInstance = Room.get(params.id)
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		def project = Project.findById( projectId )
		def rackInstanceList = Rack.findAllByRoom(roomInstance)
		def moveBundleList = MoveBundle.findAllByProject( project )
		def newRacks = []
		for(int i = 50000 ; i<50051; i++ ){
			newRacks << i
		}
        if (!roomInstance) {
        	flash.message = "Current Room not found"
            redirect(action: "list")
        }
        else {
            [roomInstance: roomInstance, rackInstanceList:rackInstanceList, moveBundleList:moveBundleList,
			 moveBundleId:params.moveBundleId, source:params.source, target:params.target, newRacks : newRacks]
        }
    }

    def update = {
        def roomInstance = Room.get(params.id)
		List rackIds = request.getParameterValues("rackId")

        if (roomInstance) {
    		roomInstance.roomName = params.roomName
			roomInstance.location = params.location
			roomInstance.roomWidth = params.roomWidth ? Integer.parseInt(params.roomWidth) : null
			roomInstance.roomDepth = params.roomDepth ? Integer.parseInt(params.roomDepth) : null
            if (!roomInstance.hasErrors() && roomInstance.save(flush: true)) {
				
                def racks = Rack.findAllByRoom( roomInstance )
				racks.each{rack->
                	if(rackIds?.contains(rack.id.toString())){
						rack.tag = params["tag_"+rack.id]
	                	rack.roomX = params["roomX_"+rack.id] ? Integer.parseInt(params["roomX_"+rack.id]) : 0
	                	rack.roomY = params["roomY_"+rack.id] ? Integer.parseInt(params["roomY_"+rack.id]) : 0
	                	rack.powerA = params["powerA_"+rack.id] ? Integer.parseInt(params["powerA_"+rack.id]) : 0
	                	rack.powerB = params["powerB_"+rack.id] ? Integer.parseInt(params["powerB_"+rack.id]) : 0
	                	rack.powerC = params["powerC_"+rack.id] ? Integer.parseInt(params["powerC_"+rack.id]) : 0
						rack.rackType = params["rackType_"+rack.id]
						rack.front = params["front_"+rack.id]
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
							def newRack = Rack.findOrCreateWhere(source:1, 'project.id':roomInstance.project.id, location:roomInstance.location, 'room.id':roomInstance?.id, tag:params["tag_"+id])
							if(newRack){
								newRack.roomX = params["roomX_"+id] ? Integer.parseInt(params["roomX_"+id]) : 0
								newRack.roomY = params["roomY_"+id] ? Integer.parseInt(params["roomY_"+id]) : 0
								newRack.powerA = params["powerA_"+id] ? Integer.parseInt(params["powerA_"+id]) : 0
								newRack.powerB = params["powerB_"+id] ? Integer.parseInt(params["powerB_"+id]) : 0
								newRack.powerC = params["powerC_"+id] ? Integer.parseInt(params["powerC_"+id]) : 0
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
				assetEntity.save(flush:true)
	        }
			rack.targetAssets.each{assetEntity ->
				assetEntity.targetRack = rack.tag
				assetEntity.targetRoom = roomInstance.roomName
				assetEntity.save(flush:true)
	        }
    	}
    	redirect(action: "show", id: params.id)
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
	   def rackId = params.rackId
	   def rack = Rack.get(rackId)
	   def powerA = 0
	   def powerB = 0
	   def powerC = 0
	   def powerX = 0
	   def assets = AssetEntity.findAllByRackSource( rack )
	   if(rack.source != 1){
	   		assets = AssetEntity.findAllByRackTarget( rack )
	   }
	   assets.each{ asset->
		   def assetPowerCabling = AssetCableMap.findAll("FROM AssetCableMap cap WHERE cap.fromConnectorNumber.type = ? AND cap.fromAsset = ? and (cap.toPower is not null OR toPower <> '')",["Power",asset])
		   def powerConnectors = assetPowerCabling.size()
		   def powerUse = asset.model?.powerUse ? asset.model?.powerUse : 0
		   if(powerConnectors){
			   def powerUseForConnector = powerUse ? powerUse / powerConnectors : 0
			   assetPowerCabling.each{ cables ->
				   if(cables.toPower){
					   switch(cables.toPower){
						   case "A": powerA += powerUseForConnector
						   break;
						   case "B": powerB += powerUseForConnector
						   break;
						   case "C": powerC += powerUseForConnector
						   break;
						   powerX += powerUseForConnector
					   }
				   }
			   }
		   } else {
		   		powerX += powerUse
		   }
	   }
	   render "<table border=0><tr><td colspan=4 class='powertable_L'><b>Rack : ${rack.tag}</b></td></tr><tr><td class='powertable_L'>Power (w)</td><td class='powertable_C'>A</td><td class='powertable_C'>B</td><td class='powertable_C'>C</td><td class='powertable_C'>TBD</td></tr><tr><td class='powertable_R'>&nbsp;In Rack:</td><td class='powertable_R'>${rack.powerA}</td><td class='powertable_R'>${rack.powerB}</td><td class='powertable_R'>${rack.powerC}</td><td class='powertable_R'>&nbsp;</td></tr><tr><td class='powertable_R'>&nbsp;Used:</td><td class='powertable_R'>${powerA}</td><td class='powertable_R'>${powerB}</td><td class='powertable_R'>${powerC}</td><td class='powertable_R'>${powerX}</td></tr></table>"
   }
   /**
    *  Return assets list as html row format to assign racks
    */
   def getAssetsListToAddRack = {
	   def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
	   def source = params.source
	   def assetEntityList = null
	   if(source == '1'){
		   	assetEntityList = AssetEntity.findAll("from AssetEntity where rackSource is null and project = ${projectId} and assetType != 'Blade'")
	   } else {
	   		assetEntityList = AssetEntity.findAll("from AssetEntity where rackTarget is null and project = ${projectId} and assetType != 'Blade'")
	   }
	   def stringToReturn = new StringBuffer()
	   if(assetEntityList.size() > 0){
		   assetEntityList.eachWithIndex{ obj, i ->
			   stringToReturn.append("""<tr class="${(i % 2) == 0 ? 'odd' : 'even'}" onclick="editDialog( ${obj.id},'${source}','${params.rack}','${params.roomName}','${params.location}','${params.position}')">
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
	   if(source == '1'){
			   assetEntityList = AssetEntity.findAll("from AssetEntity where sourceBladeChassis is null and project = ${projectId} and assetType = 'Blade'")
	   } else {
			   assetEntityList = AssetEntity.findAll("from AssetEntity where targetBladeChassis is null and project = ${projectId} and assetType = 'Blade'")
	   }
	   def stringToReturn = new StringBuffer()
	   if(assetEntityList.size() > 0){
		   assetEntityList.eachWithIndex{ obj, i ->
			   stringToReturn.append("""<tr class="${(i % 2) == 0 ? 'odd' : 'even'}" onclick="editBladeDialog( ${obj.id},'${source}','${params.blade}','${params.position}')">
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
}
