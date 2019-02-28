package net.transitionmanager.domain

import grails.converters.JSON

class Notice {

	static enum NoticeType {
		Prelogin(1), Postlogin(2), General(3)

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

	// Flag if the notice can be acknowledged by the user and hidden
	Boolean acknowledgeable = false

	// Flag if the notice should be shown
	Boolean active = false

	Project project

	// The datetime after which the notice should appear or if null always appears
	Date activationDate

	// The datetime after which the notice should no longer be displayed or if null always appears
	Date expirationDate

	Person createdBy
	Date dateCreated
	Date lastModified

	static constraints = {
		activationDate nullable: true
		expirationDate nullable: true
		htmlText minSize: 1, maxSize: 65535
		project nullable: true
		rawText minSize: 1, maxSize: 65535
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

	// Be sure to delete all acknowledgments before deleting a notice
	def beforeDelete() {
		executeUpdate('delete NoticeAcknowledgment where notice = ?', [this])
	}

	def beforeValidate() {
		lastModified = new Date()
	}

	private static final Map<String, Class> MARSHALLER_TYPES = [
		acknowledgeable: Object,
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

					jsonData[name] = value
				}
			}

			return jsonData
		}
	}
}
