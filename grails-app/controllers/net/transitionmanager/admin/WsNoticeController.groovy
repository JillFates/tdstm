package net.transitionmanager.admin

import com.tdsops.common.security.SecurityUtil
import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.notice.Notice
import net.transitionmanager.notice.NoticeService
import net.transitionmanager.security.Permission

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
	@HasPermission(Permission.NoticeView)
	def fetch(Integer typeId) {
		try {
			Notice.NoticeType type

			if (typeId) {
				type = Notice.NoticeType.forId(typeId)
			}

			List<Notice> notices = noticeService.fetch(type)
			renderAsJson(notices: notices)
		}
		catch (e) {
			renderError500 e
		}
	}

	/**
	 * Get Notice By ID
	 */
	@HasPermission(Permission.NoticeView)
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
	 * 		"type":"PRE_LOGIN"
	 */
	@HasPermission(Permission.NoticeCreate)
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

	@HasPermission(Permission.NoticeEdit)
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
	@HasPermission(Permission.NoticeDelete)
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
	@HasPermission(Permission.UserGeneralAccess)
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

	/**
	 * Fetch a list of person post notices that has not been acknowledged
	 */
	@HasPermission(Permission.NoticeView)
	def fetchPostLoginNotices() {
		def result = [
				notices: noticeService.fetchPersonPostLoginNotices(currentPerson()),
				redirectUri: session[SecurityUtil.REDIRECT_URI]
		]

		renderSuccessJson(result)
	}

	private void renderError500(Exception e) {
		response.status = 500
		renderAsJson(errors: [e.message])
	}
}
