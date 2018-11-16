package net.transitionmanager.service

import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.tm.enums.domain.ApiActionHttpMethod
import com.tdsops.tm.enums.domain.AuthenticationMethod
import com.tdsops.tm.enums.domain.CredentialEnvironment
import com.tdsops.tm.enums.domain.CredentialStatus
import com.tdssrc.grails.FileSystemUtil
import com.tdssrc.grails.JsonUtil
import com.tdssrc.grails.ThreadLocalUtil
import com.tdssrc.grails.UrlUtil
import com.tdssrc.grails.XmlUtil
import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import net.transitionmanager.asset.AssetFacade
import net.transitionmanager.domain.Credential
import net.transitionmanager.integration.*
import net.transitionmanager.task.TaskFacade
import org.apache.commons.io.IOUtils
import org.apache.http.Consts
import org.apache.http.Header
import org.apache.http.HeaderIterator
import org.apache.http.HttpRequest
import org.apache.http.HttpResponse
import org.apache.http.HttpVersion
import org.apache.http.NameValuePair
import org.apache.http.NoHttpResponseException
import org.apache.http.auth.Credentials
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPatch
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.apache.http.client.methods.HttpRequestBase
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.client.utils.URIBuilder
import org.apache.http.config.Registry
import org.apache.http.config.RegistryBuilder
import org.apache.http.conn.HttpClientConnectionManager
import org.apache.http.conn.socket.ConnectionSocketFactory
import org.apache.http.conn.socket.PlainConnectionSocketFactory
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.entity.InputStreamEntity
import org.apache.http.entity.StringEntity
import org.apache.http.impl.auth.BasicScheme
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.conn.BasicHttpClientConnectionManager
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType

import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.nio.charset.Charset
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.regex.Pattern

/**
 * Service class to execute HTTP requests used by ApiActions for Data Ingestion
 */
@Slf4j
class HttpProducerService {
    private static final String FILENAME_PREFIX = 'download-'
    private static final String HTTP_METHOD = 'HttpMethod'
//    private static final String VALID_HTTP_METHODS = /^(GET|POST|PUT|PATCH|DELETE|OPTIONS|HEAD)$/
//    private static final String DEFAULT_HTTP_METHOD = HttpMethod.GET.name()
    private static final String DEFAULT_CONTENT_TYPE_HEADER = MediaType.APPLICATION_JSON_VALUE
    private static final String DEFAULT_ACCEPT_HEADER = MediaType.APPLICATION_JSON_VALUE
    private static final int DEFAULT_REQUEST_TIMEOUT = -1
    private static final Charset DEFAULT_CHARSET = Consts.UTF_8
    private static final String RESPONSE_TEMPORARY_FILENAME_PREFIEX = 'APIActionResponse'
    private static final String DEFAULT_TDSTM_TMP_RESPONSE_FILENAME_HEADER  = 'X-TDSTM-TMP-RESPONSE-FILENAME'
    private static final Pattern JSON = ~/.*${MediaType.APPLICATION_JSON_VALUE}.*/
    private static final Pattern XML = ~/.*${MediaType.APPLICATION_XML_VALUE}.*/
    private static final Pattern FORM_URLENCODED = ~/.*${MediaType.APPLICATION_FORM_URLENCODED_VALUE}.*/
    private static final Pattern TXT = ~/.*${MediaType.TEXT_PLAIN_VALUE}.*/
    private static final Pattern CSV = ~/.*application\/csv.*/
    private static final Pattern TXT_CSV = ~/.*text\/csv.*/
    private static final Pattern XLS = ~/.*application\/vnd.ms-excel.*/
    private static final Pattern XLSX = ~/.*application\/vnd.openxmlformats-officedocument.spreadsheetml.sheet.*/
    private static final String HTTP_ERROR_DNS_NOT_FOUND = 'Unable to resolve host name (%s)'
    private static final String HTTP_ERROR_NO_RESPONSE = 'Unable to connect to endpoint'
    private static final String HTTP_ERROR_400 = 'Failure due to bad request (Action improperly configured) (400)'
    private static final String HTTP_ERROR_401 = 'Failure due to invalid credentials (401)'
    private static final String HTTP_ERROR_403 = 'Failure due to credentials lacking necessary permission (403)'
    private static final String HTTP_ERROR_404 = 'Failure because endpoint was not found (action URL wrong or method type) (404)'
    private static final String HTTP_ERROR_405 = 'Failure due to incorrect Method type (incorrect Action HTTP method) (405)'
    private static final String HTTP_ERROR_406_499 = 'Endpoint reported error code 4xx'
    private static final String HTTP_ERROR_500 = 'Endpoint reported Internal Server Error (500)'
    private static final String HTTP_ERROR_501 = 'Endpoint reported method is not implemented (501)'
    private static final String HTTP_ERROR_502 = 'Endpoint reported Bad Gateway (502)'
    private static final String HTTP_ERROR_503 = 'Endpoint reported the Service Unavailable (503)'
    private static final String HTTP_ERROR_504_599 = 'Endpoint reported error code 5xx'
    private static final String HTTP_ERROR_UNKNOWN = 'Unkown error code received from endpoint. (%s)'

