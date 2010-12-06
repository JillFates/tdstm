import com.tdssrc.grails.GormUtil;

class ModelController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [modelInstanceList: Model.list(params), modelInstanceTotal: Model.count()]
    }

    def create = {
        def modelInstance = new Model()
        modelInstance.properties = params
		def modelConnector = new ModelConnector()
        return [modelInstance: modelInstance, modelConnector : modelConnector ]
    }

    def save = {
        def modelInstance = new Model(params)
        if (modelInstance.save(flush: true)) {
        	def connectorCount = Integer.parseInt(params.connectorCount)
			if(connectorCount > 0){
	        	for(int i=1; i<=connectorCount; i++){
	        		int exit = params["exist"+i] ? 1 : 0
	        		def modelConnector = new ModelConnector(model : modelInstance,
	        												connector : params["connector"+i],
									        				exist : exit,
															label : params["label"+i],
															type :params["type"+i],
															labelPosition : params["labelPosition"+i],
															connectorPosX : Integer.parseInt(params["connectorPosX"+i]),
															connectorPosY : Integer.parseInt(params["connectorPosY"+i]),
															status:params["status"+i] )
	        		
	        		if (!modelConnector.hasErrors() )
	        			modelConnector.save(flush: true)
	        	}
        	}
            flash.message = "${modelInstance.modelName} created"
            redirect(action: "show", id: modelInstance.id)
        }
        else {
            render(view: "create", model: [modelInstance: modelInstance])
        }
    }

    def show = {
        def modelInstance = Model.get(params.id)
        if (!modelInstance) {
        	flash.message = "Model not found with Id ${params.id}"
            redirect(action: "list")
        }
        else {
        	def modelConnectors = ModelConnector.findAllByModel( modelInstance )
            return [ modelInstance : modelInstance, modelConnectors : modelConnectors ]
        }
    }

    def edit = {
        def modelInstance = Model.get(params.id)
        if (!modelInstance) {
            flash.message = "Model not found with Id ${params.id}"
            redirect(action: "list")
        }
        else {
        	def modelConnectors = ModelConnector.findAllByModel( modelInstance )
			def otherConnectors = []
			for(int i = modelConnectors.size()+1 ; i<51; i++ ){
				otherConnectors << i
			}
            return [ modelInstance: modelInstance, modelConnectors : modelConnectors, otherConnectors : otherConnectors ]
        }
    }

    def update = {
        def modelInstance = Model.get(params.id)
        if (modelInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (modelInstance.version > version) {
                    
                    render(view: "edit", model: [modelInstance: modelInstance])
                    return
                }
            }
            modelInstance.properties = params
            if (!modelInstance.hasErrors() && modelInstance.save(flush: true)) {
            	def connectorCount = Integer.parseInt(params.connectorCount)
				if(connectorCount > 0){
		        	for(int i=1; i<=connectorCount; i++){
		        		int exit = params["exist"+i] ? 1 : 0

		        		def modelConnector = ModelConnector.findByModelAndConnector(modelInstance,params["connector"+i])
						if(modelConnector){
							modelConnector.connector = params["connector"+i]
							modelConnector.exist = exit
							modelConnector.label = params["label"+i]
							modelConnector.type = params["type"+i]
							modelConnector.labelPosition = params["labelPosition"+i]
							modelConnector.connectorPosX = Integer.parseInt(params["connectorPosX"+i])
							modelConnector.connectorPosY = Integer.parseInt(params["connectorPosY"+i])
							modelConnector.status = params["status"+i]
							
						} else {
							modelConnector = new ModelConnector(model : modelInstance,
		        												connector : params["connector"+i],
										        				exist : exit,
																label : params["label"+i],
																type : params["type"+i],
																labelPosition : params["labelPosition"+i],
																connectorPosX : Integer.parseInt(params["connectorPosX"+i]),
																connectorPosY : Integer.parseInt(params["connectorPosY"+i]),
																status : params["status"+i] )
		        		
						}
		        		if (!modelConnector.hasErrors() )
		        			modelConnector.save(flush: true)
		        	}
	        	}
                flash.message = "${modelInstance.modelName} Updated"
                redirect(action: "show", id: modelInstance.id)
            }
            else {
            	def modelConnectors = ModelConnector.findAllByModel( modelInstance )
				def otherConnectors = []
				for(int i = modelConnectors.size()+1 ; i<51; i++ ){
					otherConnectors << i
				}
                render(view: "edit", model: [modelInstance: modelInstance, modelConnectors : modelConnectors, otherConnectors : otherConnectors])
            }
        }
        else {
            flash.message = "Model not found with Id ${params.id}"
            redirect(action: "list")
        }
    }

    def delete = {
        def modelInstance = Model.get(params.id)
        if (modelInstance) {
            try {
            	ModelConnector.executeUpdate("delete ModelConnector where model = ?",[modelInstance])
                modelInstance.delete(flush: true)
                flash.message = "${modelInstance} deleted"
                redirect(action: "list")
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
            	flash.message = "${modelInstance} not deleted"
                redirect(action: "show", id: params.id)
            }
        }
        else {
        	flash.message = "Model not found with Id ${params.id}"
            redirect(action: "list")
        }
    }
}
