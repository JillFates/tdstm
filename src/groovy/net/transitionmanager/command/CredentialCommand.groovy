package net.transitionmanager.command

import com.tdsops.tm.enums.domain.AuthenticationMethod
import com.tdsops.tm.enums.domain.AuthenticationRequestMode
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
class CredentialCommand implements CommandObject {
    Provider provider
    String name
    String description = ''
    CredentialHttpMethod httpMethod
    CredentialEnvironment environment
    CredentialStatus status
    AuthenticationMethod authenticationMethod
    AuthenticationRequestMode requestMode
    String username
    String password=''
    String authenticationUrl=''
    String terminateUrl=''
    String renewTokenUrl = ''
    String sessionName=''
    String validationExpression=''
}
