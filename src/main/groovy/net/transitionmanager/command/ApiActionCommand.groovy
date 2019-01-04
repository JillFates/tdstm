package net.transitionmanager.command

import com.tdsops.tm.enums.domain.ApiActionHttpMethod
import net.transitionmanager.connector.CallbackMode
import net.transitionmanager.domain.ApiCatalog
import net.transitionmanager.domain.Credential
import net.transitionmanager.domain.DataScript
import net.transitionmanager.domain.Provider
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
}