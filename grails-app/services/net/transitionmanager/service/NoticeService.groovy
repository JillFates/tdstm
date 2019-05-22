package net.transitionmanager.service

import com.tdsops.common.security.SecurityUtil
import com.tdssrc.grails.TimeUtil
import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import net.transitionmanager.command.NoticeCommand
import net.transitionmanager.domain.Notice
import net.transitionmanager.domain.Notice.NoticeType
import net.transitionmanager.domain.NoticeAcknowledgement
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import org.hibernate.criterion.Restrictions
import org.hibernate.sql.JoinType

/**
 * @author octavio
 */
@Slf4j
class NoticeService implements ServiceMethods {

	/**
	 * Used to record when a user has acknowledged a notice
	 * @param id - the ID of the notice to be acknowledged
	 * @param person - the person whom is acknowledging the notice
	 * @return void
	 */
	@Transactional
	void acknowledge(Long id, Person person) {
		log.info 'Person: {}, is acknowledging notice: {}', person, id
		Notice notice = Notice.read(id)
		if (!notice) {
			throw new EmptyResultException()
		}
		NoticeAcknowledgement acknowledgement = new NoticeAcknowledgement(notice: notice, person: person)
		acknowledgement.save(failOnError: true)

		if (!hasUnacknowledgedNotices(person)) {
			session.removeAttribute(SecurityUtil.HAS_UNACKNOWLEDGED_NOTICES)
			session.removeAttribute(SecurityUtil.REDIRECT_URI)
		}
	}

	/**
	 * Delete a notice by id
	 * @param id - the notice id
	 * @return - true when notice is deleted
	 */
	@Transactional
	boolean delete(Long id) {
		1 == Notice.where { id == id }.deleteAll()
	}

	/**
	 * Fetch a list of notices by type or all
	 * @param type - notice type filter
	 * @return - a list of notices filtered by type or all if filter is null
	 */
	List<Notice> fetch(Notice.NoticeType noticeType = null) {
		Notice.where {
			if (noticeType) {
				typeId == noticeType
			}
		}.list()
	}

	/**
	 * Get a notice by id
	 * @param id - notice id
	 * @return
	 */
	Notice get(Long id) {
		Notice.where { id == id }.find()
	}

	/**
	 * Create or update a notice
	 * @param noticeCommand - the notice command
	 * @param id - if updating an existing notice, it is the notice id
	 * @return - new created or updated notice
	 */
	@Transactional
	Notice saveOrUpdate(NoticeCommand noticeCommand, Long id = null) {
		Notice notice
		if (id) {
			notice = get(id)
		} else {
			notice = new Notice(createdBy: securityService.userLoginPerson)
		}
		noticeCommand.populateDomain(notice, false)
		notice.save(failOnError: true)
		return notice
	}


	/**
	 * Check for any unacknowledged notices for a user using their current project context
	 * @param person
	 * @param requiredNoticesOnly - defaulted to false, this flag signals if only required notices are needed.
	 * @return true if there are any unacknowledged notices
	 */
	Boolean hasUnacknowledgedNotices(Person person, boolean requiredNoticesOnly = false) {
		Project project = securityService.userCurrentProject
		return  hasUnacknowledgedNotices(person, project, requiredNoticesOnly)
	}

	/**
	 * Determine whether or not the person logging in has unacknowledged notices.
	 *
	 * @param person - the person logging in into the application.
	 * @return whether or not the person has unacknowledge notices.
	 */
	Boolean hasUnacknowledgedNoticesForLogin(Person person) {
		Project project = securityService.userCurrentProject
		return hasUnacknowledgedNotices(person, project, false, true)
	}


	/**
	 * Check for any unacknowledged notices
	 * @param person
	 * @param project - the project currently selected in the user's context
	 * @param requiredNoticesOnly - defaulted to false, this flag signals if only required notices are needed.
	 * @param forLogin - if true the presence of the HAS_UNACKNOWLEDGED_NOTICES attribute in the session will be
	 *  bypassed. This is to take into account that, during the login process, this attribute hasn't been set, yet.
	 * @return
	 */
	Boolean hasUnacknowledgedNotices(Person person, Project project, boolean requiredNoticesOnly = false, boolean forLogin = false) {
		// If the HAS_UNACKNOWLEDGED_NOTICES variable is not set in the session, return false.
		if (!forLogin && session.getAttribute(SecurityUtil.HAS_UNACKNOWLEDGED_NOTICES) == null) {
			return false
		}

		Closure criteriaClosure = {
			unacknowledgedNoticesCriteriaClosure.delegate = delegate
        	unacknowledgedNoticesCriteriaClosure(person, project, new Date())

			if (requiredNoticesOnly) {
				eq('needAcknowledgement', true)
			}

			projections { count () }
		}

		List result = Notice.createCriteria().list(criteriaClosure)
		return result ? (result[0] > 0) : false
	}

	/**
	 * Return a list of person post login notices that has not been acknowledged that are either
	 * global or based on the user's selected project
	 * @param person - person requesting list notices
	 * @param project - the project in the user's contect
	 * @return
	 */
	List<Notice> fetchPostLoginNotices(Person person, Project project) {

		// If the HAS_UNACKNOWLEDGED_NOTICES variable is not set in the session, return an empty list.
		if (session.getAttribute(SecurityUtil.HAS_UNACKNOWLEDGED_NOTICES) == null) {
			return []
		}

		Closure criteriaClosure = {
			unacknowledgedNoticesCriteriaClosure.delegate = delegate
        	unacknowledgedNoticesCriteriaClosure(person, project, new Date())

			order('needAcknowledgement', 'desc')
			order('sequence', 'desc')
			order('dateCreated', 'asc')
		}

		List<Notice> list = Notice.createCriteria().list(criteriaClosure)

		return list
	}

	/**
	 * Returns a criteria that has the query to find active Notices that have not been
	 * acknowledged by a particular person and optional project as of this moment
	 * @param person - person requesting list notices
	 * @param project - the project in the user's contect
	 * @param now - the current datetime
	 * @return a closure for the query
	 */
	Closure unacknowledgedNoticesCriteriaClosure = { Person person, Project project, Date now ->
		createAlias('noticeAcknowledgements', 'ack', JoinType.LEFT_OUTER_JOIN, Restrictions.eq('ack.person', person))
		eq('active', true)
		eq('typeId', NoticeType.POST_LOGIN)
		isNull('ack.person')
		or {
			isNull('project')
			eq('project', project)
		}
		and {
			or {
				isNull('activationDate')
				lt('activationDate', now)
			}
			or {
				isNull('expirationDate')
				gt('expirationDate', now)
			}
		}
	}
}
