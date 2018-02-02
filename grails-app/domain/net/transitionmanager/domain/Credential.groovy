package net.transitionmanager.domain

import com.tdsops.tm.enums.domain.AuthenticationMethod
import com.tdsops.tm.enums.domain.CredentialStatus
import com.tdsops.tm.enums.domain.CredentialType

class Credential {
    Project project
    Provider provider
    String name

    CredentialType type

    CredentialStatus status

    // Determines how to authenticate
    AuthenticationMethod method

    // The salt will be combined with the system salt prefix to make it all the more difficult
    // to crack the encrypted values. The salt is a randomly generated string for each credential
    String salt

    // The username / access key used for authentication that is encrypted
    String accessKey

    // The password that is encrypted
    String password

    // The URL to the endpoint used to perform the authentication
    String authenticationUrl

    // The URL to the endpoint to renew tokens
    String renewTokenUrl

    Date dateCreated
    Date lastUpdated

    // The date that the credential will expire and/or will no longer be used
    Date expirationDate

    static belongsTo = [
            project: Project,
            provider: Provider
    ]

    static constraints = {
        name size: 1..255, unique: 'project'
        lastUpdated nullable: true
    }

    static mapping = {
        id column: 'credential_id'
        name sqlType: 'VARCHAR(255)'
        salt sqlType: 'VARCHAR(255)'
        accessKey sqlType: 'VARCHAR(255)'
        password sqlType: 'VARCHAR(255)'
        authenticationUrl sqlType: 'VARCHAR(255)'
        renewTokenUrl sqlType: 'VARCHAR(255)'

        sort 'name'
    }
}
