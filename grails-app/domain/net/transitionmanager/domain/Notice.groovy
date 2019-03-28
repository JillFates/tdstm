package net.transitionmanager.domain

import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import grails.converters.JSON

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
	Date dateCreated
	Date lastModified

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
	}

	static mapping = {
		version false
		columns {
			htmlText sqlType: 'text'
			id column: 'notice_id'
			rawText sqlType: 'text'
		}
	}

	def beforeInsert = {
		dateCreated = TimeUtil.nowGMT()
	}
	def beforeUpdate = {
		lastModified = TimeUtil.nowGMT()
	}

	private static final Map<String, Class> MARSHALLER_TYPES = [
		needAcknowledgement: Object,
		active:          Object,
		createdBy:       Person,
		dateCreated:     Object,
		expirationDate:  Object,
		htmlText:        Object,
		id:              Object,
		lastModified:    Object,
		project:         Project,
		rawText:         Object,
		title:           Object,
		typeId:          NoticeType
	]

	static void registerObjectMarshaller() {
		JSON.registerObjectMarshaller(Notice) { Notice notice ->
			Map jsonData = [:]

			MARSHALLER_TYPES.each { String name, Class type ->
				def value = notice[name]

				if (value != null) {
					switch (type) {
						case NoticeType:
							value = value.id
							break

						case Person:
							Person createdBy = value
							value = [id: createdBy.id, fullname: createdBy.toString()]
							break

						case Project:
							Project project = value
							value = [id  : project.id, code: project.projectCode, name: project.name]
							break
					}

					if (name == 'htmlText') {
						value = StringUtil.sanitizeJavaScript(value)
					}

					jsonData[name] = value
				}
			}

			return jsonData
		}
	}
}
