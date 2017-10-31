package net.transitionmanager.command

import com.tdsops.tm.enums.domain.AuthenticationMethod
import com.tdsops.tm.enums.domain.CredentialStatus
import com.tdsops.tm.enums.domain.CredentialType
import grails.validation.Validateable
import net.transitionmanager.domain.Credential
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider

@Validateable
class CredentialCO implements CommandObject {
    Long id
    Project project
    Provider provider
    String name
    CredentialType type
    CredentialStatus status
    AuthenticationMethod method
    String accessKey
    String password
    String authenticationUrl
    String renewTokenUrl
    Date expirationDate
    Long version

    static constraints = {
        importFrom Credential, include: [
                'name'
        ]
    }
}
