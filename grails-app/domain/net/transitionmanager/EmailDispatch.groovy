package net.transitionmanager

import com.tdssrc.grails.TimeUtil
import com.tdsops.tm.enums.domain.EmailDispatchOrigin

class EmailDispatch {

	EmailDispatchOrigin origin // What part of the system originated the email
	String subject // The subject to use for the email
	String bodyTemplate // The name of the template to use for rendering the email body
	String paramsJson // A set of properties that are used to populate the bodyTemplate
	String fromAddress // The email address that the message is sent on behalf of
	String toAddress // The email address that the message it to be delivered to
	Person toPerson // The Person, if applicable that the email was being sent to
	Person createdBy // The individual that originated the ForgotMyPassword, null if by USER
	Date sentDate // Datetime that the email was actual sent
	Date createdDate
	Date lastModified

	static constraints = {
		subject nullable:false
		bodyTemplate nullable:false
		paramsJson nullable:true
		fromAddress( blank: false, nullable:false )
		toAddress( blank: false, nullable:false )
		toPerson nullable:false
		createdBy nullable:true
		sentDate nullable:true
		createdDate nullable:true
		lastModified nullable:true
	}

	static mapping  = {
		version false
		autoTimestamp false
		id column:'email_dispatch_id'
		sentDate sqltype: 'DateTime'
		createdDate sqltype: 'DateTime'
		lastModified sqltype: 'DateTime'
	}

	def beforeInsert = {
		createdDate = TimeUtil.nowGMT()
		lastModified = TimeUtil.nowGMT()
	}

	def beforeUpate = {
		lastModified = TimeUtil.nowGMT()
	}

}
