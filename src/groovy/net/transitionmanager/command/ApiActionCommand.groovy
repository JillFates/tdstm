package net.transitionmanager.command

import grails.validation.Validateable
import net.transitionmanager.agent.AbstractAgent
import net.transitionmanager.agent.AgentClass
import net.transitionmanager.agent.CallbackMode
import net.transitionmanager.domain.ApiAction
import net.transitionmanager.domain.Credential
import net.transitionmanager.domain.DataScript
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import net.transitionmanager.i18n.Message
import net.transitionmanager.service.ApiActionService
import net.transitionmanager.service.ProviderService

/**
 * This Command Object has three functions:
 * 1) Validate the user's input to make sure it's correct.
 * 2) Populate an API Action (domain object)
 * 3) Return the 'Map' representation of an API Action (static method).
 *
 * If you need to add a simple property -- string, number, boolean, etc -- that doesn't require any
 * special validation, you can add it to the 'standardProperties' list. Members of this list will be
 * automatically validated against its matching field in the ApiAction domain class. This command object will
 * also take care of adding this property when populating a domain object and converting the instance to a map.
 * Example: agentMethod.
 *
 * If you need to add a 'domainId' -- a reference to another domain class -- you'll have to provide a custom
 * validator. If you want this command object to automatically populate the domain object with the corresponding
 * reference and have a map [id, name] included when transforming the API Action to a map, you'll need to:
 * - Declare and add a 'domain' property to the 'references' list.
 * - Make this 'domain' property nullable in the constraints (even if it isn't in the domain). This is to
 * prevent any inconsistency when validating and handling errors.
 * - Assign a value to 'domain' at some point. You'll want to do this when validating 'domainId' since you'll
 * most likely be fetching the domain instance to check it exists.
 * Example: providerId
 *
 * If you want to add an enum (required or not) you'll have to provider a custom validator and, if you want it
 * to be automatically included when populating the ApiAction instance and transforming instances to a map,
 * you need to:
 * - Add the property to the 'enums' list.
 * - Create a second property with the same name plus 'Enum' with the corresponding type.
 * - Make this second property nullable in the constraints.
 * - Provide the 'Enum' property a value at some point (most likely during your custom validator).
 * Example: callbackMode
 *
 * Typical scenarios that don't follow any of the previous:
 * - name -> a simple string property but it requires specific validations not provided in the domain class.
 * - agentClass -> when converting an API Action to a map this field is not included as any other property is.
 * It needs a map instead.
 *
 */
@Validateable
class ApiActionCommand{

    static ApiActionService apiActionService
    static ProviderService providerService

    Long id

    String agentClass
    String agentMethod
    String asyncQueue
    String callbackMethod
    String callbackMode
    Long credentialId
    Long defaultDataScriptId
    String description
    String endpointPath
    String endpointUrl
    String methodParams
    String name
    Integer pollingFrequency
    Integer pollingInterval
    Integer pollingLapsedAfter
    Integer pollingStalledAfter
    Integer producesData
    Long providerId
    String reactionJson
    Integer timeout
    Integer useWithAsset
    Integer useWithTask

    // List of auxiliary properties.
    AgentClass agentClassEnum
    CallbackMode callbackModeEnum
    Credential credential
    DataScript defaultDataScript
    Project project
    Provider provider

