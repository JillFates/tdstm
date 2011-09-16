import grails.converters.JSON;

class RoomController {

	def userPreferenceService
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 100, 100)
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
		def powerType = session.getAttribute('CURR_POWER_TYPE')?.CURR_POWER_TYPE
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
	                	rack.powerA = params["powerA_"+rack.id] ? Float.parseFloat(params["powerA_"+rack.id]) : 0
						rack.powerB = params["powerB_"+rack.id] ? Float.parseFloat(params["powerB_"+rack.id]) : 0
						rack.powerC = params["powerC_"+rack.id] ? Float.parseFloat(params["powerC_"+rack.id]) : 0
						if(powerType != "Watts"){
							rack.powerA = rack.powerA * 110
							rack.powerB = rack.powerB * 110
							rack.powerC = rack.powerC * 110
						}
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
		   def assetPowerCabling = AssetCableMap.findAll("FROM AssetCableMap cap WHERE cap.fromConnectorNumber.type = ? AND cap.fromAsset = ? ",["Power",asset])
		   def powerConnectors = assetPowerCabling.size()
		   def powerConnectorsAssigned = assetPowerCabling.findAll{it.toPower != null && it.toPower != '' }.size()
		   
		   def powerUse = asset.model?.powerUse ? asset.model?.powerUse : 0
		   if(powerConnectorsAssigned){
			   def powerUseForConnector = powerUse ? powerUse / powerConnectorsAssigned : 0
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
		   } else {
		   		powerX += powerUse
		   }
	   }
	   def powerType = session.getAttribute('CURR_POWER_TYPE')?.CURR_POWER_TYPE
	   powerA = powerType != "Watts" ?  powerA ? (powerA / 110).toFloat().round(1) : 0.0 : powerA ? Math.round(powerA):0 
	   powerB = powerType != "Watts" ?  powerB ? (powerB / 110).toFloat().round(1) : 0.0 : powerB ? Math.round(powerB):0
	   powerC = powerType != "Watts" ?  powerC ? (powerC / 110).toFloat().round(1) : 0.0 : powerC ? Math.round(powerC):0
	   powerX = powerType != "Watts" ?  powerX ? (powerX / 110).toFloat().round(1) : 0.0 : powerX ? Math.round(powerX):0
	 
	
	  
	   
	    def rackPowerA = powerType != "Watts" ? rack.powerA ? (rack.powerA / 110).toFloat().round(1) : 0.0 : rack.powerA ? Math.round(rack.powerA) : 0
	    def rackPowerB = powerType != "Watts" ? rack.powerB ? (rack.powerB / 110).toFloat().round(1) : 0.0 : rack.powerB ? Math.round(rack.powerB) : 0
	    def rackPowerC = powerType != "Watts" ? rack.powerC ? (rack.powerC / 110).toFloat().round(1) : 0.0 : rack.powerC ? Math.round(rack.powerC) : 0
 	    def redTBD = false
	   if((powerA +powerB+ powerC+ powerX) > (rackPowerA+ rackPowerB + rackPowerC )){
		   redTBD = true
	   }
	   def op="""<table border=0><tr><td colspan=4 class='powertable_L'><b>Rack : ${rack.tag}</b></td></tr>
		   <tr>
		   	   <td class='powertable_L'>Power (${powerType})</td>
			   <td style='background:${ powerA > rackPowerA ? 'red':''};' class='powertable_C'>A</td>
			   <td style='background:${ powerB > rackPowerB ? 'red':''};' class='powertable_C'>B</td>
			   <td style='background:${ powerC > rackPowerC ? 'red':''};' class='powertable_C'>C</td>
			   <td style='background:${redTBD ? 'red':''};' class='powertable_C'>TBD</td>
		   </tr>
		   <tr>
			   <td class='powertable_R'>&nbsp;In Rack:</td><td class='powertable_R'>${rackPowerA}</td>
			   <td class='powertable_R'>${rackPowerB}</td><td class='powertable_R'>${rackPowerC}</td>
			   <td class='powertable_R'>&nbsp;</td></tr><tr><td class='powertable_R'>&nbsp;Used:</td>
			   <td class='powertable_R'>${powerA}</td><td class='powertable_R'>${powerB}</td><td class='powertable_R'>${powerC}</td>
			   <td class='powertable_R'>${powerX}</td>
		   </tr></table>"""
		   
		render  op
   }
   /**
    *  Return assets list as html row format to assign racks
    */
   def getAssetsListToAddRack = {
	   def order = params.order ? params.order : 'asc'
	   def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
	   def source = params.source
	   def assetEntityList = null
	   def sort = params.sort ?  params.sort : 'assetName'
	   def assign = params.assign
	   if(assign != 'all') {
		   if(source == '1'){
			   	assetEntityList = AssetEntity.findAll("from AssetEntity where rackSource is null and project = ${projectId} and assetType != 'Blade' order by ${sort} ${order}")
		   } else {
		   		assetEntityList = AssetEntity.findAll("from AssetEntity where rackTarget is null and project = ${projectId} and assetType != 'Blade' order by ${sort} ${order}")
		   }
	   } else {
	   		assetEntityList = AssetEntity.findAll("from AssetEntity where project = ${projectId} and assetType != 'Blade' order by ${sort} ${order}")
	   }
	   order = order == 'asc' ? 'desc' : 'asc'
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
		def racks = Rack.findAllByRoom(room)
		def capacityView = params.capacityView
		def capacityType = params.capacityType
		def rackData = [:]
		def maxU = 42
		def maxPower = 1
		def location = room.source == 1 ? "source" : "target"
		racks.each{rack->
			def rackPower = rack.powerA + rack.powerB + rack.powerC
			if( maxPower < rackPower ){
				maxPower = rackPower
			}
			
			def assetsInRack = location == "source" ? AssetEntity.findAllByRackSource(rack) : AssetEntity.findAllByRackTarget(rack)
			def usedRacks = 0
			def powerUsed = 0
			assetsInRack.each{ assetEntity ->
				usedRacks += assetEntity?.model?.usize ? assetEntity?.model?.usize : 1
				def powerConnectors = AssetCableMap.findAll("FROM AssetCableMap cap WHERE cap.toPower is not null AND cap.fromConnectorNumber.type = ? AND cap.fromAsset = ? ",["Power",assetEntity])
				def powerConnectorsAssigned = powerConnectors.size()
				def totalPower = assetEntity.model?.powerUse ? assetEntity.model?.powerUse : 0
				if(powerConnectorsAssigned){
					def powerUseForConnector = totalPower ? totalPower / powerConnectorsAssigned : 0
					powerConnectors.each{ cables ->
						powerUsed += powerUseForConnector
					}
				}
			}
			switch(capacityView){
				case "Space":
					if(capacityType != "Used"){
						usedRacks = maxU - usedRacks
						if(usedRacks <= Math.round(maxU*0.2)){
							rackData["${rack.id}"] = "rack_cap80"
						}else if(usedRacks >  Math.round(maxU*0.2)  && usedRacks <= Math.round(maxU*0.32)){
							rackData["${rack.id}"] = "rack_cap68"
						}else if(usedRacks >=  Math.round(maxU*0.32) && usedRacks <= Math.round(maxU*0.44)){
							rackData["${rack.id}"] = "rack_cap56"
						}else if(usedRacks >=  Math.round(maxU*0.44) && usedRacks <= Math.round(maxU*0.56)){
							rackData["${rack.id}"] = "rack_cap44"
						}else if(usedRacks >=  Math.round(maxU*0.56) && usedRacks <= Math.round(maxU*0.68)){
							rackData["${rack.id}"] = "rack_cap32"
						}else if(usedRacks >=  Math.round(maxU*0.68)){
							rackData["${rack.id}"] = "rack_cap20"
						} 
						
					}else{
						if(usedRacks <= Math.round(maxU*0.2)){
							rackData["${rack.id}"] = "rack_cap20"
						}else if(usedRacks >  Math.round(maxU*0.2)  && usedRacks <= Math.round(maxU*0.32)){
							rackData["${rack.id}"] = "rack_cap32"
						}else if(usedRacks >  Math.round(maxU*0.32) && usedRacks <= Math.round(maxU*0.44)){
							rackData["${rack.id}"] = "rack_cap44"
						}else if(usedRacks >  Math.round(maxU*0.44) && usedRacks <= Math.round(maxU*0.56)){
							rackData["${rack.id}"] = "rack_cap56"
						}else if(usedRacks >  Math.round(maxU*0.56) && usedRacks <= Math.round(maxU*0.68)){
							rackData["${rack.id}"] = "rack_cap68"
						}else if(usedRacks >  Math.round(maxU*0.68)){
							rackData["${rack.id}"] = "rack_cap80"
						}
						
					}
					break;
			  case "Power":
				     if(capacityType != "Used"){
						powerUsed = rackPower - powerUsed
						if(powerUsed <= Math.round(rackPower*0.2) ){
							rackData["${rack.id}"] = "rack_cap80"
						}else if (powerUsed > Math.round(rackPower*0.2)  && powerUsed  <= Math.round(rackPower*0.32) ){
						    rackData["${rack.id}"] = "rack_cap68"
						}else if (powerUsed > Math.round(rackPower*0.32) && powerUsed <= Math.round(rackPower*0.44) ){
						    rackData["${rack.id}"] = "rack_cap56"
						}else if (powerUsed > Math.round(rackPower*0.44) && powerUsed <= Math.round(rackPower*0.56) ){
						    rackData["${rack.id}"] = "rack_cap44"
						}else if (powerUsed > Math.round(rackPower*0.56) && powerUsed <= Math.round(rackPower*0.68) ){
						    rackData["${rack.id}"] = "rack_cap32"
					    }else if (powerUsed > Math.round(rackPower*0.68) ){
						    rackData["${rack.id}"] = "rack_cap20"
					    }
					}else{
						if(powerUsed <= Math.round(rackPower*0.2) ){
							rackData["${rack.id}"] = "rack_cap20"
						}else if (powerUsed > Math.round(rackPower*0.2) && powerUsed <= Math.round(rackPower*0.32) ){
						    rackData["${rack.id}"] = "rack_cap32"
						}else if (powerUsed > Math.round(rackPower*0.32) && powerUsed <= Math.round(rackPower*0.44) ){
						    rackData["${rack.id}"] = "rack_cap44"
						}else if (powerUsed > Math.round(rackPower*0.44) && powerUsed <= Math.round(rackPower*0.56) ){
						    rackData["${rack.id}"] = "rack_cap56"
						}else if (powerUsed > Math.round(rackPower*0.56) && powerUsed <= Math.round(rackPower*0.68) ){
						    rackData["${rack.id}"] = "rack_cap68"
					    }else if (powerUsed > Math.round(rackPower*0.68) ){
						    rackData["${rack.id}"] = "rack_cap80"
					    }
					}
					break;
			}
		}
		
		// Implement the Scale display  
		capacityData.rackData = rackData
		capacityData.racks = racks.id
		def powerType = session.getAttribute('CURR_POWER_TYPE')?.CURR_POWER_TYPE
		powerType = powerType ?: "Watts"
		maxPower = powerType != "Watts" ? Math.round(maxPower / 110) : maxPower
		// Added switch to use if we use other capacityViews 
		switch(capacityView){
			case "Space":
				if(capacityType != "Used"){
					capacityData.view = [
							cap20:"${Math.round(maxU*0.8)} RU",
							cap32:"${Math.round(maxU*0.68)} RU",
							cap44:"${Math.round(maxU*0.56)} RU",
							cap56:"${Math.round(maxU*0.44)} RU",
							cap68:"${Math.round(maxU*0.32)} RU",
							cap80:"${Math.round(maxU*0.2)} RU"
						]
				} else {
					capacityData.view = [
							cap20:"${Math.round(maxU*0.2)} RU",
							cap32:"${Math.round(maxU*0.32)} RU",
							cap44:"${Math.round(maxU*0.44)} RU",
							cap56:"${Math.round(maxU*0.56)} RU",
							cap68:"${Math.round(maxU*0.68)} RU",
							cap80:"${Math.round(maxU*0.8)} RU"
						]
				}
				break;
			case "Power":
				if(capacityType != "Used"){
					capacityData.view = [
							cap20:"${Math.round(maxPower*0.8)} ${powerType}",
							cap32:"${Math.round(maxPower*0.68)} ${powerType}",
							cap44:"${Math.round(maxPower*0.56)} ${powerType}",
							cap56:"${Math.round(maxPower*0.44)} ${powerType}",
							cap68:"${Math.round(maxPower*0.32)} ${powerType}",
							cap80:"${Math.round(maxPower*0.2)} ${powerType}"
						]
				} else {
					capacityData.view = [
							cap20:"${Math.round(maxPower*0.2)} ${powerType}",
							cap32:"${Math.round(maxPower*0.32)} ${powerType}",
							cap44:"${Math.round(maxPower*0.44)} ${powerType}",
							cap56:"${Math.round(maxPower*0.56)} ${powerType}",
							cap68:"${Math.round(maxPower*0.68)} ${powerType}",
							cap80:"${Math.round(maxPower*0.8)} ${powerType}"
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
}