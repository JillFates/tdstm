package com.tdsops.tm.enums.domain

import net.transitionmanager.exception.InvalidParamException
import spock.lang.Specification

class UserPreferenceEnumTests extends Specification {

    def '1. getting valueOfNameOrCode with invalid name or value throws InvalidParamException'() {
        when:
            UserPreferenceEnum.valueOfName('i-v-a-l-i-d-n-a-m-e-o-r-v-a-l-u-e')
        then:
            thrown InvalidParamException
    }

    def '2. getting valueOfNameOrCode with valid name does not throw InvalidParamException'() {
        when:
            UserPreferenceEnum.valueOfName('ASSET_LIST_SIZE')
        then:
            noExceptionThrown()
    }

    def '3. getting valueOfNameOrCode passing value results in a UserPreferenceEnum instance'() {
        expect:
            UserPreferenceEnum.ASSET_LIST_SIZE == UserPreferenceEnum.valueOfName('ASSET_LIST_SIZE')
    }

    def '4. getting valueOfNameOrCode entry has no value results in a UserPreferenceEnum instance'() {
        expect:
            UserPreferenceEnum.ImportDatabase == UserPreferenceEnum.valueOfName('ImportDatabase')
    }

    def '5. is session only preference'() {
        expect:
            UserPreferenceEnum.isSessionOnlyPreference('TASK_CREATE_EVENT')
            !UserPreferenceEnum.isSessionOnlyPreference('ASSET_LIST_SIZE')
    }
}
