/**
 * Notice
 */

class Notice {
    public enum NoticeType {
        Prelogin, Postlogin, General
    }

    // The title of the notice
    String title

    // The markup text from the Richtext editor
    String rawText

    // The rendered HTML from the Richtext editor
    String htmlText

    NoticeType type

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

}


