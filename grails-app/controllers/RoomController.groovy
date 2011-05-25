class RoomController {

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
        [roomInstanceList: roomInstanceList, roomInstanceTotal: roomInstanceList.size(), projectId:projectId]
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
		def project = Project.findById( projectId )
		def roomInstanceList = Room.findAllByProject( project, [sort:"roomName",order:'asc'])
		def moveBundleList = MoveBundle.findAllByProject( project )
        if (!roomInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'room.label', default: 'Room'), params.id])}"
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
		def roomInstanceList = Room.findAllByProject( project, [sort:"roomName",order:'asc'])
		def moveBundleList = MoveBundle.findAllByProject( project )
        if (!roomInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'room.label', default: 'Room'), params.id])}"
            redirect(action: "list")
        }
        else {
            [roomInstance: roomInstance, roomInstanceList:roomInstanceList, moveBundleList:moveBundleList, 
			 moveBundleId:params.moveBundleId, source:params.source, target:params.target]
        }
    }

    def update = {
        def roomInstance = Room.get(params.id)
        if (roomInstance) {
    		roomInstance.roomName = params.roomName
			roomInstance.location = params.location
			roomInstance.roomWidth = params.roomWidth ? Integer.parseInt(params.roomWidth) : null
			roomInstance.roomDepth = params.roomDepth ? Integer.parseInt(params.roomDepth) : null
            if (!roomInstance.hasErrors() && roomInstance.save(flush: true)) {
                def racks = Rack.findAllByRoom( roomInstance )
				racks.each{rack->
                	rack.tag = params["tag_"+rack.id]
                	rack.roomX = params["roomX_"+rack.id] ? Integer.parseInt(params["roomX_"+rack.id]) : 0
                	rack.roomY = params["roomY_"+rack.id] ? Integer.parseInt(params["roomY_"+rack.id]) : 0
                	rack.powerA = params["powerA_"+rack.id] ? Integer.parseInt(params["powerA_"+rack.id]) : 0
                	rack.powerB = params["powerB_"+rack.id] ? Integer.parseInt(params["powerB_"+rack.id]) : 0
                	rack.powerC = params["powerC_"+rack.id] ? Integer.parseInt(params["powerC_"+rack.id]) : 0
                	rack.save(flush:true)
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
}
