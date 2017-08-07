import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.GreenMailUtil
import grails.plugin.mail.MailService
import grails.test.spock.IntegrationSpec

/**
 * Created by octavio on 7/26/17.
 */
class TestGreenMailSpec extends IntegrationSpec {
	GreenMail greenMail
	MailService mailService

	def "create registration and send email"() {
		when: "a mail is sent"
		mailService.sendMail {
			to "octavio.luna@gmail.com"
			from "sender@test.com"
			subject "Hello World"
			body "Test Body..."
		}

		then: "the email should be sent successfully"
		assert 1 == greenMail.getReceivedMessages().length

		def message = greenMail.getReceivedMessages()[0]
		assert "sender@test.com" == GreenMailUtil.getAddressList(message.from)
		assert "Hello World" == message.subject
	}
}
