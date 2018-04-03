package net.transitionmanager.service

import com.tdsops.common.builder.HttpRouteBuilder
import com.tdsops.common.lang.ExceptionUtil
import com.tdssrc.grails.JsonUtil
import com.tdssrc.grails.ThreadLocalUtil
import groovy.util.logging.Slf4j
import net.transitionmanager.asset.AssetFacade
import net.transitionmanager.command.FileCommand
import net.transitionmanager.command.UploadFileCommand
import net.transitionmanager.integration.*
import net.transitionmanager.task.TaskFacade
import org.apache.camel.Exchange
import org.apache.camel.Message
import org.apache.camel.ProducerTemplate
import org.apache.camel.model.RouteDefinition
import org.apache.camel.spring.SpringCamelContext
import org.apache.http.HttpHeaders
import org.codehaus.groovy.grails.plugins.testing.GrailsMockMultipartFile
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.multipart.MultipartFile

/**
 * Service class to execute Camel routes required by RESTful API Action
 */
@Slf4j
class HttpProducerService {
    private static final String DIRECT_RESTFUL_CALL = 'direct:RESTfulCall-'
    private static final String DIRECT_START = 'direct:start'
    private static final String CONTENT_DISPOSITION = 'Content-Disposition'

    @Autowired
    @Qualifier("camelContext")
    SpringCamelContext camelContext;

    HttpRouteBuilder httpRouteBuilder
    TaskService taskService
    ApiActionService apiActionService
    FileSystemService fileSystemService

    /**
     * Executes a Camel route for the given action request
     * @see <code>HttpAgent.executeCall()</code>
     *
     * @param actionRequest - the action request
     */
    void executeCall(ActionRequest actionRequest) {
        try {
            log.debug('RESTful executeCall action request: {}', actionRequest)
            RouteDefinition routeDefinition = httpRouteBuilder.getRouteDefinition(actionRequest)
            log.debug('RESTful route defined: {}', routeDefinition)
            camelContext.addRouteDefinition(routeDefinition)

            camelContext.setTracing(true)
            ProducerTemplate producerTemplate = camelContext.createProducerTemplate()
            producerTemplate.setDefaultEndpointUri(DIRECT_RESTFUL_CALL + actionRequest.options.actionId)
            producerTemplate.sendBody(DIRECT_START)
        } catch (Exception e) {
            log.error('Error when executing Camel route. ' + ExceptionUtil.stackTraceToString(e))
            Long taskId = actionRequest.options.taskId
            taskService.updateTaskStateByMessage([taskId: taskId, status: 'error', cause: e.message])
        }
    }

