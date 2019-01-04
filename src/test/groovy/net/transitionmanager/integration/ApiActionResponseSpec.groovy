package net.transitionmanager.integration

import spock.lang.Specification

class ApiActionResponseSpec extends Specification {

    def 'test set action response property when the class is not immutable'() {
        setup: 'giving an ApiActionResponse object'
            ApiActionResponse apiActionResponse = new ApiActionResponse()
        when: 'trying to set a property and the object is not immutable'
            apiActionResponse.data = 'anything'
        then: 'the property value should be set to the expected value'
            apiActionResponse.data == 'anything'
    }

    def 'test set action response property when the class is immutable'() {
        setup: 'giving an ApiActionResponse object'
            ApiActionResponse apiActionResponse = new ApiActionResponse()
            apiActionResponse.data = 'anything'
        when: 'getting the action response as immutable'
            ApiActionResponse immutable = apiActionResponse.asImmutable()
        and: 'trying to set any of the properties of the immutable object'
            immutable.data = 'updated'
        then: 'an exception should be thrown indicating that we are trying to write on a readonly property'
            ReadOnlyPropertyException e = thrown ReadOnlyPropertyException
            "Cannot set readonly property: data for class: net.transitionmanager.integration.ApiActionResponse" == e.message
    }

}
