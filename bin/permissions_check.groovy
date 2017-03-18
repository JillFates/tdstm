#!/usr/bin/env groovy
// usage:
//        groovy permissions_check.groovy <logLevel> <methodsFilePath> <permissionsFilePath> <username> <password> <role>
//
// https://mvnrepository.com/artifact/org.codehaus.groovy.modules.http-builder/http-builder
@GrabResolver(name = 'mvnrepository', root = 'https://mvnrepository.com/', m2Compatible = 'true')
@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.7.1')
@Grab(group = 'org.apache.httpcomponents', module = 'httpclient', version = '4.5.3')
@Grab(group = 'org.apache.httpcomponents', module = 'httpcore', version = '4.4.6')
@Grab(group='com.google.code.gson', module='gson', version='2.8.0')
@Grab(group='log4j', module='log4j', version='1.2.17')
@GrabExclude(group = 'commons-lang', module = 'commons-lang')
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseDecorator
import org.apache.http.ConnectionClosedException
import org.apache.log4j.ConsoleAppender
import org.apache.log4j.Level
import org.apache.log4j.PatternLayout

import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

import org.apache.http.HttpRequest
import org.apache.http.HttpResponse
import org.apache.http.impl.client.LaxRedirectStrategy
import org.apache.http.impl.client.DefaultRedirectStrategy
import org.apache.http.protocol.*

import com.google.gson.Gson
import groovy.util.logging.Log4j

@Log4j
class Main {
    private static final Gson gson = new Gson();
    private static enum ResponseCode { HTTP_CODE_NOT_FOUND, HTML_FOUND, JSON_FOUND, NO_RESPONSE_FOUND }

    def baseUrl
    def httpBuilder
    def cookies

    def methods
    def methodsExclude = [
            "/admin/restartAppServiceAction",
//            "/assetEntity/exportSpecialReport",
//            "/modelSyncBatch/list",
//            "/modelSyncBatch/process",
//            "/moveBundleAsset/list"
    ]

    def rolePermissions

    Main(Level logLevel) {
        // Programmatic log format configuration
        log.setLevel(logLevel ?: Level.INFO)
        log.setAdditivity(false)

        ConsoleAppender consoleAppender = new ConsoleAppender()
        consoleAppender.setWriter(new OutputStreamWriter(System.out));
        consoleAppender.setLayout(new PatternLayout("%d [%-5p] %m%n"))

        log.removeAllAppenders()
        log.addAppender(consoleAppender)

        baseUrl = "http://localhost:8080"
        httpBuilder = initializeHttpBuilder()
        cookies = []
        methods = [[:]]
        rolePermissions = [[:]]
    }

    def readMethodsCsvData(String path) {
        methods = []
        new File(path).splitEachLine(",") { fields ->
            methods.add(
                    [
                            controller: fields[0],
                            method    : fields[1],
                            annotation: fields[2],
                            httpMethod: GET  // defaulting to HTTP GET
                    ]
            )
        }
        // remove first line "headers"
        if (methods && methods.size() > 0) {
            methods = methods.drop(1)
        }
    }

    def readRolesPermissionsCsvData(String path) {
        rolePermissions = []
        new File(path).splitEachLine(",") { fields ->
            rolePermissions.add(
                    [
                            permission       : fields[0],
                            role_ADMIN       : fields[1].toBoolean(),
                            role_CLIENT_ADMIN: fields[2].toBoolean(),
                            role_CLIENT_MGR  : fields[3].toBoolean(),
                            role_SUPERVISOR  : fields[4].toBoolean(),
                            role_EDITOR      : fields[5].toBoolean(),
                            role_USER        : fields[6].toBoolean()
                    ]
            )
        }
        // remove first line "headers"
        if (rolePermissions && rolePermissions.size() > 0) {
            rolePermissions = rolePermissions.drop(1)
        }
    }

    def request(Method method, ContentType contentType, String url, Map<String, Serializable> params) {
        log.debug("Send ${method} request to ${this.baseUrl}${url}: ${params}")

        try {
            httpBuilder.request(method, contentType) { request ->
                uri.path = url
                if (method == POST) {
                    body = params
                } else {
                    uri.query = params
                }
                headers['User-Agent'] = 'TDSTM/4.1.0'
                headers['Accept'] = ANY
                headers['Cookie'] = cookies.join(';')
            }
        } catch (ConnectionClosedException e) {
            log.fatal(e.message + "in ${this.baseUrl}${url}:", e)
        }
    }

    def login(String username, String password) {
        cookies = []
        request(POST, URLENC, '/tdstm/auth/signIn', ["username": username, "password": password])
    }

    def performMethodSecurityCheck(String userRole) {
        methods.each { controllerMethod ->
            def uri = "/" + controllerMethod.controller + "/" + controllerMethod.method
            log.debug(uri)
            if (methodsExclude.contains(uri)) {
                log.info("Method excluded: ${uri}")
            } else {
                evalResult(controllerMethod as Map, userRole, request(controllerMethod.httpMethod, TEXT, "/tdstm" + uri, null))
            }
        }
    }

