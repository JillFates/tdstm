import org.jsecurity.crypto.hash.Sha1Hash;
class UserLoginController {
    
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        if(!params.max) params.max = 10
        [ userLoginInstanceList: UserLogin.list( params ) ]
    }

    def show = {
        def userLoginInstance = UserLogin.get( params.id )
        def projects = ProjectPartyRole.findAllWhere(party:userLoginInstance.person.party).project
        if(!userLoginInstance) {
            flash.message = "UserLogin not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ userLoginInstance : userLoginInstance ] }
    }

    def delete = {
        def userLoginInstance = UserLogin.get( params.id )
        if(userLoginInstance) {
            userLoginInstance.delete()
            flash.message = "UserLogin ${params.id} deleted"
            redirect(action:list)
        }
        else {
            flash.message = "UserLogin not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def userLoginInstance = UserLogin.get( params.id )

        if(!userLoginInstance) {
            flash.message = "UserLogin not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ userLoginInstance : userLoginInstance ]
        }
    }

    def update = {
        def userLoginInstance = UserLogin.get( params.id )
        if(userLoginInstance) {
            userLoginInstance.properties = params
            //convert password onto Hash code
            	userLoginInstance.password = new Sha1Hash(params['password']).toHex()
            if(!userLoginInstance.hasErrors() && userLoginInstance.save()) {
                flash.message = "UserLogin ${params.id} updated"
                redirect(action:show,id:userLoginInstance.id)
            }
            else {
                render(view:'edit',model:[userLoginInstance:userLoginInstance])
            }
        }
        else {
            flash.message = "UserLogin not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }

    def create = {
        def userLoginInstance = new UserLogin()
        userLoginInstance.properties = params
        return ['userLoginInstance':userLoginInstance]
    }

    def save = {
        def userLoginInstance = new UserLogin(params)
        //convert password onto Hash code
        userLoginInstance.password = new Sha1Hash(params['password']).toHex()
        if(!userLoginInstance.hasErrors() && userLoginInstance.save()) {
            flash.message = "UserLogin ${userLoginInstance.id} created"
            redirect(action:show,id:userLoginInstance.id)
        }
        else {
            render(view:'create',model:[userLoginInstance:userLoginInstance])
        }
    }
}
