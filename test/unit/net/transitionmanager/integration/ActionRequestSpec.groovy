package net.transitionmanager.integration

import org.apache.camel.Exchange
import spock.lang.Specification

class ActionRequestSpec extends Specification {

    def 'test set parameter value will throw MissingPropertyException'() {
        setup:
            Map<String, Object> parameters = ['property1': 'value1']
            ActionRequest actionRequest = new ActionRequest(parameters)
        when:
            actionRequest.param.property2 = 'value2'
        then:
            thrown MissingPropertyException
    }

    def 'test set existing parameter value when action request is not readonly' () {
        setup:
            Map<String, Object> parameters = ['property1': 'value1']
            ActionRequest actionRequest = new ActionRequest(parameters)
        when:
            actionRequest.param.property1 = 'value2'
        then:
            actionRequest.param.property1 == 'value2'
    }

    def 'test set existing parameter value when action request is readonly will throw ReadOnlyPropertyException' () {
        setup:
            Map<String, Object> parameters = ['property1': 'value1']
            ActionRequest actionRequest = new ActionRequest(parameters)
            actionRequest.readonly = true
        when:
            actionRequest.param.property1 = 'value2'
        then:
            thrown ReadOnlyPropertyException
    }

    def 'test read parameter value when action request is readonly will return the expected value' () {
        setup:
            Map<String, Object> parameters = ['property1': 'value1']
            ActionRequest actionRequest = new ActionRequest(parameters)
            actionRequest.readonly = true
        expect:
            actionRequest.param.property1 == 'value1'
    }

    def 'test set header value when action request is not readonly' () {
        setup:
            ActionRequest actionRequest = new ActionRequest()
        when:
            actionRequest.headers.add('header1', 'value1')
        then:
            actionRequest.headers.get('header1') == 'value1'
    }

    def 'test set header value when action request is readonly will throw ReadOnlyPropertyException' () {
        setup:
            ActionRequest actionRequest = new ActionRequest()
            actionRequest.readonly = true
        when:
            actionRequest.headers.add('header1', 'value1')
        then:
            thrown ReadOnlyPropertyException
    }

    def 'test set config value will set the expected value' () {
        setup:
            ActionRequest actionRequest = new ActionRequest()
        when:
            actionRequest.config.setProperty(Exchange.CONTENT_TYPE, 'application/json')
        then:
            actionRequest.config.getProperty(Exchange.CONTENT_TYPE) == 'application/json'
    }

    def 'test get non existing config value will return null' () {
        setup:
            ActionRequest actionRequest = new ActionRequest()
        expect:
            null == actionRequest.config.getProperty(Exchange.CONTENT_TYPE)
    }

}
