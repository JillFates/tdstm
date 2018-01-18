package net.transitionmanager.service

import com.tdssrc.grails.GormUtil
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import org.codehaus.groovy.grails.web.json.JSONObject

class ProviderService implements ServiceMethods {

    SecurityService securityService

    /**
     * Validate that a given name is unique for providers for the project.
     * @param providerName
     * @param providerId
     * @param project
     * @return
     */
    boolean validateUniqueName(String providerName, Long providerId, Project project = null) {
        // If the project is null, fetch the user's current project
        if (!project) {
            project = securityService.userCurrentProject
        }
        boolean isUnique = true

        // Lookup a provider with the given name and project
        Provider provider = Provider.where {
            name == providerName
            project == project
        }.find()

        // If a provider was found, check if the IDs match. If not, the name is not unique.
        if (provider) {
            if (provider.id != providerId) {
                isUnique = false
            }
        }

        return isUnique
    }

    /**
     * Create/Update a provider instance.
     *
     * @param providerJson
     * @param providerId
     * @return
     */
    Provider saveOrUpdateProvider(JSONObject providerJson, Long providerId = null) {
        Project currentProject = securityService.userCurrentProject

        // Validate the provider name is unique.
        if (!validateUniqueName(providerJson.name, providerId, currentProject)) {
            throw new DomainUpdateException("Cannot update or create Provider because the name is not unique for this project.")
        }

        Provider provider

        // Check if it's creating or updating a provider
        if (providerId) {
            // Find the corresponding provider
            provider = getProvider(providerId, currentProject)
        } else {
            // If it's creating a new provider, create a new instance.
            provider = new Provider()
        }

        provider.with {
            name = providerJson.name
            description = providerJson.description
            comment = providerJson.comment
            // Assign the project only if it's a new Provider instance.
            if (!id) {
                project = currentProject
            }
        }

        // Try to save or fail.
        if (!provider.save()) {
            throw new DomainUpdateException("Error creating or updating Provider ${GormUtil.allErrorsString(provider)}")
        }

        return provider
    }

    /**
     * Fetch a Provider from the database.
     * @param providerId
     * @param project
     * @return
     */
    Provider getProvider(Long providerId, Project project = null, boolean throwException = false) {
        if (!project) {
            project = securityService.userCurrentProject
        }
        // Find a provider with the given id for this project.
        Provider provider = Provider.where{
            id == providerId
            project == project
        }.find()

        if (!provider && throwException) {
            throw new EmptyResultException("No Provider with id ${providerId} exists for this project.")
        }
        return provider
    }

    /**
     * Return the providers for this project.
     * @return
     */
    List<Provider> getProviders() {
        return Provider.where {
            project == securityService.userCurrentProject
        }.list()
    }

    /**
     * Delete a provider from the system.
     * @param providerId
     */
    void deleteProvider(Long providerId) {
        Provider provider = getProvider(providerId, null, true)
        if (provider) {
            provider.delete()
        }
    }
}