    TaskService taskService
    ApiActionService apiActionService
    FileSystemService fileSystemService
    CredentialService credentialService

    /**
     * Executes a HTTP request for the given action request
     * @see <code>HttpAgent.executeCall()</code>
     *
     * @param actionRequest - the action request
     * @return the ApiActionResponse
     */
    ApiActionResponse executeCall(ActionRequest actionRequest) {
        try {
            return reaction(invokeActionRequest(actionRequest))
        } catch (Exception e) {
            log.error('Error when executing HTTP request. {}', ExceptionUtil.stackTraceToString(e))
            String errorMessage = translateHttpException(e)

            // this is used to notify tasks when something went wrong by adding a task note
            if (actionRequest.options.hasProperty('taskId')) {
                Long taskId = actionRequest.options.taskId
                taskService.updateTaskStateByMessage([taskId: taskId, status: 'error', cause: errorMessage])
            }
            ApiActionResponse errorApiActionResponse = new ApiActionResponse()
            errorApiActionResponse.error = errorMessage
            errorApiActionResponse.successful = false
            return errorApiActionResponse.asImmutable()
        }
    }

    /**
     * Process HttpResponse and produces an ApiActionResponse
     * explore and process accordingly
     *
     * @param httpResponse - the HttpResponse produced by the HTTPClient
     * @return the ApiActionResponse as immutable
     */
    ApiActionResponse reaction(HttpResponse httpResponse) {

        if (!httpResponse) {
            throw new RuntimeException('Server response is null, please contact system administrator for support.')
        }

        // retrieve from ThreadLocal and prepare objects needed to attend the reaction
        ActionRequest actionRequest = ThreadLocalUtil.getThreadVariable(ActionThreadLocalVariable.ACTION_REQUEST)
        TaskFacade taskFacade = ThreadLocalUtil.getThreadVariable(ActionThreadLocalVariable.TASK_FACADE)
        JSONObject reactionScripts = ThreadLocalUtil.getThreadVariable(ActionThreadLocalVariable.REACTION_SCRIPTS)
        AssetFacade assetFacade = ThreadLocalUtil.getThreadVariable(ActionThreadLocalVariable.ASSET_FACADE)
        ApiActionJob apiActionJob = new ApiActionJob()

        // obtain content type from httpResponse headers
        String contentType = httpResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE)?.value as String

        // obtain response temporary filename from httpResponse headers
        String tmpResponseFilename = httpResponse.getFirstHeader(DEFAULT_TDSTM_TMP_RESPONSE_FILENAME_HEADER)?.value as String

        // constructs ApiActionResponse object
        ApiActionResponse apiActionResponse = new ApiActionResponse()

