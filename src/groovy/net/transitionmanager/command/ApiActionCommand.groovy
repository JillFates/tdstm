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


}
