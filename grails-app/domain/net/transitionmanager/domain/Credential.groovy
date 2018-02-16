package net.transitionmanager.domain

import com.tdsops.tm.enums.domain.AuthenticationMethod
import com.tdsops.tm.enums.domain.CredentialHttpMethod
import com.tdsops.tm.enums.domain.CredentialStatus
import com.tdsops.tm.enums.domain.CredentialEnvironment

class Credential {
    Project project
    Provider provider
    String name

	CredentialStatus status
	CredentialEnvironment environment
	AuthenticationMethod authenticationMethod

    // Determines how to authenticate
    CredentialHttpMethod httpMethod = CredentialHttpMethod.POST

    // The salt will be combined with the system salt prefix to make it all the more difficult
    // to crack the encrypted values. The salt is a randomly generated string for each credential
    String salt

    // The username / access key used for authentication that is encrypted
    String username

    // The password that is encrypted
    String password

    // The URL to the endpoint used to perform the authentication
    String authenticationUrl=''

    // The URL to the endpoint to renew tokens
    String renewTokenUrl=''

	// The date that the credential will expire and/or will no longer be used
	Date expirationDate

	Date dateCreated
    Date lastUpdated

    static belongsTo = [
            project: Project,
            provider: Provider
    ]

    static constraints = {
        name size: 1..255, unique: 'project'
		renewTokenUrl nullable: true, blank: true
        lastUpdated nullable: true
        expirationDate nullable: true
    }

    static mapping = {
        id column: 'credential_id'
        name sqlType: 'VARCHAR(255)'
        salt sqlType: 'VARCHAR(255)'
        username sqlType: 'VARCHAR(255)'
        password sqlType: 'VARCHAR(255)'
        authenticationUrl sqlType: 'VARCHAR(255)'
        renewTokenUrl sqlType: 'VARCHAR(255)'
        authenticationMethod enumType: 'String'
        environment enumType: 'String'
        httpMethod enumType: 'String'
        status enumType: 'String'

		sort 'name'
    }

    // Closure validateRenewTokenUrl = { ... }

    /**
     * Converts this credential object to a map.
     * @return
     */
    Map toMap() {
        Map data = [
                id						: id,
                project					: [id: project.id, name: project.name],
                provider				: [id: provider.id, name: provider.name],
                name					: name,
                environment				: environment.name(),
                status					: status.name(),
                authenticationMethod	: authenticationMethod.name(),
                httpMethod              : httpMethod.name(),
                username        		: username,
                authenticationUrl       : authenticationUrl,
                renewTokenUrl           : renewTokenUrl,
                expirationDate  		: expirationDate,
                dateCreated     		: dateCreated,
                lastUpdated     		: lastUpdated,
                version                 : version
        ]
        return data
    }
}
