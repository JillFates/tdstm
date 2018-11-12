package net.transitionmanager.service

import com.tdsops.common.exceptions.ServiceException
import com.tdssrc.grails.StringUtil
import grails.transaction.Transactional
import net.transitionmanager.domain.Notice
import net.transitionmanager.domain.NoticeAcknowledgment
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
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

		NoticeAcknowledgment notack = new NoticeAcknowledgment(notice: notice, person: person)
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
}
