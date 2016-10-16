import grails.converters.JSON

/**
 * Created by octavio on 10/10/16.
 * @author oluna
 */
class WsNoticeController{
	NoticeService noticeService

	/**
	 * Fetch using pType
	 * We might expand this to add different tipe of filters
	 * @return
	 */
	public fetch(){
		try {
			Notice.NoticeType type = null
			def typeId = params.typeId
			
			if(typeId){
				typeId =  Integer.parseInt(typeId)
				type = Notice.NoticeType.forId(typeId)
			}

			def list = noticeService.fetch(type)
			render( [notices: list] as JSON )
		}catch(Exception e){
			response.status = 500
			render( [errors:[e.getMessage()]] as JSON )
		}
	}

	/**
	 * Get Notice By ID
	 * @return
	 */
	public get(){
		try {
			def notice = noticeService.get(params.id)

			if (!notice) {
				response.status = 404
			}
			render( notice as JSON )
		}catch(Exception e){
			response.status = 500
			render( [errors:[e.getMessage()]] as JSON )
		}
	}

	/**
	 * Insert/Update Notice
	 *
	 * Example:
	 * {
	 * 		"title":"titulo",
	 * 		"rawText":"este es el Mensaje",
	 * 		"htmlText":"<strong>este es el Mensaje</strong>",
	 * 		"type":"Prelogin"
	 * }
	 * @return
	 */
	public create(){
		try {
			def json = request.JSON
			def result = noticeService.create(json)

			if (!result.status) {
				response.status = 400
			}
			render(result.data as JSON)
		}catch(Exception e){
			response.status = 500
			render([errors:[e.getMessage()]] as JSON)
		}
	}

	public update(){
		try {
			def json = request.JSON
			def id = params.id
			def result = noticeService.update(id, json)

			if (!result.status) {
				response.status = 404
			}

			render(result.data as JSON)
		}catch(Exception e){
			response.status = 500
			render( [errors:[e.getMessage()]] as JSON )
		}
	}

	/**
	 * Deletes an existing Notice
	 * @return
	 */
	public delete(){
		try {
			def result = noticeService.delete(params.id)

			if (!result) {
				response.status = 404
			}
			render( [notices:[]] as JSON)
		}catch(Exception e){
			response.status = 500
			render([errors:[e.getMessage()]] as JSON)
		}
	}
}
