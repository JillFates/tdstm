import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.Notice
import net.transitionmanager.service.NoticeService

/**
 * @author oluna
 */
@Secured('isAuthenticated()')
class WsNoticeController implements ControllerMethods {

	NoticeService noticeService

	/**
	 * Fetch using pType
	 * We might expand this to add different type of filters
	 */
	def fetch(Integer typeId) {
		try {
			Notice.NoticeType type
			if (typeId) {
				type = Notice.NoticeType.forId(typeId)
			}

			renderAsJson(notices: noticeService.fetch(type))
		}
		catch (e) {
			renderError500 e
		}
	}

	/**
	 * Get Notice By ID
	 */
	def fetchById(Long id) {
		try {
			Notice notice = noticeService.get(id)
			if (!notice) {
				response.status = 404
			}
			renderAsJson notice
		}
		catch (e) {
			renderError500 e
		}
	}

	/**
	 * Insert/Update Notice
	 *
	 * Example:
	 *{* 		"title":"titulo",
	 * 		"rawText":"este es el Mensaje",
	 * 		"htmlText":"<strong>este es el Mensaje</strong>",
	 * 		"type":"Prelogin"
	 */
	def create() {
		try {
			Map<String, ?> result = noticeService.create(request.JSON)
			if (!result.status) {
				response.status = 400
			}
			renderAsJson result.data
		}
		catch (e) {
			renderError500 e
		}
	}

	def update() {
		try {
			Map<String, ?> result = noticeService.update(params.long('id'), request.JSON)
			if (!result.status) {
				response.status = 404
			}

			renderAsJson result.data
		}
		catch (e) {
			renderError500 e
		}
	}

	/**
	 * Deletes an existing Notice
	 */
	def delete(Long id) {
		try {
			boolean result = noticeService.delete(id)
			if (!result) {
				response.status = 404
			}
			renderAsJson(notices: [])
		}
		catch (e) {
			renderError500 e
		}
	}

	/**
	 * Mark a Note Acknowledge by a User
	 * TODO: (oluna)Still need to review the case of don't having a Person for the UserLogin (@see NoticeService::ack)
	 */
	def ack(Long id, String username) {
		try {
			boolean result = noticeService.ack(id, username)
			if (!result) {
				response.status = 404
			}
			render("")
		}
		catch (e) {
			renderError500 e
		}
	}

	private void renderError500(Exception e) {
		response.status = 500
		renderAsJson(errors: [e.message])
	}
}
