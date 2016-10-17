/**
 * Created by octavio on 10/10/16.
 */
class NoticeAcknowledgment {
	Notice notice
	Person person
	Date   dateCreated

	def beforeValidate() {
		if(!dateCreated){
			dateCreated = new Date()
		}
	}
}