        // pull headers from HttpResponse and populate ApiActionResponse headers
        HeaderIterator headerIterator = httpResponse.headerIterator()
        while (headerIterator.hasNext()) {
            Header header = headerIterator.nextHeader()
            apiActionResponse.headers.put(header.name, header.value)
        }

        apiActionResponse.status = httpResponse.getStatusLine().getStatusCode()
        apiActionResponse.elapsed = httpResponse.getFirstHeader(HttpHeaders.AGE)?.value as Integer

        apiActionResponse.successful = apiActionResponse.status in [
                ReactionHttpStatus.OK,
                ReactionHttpStatus.CREATED,
                ReactionHttpStatus.ACCEPTED,
                ReactionHttpStatus.AUTHORITATIVE_INFORMATION,
                ReactionHttpStatus.NO_CONTENT,
                ReactionHttpStatus.RESET_CONTENT,
                ReactionHttpStatus.PARTIAL_CONTENT
        ]
        log.info('Action response status line: {}, success: {}', httpResponse?.statusLine, apiActionResponse.successful)
        log.info('Action response headers: {}', apiActionResponse?.headers)

        if (!apiActionResponse.successful) {
            apiActionResponse.error = getHttpResponseError(apiActionResponse.status)
            apiActionResponse.data = null
        } else {
            InputStream is = httpResponse?.entity?.content
            if (actionRequest.options.producesData == 1) {
                // if the api action call produces data, it means that a file is being downloaded
                // and therefore it requires to be saved into the file system
                String originalFilename = getFilenameFromHeaders(apiActionResponse.headers, contentType)
                String targetFileExtension = FileSystemUtil.getFileExtension(originalFilename)
                String targetFilename = fileSystemService.getUniqueFilename('', '', targetFileExtension)
                fileSystemService.renameTemporaryFile(tmpResponseFilename, targetFilename)
                apiActionResponse.filename = targetFilename
                apiActionResponse.originalFilename = originalFilename
            } else {
                // else, just store it in the action response for further handling
                apiActionResponse.data = getData(is, contentType)
            }
        }

        // if there is a task facade and reaction scripts, process and execute them accordingly
        if (taskFacade && reactionScripts) {
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
        } // end if (taskFacade && reactionScripts)

