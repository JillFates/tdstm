package net.transitionmanager.command

import com.tdsops.tm.enums.domain.ApiActionHttpMethod
import grails.validation.Validateable
import net.transitionmanager.agent.AgentClass
import net.transitionmanager.agent.CallbackMode
import net.transitionmanager.domain.Credential
import net.transitionmanager.domain.DataScript
import net.transitionmanager.domain.Provider

/**
* Command Object for handling API Action endpoints
*/
@Validateable
class ApiActionCommand implements CommandObject {
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
    String methodParams
    String reactionScripts
    ApiActionHttpMethod httpMethod
    String endpointUrl
    String docUrl
    Integer isPolling = 0
    Integer pollingInterval = 0
    Integer pollingLapsedAfter = 0
    Integer pollingStalledAfter = 0
    Integer producesData = 0
    Integer timeout = 0
    Integer useWithAsset = 0
    Integer useWithTask = 0
}