package net.transitionmanager.service

import com.tdsops.tm.enums.domain.AuthenticationMethod
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.JsonUtil
import grails.plugins.rest.client.RestBuilder
import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import net.transitionmanager.command.CredentialCreateCO
import net.transitionmanager.command.CredentialUpdateCO
import net.transitionmanager.domain.Credential
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

@Transactional
@Slf4j
class CredentialService implements ServiceMethods {
    ProjectService projectService
    SecurityService securityService

    /**
     * Creates a new credential
     * @param credential
     * @return
     */
    Credential createCredential(CredentialCreateCO credentialCO) {
        assert credentialCO.provider != null : 'Invalid provider param.'

        if (credentialCO.hasErrors()) {
            throw new InvalidParamException(GormUtil.allErrorsString(credentialCO))
        }

        Credential credentialInstance = new Credential()
        credentialCO.populateDomain(credentialInstance)
        credentialInstance.project = securityService.getUserCurrentProject()
        credentialInstance.salt = '{sha}'

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
        Project project = securityService.getUserCurrentProject()
        return GormUtil.findInProject(project, Credential.class, id, true)
    }

    /**
     * Find all credential belonging to a project
     * @param project
     * @return
     */
    List<Credential> findAllByProject(Project project) {
        return Credential.where {
            project == project
        }.list()
    }

    /**
     * Find all credential belonging to current user project
     * @param project
     * @return
     */
    List<Credential> findAllByCurrentUserProject() {
        Project project = securityService.getUserCurrentProject()
        return Credential.where {
            project == project
        }.list()
    }

    /**
     * Find all credential belonging to a provider
     * @param provider
     * @return
     */
    List<Credential> findAllByProvider(Provider provider) {
        Project project = securityService.getUserCurrentProject()
        return Credential.where {
            project == project
            provider == provider
        }.list()
    }

    /**
     * Update a credential
     * @param credential
     * @return
     */
    Credential updateCredential(CredentialUpdateCO credentialCO) {
        Credential credentialInstance = findById(credentialCO.id)
        GormUtil.optimisticLockCheck(credentialInstance, credentialCO.properties, 'Credential')

        credentialCO.populateDomain(credentialInstance, true)
        credentialInstance.lastUpdated = new Date()

        if (credentialCO.hasErrors()) {
            throw new InvalidParamException(GormUtil.allErrorsString(credentialCO))
        }

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
        switch (credential.authenticationMethod) {
            case AuthenticationMethod.JWT:
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
        String jsonString = JsonUtil.convertMapToJsonString([username: credential.getUsername(), password: credential.getPassword()])
        RestBuilder rest = new RestBuilder()
        def resp = rest.post(credential.getAuthenticationUrl()) {
            header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            json jsonString
        }
        return JsonUtil.convertJsonToMap(resp.json)
    }

    /**
     * Find a Credential instance with the given id, project and provider.
     *
     * @param id
     * @param project
     * @param provider
     * @param throwException
     * @return
     */
    private Credential findByProjectAndProvider(Long id, Project project, Provider provider, boolean throwException = false) {
        Credential credential = Credential.where {
            id == id
            project == project
            provider == provider
        }.find()

        if (! credential && throwException) {
            throw new EmptyResultException("No Credential exists with the ID $id for the Project $project and Provider $provider.")
        }
        return credential
    }
}
