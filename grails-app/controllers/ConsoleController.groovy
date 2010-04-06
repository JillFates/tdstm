import grails.converters.JSON;
class ConsoleController {
	
	def stepSnapshotService
	
    def index = { }
    
    def invokeSnapshot = {
		def moveBundleId = params.moveBundle
		stepSnapshotService.process( moveBundleId )
		render "success"
    }
}
