package net.transitionmanager.service

import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.StringUtil
import grails.transaction.Transactional
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import org.codehaus.groovy.grails.web.json.JSONObject

@Transactional
class ProviderService implements ServiceMethods {

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

        // Should the provider name be empty, throw an exception.
        if (!providerName) {
            throw new InvalidParamException("The name for the Provider cannot be null.")
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

        Provider provider

        // Check if it's creating or updating a provider
        if (providerId) {
            // Find the corresponding provider, fail otherwise
            provider = getProvider(providerId, currentProject, true)
        } else {
            // If it's creating a new provider, create a new instance.
            provider = new Provider()
        }

        // Validate the provider name is unique.
        if (!validateUniqueName(providerJson.name, providerId, currentProject)) {
            throw new DomainUpdateException("Cannot update or create Provider because the name is not unique for this project.")
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
        }.list([sort:'name',order:'asc'])
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

    /**
     * Fetch a Provider from database by name
     * @param providerName - provider name
     * @param project - project
     * @param throwException - whether to throw or not an exception if provider is not found
     * @return a provider instance
     */
    Provider getProvider(String name, Project project = null, boolean throwException = false) {
        if (!project) {
            project = securityService.userCurrentProject
        }
        // Find a provider with the given name for this project.
        Provider provider = GormUtil.findInProjectByAlternate(project, Provider, name, throwException)

        return provider
    }

    /**
     * Create new provider for api catalog
     * @param providerName new or existing provider name
     * @return a provider instance
     * @throws InvalidParamException
     */
    Provider findOrCreateProvider(String providerName, Project project) {
        if (StringUtil.isBlank(providerName)) {
            throw new InvalidParamException("Provider name cannot be blank or null.")
        }

        Provider provider = getProvider(providerName, project, false)
        if (!provider) {
            JSONObject jsonObject = new JSONObject([name: providerName, description: '', comment: ''])
            provider = saveOrUpdateProvider(jsonObject)
        }
        return provider
    }

    /**
     * Clone any existing providers associated to sourceProject (if any),
     * then associate those newly created providers to targetProject.
     *
     * @param sourceProject  The project from which the existing providers will be cloned.
     * @param targetProject  The project to which the new providers will be associated.
     */
    void cloneProjectProviders(Project sourceProject, Project targetProject) {
        List<Provider> providers = Provider.where {
            project == sourceProject
        }.list()

        if (!providers.isEmpty()) {
            providers.each { Provider sourceProvider ->
                Provider newProvider = (Provider)GormUtil.cloneDomainAndSave(sourceProvider,
                        [project: targetProject], false, false);
                log.debug "Cloned provider ${newProvider.name} for project ${targetProject.toString()}"
            }
        }
    }
}
