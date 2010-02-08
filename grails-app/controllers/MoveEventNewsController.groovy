import grails.converters.JSON
class MoveEventNewsController {

	//def scaffold = MoveEventNews
	def list = {
			def list = [
					      ["id" : 123, "type": "N", "state": "L", "text": "The truck is now in route to the new data center", "created": "2010-04-27T23:20:50.52Z"],
					      ["id" : 282, "type": "I", "state": "L", "text": "Asset MSSQLSRV02 : Is on Hold", "created": "2010-04-27T22:20:50.52Z"],
					      ["id" : 283, "type": "I", "state": "L", "text": "Asset MSSQLSRV02 : Is on Hold", "created": "2010-04-27T22:20:50.52Z"],
					      ["id" : 284, "type": "I", "state": "L", "text": "Asset MSSQLSRV02 : Is on Hold", "created": "2010-04-27T22:20:50.52Z"],
					      ["id" : 285, "type": "I", "state": "L", "text": "Asset MSSQLSRV02 : Is on Hold", "created": "2010-04-27T22:20:50.52Z"],
					      ["id" : 286, "type": "I", "state": "L", "text": "Asset MSSQLSRV02 : Is on Hold", "created": "2010-04-27T22:20:50.52Z"],
					      ["id" : 287, "type": "I", "state": "L", "text": "Asset MSSQLSRV02 : Is on Hold", "created": "2010-04-27T22:20:50.52Z"],
					      ["id" : 288, "type": "I", "state": "L", "text": "Asset MSSQLSRV02 : Is on Hold", "created": "2010-04-27T22:20:50.52Z"],
					      ["id" : 289, "type": "I", "state": "L", "text": "Asset MSSQLSRV02 : Is on Hold", "created": "2010-04-27T22:20:50.52Z"],
					      ["id" : 280, "type": "I", "state": "L", "text": "Asset MSSQLSRV02 : Is on Hold", "created": "2010-04-27T22:20:50.52Z"],
					      ["id" : 2811, "type": "I", "state": "L", "text": "Asset MSSQLSRV02 : Is on Hold", "created": "2010-04-27T22:20:50.52Z"],
					      ["id" : 2812, "type": "I", "state": "L", "text": "Asset MSSQLSRV02 : Is on Hold", "created": "2010-04-27T22:20:50.52Z"],
					      ["id" : 2813, "type": "I", "state": "L", "text": "Asset MSSQLSRV02 : Is on Hold", "created": "2010-04-27T22:20:50.52Z"],
					      ["id" : 2814, "type": "I", "state": "L", "text": "Asset MSSQLSRV02 : Is on Hold", "created": "2010-04-27T22:20:50.52Z"],
					      ["id" : 823, "type": "N", "state": "A", "text": "Neque porro quisquam est qui dolorem amet, consectetur, adipisci velit...", "created": "2010-04-27T18:20:50.52Z"],
					]
			
			render list as JSON
	}
	
}
