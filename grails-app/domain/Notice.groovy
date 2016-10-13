/**
 * Notice
 */

class Notice {
    public enum NoticeType {
        Prelogin(1), Postlogin(2), General(3)

        final int id
        private NoticeType(int id) { this.id = id }

        static NoticeType forId(int id) {
            return NoticeType.values().find { it.id == id }
        }
    }

    // The title of the notice
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

    // The project that the notice is associated with (optional)
    Project project

    // The datetime afterwhich the notice should appear or if null always appears
    Date activationDate

    // The datetime afterwhich the notice should no longer be displayed or if null always appears
    Date expirationDate

    Person createdBy
    Date   dateCreated
    Date   lastModified

    static mapping = {
        version false
        autoTimestamp false
        columns {
            id column: 'notice_id'
            rawText sqlType: 'text'
            htmlText sqlType: 'text'
        }
    }

    static constraints = {
        activationDate nullable:true
        expirationDate nullable:true
        project     nullable:true
        title       minSize:1, maxSize:255
        rawText     minSize:1, maxSize:65535
        htmlText    minSize:1, maxSize:65535
    }

    // Be sure to delete all acknowledgments before deleting a notice
    def beforeDelete() {
        Notice.executeUpdate('delete NoticeAcknowledgment ma where ma.notice = ?', [this])
    }

    def beforeValidate() {
        lastModified = new Date()
        if(!dateCreated){
            dateCreated = lastModified
        }
    }

    static {
        grails.converters.JSON.registerObjectMarshaller(Notice) {
            def json = [:]

            [
                [name: "id",              type:Object],
                [name: "acknowledgeable", type:Object],
                [name: "active",          type:Object],
                [name: "dateCreated",     type:Object],
                [name: "expirationDate",  type:Object],
                [name: "htmlText",        type:Object],
                [name: "lastModified",    type:Object],
                [name: "rawText",         type:Object],
                [name: "title",           type:Object],
                [name: "typeId",          type:NoticeType],
                [name: "createdBy",       type:Person],
                [name: "project",         type:Project]
            ].each { prop ->
                def name = prop.name
                def type = prop.type
                def value = it[name]

                if(value != null){
                    switch(type){
                        case NoticeType:
                            value = value.id
                            break

                        case Person:
                            def createdBy = value
                            value = [
                                id: createdBy.id,
                                fullname: createdBy.toString()
                            ]
                            break

                        case Project:
                            def project = value
                            value = [
                                    id: project.id,
                                    code: project.projectCode,
                                    name: project.name
                            ]
                            break
                    }
                    
                    json[name] = value
                }
            }

            return json
        }
    }


}


