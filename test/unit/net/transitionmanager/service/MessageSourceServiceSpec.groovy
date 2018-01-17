package net.transitionmanager.service

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.StaticMessageSource
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class MessageSourceServiceSpec extends Specification {

    StaticMessageSource messageSource

    static doWithSpring = {
        messageSourceService(MessageSourceService) { bean ->
            messageSource = ref('messageSource')
        }
    }

    def setup() {
        LocaleContextHolder.setLocale(Locale.ENGLISH)
        messageSource = applicationContext.getBean(MessageSource)
    }

    void "test getting non existing i18n message should return blank"() {
        setup: 'giving a MessageSourceService'
            MessageSourceService messageSourceService = applicationContext.getBean(MessageSourceService)
        expect: 'a blank message when a message code does not exists'
            '' == messageSourceService.i18nMessage('property.does.not.exists.unit.test.message')
    }

    void "test getting an i18n message should return expected value"() {
        setup: 'giving a MessageSourceService'
            MessageSourceService messageSourceService = applicationContext.getBean(MessageSourceService)
            messageSource.addMessage('unit.test.message', Locale.ENGLISH, 'Blah')
        expect: 'a default locale message should be returned for the code provided'
            'Blah' == messageSourceService.i18nMessage('unit.test.message')
    }

    void "test getting a non existing i18n message for a different locale should return null when default message is null"() {
        setup: 'giving a MessageSourceService'
            MessageSourceService messageSourceService = applicationContext.getBean(MessageSourceService)
            messageSource.addMessage('unit.test.message', Locale.ENGLISH, 'Blah')
        expect: 'null should be returned when getting a message for a different locale'
            null == messageSourceService.i18nMessage('unit.test.message', null, null, Locale.FRANCE)
    }

    void "test getting an interpolated i18n message should return the expected value"() {
        setup: 'giving a MessageSourceService'
            MessageSourceService messageSourceService = applicationContext.getBean(MessageSourceService)
            messageSource.addMessage('unit.test.message', Locale.ENGLISH, 'Hello {0}')
        expect: 'the expected interpolated message should be returned'
            'Hello world' == messageSourceService.i18nMessage('unit.test.message', ['world'] as Object[])
    }

}
