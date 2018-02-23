package net.transitionmanager.service

import com.tdsops.common.security.AESCodec
import com.tdsops.tm.enums.domain.AuthenticationMethod
import com.tdsops.tm.enums.domain.AuthenticationRequestMode
import com.tdsops.tm.enums.domain.CredentialEnvironment
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.JsonUtil
import com.tdssrc.grails.UrlUtil
import grails.plugins.rest.client.RestBuilder
import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import net.transitionmanager.command.CredentialCommand
import net.transitionmanager.domain.ApiAction
import net.transitionmanager.domain.Credential
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate

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
    Map<String, ?> authenticate(Credential credential) {
        assert credential != null : 'Invalid credential information provided.'

        // TODO look for authentication in cache by credential ID, if there is one
        // we shouldn't be doing re-authentication

        Map<String, ?> authenticationResponse
        switch (credential.authenticationMethod) {
			case AuthenticationMethod.BASIC_AUTH:
				authenticationResponse = ['username': credential.username, 'password': decryptPassword(credential)]
				break;
            case AuthenticationMethod.JWT:
                authenticationResponse = doJWTTokenAuthentication(credential)
                break
            case AuthenticationMethod.COOKIE:
                authenticationResponse = doCookieAuthentication(credential)
                break
            default:
                authenticationResponse = [error: "Authentication method [${credential.authenticationMethod}] not implemented yet" ]
                break
        }
        return authenticationResponse
    }

    /**
     * Issue a JWT authentication process using provided credentials
     * @param credential
     * @return
     */
    private Map<String, ?> doJWTTokenAuthentication(Credential credential) {
        String jsonString = JsonUtil.convertMapToJsonString([username: credential.getUsername(), password: decryptPassword(credential)])
        RestBuilder rest = getRestBuilderForCredentialEnvironment(credential.authenticationUrl, credential.environment)
        def resp = rest.post(credential.getAuthenticationUrl()) {
            // TODO determine the httpMethod is GET, POST or PUT
            // verify if we need to parse parameters out of the URL
            header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            json jsonString
        }
        return JsonUtil.convertJsonToMap(resp.json)
    }

    /**
     * Issue a COOKIE authentication process using provided credentials
     * @param credential
     * @return
     */
    private Map<String, ?> doCookieAuthentication(Credential credential) {
        RestBuilder rest = getRestBuilderForCredentialEnvironment(credential.authenticationUrl, credential.environment)
        def resp = rest.post(credential.getAuthenticationUrl()) {
            // TODO determine the httpMethod is GET, POST or PUT
            // verify if we need to parse parameters out of the URL
            switch (credential.requestMode) {
                case AuthenticationRequestMode.BASIC_AUTH:
                    auth credential.username, AESCodec.instance.decode(credential.password, credential.salt)
                    break
                case AuthenticationRequestMode.FORM_VARS:
                    MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>()
                    form.add('username', credential.username)
                    form.add('password', decryptPassword(credential))
                    header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    body(form)
                    break
            }
            header(HttpHeaders.ACCEPT, MediaType.ALL_VALUE)
        }

        // pull out session header data
        Map<String, ?> authentication = [:]
        for (String header : resp.getHeaders().get(HttpHeaders.SET_COOKIE)) {
            if (header.contains(credential.sessionName)) {
                String sessionHeader = header.split(';')[0]
                String[] sessionName = sessionHeader.split('=')
                authentication = ['sessionName': sessionName[0], 'sessionValue': sessionName[1]]
            }
        }
		return authentication
    }

    /**
     * Gets the RestTemplate for the RestBuilder according to the environment
     * For PRODUCTION we should trust SSL certificates as they come but for
     * other environments some certificates are self-signed so HTTPClient needs
     * some help to trust them, so the custom trust store is doing that.
     *
     * @param environment - the credential environment
     * @return
     */
    private RestBuilder getRestBuilderForCredentialEnvironment(String url, CredentialEnvironment environment) {
        if (UrlUtil.isSecure(url) && environment != CredentialEnvironment.PRODUCTION) {
            return new RestBuilder(getRestTemplateWithTrustStore())
        } else {
            return new RestBuilder()
        }
    }

    /**
     * Trust Store logic for verifying certificates from root ca by java itself
     * Security note: Use this only for non-production environments and end-points
     *
     * @return a RestTemplate configured with a Allow All Trust Store
     */
    private RestTemplate getRestTemplateWithTrustStore() {
        TrustManager[] trustAllCerts = [
                new X509TrustManager() {
                    X509Certificate[] getAcceptedIssuers() { return [] }
                    void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
                    void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
                }
        ] as TrustManager[]

        SSLContext sslContext = SSLContext.getInstance('TLS')
        sslContext.init(null, trustAllCerts, new SecureRandom())
        SSLConnectionSocketFactory connectionSocketFactory = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(connectionSocketFactory)
                .build()
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory()
        requestFactory.setHttpClient(httpClient)
        return new RestTemplate(requestFactory)
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
     * Used to encrypt the password when the password has a value. Passwords have a limit of 60 characters
     * due to the encoded value is 2.5x bigger.
     * @param credential - the domain instance to set the password on
     * @param password - the cleartext password to encrypt
     * @throws InvalidParamException if password is more than 60 characters
     */
    private void setEncryptedPassword(Credential credential, String password) {
        int size = (password ? password.trim().size() : 0)
        if (size > 60) {
            throw new InvalidParamException('Passwords have a limit of 60 characters')
        }
        if ( size > 0) {
            // If the request has a password then a new salt will be created and used to encode the password
            credential.salt = AESCodec.instance.generateRandomSalt().substring(0,16)
            credential.password = AESCodec.instance.encode(password, credential.salt)
        }
    }

    /**
     * Used to decrypt a password from a Credential record
     * @param credential - a Credential object with a password to be decrypted
     * @return the unencrypted password
     */
    String decryptPassword(Credential credential) {
        return AESCodec.instance.decode(
                credential.password,
                credential.salt
        )
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
            List ids = Provider.where { id == cmdObj.provider.id }
                .projections { property('project.id')}
                .list()
            if (ids) {
                providerProjectId = ids[0]
            }

        }
        if (providerProjectId == 0 || providerProjectId != project.id) {
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