        ThreadLocalUtil.destroy(ApiActionService.THREAD_LOCAL_VARIABLES)
        return apiActionResponse.asImmutable()
    }

    /**
     * Translate a Http exception to a user readable message
     * @param e - the Exception
     * @return a String containing a user readable message with the http exception found
     */
    private String translateHttpException(Exception e) {
        if (e instanceof UnknownHostException) {
            String unknownHost = e.message.substring(0, e.message.indexOf(':'))
            return String.format(HTTP_ERROR_DNS_NOT_FOUND, unknownHost)
        } else if (e instanceof NoHttpResponseException) {
            return HTTP_ERROR_NO_RESPONSE
        } else {
            return e.message
        }
    }

    /**
     * Get the user readable http response error corresponding to the http status code
     * @param httpResponse - the HttpResponse
     * @return a String containing a user readable message with the http status code found
     */
    String getHttpResponseError(int statusCode) {
        if (statusCode <= 0) {
            return HTTP_ERROR_NO_RESPONSE
        }

        switch (statusCode) {
            case 400:
                return HTTP_ERROR_400
                break
            case 401:
                return HTTP_ERROR_401
                break
            case 403:
                return HTTP_ERROR_403
                break
            case 404:
                return HTTP_ERROR_404
                break
            case 405:
                return HTTP_ERROR_405
                break
            case 406..499:
                return HTTP_ERROR_406_499
                break
            case 500:
                return HTTP_ERROR_500
                break
            case 501:
                return HTTP_ERROR_501
                break
            case 502:
                return HTTP_ERROR_502
                break
            case 503:
                return HTTP_ERROR_503
                break
            case 504..599:
                return HTTP_ERROR_504_599
                break
            default:
                return String.format(HTTP_ERROR_UNKNOWN, statusCode)
        }
    }

    /**
     * Get the filename from the Content-Disposition header
     * @param message
     * @return
     */
    private String getFilenameFromHeaders(LinkedHashMap<String, String> headers, String contentType) {
        String disposition = headers.get(HttpHeaders.CONTENT_DISPOSITION)
		Integer index = disposition?.indexOf('filename=')
		if (index && index > 0) {
            return URLDecoder.decode(disposition.split('=')[1].trim().replaceAll('"',''), 'UTF-8')
        } else {
            // when there is no Content-Disposition header then, creates a random filename
			return FILENAME_PREFIX + UUID.randomUUID().toString() + '.' + getFileExtensionForContentType(contentType)
        }
    }

	/**
	 * Gets file extension from content type
	 * @param contentType - produced data content type, this is taken from the Content-Type HTTP header
	 * @return
	 */
	// TODO : SL - 04/2018 : Move to FileSystemService or util class
	private String getFileExtensionForContentType(String contentType) {
		if (contentType =~ JSON) {
			return 'json'
		} else if (contentType =~ CSV) {
			return 'csv'
		} else if (contentType =~ TXT) {
			return 'txt'
		} else if (contentType =~ TXT_CSV) {
			return 'csv'
		} else if (contentType =~ XML) {
			return 'xml'
		} else if (contentType =~ XLS) {
			return 'xls'
		} else if (contentType =~ XLSX) {
			return 'xlsx'
		} else {
			return 'bin'
		}
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

        if (contentType =~ JSON) {
            return JsonUtil.parseFile(is)
        } else if (contentType =~ TXT) {
            return IOUtils.toString(is)
        } else if (contentType =~ XML) {
            return new XmlSlurper().parseText(IOUtils.toString(is))
        } else {
            return is
        }
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

    /**
     * Prepare action request with provided headers if present or default ones, then call invoke http call
     *
     * @param actionRequest
     * @return
     */
    @Transactional(noRollbackFor = [RuntimeException])
    private HttpResponse invokeActionRequest(ActionRequest actionRequest) {
        // set http method from the ApiAction
        String method = actionRequest.options.apiAction.httpMethod

        // override http method if provided in the pre script
        if (actionRequest.config.hasProperty(HTTP_METHOD)) {
            String httpMethod = actionRequest.config.getProperty(HTTP_METHOD)
            if (ApiActionHttpMethod.isValidHttpMethod(httpMethod)) {
                method = actionRequest.config.getProperty(HTTP_METHOD)
            } else {
                throw new RuntimeException("Invalid HTTPMethod ${httpMethod} has not been provided in the PRE script")
            }
        }

        // set http accept header if provided, else, default
        String accept = DEFAULT_ACCEPT_HEADER
        if (actionRequest.config.hasProperty(HttpHeaders.ACCEPT)) {
            accept = actionRequest.config.getProperty(HttpHeaders.ACCEPT)
        }

        // set http content-type header if provided, else, default
        String contentType = DEFAULT_CONTENT_TYPE_HEADER
        if (actionRequest.config.hasProperty(HttpHeaders.CONTENT_TYPE)) {
            contentType = actionRequest.config.getProperty(HttpHeaders.CONTENT_TYPE)
        }

        // set default action request headers
        actionRequest.headers.add(HTTP_METHOD, method)
        actionRequest.headers.add(HttpHeaders.ACCEPT, accept)
        actionRequest.headers.add(HttpHeaders.CONTENT_TYPE, contentType)

        return executeHttpCall(actionRequest)
    }

    /**
     * Prepare a HttClient to invoke an action request. It creates the http client, set credentials if required
     * invokes action request and return the HttpResponse
     *
     * @param actionRequest
     * @return
     */
    @Transactional(noRollbackFor = [RuntimeException])
    private HttpResponse executeHttpCall(ActionRequest actionRequest) {
        RequestConfig.Builder requestBuilder = RequestConfig.custom().setConnectTimeout(DEFAULT_REQUEST_TIMEOUT)
                .setConnectionRequestTimeout(DEFAULT_REQUEST_TIMEOUT)
                .setSocketTimeout(DEFAULT_REQUEST_TIMEOUT);

        ActionHttpRequestElements httpElements = new ActionHttpRequestElements(actionRequest)
        HttpMethod httpMethod = HttpMethod.valueOf(actionRequest.headers.get(HTTP_METHOD))
        URIBuilder builder = new URIBuilder(httpElements.uri(httpMethod))
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create().setDefaultRequestConfig(requestBuilder.build())

        // only provides a trust store if endpoint url is secure and credentials are nor for production
        if (UrlUtil.isSecure(actionRequest.options.apiAction.endpointUrl)) {
            if (actionRequest.options.credentials && actionRequest.options.credentials.environment != CredentialEnvironment.PRODUCTION.name()) {
                configureTrustStore(httpClientBuilder)
            }
        }

        CloseableHttpClient httpClient = httpClientBuilder.build()
        URL url = builder.build().toURL()

        // HttpEntityEnclosingRequestBase baseRequest = null // this is used when sending a body to the endpoint as part of the request
        HttpRequest baseRequest = null
        switch (httpMethod) {
            case HttpMethod.GET:
                baseRequest = new HttpGet(url.toURI())
                baseRequest.setProtocolVersion(HttpVersion.HTTP_1_1);
                break;
            case HttpMethod.POST:
                baseRequest = new HttpPost(url.toURI())
                baseRequest.setProtocolVersion(HttpVersion.HTTP_1_1);
                break;
            case HttpMethod.PUT:
                baseRequest = new HttpPut(url.toURI());
                baseRequest.setProtocolVersion(HttpVersion.HTTP_1_1);
                break;
            case HttpMethod.DELETE:
                baseRequest = new HttpDelete(url.toURI());
                baseRequest.setProtocolVersion(HttpVersion.HTTP_1_1);
                break;
            case HttpMethod.PATCH:
                baseRequest = new HttpPatch(url.toURI());
                baseRequest.setProtocolVersion(HttpVersion.HTTP_1_1);
                break;
            default:
                throw new InvalidParamException('method not supported')
                break;
        }

        // perform and set authentication if present
        if (actionRequest.options.credentials) {
            if (actionRequest.options.credentials.status == CredentialStatus.INACTIVE.name()) {
                throw new InvalidRequestException("The Credential associated with API Action is disabled")
            }

            // fetch a fresh copy of the credentials to have access to password and salt when needed
            Credential credential = credentialService.findById(actionRequest.options.credentials.id)
            switch (credential.authenticationMethod) {
                case AuthenticationMethod.BASIC_AUTH:
                    Credentials credentials = new UsernamePasswordCredentials(credential.username, credentialService.decryptPassword(credential))
                    BasicScheme basicScheme = new BasicScheme(DEFAULT_CHARSET)
                    baseRequest.addHeader(basicScheme.authenticate(credentials, baseRequest, HttpClientContext.create()))
                    break
                case AuthenticationMethod.HEADER:
                    // TODO : SL - 04/2018 : need to find a way to determine when to pass "Bearer" as part of the header value
                    // e.g. Authentication: Bearer VERTIFRyYW5zaXRpb24gTWFuYWdlcg==
                    // Ticket added for this TM-9868
                    Map<String, ?> authentication = credentialService.authenticate(credential)
                    baseRequest.addHeader(authentication.sessionName as String, authentication.sessionValue as String)
                    break
                case AuthenticationMethod.COOKIE:
                    Map<String, ?> authentication = credentialService.authenticate(credential)
                    baseRequest.addHeader(HttpHeaders.COOKIE, authentication.sessionName + '=' + authentication.sessionValue)
                    break
                default:
                    throw new RuntimeException("Authentication method ${credential.authenticationMethod} has not been implemented in HttpRouteBuilder")
            }
        }

        // setting HTTP headers
        actionRequest.headers.headersAsMap.each { k, v ->
            baseRequest.addHeader(k, v)
        }

        // setting HTTP request body and its type
        if (httpElements.extraParams && baseRequest instanceof HttpEntityEnclosingRequestBase) {
            String contentType = actionRequest.headers.get(HttpHeaders.CONTENT_TYPE)

            if (contentType =~ JSON) {
                // construct a JSON string entity
                StringEntity input = new StringEntity(JsonUtil.convertMapToJsonString(httpElements.extraParams))
                input.setContentType(contentType)
                ((HttpEntityEnclosingRequestBase)baseRequest).setEntity(input)
            } else if (contentType =~ FORM_URLENCODED) {
                // construct the list of name value pairs
                List<NameValuePair> nvpList = new ArrayList<>(httpElements.extraParams.size());
                for (Map.Entry<String, String> entry : httpElements.extraParams.entrySet()) {
                    nvpList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
                // construct a URL encoded form entity
                UrlEncodedFormEntity input = UrlEncodedFormEntity(nvpList, DEFAULT_CHARSET.toString())
                input.setContentType(contentType)
                ((HttpEntityEnclosingRequestBase)baseRequest).setEntity(input)
            } else if (contentType =~ XML) {
                // construct a XML string entity
                StringEntity input = new StringEntity(XmlUtil.convertMapToXmlString(httpElements.extraParams))
                input.setContentType(contentType)
                ((HttpEntityEnclosingRequestBase)baseRequest).setEntity(input)
            } else {
                throw new RuntimeException('Content type not supported: ' + contentType)
            }
        }

        // execute http method
        CloseableHttpResponse closeableHttpResponse = null
        if (baseRequest instanceof HttpEntityEnclosingRequestBase) {
            closeableHttpResponse = httpClient.execute((HttpEntityEnclosingRequestBase)baseRequest)
        } else {
            closeableHttpResponse = httpClient.execute((HttpRequestBase)baseRequest)
        }
        log.debug('HTTP response status line: {}', closeableHttpResponse.getStatusLine())

        // create a temporary file with the http response input stream
        def (String tmpFilename, OutputStream os) = fileSystemService.createTemporaryFile(RESPONSE_TEMPORARY_FILENAME_PREFIEX)

        // check whether http response has content
        // this case is valid when Http STATUS code is 204 (No Content), so the verification is to prevent
        // copying an null InputStream into a OutputStream which throws an error
        if (closeableHttpResponse?.entity?.content != null) {
            IOUtils.copy(closeableHttpResponse?.entity?.content, os)
        }
        os.flush()
        os.close()

        // update httpResponse, set httpEntity with the temporary file input stream
        EntityUtils.updateEntity(closeableHttpResponse, new InputStreamEntity(fileSystemService.openTemporaryFile(tmpFilename)))

        // additional header added to set the response temporary file created
        closeableHttpResponse.addHeader(DEFAULT_TDSTM_TMP_RESPONSE_FILENAME_HEADER, tmpFilename)

        //  return HTTP response
        return closeableHttpResponse
    }

    /**
     * Configures a trust store for non-production environments.
     * Useful when calling endpoints using SSL with self-signed certificates, so we trust them
     * to avoid hand shaking errors.
     *
     * @param httpClientBuilder
     */
    private void configureTrustStore(HttpClientBuilder httpClientBuilder) {
        TrustManager[] trustAllCerts = [
                new X509TrustManager() {
                    X509Certificate[] getAcceptedIssuers() { return [] }

                    void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

                    void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
                }
        ] as TrustManager[]

        SSLContext sslContext = SSLContext.getInstance('TLS')
        sslContext.init(null, trustAllCerts, new SecureRandom())
        SSLConnectionSocketFactory connectionSocketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE)

        final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create()
                .register('http', PlainConnectionSocketFactory.getSocketFactory())
                .register('https', connectionSocketFactory)
                .build();

        HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry)
        httpClientBuilder.setSSLContext(sslContext)
        httpClientBuilder.setSSLSocketFactory(connectionSocketFactory)
        httpClientBuilder.setConnectionManager(ccm)
    }
}