    /**
     * After a api action Camel route execution, the Camel context comes to this point to
     * explore and process the exchange accordingly
     *
     * @see <code>HttpRouteBuilder.buildRESTfulReactionEndpoint()</code> for details
     *
     * @param exchange
     */
    void reaction(Exchange exchange) {

        // retrieve from ThreadLocal and prepare objects needed to attend the reaction
        ActionRequest actionRequest = ThreadLocalUtil.getThreadVariable(ActionThreadLocalVariable.ACTION_REQUEST)
        TaskFacade taskFacade = ThreadLocalUtil.getThreadVariable(ActionThreadLocalVariable.TASK_FACADE)
        JSONObject reactionScripts = ThreadLocalUtil.getThreadVariable(ActionThreadLocalVariable.REACTION_SCRIPTS)
        AssetFacade assetFacade = ThreadLocalUtil.getThreadVariable(ActionThreadLocalVariable.ASSET_FACADE)
        ApiActionJob apiActionJob = new ApiActionJob()

        InputStream body = exchange.getIn().getBody(InputStream.class)
        String contentType = getContentType(exchange.getIn())
        log.debug('RESTful action result: {}', exchange)
        log.debug('RESTful action response body: {}', body)

        ApiActionResponse apiActionResponse = new ApiActionResponse()
        apiActionResponse.headers = exchange.getIn().getHeaders() as Map<String, String>
        apiActionResponse.status = apiActionResponse.getHeader(Exchange.HTTP_RESPONSE_CODE) as Integer
        apiActionResponse.elapsed = exchange.getIn().getHeader(HttpHeaders.AGE) as Integer

// TODO : JPM 3/2018 : The determinination of the success should be done by the reaction Status Determination script so we're hard coding true for the moment
// Perhaps we can set true as long as success is in 200's?

        apiActionResponse.successful = apiActionResponse.status in [
            ReactionHttpStatus.OK,
            ReactionHttpStatus.CREATED,
            ReactionHttpStatus.ACCEPTED,
            ReactionHttpStatus.AUTHORITATIVE_INFORMATION,
            ReactionHttpStatus.NO_CONTENT,
            ReactionHttpStatus.RESET_CONTENT,
            ReactionHttpStatus.PARTIAL_CONTENT
        ]
        log.debug 'reaction() reponse status={}, success={}', apiActionResponse.status, apiActionResponse.successful

        if (!apiActionResponse.successful) {
            apiActionResponse.error = body?.text
            apiActionResponse.data = null
        } else {
            if (actionRequest.options.producesData == 1) {
                // if the api action call produces data, it means that a file is being downloaded
                // and therefore it requires to be saved into the file system
                String filename = getFilename(exchange.getIn())
                apiActionResponse.filename = fileSystemService.transferFileToFileSystem(getFileCommandFromInputStream(body, filename, contentType))
                apiActionResponse.originalFilename = filename
            } else {
                // else, just store it in the action response for further handling
                apiActionResponse.data = getData(body, contentType)
            }
        }

        // reaction scripts
        String statusScript = reactionScripts[ReactionScriptCode.STATUS.name()]
        String errorScript = reactionScripts[ReactionScriptCode.ERROR.name()]
        String defaultScript = reactionScripts[ReactionScriptCode.DEFAULT.name()]
        String finalizeScript = reactionScripts[ReactionScriptCode.FINAL.name()]
        String successScript = reactionScripts[ReactionScriptCode.SUCCESS.name()]
        String failScript = reactionScripts[ReactionScriptCode.FAILED.name()]

        if (apiActionResponse.successful) {
            try {
                Map<String, ?> statusResult = apiActionService.invokeReactionScript(ReactionScriptCode.STATUS, statusScript, actionRequest, apiActionResponse, taskFacade, assetFacade, apiActionJob)
                log.debug('{} script execution result: {}', ReactionScriptCode.STATUS, statusResult.result)
                if (statusResult.result == ReactionScriptCode.SUCCESS) {
                    assetFacade.setReadonly(false)
                    try {
                        Map<String, ?> successResult = apiActionService.invokeReactionScript(ReactionScriptCode.SUCCESS, successScript, actionRequest, apiActionResponse, taskFacade, assetFacade, apiActionJob)
                        log.debug('{} script execution result: {}', ReactionScriptCode.SUCCESS, successResult.result)
                        if (successResult.result == ReactionScriptCode.ERROR) {
                            // execute ERROR or DEFAULT scripts if present
                            if (errorScript) {
                                try {
                                    apiActionService.invokeReactionScript(ReactionScriptCode.ERROR, errorScript, actionRequest, apiActionResponse, taskFacade, assetFacade, apiActionJob)
                                } catch (ApiActionException errorScriptException) {
                                    addTaskScriptInvocationError(taskFacade, ReactionScriptCode.ERROR, errorScriptException)
                                }
                            } else if (defaultScript) {
                                try {
                                    apiActionService.invokeReactionScript(ReactionScriptCode.DEFAULT, defaultScript, actionRequest, apiActionResponse, taskFacade, assetFacade, apiActionJob)
                                } catch (ApiActionException defaultScriptException) {
                                    addTaskScriptInvocationError(taskFacade, ReactionScriptCode.DEFAULT, defaultScriptException)
                                }
                            }
                        }
                    } catch (ApiActionException successScriptException) {
                        addTaskScriptInvocationError(taskFacade, ReactionScriptCode.SUCCESS, successScriptException)
                        // execute ERROR or DEFAULT scripts if present when SUCCESS script execution fails
                        if (errorScript) {
                            try {
                                apiActionService.invokeReactionScript(ReactionScriptCode.ERROR, errorScript, actionRequest, apiActionResponse, taskFacade, assetFacade, apiActionJob)
                            } catch (ApiActionException errorScriptException) {
                                addTaskScriptInvocationError(taskFacade, ReactionScriptCode.ERROR, errorScriptException)
                            }
                        } else if (defaultScript) {
                            try {
                                apiActionService.invokeReactionScript(ReactionScriptCode.DEFAULT, defaultScript, actionRequest, apiActionResponse, taskFacade, assetFacade, apiActionJob)
                            } catch (ApiActionException defaultScriptException) {
                                addTaskScriptInvocationError(taskFacade, ReactionScriptCode.DEFAULT, defaultScriptException)
                            }
                        }
                    }
                    assetFacade.setReadonly(true)
                } else {
                    assetFacade.setReadonly(false)
                    // execute ERROR or DEFAULT scripts if present when STATUS script execution returns ERROR
                    if (errorScript) {
                        try {
                            apiActionService.invokeReactionScript(ReactionScriptCode.ERROR, errorScript, actionRequest, apiActionResponse, taskFacade, assetFacade, apiActionJob)
                        } catch (ApiActionException errorScriptException) {
                            addTaskScriptInvocationError(taskFacade, ReactionScriptCode.ERROR, errorScriptException)
                        }
                    } else if (defaultScript) {
                        try {
                            apiActionService.invokeReactionScript(ReactionScriptCode.DEFAULT, defaultScript, actionRequest, apiActionResponse, taskFacade, assetFacade, apiActionJob)
                        } catch (ApiActionException defaultScriptException) {
                            addTaskScriptInvocationError(taskFacade, ReactionScriptCode.DEFAULT, defaultScriptException)
                        }
                    }
                    assetFacade.setReadonly(true)
                }
            } catch (ApiActionException statusScriptException) {
                addTaskScriptInvocationError(taskFacade, ReactionScriptCode.STATUS, statusScriptException)
                // execute ERROR or DEFAULT scripts if present when STATUS script execution returns ERROR
                if (errorScript) {
                    assetFacade.setReadonly(false)
                    try {
                        apiActionService.invokeReactionScript(ReactionScriptCode.ERROR, errorScript, actionRequest, apiActionResponse, taskFacade, assetFacade, apiActionJob)
                    } catch (ApiActionException errorScriptException) {
                        addTaskScriptInvocationError(taskFacade, ReactionScriptCode.ERROR, errorScriptException)
                    }
                    assetFacade.setReadonly(true)
                } else if (defaultScript) {
                    assetFacade.setReadonly(false)
                    try {
                        apiActionService.invokeReactionScript(ReactionScriptCode.DEFAULT, defaultScript, actionRequest, apiActionResponse, taskFacade, assetFacade, apiActionJob)
                    } catch (ApiActionException defaultScriptException) {
                        addTaskScriptInvocationError(taskFacade, ReactionScriptCode.DEFAULT, defaultScriptException)
                    }
                    assetFacade.setReadonly(true)
                }
            }
        } else {
            // execute FAILED or DEFAULT scripts if present when call to endpoint was not successful
            // Network error, Timeout, Bad credentials, etc...
            if (failScript) {
                try {
                    apiActionService.invokeReactionScript(ReactionScriptCode.FAILED, failScript, actionRequest, apiActionResponse, taskFacade, assetFacade, apiActionJob)
                } catch (ApiActionException failedScriptException) {
                    addTaskScriptInvocationError(taskFacade, ReactionScriptCode.FAILED, failedScriptException)
                }
            } else if (defaultScript) {
                assetFacade.setReadonly(false)
                try {
                    apiActionService.invokeReactionScript(ReactionScriptCode.DEFAULT, defaultScript, actionRequest, apiActionResponse, taskFacade, assetFacade, apiActionJob)
                } catch (ApiActionException defaultScriptException) {
                    addTaskScriptInvocationError(taskFacade, ReactionScriptCode.DEFAULT, defaultScriptException)
                }
                assetFacade.setReadonly(true)
            }
        }

        // finalize STATUS branch when it succeeded
        if (finalizeScript) {
            try {
                apiActionService.invokeReactionScript(ReactionScriptCode.FINAL, finalizeScript, actionRequest, apiActionResponse, taskFacade, assetFacade, apiActionJob)
            } catch (ApiActionException finalizeScriptException) {
                addTaskScriptInvocationError(taskFacade, ReactionScriptCode.FINAL, finalizeScriptException)
            }
        }

        ThreadLocalUtil.destroy(ApiActionService.THREAD_LOCAL_VARIABLES)
    }

