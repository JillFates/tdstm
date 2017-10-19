package net.transitionmanager.service

import com.tdsops.tm.enums.domain.AuthenticationMethod
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.JsonUtil
import grails.plugins.rest.client.RestBuilder
import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import net.transitionmanager.domain.Credential
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

@Transactional
@Slf4j
class CredentialService {
    ProjectService projectService

    /**
     * Creates a new credential
     * @param credential
     * @return
     */
    Credential createCredential(Credential credential) {
        assert credential.project != null : 'Invalid project param.'
        assert credential.provider != null : 'Invalid provider param.'

        Credential credentialInstance = new Credential(credential.properties)
        credentialInstance.save()
        return credentialInstance
    }

    /**
     * Find a credential by Id
     * @param id
     * @return
     */
    Credential findById(Long id) {
        assert id != null : 'Invalid id param.'
        return Credential.findById(id)
    }

    /**
     * Find all credential belonging to a project
     * @param project
     * @return
     */
    List<Credential> findAllByProject(Project project) {
        return Credential.findAllByProject(project)
    }

    /**
     * Find all credential belonging to a provider
     * @param provider
     * @return
     */
    List<Credential> findAllByProvider(Provider provider) {
        return Credential.findAllByProvider(provider)
    }

    /**
     * Update a credential
     * @param credential
     * @return
     */
    Credential updateCredential(Credential credential) {
        Credential credentialInstance = findById(credential.id)
        GormUtil.optimisticLockCheck(credentialInstance, credential.properties, 'Credential')

        credentialInstance.name = credential.name
        credentialInstance.type = credential.type
        credentialInstance.status = credential.status
        credentialInstance.salt = credential.salt
        credentialInstance.accessKey = credential.accessKey
        credentialInstance.password = credential.password
        credentialInstance.authenticationUrl = credential.authenticationUrl
        credentialInstance.renewTokenUrl = credential.renewTokenUrl
        credentialInstance.expirationDate = credential.expirationDate
        credentialInstance.lastUpdated = new Date()
        credentialInstance.save()

        return credentialInstance
    }

    /**
     * Delete a credential
     * @param credential
     */
    void deleteCredential(Credential credential) {
        if (credential) {
            credential.delete()
        }
    }

    /**
     * Delete a credential by Id
     * @param id
     */
    void deleteCredential(Long id) {
        deleteCredential(findById(id))
    }

    /**
     * Issue an authentication process using provided credential Id
     * @param id
     * @return
     */
    Map authenticate(Long id) {
        authenticate(findById(id))
    }

    /**
     * Issue an authentication process using provided credential
     * @param credential
     * @return
     */
    Map authenticate(Credential credential) {
        assert credential != null : 'Invalid credential information provided.'

        Map authenticationResponse = null
        switch (credential.method) {
            case AuthenticationMethod.JWT_TOKEN:
                authenticationResponse = doJWTTokenAuthentication(credential)
                break
            default:
                // do nothing
                log.info('Authentication method provided not implemented yet. {}', credential.method)
                break
        }
        return authenticationResponse
    }

    /**
     * Issue an JWT authentication process using provided credentials
     * @param credential
     * @return
     */
    private Map doJWTTokenAuthentication(Credential credential) {
        String jsonString = JsonUtil.convertMapToJsonString([username: credential.getAccessKey(), password: credential.getPassword()])
        RestBuilder rest = new RestBuilder()
        def resp = rest.post(credential.getAuthenticationUrl()) {
            header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            json jsonString
        }
        return JsonUtil.convertJsonToMap(resp.json)
    }
}
