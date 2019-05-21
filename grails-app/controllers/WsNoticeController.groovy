import com.tdsops.common.security.SecurityUtil
import grails.plugin.springsecurity.annotation.Secured
import com.tdsops.common.security.spring.HasPermission
import net.transitionmanager.command.NoticeCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.Notice
import net.transitionmanager.domain.Person
import net.transitionmanager.security.Permission
import net.transitionmanager.service.NoticeService
import net.transitionmanager.service.EmptyResultException

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
	 * { 	"title":"titulo",
	 * 		"rawText":"este es el Mensaje",
	 * 		"htmlText":"<strong>este es el Mensaje</strong>",
	 * 		"type":"PRE_LOGIN"
	 * }
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

	/**
	 * Return whether or not the current user has unaccepted mandatory notices. If this is not the case,
	 * the following session variables are cleared:
	 * - SecurityUtil.HAS_UNACKNOWLEDGED_NOTICES
	 * - SecurityUtil.REDIRECT_URI
	 */
	@HasPermission(Permission.UserGeneralAccess)
	def hasMandatoryUnacknowledgedNotices() {
		Person currentPerson = securityService.loadCurrentPerson()
		boolean hasMandatoryUnacknowledgedNotices = noticeService.hasUnacknowledgedNotices(currentPerson, true)
		if (!hasMandatoryUnacknowledgedNotices) {
			session.removeAttribute(SecurityUtil.HAS_UNACKNOWLEDGED_NOTICES)
			session.removeAttribute(SecurityUtil.REDIRECT_URI)
		}
		renderSuccessJson([unacknowledgedNotices: hasMandatoryUnacknowledgedNotices])
	}
}
