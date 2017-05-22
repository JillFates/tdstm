package net.transitionmanager.service

import com.tdsops.tm.enums.domain.EmailDispatchOrigin
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
import grails.converters.JSON
import grails.transaction.Transactional
import net.transitionmanager.EmailDispatch
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.quartz.Scheduler
import org.quartz.Trigger
import org.quartz.impl.triggers.SimpleTriggerImpl

/**
 * Generic service to send emails using templates
 *
 * @author Diego Scarpa
 */
class EmailDispatchService implements ServiceMethods {

	GrailsApplication grailsApplication
	def mailService
	Scheduler quartzScheduler

	SecurityService securityService

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
		if (!ed.validate() || !ed.save()) {
			log.error "Can't create email dispatch object: ${GormUtil.allErrorsString(ed)}"
			return null
		}

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

		/* For later use when assuming the identity of this user during the
		 execution of the Quartz Job. */
		if (!trigger.jobDataMap.username) {
			trigger.jobDataMap.username = securityService.currentUsername
			if (! trigger.jobDataMap.username) {
				// TODO TM-6428 - throw an exception here - need to test the that the UI handles the exception
			}
		}

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
			to ed.toPerson.email
			subject ed.subject
			body (
				view: getTemplateView(ed),
				model: createModel(ed)
			)
		}

		ed.sentDate = TimeUtil.nowGMT()

		if (! ed.validate() || ! ed.save(flush:true)) {
			log.error "sendEmail() Unable to update email dispatch object: ${GormUtil.allErrorsString(ed)}"
		}
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
					username: emailParams.username]
			case "adminResetPassword":
				return [
					person: ed.toPerson.firstName,
					activationURL : serverURL + "/auth/resetPassword/" + emailParams.token,
					ttl: emailParams.expiredTime,
					username: emailParams.username,
					sysAdminEmail: emailParams.sysAdminEmail]
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
			case "passwordReset":      return "/auth/_resetPasswordEmail"
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
