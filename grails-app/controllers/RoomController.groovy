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
		[roomInstanceList: roomInstanceList, roomInstanceTotal: roomInstanceList.size(), 
		 projectId:projectId, roomId:roomId, viewType:params.viewType]
    }

    def create = {
        def roomInstance = new Room()
        roomInstance.properties = params
        return [roomInstance: roomInstance]
    }

    def save = {
        def roomInstance = new Room(params)
        if (roomInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'room.label', default: 'Room'), roomInstance.id])}"
            redirect(action: "show", id: roomInstance.id)
        }
        else {
            render(view: "create", model: [roomInstance: roomInstance])
        }
    }

    def show = {
        def roomInstance = Room.get(params.id)
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		userPreferenceService.setPreference( "CURR_ROOM", "${roomInstance.id}" )
		def project = Project.findById( projectId )
		def roomInstanceList = Room.findAllByProject( project, [sort:"roomName",order:'asc'])
		def moveBundleList = MoveBundle.findAllByProject( project )
        if (!roomInstance) {
            flash.message = "Current Room not found"
            redirect(action: "list")
        }
        else {
            [roomInstance: roomInstance, roomInstanceList:roomInstanceList, moveBundleList:moveBundleList, 
			 moveBundleId:params.moveBundleId, source:params.source, target:params.target]
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
        def roomInstance = Room.get(params.id)
        if (roomInstance) {
            try {
                roomInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'room.label', default: 'Room'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'room.label', default: 'Room'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'room.label', default: 'Room'), params.id])}"
            redirect(action: "list")
        }
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
}
