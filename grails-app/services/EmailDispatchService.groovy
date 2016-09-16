import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
import net.transitionmanager.EmailDispatch
import net.transitionmanager.PasswordReset
import com.tdsops.tm.enums.domain.EmailDispatchOrigin
import org.quartz.SimpleTrigger
import org.quartz.impl.triggers.SimpleTriggerImpl
import org.quartz.Trigger
import grails.converters.JSON

/**
 * Generic service to send emails using templates
 *
 * @author Diego Scarpa
 */
class EmailDispatchService {

	def grailsApplication
	def mailService
	def quartzScheduler

	static SimpleEmailDispatchJob = "EmailDispatchJob"

	/**
	 * Creates a new EmailDispatch entity initializing all attributes
	 */
	EmailDispatch basicEmailDispatchEntity(EmailDispatchOrigin origin, String subject, String bodyTemplate, paramsJson, fromAddress, toAddress, toPerson, createdBy) {

		EmailDispatch ed = new EmailDispatch()

		ed.origin = origin
		ed.subject = subject
		ed.bodyTemplate = bodyTemplate
		ed.paramsJson = paramsJson
		ed.fromAddress = fromAddress
		ed.toAddress = toAddress
		ed.toPerson = toPerson
		ed.createdBy = createdBy

		if (!ed.validate() || !ed.save(flush:true)) {
			log.error "Can't create email dispatch object: " + GormUtil.allErrorsString(ed)
			ed = null
		}

		return ed
	}

	/**
	 * Creates a quarts job to send the email
	 */
	def createEmailJob(emailDispatch, dataMap) {
		def jobName = "TM-EmailDispatchJob-" + UUID.randomUUID().toString()

		// Delay 2 seconds to allow this current transaction to commit before firing off the job
		Trigger trigger = new SimpleTriggerImpl(jobName, null, new Date(System.currentTimeMillis() + 2000) )

		// Add any additional information to the job
		dataMap.each{ k, v ->
			trigger.jobDataMap.put(k, v)
		}

		trigger.jobDataMap.put('edId', emailDispatch.id)
		trigger.setJobName("EmailDispatchJob")
		trigger.setJobGroup('tdstm-send-email')

		quartzScheduler.scheduleJob(trigger)
	}

	/* ********************************************************************** */

	/**
	 * Used to send an email using a template defined in EmailDispatch
	 */
	def sendEmail(dataMap) {
		log.info("Send email: Start.")

		def edId = dataMap.getLongValue('edId')
		def ed = EmailDispatch.get(edId)

		if (!ed) {
			log.error "Invalid EmailDispatch id: $edId"
			return
		}

		log.info "sendEmail: edId=${edId} to=${ed.toPerson.id}/${ed.toPerson.email}"

		mailService.sendMail {
			to ed.toPerson.email
			subject ed.subject
			body (
				view: getTemplateView(ed),
				model: createModel(ed)
			)
		}

		ed.sentDate = TimeUtil.nowGMT()

		if (! ed.validate() || ! ed.save(flush:true)) {
			log.error "sendEmail() Unable to update email dispatch object: " + GormUtil.allErrorsString(ed)
		}
	}

	/**
	 * Creates the model for the given email dispatch
	 */
	private def createModel(ed) {
		def result = [:]
		def emailParams = JSON.parse(ed.paramsJson)

		//TODO: here we should create a model for each. This should be changed to a OOP approach
		switch (ed.bodyTemplate) {
			case "passwordReset":
				result = [
						person: ed.toPerson.firstName,
						resetPasswordUrl: grailsApplication.config.grails.serverURL + "/auth/resetPassword/" + emailParams.token,
						expiredTime: emailParams.expiredTime,
						supportEmail: "support@transitionaldata.com"
					]
				break
			case "passwordResetNotif":
				result = [
						person: ed.toPerson.firstName
					]
				break
			case "accountActivation":
				result = [
					person: ed.toPerson.firstName,
					customMessage: emailParams.customMessage,
					serverURL: grailsApplication.config.grails.serverURL,
					activationURL :  grailsApplication.config.grails.serverURL + "/auth/resetPassword/" + emailParams.token,
					ttl: emailParams.expiredTime,
					sysAdminEmail: emailParams.from,
					username: emailParams.username
				]
				break
			case "adminResetPassword":
				result = [
					person: ed.toPerson.firstName,
					activationURL :  grailsApplication.config.grails.serverURL + "/auth/resetPassword/" + emailParams.token,
					ttl: emailParams.expiredTime,
					username: emailParams.username,
					sysAdminEmail: emailParams.sysAdminEmail
				]
				break
		}
		return result
	}

	/**
	 * Look up for the template for the given email dispatch
	 */
	private def getTemplateView(ed) {
		def result
		//TODO: here we should create a model for each. This should be changed to a OOP approach
		switch (ed.bodyTemplate) {
			case "passwordReset":
				result = "/auth/_resetPasswordEmail"
				break
			case "passwordResetNotif":
				result = "/auth/_resetPasswordNotificationEmail"
				break
			case "accountActivation":
				result = "/auth/_accountActivationNotificationEmail"
				break
			case "adminResetPassword":
				result = "/admin/_ResetPasswordNotificationEmail"
				break
		}
		return result
	}


	/**
	 * Determines the granularity for the expiry date.
	 */
	public def getExpiryGranularity(ed) {
		def result
		switch (ed.bodyTemplate) {
			case "passwordReset":
				result = TimeUtil.GRANULARITY_MINUTES
				break
			case "accountActivation":
				result = TimeUtil.GRANULARITY_HOURS
				break
			case "adminResetPassword":
				result = TimeUtil.GRANULARITY_MINUTES
				break
		}
		return result
	}
}