    /**
     * This is a list of those standards properties which can be
     * automatically mapped to and from a domain object.
     */
    static final List<String> standardProperties = [
            'agentMethod',
            'asyncQueue',
            'description',
            'endpointPath',
            'endpointUrl',
            'methodParams',
            'pollingFrequency',
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
     * References to other domain classes. These fields need to be given a value before
     * populating domain objects.
     */
    static final List<String> references = [
            "provider",
            "defaultDataScript",
            "credential",
    ]

    /**
     * List of simple enum properties -- those that don't require any special treatment
     * when populating a domain object or converting to a map.
     */
    static final List<String> enums = [
            "callbackMode",
    ]

    /**
     * Constrains for validating the different properties. The standard properties
     * will be validated using against their constraints in the domain class.
     * Enums and other references have a custom validator here.
     */
    static constraints = {

        id nullable: true, validator: { id ->
            if (id && !apiActionService.find(id)) {
                return Message.ApiActionInvalidId
            }

        }

        importFrom(ApiAction, include: standardProperties)

        // The name cannot be empty and must be unique in the project.
        name nullable: false, validator: { name, cmd->
            if (!apiActionService.validateApiActionName(cmd.project, name, cmd.id)) {
                return Message.ApiActionInvalidName
            }
        }

        // The provider can't be null and has to be associated with the user's current project.
       providerId validator: { providerId, cmd ->
            cmd.provider = providerService.getProvider(providerId, cmd.project)
            if (!cmd.provider) {
                return Message.ApiActionInvalidProvider
            }
        }

        // The credential is optional but has to have the same provider as the current API Action.
        credentialId nullable: true, validator: { credentialId, cmd ->
            if (credentialId) {
                if (cmd.provider) {
                    cmd.credential = apiActionService.getCredential(cmd.project, cmd.provider, credentialId)
                    if (!cmd.credential) {
                        return Message.ApiActionInvalidCredential
                    }
                } else {
                    return Message.ApiActionInvalidProviderPreventsCredentialValidation
                }

            }
        }

        // The default datascript is optional but its provider must match the API Action's.
        defaultDataScriptId nullable: true, validator: { defaultDataScriptId, cmd ->

            if (defaultDataScriptId) {
                if (cmd.provider) {
                    cmd.defaultDataScript = apiActionService.getDataScript(cmd.project, cmd.provider, cmd.defaultDataScriptId)
                    if (!cmd.defaultDataScript) {
                        return Message.ApiActionInvalidDefaultDataScript
                    }
                } else {
                    return Message.ApiActionInvalidProviderPreventsDataScriptValidation
                }
            }

        }

        // The agent class is mandatory and has to be a valid value.
        agentClass nullable: false, validator: { agentClass, cmd ->
            cmd.agentClassEnum = apiActionService.parseEnum(AgentClass, "agentClass", agentClass, true)
            if (!cmd.agentClassEnum) {
                return Message.ApiActionInvalidAgentClass
            }
        }

        // The callback mode is optional but it has to be a valid enum value.
        callbackMode nullable: true, validator: { callbackMode, cmd ->
            if (callbackMode) {
                cmd.callbackModeEnum = apiActionService.parseEnum(CallbackMode, "callbackMode", callbackMode, true,)
                if (!cmd.callbackModeEnum) {
                    return Message.ApiActionInvalidCallbackMode
                }
            }
        }

        // Auxiliary properties need to be nullable to avoid inconsistencies when handling errors.
        callbackModeEnum nullable: true
        agentClassEnum nullable: true
        credential nullable: true
        defaultDataScript nullable: true
        provider nullable: true

    }

    /**
     * Populate an API Action with the properties in this command object.
     * For safety, this method won't set the id nor the project.
     *
     * @param apiAction - the target ApiAction instance.
     */
    void populateDomain(ApiAction apiAction) {
        if (apiAction) {

            // Set those fields that are not automatically handled.
            apiAction.name = name
            apiAction.agentClass = agentClassEnum

            // Copy standard properties
            for (prop in standardProperties) {
                if (apiAction[prop] != this[prop]) {
                    apiAction[prop] = this[prop]
                }
            }

            // Copy the references
            for (ref in references) {
                if (apiAction[ref] != this[ref]) {
                    apiAction[ref] = this[ref]
                }
            }

            // Copy the enums
            for (enumProp in enums) {
                if (apiAction[enumProp] != this["${enumProp}Enum"]) {
                    apiAction[enumProp] = this["${enumProp}Enum"]
                }
            }
        }
    }

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
                if (apiAction[ref]) {
                    domainMap[ref] = [
                            id: apiAction[ref].id,
                            name: apiAction[ref].name
                    ]
                }
            }

            // Copy enums
            for (enumProp in enums) {
                String enumValue = null
                if (apiAction[enumProp]) {
                    enumValue = apiAction[enumProp].name()
                }
                domainMap[enumProp] = enumValue
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
