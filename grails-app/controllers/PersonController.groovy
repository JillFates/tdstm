class PersonController {
    
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        if(!params.max) params.max = 10
        [ personInstanceList: Person.list( params ) ]
    }

    def show = {
        def personInstance = Person.get( params.id )

        if(!personInstance) {
            flash.message = "Person not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ personInstance : personInstance ] }
    }

    def delete = {
        def personInstance = Person.get( params.id )
        if(personInstance) {
            personInstance.delete()
            flash.message = "Person ${params.id} deleted"
            redirect(action:list)
        }
        else {
            flash.message = "Person not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def personInstance = Person.get( params.id )

        if(!personInstance) {
            flash.message = "Person not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ personInstance : personInstance ]
        }
    }

    def update = {
        def personInstance = Person.get( params.id )
        if(personInstance) {
            personInstance.properties = params
            if(!personInstance.hasErrors() && personInstance.save()) {
                flash.message = "Person ${params.id} updated"
                redirect(action:show,id:personInstance.id)
            }
            else {
                render(view:'edit',model:[personInstance:personInstance])
            }
        }
        else {
            flash.message = "Person not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }

    def create = {
        def personInstance = new Person()
        personInstance.properties = params
        return ['personInstance':personInstance]
    }

    def save = {
        def personInstance = new Person(params)
        if(!personInstance.hasErrors() && personInstance.save()) {
            flash.message = "Person ${personInstance.id} created"
            redirect(action:show,id:personInstance.id)
        }
        else {
            render(view:'create',model:[personInstance:personInstance])
        }
    }
}
