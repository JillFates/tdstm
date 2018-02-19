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

import net.transitionmanager.i18n.Message

/**
 * Command Object for handling API Action endpoints.
 *
 */
@Validateable
class ApiActionCommand {

    String name
    String description=''
    Provider provider
    Credential credential
    AgentClass agentClass
    String agentMethod
    String asyncQueue
    String callbackMethod
    CallbackMode callbackMode
    DataScript defaultDataScript
    String endpointPath
    String endpointUrl
    Integer isPolling = 0
    String methodParams
    Integer pollingInterval = 0
    Integer pollingLapsedAfter = 0
    Integer pollingStalledAfter = 0
    Integer producesData
    String reactionScripts
    Integer timeout
    Integer useWithAsset = 0
    Integer useWithTask = 0
    Integer version=0

    static constraints = {
        callbackMethod nullable: true
        callbackMode nullable: true
        credential nullable: true
        defaultDataScript nullable:true
        version nullable:true
    }

}