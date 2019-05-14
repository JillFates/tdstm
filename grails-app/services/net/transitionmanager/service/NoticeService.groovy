package net.transitionmanager.service

import com.tdssrc.grails.TimeUtil
import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import net.transitionmanager.command.NoticeCommand
import net.transitionmanager.domain.Notice
import net.transitionmanager.domain.Notice.NoticeType
import net.transitionmanager.domain.NoticeAcknowledgement
import net.transitionmanager.domain.Person
import org.hibernate.criterion.Restrictions
import org.hibernate.sql.JoinType
import org.springframework.context.MessageSource


/**
 * @author octavio
 */
@Slf4j
class NoticeService implements ServiceMethods {

	MessageSource messageSource
	PersonService personService

	/**
	 * Used to record when a user has acknowledged a notice
	 * @param id - the ID of the notice to be acknowledged
	 * @param person - the person whom is acknowledging the notice
	 * @return void
	 */
	@Transactional
	void acknowledge(Long id, Person person) {
		log.info 'User: {}, is acknowledging notice: {}', username, id 
		Notice notice = doGet(Notice, id, true)
		NoticeAcknowledgement acknowledgement = new NoticeAcknowledgement(notice: notice, person: person)
		acknowledgement.save(failOnError: true)
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
	List<Notice> fetch(Notice.NoticeType type = null) {
		Notice.where {
			if (type) {
				type == type
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
	 * Check for any unacknowledged notices
	 * @param person
	 * @return
	 */
	Boolean hasUnacknowledgedNotices(Person person) {
		Date now = TimeUtil.nowGMT()
		log.info('Check if person: [{}] has unacknowledged notices.', person.id)

		def result = Notice.withCriteria {
			createAlias('noticeAcknowledgements', 'ack', JoinType.LEFT_OUTER_JOIN, Restrictions.eq("ack.person", person))
			eq('active', true)
			eq('project', securityService.userCurrentProject)
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
			isNull('ack.person')

			projections {
				count()
			}
		}

		if (!result) {
			return false
		}

		return result[0] > 0
	}

	/**
	 * Return a list of person post login notices that has not been acknowledged
	 * @param person - person requesting list notices
	 * @return
	 */
	List<Notice> fetchPersonPostLoginNotices(Person person) {
		Date now = TimeUtil.nowGMT()
		log.info('List person: [{}] post login notices.', person.id)

		def result = Notice.withCriteria {
			createAlias('noticeAcknowledgements', 'ack', JoinType.LEFT_OUTER_JOIN, Restrictions.eq('ack.person', person))
			eq('active', true)
			eq('typeId', NoticeType.POST_LOGIN)
			eq('project', securityService.userCurrentProject)
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
			isNull('ack.person')

			order('needAcknowledgement', 'desc')
			order('sequence', 'desc')
		}

		return result
	}
}
