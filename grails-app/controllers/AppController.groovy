/**
 * Holder for AngularJs App
 *
 * @author Jorge Morayta
 */
class AppController {

    /*
     * It show the module.html that draw the AngularJs App
     */
    def index() {
        render( view: "index", model: [])
    }

}