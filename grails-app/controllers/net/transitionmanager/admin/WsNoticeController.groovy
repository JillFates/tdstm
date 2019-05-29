package net.transitionmanager.admin

import com.tdsops.common.security.SecurityUtil
import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.NoticeCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.exception.EmptyResultException
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
	 * Fetch using Type
	 * We might expand this to add different type of filters
	 */
	@HasPermission(Permission.NoticeView)
	def fetch(Integer typeId) {
		Notice.NoticeType type
		if (typeId) {
			type = Notice.NoticeType.forId(typeId)
		}

		List<Notice> notices = noticeService.fetch(type)
		renderAsJson(notices: notices)
	}

	/**
	 * Get Notice By ID
	 */
	@HasPermission(Permission.NoticeView)
	def fetchById(Long id) {
		Notice notice = noticeService.get(id)
		if (!notice) {
			throw new EmptyResultException()
		}
		renderAsJson notice
	}

	/**
	 * Insert/Update Notice
	 *
	 * Example:
	 *{* 	"title":"titulo",
	 * 		"rawText":"este es el Mensaje",
	 * 		"htmlText":"<strong>este es el Mensaje</strong>",
	 * 		"type":"PRE_LOGIN"
	 */
	@HasPermission(Permission.NoticeCreate)
	def create() {
		NoticeCommand command = populateCommandObject(NoticeCommand.class)
		validateCommandObject(command)

		Notice notice = noticeService.saveOrUpdate(command)

		renderAsJson([model: notice])
	}

	@HasPermission(Permission.NoticeEdit)
	def update(Long id) {
		NoticeCommand command = populateCommandObject(NoticeCommand.class)
		command.id = id
		validateCommandObject(command)

		Notice notice = noticeService.saveOrUpdate(command, id)

		renderAsJson([model: notice])
	}

	/**
	 * Deletes an existing Notice
	 */
	@HasPermission(Permission.NoticeDelete)
	def delete(Long id) {
		boolean result = noticeService.delete(id)
		if (!result) {
			throw new EmptyResultException()
		}
		renderSuccessJson()
	}

	/**
	 * Mark a Note Acknowledged by a User
	 * @param id - the id of the notice that the current user is acknowledging
	 */
	@HasPermission(Permission.UserGeneralAccess)
	def acknowledge(Long id) {
		boolean result = noticeService.acknowledge(id, currentPerson() )
		renderSuccessJson()
	}

	/**
	 * Fetch a list of person post notices that has not been acknowledged
	 */
	@HasPermission(Permission.UserGeneralAccess)
	def fetchPostLoginNotices() {
		List<Notice> notices = noticeService.fetchPostLoginNotices(currentPerson(), getProjectForWs())
		Map result = [
			notices: notices,
			redirectUri: session[SecurityUtil.REDIRECT_URI]
		]

		renderSuccessJson(result)
	}

}
