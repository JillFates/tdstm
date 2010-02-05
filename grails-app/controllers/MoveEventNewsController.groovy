import grails.converters.JSON
class MoveEventNewsController {

	//def scaffold = MoveEventNews
	def list = {
			def list = [
					      ["id" : 123, "type": "N", "state": "L", "text": "The truck is now in route to the new data center", "created": "2010-04-27T23:20:50.52Z"],
					      ["id" : 281, "type": "I", "state": "L", "text": "Asset MSSQLSRV02 : Is on Hold", "created": "2010-04-27T22:20:50.52Z"],
					      ["id" : 123, "type": "N", "state": "A", "text": "Neque porro quisquam est qui dolorem amet, consectetur, adipisci velit...", "created": "2010-04-27T18:20:50.52Z"],
					] 
			render list as JSON
	}
	
}
