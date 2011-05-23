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
        println"params--->"+params
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
	    println"params--->"+params
		def project = Project.findById( projectId )
		def roomInstanceList = Room.findAllByProject( project, [sort:"roomName",order:'asc'])
		def moveBundleList = MoveBundle.findAllByProject( project )
        if (!roomInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'room.label', default: 'Room'), params.id])}"
            redirect(action: "list")
        }
        else {
            [roomInstance: roomInstance, roomInstanceList:roomInstanceList, moveBundleList:moveBundleList]
        }
    }

    def edit = {
        def roomInstance = Room.get(params.id)
        if (!roomInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'room.label', default: 'Room'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [roomInstance: roomInstance]
        }
    }

    def update = {
        def roomInstance = Room.get(params.id)
        if (roomInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (roomInstance.version > version) {
                    
                    roomInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'room.label', default: 'Room')] as Object[], "Another user has updated this Room while you were editing")
                    render(view: "edit", model: [roomInstance: roomInstance])
                    return
                }
            }
            roomInstance.properties = params
            if (!roomInstance.hasErrors() && roomInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'room.label', default: 'Room'), roomInstance.id])}"
                redirect(action: "show", id: roomInstance.id)
            }
            else {
                render(view: "edit", model: [roomInstance: roomInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'room.label', default: 'Room'), params.id])}"
            redirect(action: "list")
        }
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
