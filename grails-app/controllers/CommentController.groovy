/**
 * CRUD for comments
 *
 * @author Diego Scarpa
 */
class CommentController {
    
    /*
     * This list returns comments and tasks
     */
    def list = {
        render( view: "_list", model: [])
    }

    def editComment = {
        render( view: "_editComment", model: [])
    }
    
    def showComment = {
        render( view: "_showComment", model: [])
    }

}