    /**
     * Get the filename from the Content-Disposition header
     * @param message
     * @return
     */
    private String getFilename(Message message) {
        String disposition = (String) message.getHeader(CONTENT_DISPOSITION)
        int index = disposition.indexOf('filename=')
        if (index > 0) {
            return URLDecoder.decode(disposition.split('=')[1].trim().replaceAll('"',''), 'UTF-8')
        } else {
            return null
        }
    }

    /**
     * Gets the content type received by the exchange
     * @param message - the exchange message
     * @return
     */
    private String getContentType(Message message) {
        return message.getHeader(Exchange.CONTENT_TYPE)
    }

    /**
     * Gets the data received by the exchange
     * @param is - the input stream which is the exchange message body
     * @param contentType - the content type
     * @return
     */
    private Object getData(InputStream is, String contentType) {
        if (!is) {
            return null
        }

        if (contentType =~ /.*application\/json.*/) {
            JSONObject jsonObject = JsonUtil.parseFile(is)
            return jsonObject
        } else if (contentType =~ /.*text\/plain.*/) {
            return is.text
        } else {
            return is
        }
    }

    /**
     * Gets a file commands object from a given input stream
     * @param is - the input stream
     * @param filename - the original filename
     * @param contentType - the content type
     * @return
     */
    private FileCommand getFileCommandFromInputStream(InputStream is, String filename, String contentType) {
        MultipartFile multipartFile = new GrailsMockMultipartFile('file', filename, contentType, is)
        UploadFileCommand uploadFileCommand = new UploadFileCommand()
        uploadFileCommand.file = multipartFile
        return uploadFileCommand
    }

    /**
     * Add task error message through the task facade
     * @param taskFacade - the task facade wrapper
     * @param reactionScriptCode - the reaction script code generating the message
     * @param apiActionException - the API Exception
     */
    private void addTaskScriptInvocationError(TaskFacade taskFacade, ReactionScriptCode reactionScriptCode, ApiActionException apiActionException) {
        taskFacade.error(String.format('%s script failure: %s', reactionScriptCode, apiActionException.message))
    }
}
