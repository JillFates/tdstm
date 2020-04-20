package net.transitionmanager.common

import net.transitionmanager.exception.InvalidParamException
import spock.lang.Specification
import grails.testing.services.ServiceUnitTest

class EmailDispatchServiceSpec extends Specification implements ServiceUnitTest<EmailDispatchService>{

    void '01. Test getTemplateView method with valid template names'() {

        setup:'given an email dispatch that has a particular template name'
            EmailDispatch ed = new EmailDispatch(bodyTemplate: bodyTemplate)
        when: 'the method getTemplateView is called'
            String result = service.getTemplateView(ed)
        then: 'the correct Template View is returned accordingly'
            result  == validationResult
        where:
            bodyTemplate            |   validationResult
            'passwordReset'         |   "/auth/_forgotPasswordEmail"
            'passwordResetNotif'    |   "/auth/_resetPasswordNotificationEmail"
            'accountActivation'     |   "/auth/_accountActivationNotificationEmail"
            'adminResetPassword'    |   "/admin/_ResetPasswordNotificationEmail"
            'batchPostingResults'   |   "/auth/_importBatchPostingResultsEmail"
    }

    void '02. Test getTemplateView method with INVALID template name'() {

        setup:'given an email dispatch that has an invalid template name'
            EmailDispatch ed = new EmailDispatch(bodyTemplate: 'INVALID')
        when: 'the method getTemplateView is called'
            service.getTemplateView(ed)
        then: 'an exception is thrown'
            thrown InvalidParamException
    }
}
