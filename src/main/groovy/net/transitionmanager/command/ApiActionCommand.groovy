package net.transitionmanager.command

import com.tdsops.tm.enums.domain.ActionType
import com.tdsops.tm.enums.domain.ApiActionHttpMethod
import com.tdsops.tm.enums.domain.RemoteCredentialMethod
import net.transitionmanager.connector.CallbackMode
import net.transitionmanager.action.ApiCatalog
import net.transitionmanager.action.Credential
import net.transitionmanager.imports.DataScript
import net.transitionmanager.action.Provider
/**
* Command Object for handling API Action endpoints
*/

class ApiActionCommand implements CommandObject {
    String name
    String description=''
    Provider provider
    Credential credential
    ApiCatalog apiCatalog
    String connectorMethod
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
    ActionType actionType = ActionType.WEB_API
    Boolean isRemote = false
    String script
    String commandLine
    RemoteCredentialMethod remoteCredentialMethod
}