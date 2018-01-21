package net.transitionmanager.service

import com.tdssrc.grails.GormUtil
import net.transitionmanager.domain.DataScript
import net.transitionmanager.domain.DataScriptMode
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import org.codehaus.groovy.grails.web.json.JSONObject

class DataScriptService implements ServiceMethods{

    ProviderService providerService
    SecurityService securityService

    /**
     * Create or update a DataScript instance based on the JSON object received.
     *
     * @param jsonObject
     * @param dataScriptId
     * @return
     */
    DataScript saveOrUpdateDataScript(JSONObject dataScriptJson, Long dataScriptId = null) {

        // Get the current project
        Project currentProject = securityService.userCurrentProject

        DataScript dataScript

        // Get the current person
        Person currentPerson = securityService.loadCurrentPerson()

        if (!dataScriptId) {
            // Create a new DataScript instance
            dataScript = new DataScript()
        } else {
            // Fetch the DataScript with the given id.
            dataScript = getDataScript(dataScriptId, currentProject)
        }

        // Validate that there's no other DataScript for this project and provider with the same name
        if (!validateUniqueName(dataScriptJson.name, dataScriptId, dataScriptJson.providerId, currentProject)) {
            throw new DomainUpdateException("Cannot update or create DataScript because the name is not unique for this project and provider.")
        }

        // Find the provider
        Provider providerInstance = providerService.getProvider(dataScriptJson.providerId, currentProject, true)

        // Copy the values received from the JSON Object over to the DataScript instance.
        dataScript.with {
            name = dataScriptJson.name
            description = dataScriptJson.description
            target = dataScriptJson.target
            mode = DataScriptMode.forLabel(dataScriptJson.mode)
            etlSourceCode = dataScriptJson.etlSourceCode
            provider = providerInstance
            // if it's an existing instance, update the lastModifiedBy field
            if (id) {
                lastModifiedBy = currentPerson
            } else { // if it's a new instance, set the createdBy and project fields.
                createdBy = currentPerson
                project = currentProject
            }
        }

        // Try to save or fail.
        if (!dataScript.save()) {
            throw new DomainUpdateException("Error creating or updating DataScript ${GormUtil.allErrorsString(dataScript)}")
        }
        return dataScript
    }

    /**
     * Find a DataScript with the given ID, checking if it belongs
     * to the user's project.
     *
     * @param dataScriptId
     * @return
     */
    DataScript getDataScript(Long dataScriptId, Project project = null) {
        if (!project) {
            project = securityService.userCurrentProject
        }
        // Find a datascript with the given id for this project.
        DataScript dataScript = DataScript.where{
            id == dataScriptId
            project == project
        }.find()

        if (!dataScript) {
            throw new EmptyResultException("No DataScript with id ${dataScriptId} exists for this project.")
        }

        return dataScript
    }

    /**
     * Check if a given DataScript name is unique across project and provider.
     *
     * This method will check that there's no other DataScript for this project
     * and provider with the same name or, if there is, that they have the same id.
     *
     * @param params
     * @param dataScriptName
     * @return
     */
    boolean validateUniqueName(String dataScriptName, Long dataScriptId, Long providerId, Project project = null) {
        boolean isUnique = true
        if (!project) {
            project = securityService.userCurrentProject
        }

        // If the name is null don't validate, throw an exception.
        if (!dataScriptName) {
            throw new InvalidParamException("The DataScript name cannot be null.")
        }

        DataScript dataScript = DataScript.where {
            name == dataScriptName
            project == project
            provider.id == providerId
        }.find()

        if (dataScript) {
            // If the ids don't match or params has no id, then it's a duplicate.
            if (dataScriptId != dataScript.id) {
                isUnique = false
            }
        }

        return isUnique
    }

    /**
     * Return a list of DataScripts for the current project and provider (optional).
     * @param providerId
     * @return
     */
    List<DataScript> getDataScripts(Long providerId = null) {
        return DataScript.where {
            project == securityService.userCurrentProject
            if (providerId) {
                provider.id == providerId
            }
        }.list()
    }

    /**
     * Delete the given DataScript making sure it belongs to user's current project.
     *
     * @param dataScriptId
     */
    void deleteDataScript(Long dataScriptId) {
        // Fetch the DataScript, validating it belogns to the user's project.
        DataScript dataScript = getDataScript(dataScriptId)
        // Delete the DataScript if found.
        if (dataScript) {
            dataScript.delete()
        }
    }

    /**
     * Find a DataScript with the given id, project and provider.
     *
     * @param id
     * @param project
     * @param provider
     * @param throwException
     * @return
     */
    DataScript findByProjectAndProvider(Long id, Project project, Provider provider, boolean throwException = false) {
        DataScript dataScript = DataScript.where {
            id == id
            project == project
            provider == provider
        }.find()

        if (! dataScript && throwException) {
            throw new EmptyResultException("No DataScript exists with the ID $id for the Project $project and Provider $provider.")
        }
        return dataScript
    }
}
