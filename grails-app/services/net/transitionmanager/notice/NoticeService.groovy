package net.transitionmanager.notice

import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import grails.gorm.transactions.Transactional
import net.transitionmanager.exception.ServiceException
import net.transitionmanager.notice.Notice
import net.transitionmanager.notice.Notice.NoticeType
import net.transitionmanager.notice.NoticeAcknowledgement
import net.transitionmanager.person.Person
import net.transitionmanager.person.PersonService
import net.transitionmanager.project.Project
import net.transitionmanager.service.ServiceMethods
import org.hibernate.criterion.Restrictions
import org.hibernate.sql.JoinType
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

import javax.xml.bind.DatatypeConverter
/**
 * @author octavio
 */
class NoticeService implements ServiceMethods {

	MessageSource messageSource
	PersonService personService

	/**
	 * Set Acknowledment of a Note for a User
	 * TODO: (oluna) What Happen if the user doesn't have a Person associated should we change this to UserLogin Type?
	 */
	@Transactional
	boolean ack(Long id, String username) {
		log.info("ID: $id, username: $username")
		Notice notice = Notice.get(id)
		if (!notice) {
			return false
		}

		Person person = personService.findByUsername(username)
		log.info("Person: $person")
		if (!person) {
			return false
		}

		NoticeAcknowledgement notack = new NoticeAcknowledgement(notice: notice, person: person)
		notack.save()

		return !notack.hasErrors()
	}

	@Transactional
	boolean delete(Long id) {
		1 == Notice.where { id == id }.deleteAll()
	}

	List<Notice> fetch(Notice.NoticeType type = null) {
		type ? Notice.findAllByTypeId(type) : Notice.list()
	}

	Notice get(Long id) {
		Notice.get(id)
	}

	@Transactional
	Map<String, ?> update(Long id, json) {
		if (!id) {
			return [status: false, errors: ["id not provided"]]
		}

		Notice notice = get(id)
		if (notice) {
			return saveUpdate(json, notice)
		}

		return [status: false, errors: ["Resource not found with id [$id]"]]
	}

	@Transactional
	Map<String, ?> create(json) {
		saveUpdate(json, new Notice(createdBy: securityService.userLoginPerson))
	}

		private static final Map<String, Class> TYPES = [
		acknowledgeable: String,
		activationDate:  Date,
		active:          Boolean,
		expirationDate:  Date,
		htmlText:        String,
		projectId:       Project,
		rawText:         String,
		title:           String,
		typeId:          Notice.NoticeType
	]

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

	// HELPER METHODS /////////////////////////////////////////
	private Map<String, ?> saveUpdate(json, Notice notice) {
		TYPES.each { String propertyName, Class type ->
			def val = json[propertyName]

			if (val != null) {

				// Sanitize the 3rd Party HTML
				if (propertyName == 'htmlText') {
					List<String> results = StringUtil.checkHTML(val as String)
					if (!results.isEmpty()) {
						throw new ServiceException("HTML passed is unsafe to render as it contains forbidden code in: [${results.join(',')}]")
					}
				}

				switch (type) {
					case Date:
						val = DatatypeConverter.parseDateTime(val).time
						break

					case Notice.NoticeType:
						val = Notice.NoticeType.forId(val)
						break

					case Project:
						val = Project.get(val)
						break

				}

				notice[propertyName] = val
			}
		}

		def status = notice.save() // TODO this returns the Notice instance or null, not boolean
		Map<String, ?> result = [status: status, data: [:]]

		if (status) {
			result.data.notice = notice
		}
		else {
			Locale locale = LocaleContextHolder.locale
			result.data.errors = notice.errors.allErrors.collect { messageSource.getMessage(it, locale) }
		}

		return result
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