    def evalResult(Map controllerMethod, String userRole, String resp) {
        if (controllerMethod.annotation) {
            def permission = findPermissionByRole(controllerMethod.annotation)
            def uri = "/tdstm/" + controllerMethod.controller + "/" + controllerMethod.method
            if (permission) {
                log.debug("(Role: ${userRole}, Permission: ${permission.permission}) => Should be allowed: " + (permission["role_${userRole}"] == true))

                String responseErrorCode = findResponseErrorCode(resp)
                if (permission["role_${userRole}"] == true) {
                    if (isFailure(responseErrorCode)) {
                        log.error("Call allowed but: (Role: ${userRole}, Permission: ${permission.permission}) => " + uri + " - " + responseErrorCode)
                    }
                } else {
                    if (!isFailure(responseErrorCode)) {
                        log.error("Call should fail but: (Role: ${userRole}, Permission: ${permission.permission}) => " + uri + " - " + responseErrorCode)
                    }
                }
            } else {
                log.warn("Permission not found: ${controllerMethod.annotation} - " + uri)
            }
        } else {
            log.info("Method call does not have explicit permissions check: ${controllerMethod as String}")
        }
    }

    def findPermissionByRole(String permission) {
        return rolePermissions.findResult { it.permission == permission ? it : null }
    }

    def findResponseErrorCode(String resp) {
        if (resp) {
            def errorCodeRegexp = ~/<!-- HTTP_CODE=\d{3} -->/
            def errorCodeMatcher = resp =~ errorCodeRegexp
            if (errorCodeMatcher.getCount()) {
                return errorCodeMatcher[0]
            } else {
                def titleRegexp = ~/(<title>.*<\/title>|HTTP\sStatus\s\d{3})/
                def titleMatcher = resp =~ titleRegexp
                if (titleMatcher.getCount()) {
                    def htmlFound = titleMatcher.collect { it[0] }.join(" - ")
                    return "${ResponseCode.HTML_FOUND} - ${htmlFound}"
                } else {
                    if (isJSON(resp)) {
                        return ResponseCode.JSON_FOUND
                    } else {
                        return ResponseCode.HTTP_CODE_NOT_FOUND
                    }
                }
            }
        } else {
            return ResponseCode.NO_RESPONSE_FOUND
        }
    }

    boolean isFailure(String httpCode) {
        return httpCode =~ /(401|403|${ResponseCode.HTTP_CODE_NOT_FOUND})/
    }

    boolean isJSON(String jsonInString) {
        try {
            gson.fromJson(jsonInString, Object.class);
            return true;
        } catch(com.google.gson.JsonSyntaxException ex) {
            return false;
        }
    }

    def run(String methodsFilePath, String permissionsFilePath, String username, String password, String role) {
        readMethodsCsvData(methodsFilePath)
        readRolesPermissionsCsvData(permissionsFilePath)

        login(username, password)
        performMethodSecurityCheck(role.toUpperCase())
    }

    static void main(String... args) {
        if (args.size() < 6) {
            println("""
Usage: 
    logLevel            : INFO|ERROR|DEBUG|ALL
    methodsFilePath     : Controllers and Methods CSV file location
    permissionsFilePath : Permissions and Roles matrix CSV file location
    username            : Username desired to run the test
    password            : Username password
    role                : CLIENT_ADMIN|CLIENT_MGR|SUPERVISOR|EDITOR|USER 
                            
    groovy permissions_check.groovy <logLevel> <methodsFilePath> <permissionsFilePath> <username> <password> <role>
    
e.g. 
    groovy permissions_check.groovy INFO /tmp/MethodAnnotations.csv /tmp/RolePermissions.csv tdsadmin *SECRET* ADMIN
""")
        } else {
            new Main(Level.toLevel(args[0].toUpperCase())).run(args[1], args[2], args[3], args[4], args[5])
        }
    }

    private HTTPBuilder initializeHttpBuilder() {
        def httpBuilder = new HTTPBuilder(baseUrl)
        //httpBuilder.client.setRedirectStrategy(new LaxRedirectStrategy())
        httpBuilder.client.setRedirectStrategy(new DefaultRedirectStrategy() {
            @Override
            boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) {
                // do redirect
//                def redirected = super.isRedirected(request, response, context)
//                return redirected || response.getStatusLine().getStatusCode() == 302

                // prevent redirect
                return false
            }
        })

        httpBuilder.handler.success = { HttpResponseDecorator resp, InputStreamReader reader ->
            debugResponse(resp)

            resp.getHeaders('Set-Cookie').each {
                //[Set-Cookie: JSESSIONID=E68D4799D4D6282F0348FDB7E8B88AE9; Path=/tdstm/; HttpOnly]
                String cookie = it.value.split(';')[0]
                log.info("Adding cookie to collection: $cookie")
                cookies.add(cookie)
            }
            //log.trace("Response: ${reader}")
            //log.trace("Response Code: ${resp.statusLine}")
            return reader?.text
        }
        httpBuilder.handler.failure = { HttpResponseDecorator resp, InputStreamReader reader ->
            debugResponse(resp)
            //log.trace("Response: ${reader}")
            //log.trace("Response Code: ${resp.statusLine}")
            return reader?.text
        }
        return httpBuilder
    }

    def debugResponse(resp) {
        if (resp) {
            log.debug("Method call response: ${resp.statusLine}")
            def respHeadersString = 'Headers:';
            resp.headers.each() { header -> respHeadersString += "\n\t${header.name}=\"${header.value}\"" }
            log.debug(respHeadersString)
        }
    }
}
