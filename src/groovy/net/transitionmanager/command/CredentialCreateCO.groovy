package net.transitionmanager.command

import com.tdsops.tm.enums.domain.AuthenticationMethod
import com.tdsops.tm.enums.domain.CredentialEnvironment
import com.tdsops.tm.enums.domain.CredentialHttpMethod
import com.tdsops.tm.enums.domain.CredentialStatus
import grails.validation.Validateable
import net.transitionmanager.domain.Credential
import net.transitionmanager.domain.Provider

/**
 * Command object to handle credential form data binding upon creation
 */
@Validateable
class CredentialCreateCO implements CommandObject {
    Provider provider
    String name
    CredentialHttpMethod httpMethod
    CredentialEnvironment environment
    CredentialStatus status
    AuthenticationMethod authenticationMethod
    String username
    String password
    String authenticationUrl
    String renewTokenUrl=''
    Date expirationDate

    static constraints = {
        importFrom Credential, include: [
                'name',
                'renewTokenUrl'
        ]
    }
}
