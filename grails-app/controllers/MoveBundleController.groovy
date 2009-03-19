class MoveBundleController {
    
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        
        def currProj = getSession().getAttribute( "CURR_PROJ" )
        def projectId = currProj.CURR_PROJ
        def projectInstance = Project.findById( projectId )
    	def moveBundleInstanceList = MoveBundle.findAllByProject( projectInstance )
        if(!params.max) params.max = 10        
        [ moveBundleInstanceList: moveBundleInstanceList, projectId: projectId ]
    }

    def show = {
        def moveBundleInstance = MoveBundle.get( params.id )

        if(!moveBundleInstance) {
            flash.message = "MoveBundle not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ moveBundleInstance : moveBundleInstance ] }
    }

    def delete = {
        def moveBundleInstance = MoveBundle.get( params.id )
        if(moveBundleInstance) {
            moveBundleInstance.delete()
            flash.message = "MoveBundle ${params.id} deleted"
            redirect(action:list)
        }
        else {
            flash.message = "MoveBundle not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def moveBundleEdit = {

        def currProj = getSession().getAttribute( "CURR_PROJ" )
        def projectId = currProj.CURR_PROJ
        def projectInstance = Project.findById( projectId )
        def bundlesRelatedToproject = MoveBundle.findAll("from MoveBundle m where m.project ="+projectInstance.id)
        
        def moveBundleInstance = MoveBundle.get( params.id )
        def moveBundleAssetInstanceEditList = MoveBundleAsset.findAll('from MoveBundleAsset m where m.moveBundle.id = '+moveBundleInstance.id )
        
        if(!moveBundleInstance) {
            flash.message = "MoveBundle not found with id ${params.id}"
            render(view:'moveBundleEdit' , model:[moveBundleInstance:moveBundleInstance, moveBundleAssetInstanceEditList:moveBundleAssetInstanceEditList, projectInstance:projectInstance, bundlesRelatedToproject:bundlesRelatedToproject ])
        }
        else {
            return [ moveBundleInstance : moveBundleInstance,  moveBundleAssetInstanceEditList:moveBundleAssetInstanceEditList, projectInstance:projectInstance, bundlesRelatedToproject:bundlesRelatedToproject ]
        }
    }

    def update = {
        def moveBundleInstance = MoveBundle.get( params.id )
        if(moveBundleInstance) {
            moveBundleInstance.properties = params
            if(!moveBundleInstance.hasErrors() && moveBundleInstance.save()) {
                flash.message = "MoveBundle ${params.id} updated"
                redirect(action:show,id:moveBundleInstance.id)
            }
            else {
                render(view:'edit',model:[moveBundleInstance:moveBundleInstance])
            }
        }
        else {
            flash.message = "MoveBundle not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }

    def create = {
        def moveBundleInstance = new MoveBundle()
        moveBundleInstance.properties = params
        return ['moveBundleInstance':moveBundleInstance]
    }

    def save = {
        
        def currProj = getSession().getAttribute( "CURR_PROJ" )
        def projectId = currProj.CURR_PROJ
        def projectInstance = Project.findById( projectId )
        def moveBundleInstance = new MoveBundle( params )
        moveBundleInstance.project = projectInstance
        if(!moveBundleInstance.hasErrors() && moveBundleInstance.save()) {
            flash.message = "MoveBundle ${moveBundleInstance.id} created"
            redirect(action:show,id:moveBundleInstance.id)
        }
        else {
            render(view:'create',model:[moveBundleInstance:moveBundleInstance])
        }
    }

    def addMoveBundleAsset = {
        
        def moveBundleId = params.moveBundleId
        moveBundleId = moveBundleId.split(",")
        def moveBundleInstance = MoveBundle.findById( moveBundleId[0] )
        def moveBundleAssetInstance = MoveBundleAsset.get( moveBundleId[1] )
        moveBundleAssetInstance.moveBundle = moveBundleInstance
        moveBundleAssetInstance.save()        

    }
    
    def addMoveBundleSourceAsset = {
        
        def moveBundleId = params.moveBundleId
        moveBundleId = moveBundleId.split(",")
        def projectTeamInstance = ProjectTeam.findById( moveBundleId[0] )
        def moveBundleAssetInstance = MoveBundleAsset.get( moveBundleId[1] )
        moveBundleAssetInstance.sourceTeam = projectTeamInstance
        moveBundleAssetInstance.save()        

    }

    def addMoveBundleTargetAsset = {

        def moveBundleId = params.moveBundleId
        moveBundleId = moveBundleId.split(",")
        def projectTeamInstance = ProjectTeam.findById( moveBundleId[0] )
        def moveBundleAssetInstance = MoveBundleAsset.get( moveBundleId[1] )
        moveBundleAssetInstance.targetTeam = projectTeamInstance
        moveBundleAssetInstance.save()

    }


}
