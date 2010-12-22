import grails.converters.JSON

class ManufacturerController {
    
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        if(!params.max) params.max = 25
        [ manufacturerInstanceList: Manufacturer.list( params ) ]
    }

    def show = {
        def manufacturerInstance = Manufacturer.get( params.id )

        if(!manufacturerInstance) {
            flash.message = "Manufacturer not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ manufacturerInstance : manufacturerInstance ] }
    }

    def delete = {
        def manufacturerInstance = Manufacturer.get( params.id )
        if(manufacturerInstance) {
            manufacturerInstance.delete(flush:true)
            flash.message = "Manufacturer ${params.id} deleted"
            redirect(action:list)
        }
        else {
            flash.message = "Manufacturer not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def manufacturerInstance = Manufacturer.get( params.id )

        if(!manufacturerInstance) {
            flash.message = "Manufacturer not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ manufacturerInstance : manufacturerInstance ]
        }
    }

    def update = {
        def manufacturerInstance = Manufacturer.get( params.id )
        if(manufacturerInstance) {
            manufacturerInstance.properties = params
            if(!manufacturerInstance.hasErrors() && manufacturerInstance.save()) {
                flash.message = "Manufacturer ${params.id} updated"
                redirect(action:show,id:manufacturerInstance.id)
            }
            else {
                render(view:'edit',model:[manufacturerInstance:manufacturerInstance])
            }
        }
        else {
            flash.message = "Manufacturer not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }

    def create = {
        def manufacturerInstance = new Manufacturer()
        manufacturerInstance.properties = params
        return ['manufacturerInstance':manufacturerInstance]
    }

    def save = {
        def manufacturerInstance = new Manufacturer(params)
        if(!manufacturerInstance.hasErrors() && manufacturerInstance.save()) {
            flash.message = "Manufacturer ${manufacturerInstance.id} created"
            redirect(action:show,id:manufacturerInstance.id)
        }
        else {
            render(view:'create',model:[manufacturerInstance:manufacturerInstance])
        }
    }
    /*
     *  Send List of Manufacturer as JSON object
     */
	def getManufacturersListAsJSON = {
    	def manufacturers = Manufacturer.list()
		render manufacturers as JSON
    }
}
