import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.GreenMailUtil
import grails.plugins.mail.MailService
import grails.test.spock.IntegrationSpec
import spock.lang.Ignore

/**
 * Created by octavio on 7/26/17.
 * This is a placehoder to Test sending mail using the GreenMail Plugin
 * TODO: oluna this currently gives no value to the app is just an example of how to use the greenmail
 * to test the result of mails being sent, should we remove it?
 * TM-12341 Ignored in the mean time, we will create a new ticket to integrate this with the test flow
 */
@Ignore
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
