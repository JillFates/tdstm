class ProjectController {
    
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        if(!params.max) params.max = 10
        [ projectInstanceList: Project.list( params ) ]
    }

    def show = {
        def projectInstance = Project.get( params.id )

        if(!projectInstance) {
            flash.message = "Project not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ projectInstance : projectInstance ] }
    }

    def delete = {
        def projectInstance = Project.get( params.id )
        if(projectInstance) {
            projectInstance.delete()
            flash.message = "Project ${params.id} deleted"
            redirect(action:list)
        }
        else {
            flash.message = "Project not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def projectInstance = Project.get( params.id )

        if(!projectInstance) {
            flash.message = "Project not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ projectInstance : projectInstance ]
        }
    }

    def update = {
        def projectInstance = Project.get( params.id )
        if(projectInstance) {
            projectInstance.properties = params
            if(!projectInstance.hasErrors() && projectInstance.save()) {
                flash.message = "Project ${params.id} updated"
                redirect(action:show,id:projectInstance.id)
            }
            else {
                render(view:'edit',model:[projectInstance:projectInstance])
            }
        }
        else {
            flash.message = "Project not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }

    def create = {
        def projectInstance = new Project()
        projectInstance.properties = params
        return ['projectInstance':projectInstance]
    }

    def save = {
        def projectInstance = new Project(params)
        if(!projectInstance.hasErrors() && projectInstance.save()) {
            flash.message = "Project ${projectInstance.id} created"
            redirect(action:show,id:projectInstance.id)
        }
        else {
            render(view:'create',model:[projectInstance:projectInstance])
        }
    }
}
