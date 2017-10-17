package net.transitionmanager.service

import com.tdssrc.grails.GormUtil
import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import net.transitionmanager.domain.Credential
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider

@Transactional
@Slf4j
class CredentialService {
    ProjectService projectService

    Credential createCredential(Credential credential) {
        assert credential.project != null : 'Invalid project param.'
        assert credential.provider != null : 'Invalid provider param.'

        Credential credentialInstance = new Credential(credential.properties)
        credentialInstance.save()
        return credentialInstance
    }

    Credential findById(Long id) {
        assert id != null : 'Invalid id param.'
        return Credential.findById(id)
    }

    List<Credential> findAllByProject(Project project) {
        return Credential.findAllByProject(project)
    }

    List<Credential> findAllByProvider(Provider provider) {
        return Credential.findAllByProvider(provider)
    }

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

    void deleteCredential(Credential credential) {
        if (credential) {
            credential.delete()
        }
    }

    void deleteCredential(Long id) {
        deleteCredential(findById(id))
    }

}
