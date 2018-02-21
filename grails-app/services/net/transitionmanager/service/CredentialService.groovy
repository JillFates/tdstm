package net.transitionmanager.service

import com.tdsops.tm.enums.domain.AuthenticationMethod
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.JsonUtil
import com.tdsops.common.security.SecurityUtil
import grails.plugins.rest.client.RestBuilder
import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import net.transitionmanager.command.CredentialCommand
import net.transitionmanager.domain.ApiAction
import net.transitionmanager.domain.Credential
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import net.transitionmanager.service.ProjectRequiredException
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
    Credential create(CredentialCommand credentialCO) {
        Project project = securityService.getUserCurrentProject()
        if (project == null) {
            throw new ProjectRequiredException()
        }

        // Make sure that the name is unique and Provider is associated with the project
        validateBeforeSave(project, null, credentialCO)

        // Create the credential and populate it from the Co
        Credential credentialInstance = new Credential()
        credentialCO.populateDomain(credentialInstance, false, ['password','id', 'version'])

        setEncryptedPassword(credentialInstance, credentialCO.password)

        credentialInstance.project = project

        credentialInstance.save(failOnError: true)

        return credentialInstance
    }

    /**
     * Update a credential from a CommandObject. The password should ONLY be updated if the the password property
     * has been populated and not overwrite it.
     *
     * @param id - the ID of the Credential to update
     * @param credential - a Command object with values to update with
     * @return the updated Credential
     */
    Credential update(Long id, CredentialCommand credentialCO) {
        Credential credentialInstance = findById(id)
        
        GormUtil.optimisticLockCheck(credentialInstance, credentialCO.properties, 'Credential')

        Project project = securityService.getUserCurrentProject()

        validateBeforeSave(project, id, credentialCO)

        credentialCO.populateDomain(credentialInstance, false, ['password', 'version'] )

        setEncryptedPassword(credentialInstance, credentialCO.password)

        credentialInstance.save(failOnError: true)

        return credentialInstance
    }

    /**
     * Delete a credential by Id
     * @param id
     */
    void delete(Long id) {
        Credential credential = findById(id)

        // Check if the credential is referenced by any ApiActions and prevent deleting
        int count = ApiAction.where {
            credential == credential
        }.count()

        if (count > 0) {
            // TODO : JPM 2/2018 : change to use standard service method to throw message that is i18n
            throw DomainUpdateException('Unable to delete Credential since it is reference by ApiActions')
        }

        credential.delete()
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
                authenticationResponse = [error: "Authentication method [${credential.authenticationMethod}] not implemented yet" ]
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
     * Used to encrypt the password when the password has a value
     * @param credential - the domain instance to set the password on
     * @param password - the cleartext password to encrypt
     */
    private void setEncryptedPassword(Credential credential, String password) {
        // If a password was provided in the request then save the new password encrypted with a new salt
        if (password?.trim().size() > 0) {
            // TODO - switch out calls to AESCodec when ready
            credential.salt = 'lsdklkajsdfljasd'
            credential.password = password.reverse()
        }
    }

    /**
     * Find a Credential instance with the given id, project and provider.
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

    /**
     * Performs some additional checks before the save occurs that includes:
     *    - validate that the Provider is associated with the current project
     *    - validate that the name for the credential being created or updated doesn't already exist
     * If the validations fail then the InvalidParamException exception is thrown with an appropriate message.
     * @throws InvalidParamException
     */
    private void validateBeforeSave(Project project, Long id, Object cmdObj) {
        // Make certain that the provider specified is associated to the project
        // TODO : JPM 2/2018 : to be replace by ofSameProject constraint when ready        
        Long providerProjectId = 0
        if (cmdObj.provider.project) {
            providerProjectId = cmdObj.provider.project.id
        } else {
            // id = Provider.executeQuery('select p.project.id from Provider p where p.id = :id', [id: ])[0]

            List ids = Provider.where { id == cmdObj.provider.id }
                .projections { property('project.id')}
                .list()
            if (ids) {
                id = ids[0]
            }

        }
        if (id == 0 || id != project.id) {
            throw new InvalidParamException('Invalid Provider specified')
        }

        // Make sure that name is unique
        int count = Credential.where {
            project == project
            name == cmdObj.name
            if (id) {
                id != id
            }
        }.count()

        if (count > 0) {
            throw new InvalidParamException('A Credential with the same name already exists')
        }
    }
}
