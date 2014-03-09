import grails.converters.JSON


class WsSequenceController {

	def sequenceService
	
	def get = {
		def contextId = params.contextId
		def name = params.name

		int seq = sequenceService.next(contextId.toInteger(), name)

		render(ServiceResults.success('seq' : seq) as JSON)
	}
}
