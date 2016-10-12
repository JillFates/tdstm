import grails.converters.JSON

/**
 * Created by octavio on 10/10/16.
 * @author oluna
 */
class WsNoticeController{
	NoticeService noticeService

	public fetch(){
		try {
			def json = request.JSON
			def type = Notice.NoticeType.valueOf(json.type)
			def list = noticeService.fetch(type)
			render( [notices: list] as JSON )
		}catch(Exception e){
			response.status = 500
			render( [errors:[e.getMessage()]] as JSON )
		}
	}

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
	 * Example
	 {
	 "title":"titulo",
	 "rawText":"este es el Mensaje",
	 "htmlText":"<strong>este es el Mensaje</strong>",
	 "type":"Prelogin"
	 }
	 * @return
	 */
	public save(){
		try {
			def json = request.JSON
			def result = noticeService.save(json)

			if (!result.status) {
				response.status = 400
			}
			render(result.data as JSON)
		}catch(Exception e){
			response.status = 500
			render([errors:[e.getMessage()]] as JSON)
		}
	}

	public delete(){
		try {
			def json = request.JSON
			def result = noticeService.delete(json.id)

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
