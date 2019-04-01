package net.transitionmanager.service

import com.tdsops.tm.enums.domain.EmailDispatchOrigin
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import net.transitionmanager.common.EmailDispatch
import org.quartz.Scheduler
import org.quartz.Trigger
import org.quartz.impl.triggers.SimpleTriggerImpl
/**
 * Generic service to send emails using templates
 *
 * @author Diego Scarpa
 */
class EmailDispatchService implements ServiceMethods {

	def mailService
	Scheduler quartzScheduler

	static SimpleEmailDispatchJob = "EmailDispatchJob"

	/**
	 * Creates a new EmailDispatch entity initializing all attributes
	 */
	@Transactional
	EmailDispatch basicEmailDispatchEntity(EmailDispatchOrigin origin, String subject, String bodyTemplate,
	                                       paramsJson, fromAddress, toAddress, toPerson, createdBy) {

		EmailDispatch ed = new EmailDispatch()
		ed.origin = origin
		ed.subject = subject
		ed.bodyTemplate = bodyTemplate
		ed.paramsJson = paramsJson
		ed.fromAddress = fromAddress
		ed.toAddress = toAddress
		ed.toPerson = toPerson
		ed.createdBy = createdBy
		ed.save()

		return ed
	}

	/**
	 * Creates a quartz job to send the email
	 */
	def createEmailJob(emailDispatch, dataMap) {
		def jobName = "TM-EmailDispatchJob-" + UUID.randomUUID()

		// Delay 2 seconds to allow this current transaction to commit before firing off the job
		Trigger trigger = new SimpleTriggerImpl(jobName, null, new Date(System.currentTimeMillis() + 2000) )

		// Add any additional information to the job
		// TODO - does this need to be a closure or can we just merge the dataMap onto jobDataMap?
		dataMap.each { k, v -> trigger.jobDataMap[k] = v }

		trigger.jobDataMap.edId = emailDispatch.id

		String username = emailDispatch.createdBy.userLogin?.username
		if (!username) {
			username = emailDispatch.toPerson.userLogin?.username
		}
		/* For later use when assuming the identity of this user during the
		 execution of the Quartz Job. */
		trigger.jobDataMap.username = username

		trigger.setJobName("EmailDispatchJob")
		trigger.setJobGroup('tdstm-send-email')
		quartzScheduler.scheduleJob(trigger)
	}

	/* ********************************************************************** */

	/**
	 * Used to send an email using a template defined in EmailDispatch
	 */
	@Transactional
	def sendEmail(dataMap) {
		log.info("Send email: Start.")

		def edId = dataMap.getLongValue('edId')
		def ed = EmailDispatch.get(edId)

		if (!ed) {
			log.error "Invalid EmailDispatch id: $edId"
			return
		}

		log.info "sendEmail: edId=$edId to=$ed.toPerson.id/$ed.toPerson.email"

		mailService.sendMail {
			from getFrom(ed.fromAddress)
			to ed.toPerson.email
			subject ed.subject
			body (
				view: getTemplateView(ed),
				model: createModel(ed)
			)
		}

		ed.sentDate = TimeUtil.nowGMT()

		ed.save(flush:true)
	}

	/**
	 * Return desired from email address if present or default system from email address
	 * @param from
	 * @return
	 */
	private String getFrom(String from) {
		String defaultFrom = grailsApplication.config.grails.mail.default.from
		if (!StringUtil.isBlank(from)) {
			defaultFrom = from
		}
		return defaultFrom.replaceAll("&lt;", "<").replaceAll("&gt;", ">")
	}

	/**
	 * Creates the model for the given email dispatch
	 */
	private Map createModel(ed) {
		Map emailParams = JSON.parse(ed.paramsJson)

		//TODO: here we should create a model for each. This should be changed to a OOP approach
		switch (ed.bodyTemplate) {
			case "passwordReset":
				return [
						person: ed.toPerson.firstName,
						resetPasswordUrl: serverURL + "/auth/resetPassword/" + emailParams.token,
						expiredTime: emailParams.expiredTime,
						supportEmail: "support@transitionaldata.com"]
			case "passwordResetNotif":
				return [person: ed.toPerson.firstName]
			case "accountActivation":
				return [
					person: ed.toPerson.firstName,
					customMessage: emailParams.customMessage,
					serverURL: serverURL,
					activationURL : serverURL + "/auth/resetPassword/" + emailParams.token,
					ttl: emailParams.expiredTime,
					sysAdminEmail: emailParams.from,
					username: ed.toPerson.userLogin?.username]
			case "adminResetPassword":
				return [
                    person: ed.toPerson.firstName,
                    resetPasswordUrl: serverURL + "/auth/resetPassword/" + emailParams.token,
                    expiredTime: emailParams.expiredTime,
                    supportEmail: "support@transitionaldata.com",
                    username: ed.toPerson.userLogin?.username]
		}
	}

	private String getServerURL() {
		grailsApplication.config.grails.serverURL
	}

	/**
	 * Look up for the template for the given email dispatch
	 */
	private String getTemplateView(ed) {
		//TODO: here we should create a model for each. This should be changed to a OOP approach
		switch (ed.bodyTemplate) {
			case "passwordReset":      return "/auth/_forgotPasswordEmail"
			case "passwordResetNotif": return "/auth/_resetPasswordNotificationEmail"
			case "accountActivation":  return "/auth/_accountActivationNotificationEmail"
			case "adminResetPassword": return "/admin/_ResetPasswordNotificationEmail"
		}
	}

	/**
	 * Determines the granularity for the expiry date.
	 */
	String getExpiryGranularity(EmailDispatch ed) {
		switch (ed.bodyTemplate) {
			case "passwordReset":      return TimeUtil.GRANULARITY_MINUTES
			case "accountActivation":  return TimeUtil.GRANULARITY_HOURS
			case "adminResetPassword": return TimeUtil.GRANULARITY_MINUTES
		}
	}
}
