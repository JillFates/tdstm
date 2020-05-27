package net.transitionmanager.notice

import com.tdssrc.grails.StringUtil
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.person.Person
import net.transitionmanager.project.Project

class Notice {

	static enum NoticeType {
		PRE_LOGIN(1), POST_LOGIN(2), GENERAL(3)

		final int id

		private NoticeType(int id) { this.id = id }

		static NoticeType forId(int id) {
			values().find { it.id == id }
		}
	}

	String title

	// The markup text from the Richtext editor
	String rawText

	// The rendered HTML from the Richtext editor
	String htmlText

	NoticeType typeId

	// Flag if the notice should be shown
	Boolean active = false

	Project project

	// Flag if the notice requires that user acknowledges it (e.g. EULA)
	Boolean needAcknowledgement = false

	// The label to display next to the Acknowledgement checkbox for the needAcknowledgement notices
	String acknowledgeLabel = ''

	// Can be used to order the notifications when there are multiple
	Integer sequence = 0

	// Once set to 1 the notice can never be edited.
	Boolean locked = false

	// The datetime after which the notice should appear or if null always appears
	Date activationDate

	// The datetime after which the notice should no longer be displayed or if null always appears
	Date expirationDate

	Person createdBy
	Date   dateCreated
	Date   lastModified = new Date()

	Collection noticeAcknowledgements

	static hasMany = [noticeAcknowledgements: NoticeAcknowledgement]

	static constraints = {
		activationDate nullable: true
		expirationDate nullable: true
		htmlText minSize: 1, maxSize: 65535
		project nullable: true
		rawText minSize: 1, maxSize: 65535
		needAcknowledgement nullable: false
		acknowledgeLabel blank: true, nullable: false, size: 1..255
		sequence nullable: false
		locked nullable: false
		lastModified nullable: true
	}

	static mapping = {
		version false
		columns {
			htmlText sqlType: 'text'
			id column: 'notice_id'
			rawText sqlType: 'text'
			typeId enumType: 'ordinal'
		}
	}

	def beforeUpdate = {
		// Prevent users from unlocking a record
		if ( this.isDirty('locked') && this.locked == false ) {
			if ( this.getPersistentValue('locked') == true ) {
				throw new InvalidParamException('Previous locked Notices can not be unlocked')
			}
		}
	}



	Map toMap(){

		[
			acknowledgeLabel: acknowledgeLabel,
			activationDate: activationDate,
			active: active,
			createdBy: [
				id: createdBy?.id,
				fullname: createdBy?.toString()
			],
			dateCreated: dateCreated,
			expirationDate: expirationDate,
			htmlText: StringUtil.sanitizeJavaScript(htmlText),
			id: id,
			lastModified: lastModified,
			locked: locked,
			needAcknowledgement: needAcknowledgement,
			project: [
				id  : project?.id,
				code: project?.projectCode,
				name: project?.name
			],
			rawText: rawText,
			sequence: sequence,
			title: title,
			typeId: typeId.toString()
		]
	}
}
