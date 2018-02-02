package net.transitionmanager.integration

import org.apache.camel.Exchange
import spock.lang.Specification

class ActionRequestSpec extends Specification {

    def 'test set parameter value will throw MissingPropertyException'() {
        setup: 'giving an ActionRequest object with parameters being set'
            Map<String, Object> parameters = ['property1': 'value1']
            ActionRequest actionRequest = new ActionRequest(parameters)
        when: 'trying to set a non existing parameter'
            actionRequest.param.property2 = 'value2'
        then: 'an exception should be thrown indicating that passed parameter is missing'
            MissingPropertyException e = thrown MissingPropertyException
            "No such property: property2" == e.message
    }

    def 'test set existing parameter value when action request is not readonly' () {
        setup: 'giving an ActionRequest object with parameters being set'
            Map<String, Object> parameters = ['property1': 'value1']
            ActionRequest actionRequest = new ActionRequest(parameters)
        when: 'trying to set an existing parameter value and readonly flag is false'
            actionRequest.param.property1 = 'value2'
        then: 'the parameter value should be updated to the expected value'
            actionRequest.param.property1 == 'value2'
    }

    def 'test set existing parameter value when action request is readonly will throw ReadOnlyPropertyException' () {
        setup: 'giving an ActionRequest object with parameters being set and readonly flag is turned on'
            Map<String, Object> parameters = ['property1': 'value1']
            ActionRequest actionRequest = new ActionRequest(parameters)
            actionRequest.readonly = true
        when: 'trying to set an existing parameter value and readonly flag is true'
            actionRequest.param.property1 = 'value2'
        then: 'an exception should be thrown indicating that we are trying to write on a readonly property'
            ReadOnlyPropertyException e = thrown ReadOnlyPropertyException
            "Cannot set readonly property: property1 for class: net.transitionmanager.integration.ActionRequestParameter" == e.message
    }

    def 'test read parameter value when action request is readonly will return the expected value' () {
        setup: 'giving an ActionRequest object with parameters being set and readonly flag is turned on'
            Map<String, Object> parameters = ['property1': 'value1']
            ActionRequest actionRequest = new ActionRequest(parameters)
            actionRequest.readonly = true
        expect: 'we should be able to read values despite the readonly flag status'
            actionRequest.param.property1 == 'value1'
    }

    def 'test set header value when action request is not readonly' () {
        setup: 'giving an ActionRequest object'
            ActionRequest actionRequest = new ActionRequest()
        when: 'trying to set a new header value'
            actionRequest.headers.add('header1', 'value1')
        then: 'the new header value should be written and red as expected'
            actionRequest.headers.get('header1') == 'value1'
    }

    def 'test set header value when action request is readonly will throw ReadOnlyPropertyException' () {
        setup: 'giving an ActionRequest object and readonly flag is turned on'
            ActionRequest actionRequest = new ActionRequest()
            actionRequest.readonly = true
        when: 'trying to set a new header value'
            actionRequest.headers.add('header1', 'value1')
        then: 'an exception should be thrown indicating that we are trying to write on a readonly property'
            ReadOnlyPropertyException e = thrown ReadOnlyPropertyException
            "Cannot set readonly property: header1 for class: net.transitionmanager.integration.ActionRequestHeader" == e.message
    }

    def 'test set config value will set the expected value' () {
        setup: 'giving an ActionRequest object'
            ActionRequest actionRequest = new ActionRequest()
        when: 'trying to set a new configuration property'
            actionRequest.config.setProperty(Exchange.CONTENT_TYPE, 'application/json')
        then: 'the new configuration value should be written and red as expected'
            actionRequest.config.getProperty(Exchange.CONTENT_TYPE) == 'application/json'
    }

    def 'test get non existing config value will return null' () {
        setup: 'giving an ActionRequest object'
            ActionRequest actionRequest = new ActionRequest()
        expect: 'null should be received if trying to read a non existing configuration value'
            null == actionRequest.config.getProperty(Exchange.CONTENT_TYPE)
    }

}