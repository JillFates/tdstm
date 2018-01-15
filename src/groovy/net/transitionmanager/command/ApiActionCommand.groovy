package net.transitionmanager.command

import grails.validation.Validateable
import net.transitionmanager.agent.AbstractAgent
import net.transitionmanager.agent.AgentClass
import net.transitionmanager.agent.CallbackMode
import net.transitionmanager.domain.ApiAction
import net.transitionmanager.domain.Credential
import net.transitionmanager.domain.DataScript
import net.transitionmanager.domain.Provider
import net.transitionmanager.service.ApiActionService

/**
 * Command Object for handling API Action endpoints.
 *
 */
@Validateable
class ApiActionCommand{

    static ApiActionService apiActionService

    AgentClass agentClass
    String agentMethod
    String asyncQueue
    String callbackMethod
    CallbackMode callbackMode
    Credential credential
    DataScript defaultDataScript
    String description
    String endpointPath
    String endpointUrl
    String methodParams
    String name
    Integer pollingInterval
    Integer pollingLapsedAfter
    Integer pollingStalledAfter
    Integer producesData
    Provider provider
    String reactionJson
    Integer timeout
    Integer useWithAsset
    Integer useWithTask

    /**
     * Populate an API Action with the properties in this command object.
     * For safety, this method won't set the id nor the project.
     *
     * @param apiAction - the target ApiAction instance.
     */
    void populateDomain(ApiAction apiAction) {
        if (apiAction) {
            apiAction.properties = this.properties
        }
    }


    /**
     * This is a list of those standards properties which can be
     * automatically added to the map in the toMap method.
     */
    private static final List<String> standardProperties = [
            'agentMethod',
            'asyncQueue',
            'description',
            'endpointPath',
            'endpointUrl',
            'methodParams',
            'pollingInterval',
            'pollingLapsedAfter',
            'pollingStalledAfter',
            'producesData',
            'reactionJson',
            'timeout',
            'useWithAsset',
            'useWithTask'

    ]

    /**
     * References to other domain classes which require special treatment
     * in toMap
     */
    private static final List<String> references = [
            "credential",
            "provider",
            "defaultDataScript"
    ]


    /**
     * Create a return a Map representation of the given API Action.
     * @param apiAction
     * @param minimalInfo: if set to true, only the id and name will be returned
     * @return
     */
    static Map<String, Object> toMap(ApiAction apiAction, boolean minimalInfo = false) {
        Map<String, Object> domainMap = [id: apiAction.id, name: apiAction.name]

        if (!minimalInfo) {
            // Copy the standard properties
            for (prop in standardProperties) {
                domainMap[prop] = apiAction[prop]
            }

            // Copy the references
            for (ref in references) {
                Map refMap = null
                if (apiAction[ref]) {
                    refMap = [
                            id: apiAction[ref].id,
                            name: apiAction[ref].name
                    ]
                }
                domainMap[ref] = refMap
            }

            if (apiAction.callbackMode) {
                domainMap["callbackMode"] = apiAction.callbackMode.name()
            }

            AbstractAgent agent = apiActionService.agentInstanceForAction(apiAction)
            domainMap["agentClass"] = [
                    id: apiAction.agentClass.name(),
                    name: agent? agent.name : null
            ]
        }

        return domainMap
    }

}
