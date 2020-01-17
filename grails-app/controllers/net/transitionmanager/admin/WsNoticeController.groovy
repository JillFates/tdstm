package net.transitionmanager.admin

import com.tdsops.common.security.SecurityUtil
import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.NoticeCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.exception.EmptyResultException
import net.transitionmanager.notice.Notice
import net.transitionmanager.notice.NoticeService
import net.transitionmanager.person.Person
import net.transitionmanager.project.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.security.UserLogin

import javax.servlet.http.HttpSession

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

		Notice notice = noticeService.saveOrUpdate(command)

		renderAsJson([model: notice])
	}

	@HasPermission(Permission.NoticeEdit)
	def update(Long id) {
		NoticeCommand command = populateCommandObject(NoticeCommand.class)
		command.id = id

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
		Project project = getProjectForWs()
		boolean result = noticeService.acknowledge(project, request.getSession(), id, currentPerson() )
		renderSuccessJson()
	}

	/**
	 * Fetch a list of person post notices that has not been acknowledged
	 */
	@HasPermission(Permission.UserGeneralAccess)
	def fetchPostLoginNotices() {
		Project project = getProjectForWs()
		if (session.getAttribute(SecurityUtil.HAS_UNACKNOWLEDGED_NOTICES) == null) {
			UserLogin userLogin = securityService.userLogin
			boolean hasUnacknowledgedNotices = noticeService.hasUnacknowledgedNoticesForLogin(project, session, userLogin.person)
			if (hasUnacknowledgedNotices) {
				request.getSession().setAttribute(SecurityUtil.HAS_UNACKNOWLEDGED_NOTICES, true)
			}
		}
		List<Notice> notices = noticeService.fetchPostLoginNotices(currentPerson(), project, session)
		Map result = [
			notices: notices,
			redirectUri: session[SecurityUtil.REDIRECT_URI]
		]

		renderSuccessJson(result)
	}

	/**
	 * Return whether or not the current user has unaccepted mandatory notices. If this is not the case,
	 * the following session variables are cleared:
	 * - SecurityUtil.HAS_UNACKNOWLEDGED_NOTICES
	 * - SecurityUtil.REDIRECT_URI
	 */
	@HasPermission(Permission.UserGeneralAccess)
	def clearNoticesWhenNoMandatoryLeft() {
		Person currentPerson = securityService.loadCurrentPerson()
		Project project = getProjectForWs()
		HttpSession session = request.getSession()
		boolean hasMandatoryUnacknowledgedNotices = noticeService.clearNoticesWhenNoMandatoryLeft(project, session, currentPerson)
		renderSuccessJson([unacknowledgedNotices: hasMandatoryUnacknowledgedNotices])
	}

}
