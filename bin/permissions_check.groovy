#!/usr/bin/env groovy
import com.google.gson.Gson

// usage: groovy -Djava.util.logging.config.file=logging.properties permissions_check.groovy
//        groovy permissions_check.groovy
//        groovy permissions_check.groovy > /dev/null 2>role_USER.txt
//        groovy permissions_check.groovy > /dev/null 2>role_EDITOR.txt

// https://mvnrepository.com/artifact/org.codehaus.groovy.modules.http-builder/http-builder
@GrabResolver(name = 'mvnrepository', root = 'https://mvnrepository.com/', m2Compatible = 'true')
@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.7.1')
@Grab(group = 'org.apache.httpcomponents', module = 'httpclient', version = '4.5.3')
@Grab(group = 'org.apache.httpcomponents', module = 'httpcore', version = '4.4.6')
@Grab(group='com.google.code.gson', module='gson', version='2.8.0')
@GrabExclude(group = 'commons-lang', module = 'commons-lang')
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseDecorator

import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

import org.apache.http.HttpRequest
import org.apache.http.HttpResponse
import org.apache.http.impl.client.LaxRedirectStrategy
import org.apache.http.impl.client.DefaultRedirectStrategy
import org.apache.http.protocol.*

import groovy.util.logging.Log
import java.util.logging.Level

@Log
class Main {
    private static final Gson gson = new Gson();
    private static enum ResponseCode { HTTP_CODE_NOT_FOUND, HTML_FOUND, JSON_FOUND, NO_RESPONSE_FOUND }

    def baseUrl
    def httpBuilder
    def cookies

    def methods
    def methodsExclude = ["restartAppServiceAction","exportSpecialReport"]

    def rolePermissions

    Main() {
        baseUrl = "http://localhost:8080"
        httpBuilder = initializeHttpBuilder()
        cookies = []
        methods = [[:]]
        rolePermissions = [[:]]

        // Programmatic log format configuration
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1\$tY-%1\$tm-%1\$td %1\$tH:%1\$tM:%1\$tS.%1\$tL %4\$-7s [%3\$s] %5\$s %6\$s%n");
        log.setLevel(Level.INFO)
    }

    def readMethodsCsvData() {
        methods = []
        new File("/Users/sidar/Downloads/MethodAnnotations.csv").splitEachLine(",") { fields ->
            methods.add(
                    [
                            controller: fields[0],
                            method    : fields[1],
                            annotation: fields[2],
                            httpMethod: GET // defaulting to HTTP GET
                    ]
            )
        }
        // remove first line "headers"
        if (methods && methods.size() > 0) {
            methods = methods.drop(1)
        }
    }

    def readRolesPermissionsCsvData() {
        rolePermissions = []
        new File("/Users/sidar/Downloads/RolePermissions_csv.csv").splitEachLine(",") { fields ->
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
        if (log.level == Level.FINEST) {
            debug("Send ${method} request to ${this.baseUrl}${url}: ${params}")
        }
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
    }

    def login(String username, String password) {
//        request(POST, URLENC, '/tdstm/auth/signIn', ['username': 'tdsadmin', 'password': 'zelda123!', 'targetUri': ''])
//        request(POST, URLENC, '/tdstm/auth/signIn', ['username': 'testuser@mailinator.com', 'password': 'zelda123!'])
        cookies = []
        request(POST, URLENC, '/tdstm/auth/signIn', ["username": username, "password": password])
    }

    def performMethodSecurityCheck(String userRole) {
        methods.each { controllerMethod ->
            if (controllerMethod.method in methodsExclude) {
                debug("Method excluded: /${controllerMethod.controller}/${controllerMethod.method}")
            } else {
                //debugResponse(request(controllerMethod.httpMethod, TEXT, "/tdstm/" + controllerMethod.controller + "/" + controllerMethod.method, null))
                evalResult(controllerMethod as Map, userRole, request(controllerMethod.httpMethod, TEXT, "/tdstm/" + controllerMethod.controller + "/" + controllerMethod.method, null))
            }
        }
    }

    def evalResult(Map controllerMethod, String userRole, String resp) {
        if (controllerMethod.annotation) {
            def permission = findPermissionByRole(controllerMethod.annotation)
            if (permission) {
                def uri = "/tdstm/" + controllerMethod.controller + "/" + controllerMethod.method

                if (log.level == Level.FINEST) {
                    debug("(Role: ${userRole}, Permission: ${permission.permission}) => Should be allowed: " + (permission["role_${userRole}"] == true))
                }

                String responseErrorCode = findResponseErrorCode(resp)
                if (permission["role_${userRole}"] == true) {
                    //debug("Call allowed: (Role: ${userRole}, Permission: ${permission.permission}) " + uri + " - " + findResponseErrorCode(resp))
                    if (isFailure(responseErrorCode)) {
                        debug("Call allowed but: (Role: ${userRole}, Permission: ${permission.permission}) " + uri + " - " + responseErrorCode)
                    }
                } else {
                    //debug("Call should fail: (Role: ${userRole}, Permission: ${permission.permission}) " + uri + " - " + findResponseErrorCode(resp))
                    //String responseErrorCode = findResponseErrorCode(resp)
                    if (!isFailure(responseErrorCode)) {
                        debug("Call should fail but: (Role: ${userRole}, Permission: ${permission.permission}) " + uri + " - " + responseErrorCode)
                    }
                }
            } else {
                warn("Permission not found: ${controllerMethod.annotation}")
            }
        } else {
            debug("Method call does not have explicit permissions check: ${controllerMethod as String}")
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

    def run() {
        readMethodsCsvData()
        readRolesPermissionsCsvData()

        login("testuser@mailinator.com", "zelda123!")
//        login("testeditor@mailinator.com", "zelda123!")

        performMethodSecurityCheck("USER")
//        performMethodSecurityCheck("EDITOR")
    }

    static void main(String... args) {
        new Main().run()
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
            if (log.level == Level.FINEST) {
                def respHeadersString = 'Headers:';
                resp.headers.each() { header -> respHeadersString += "\n\t${header.name}=\"${header.value}\"" }
                debug(respHeadersString)
            }

            resp.getHeaders('Set-Cookie').each {
                //[Set-Cookie: JSESSIONID=E68D4799D4D6282F0348FDB7E8B88AE9; Path=/tdstm/; HttpOnly]
                String cookie = it.value.split(';')[0]
                debug("Adding cookie to collection: $cookie")
                cookies.add(cookie)
            }
            //debug("Response: ${reader}")
            //debug("Response Code: ${resp.statusLine}")
            return reader?.text
        }
        httpBuilder.handler.failure = { HttpResponseDecorator resp, InputStreamReader reader ->
            if (log.level == Level.FINEST) {
                def respHeadersString = 'Headers:';
                resp.headers.each() { header -> respHeadersString += "\n\t${header.name}=\"${header.value}\"" }
                debug(respHeadersString)
            }
            //debug("Response: ${reader}")
            //debug("Response Code: ${resp.statusLine}")
            return reader?.text
        }
        return httpBuilder
    }

    def debugResponse(resp) {
        debug("Method call response: ${resp.statusLine}")
        if (log.level == Level.FINEST) {
            def respHeadersString = 'Headers:';
            resp.headers.each() { header -> respHeadersString += "\n\t${header.name}=\"${header.value}\"" }
            debug(respHeadersString)
        }
    }

    def debug(String message) {
        log.log(log.level, message)
    }

    def warn(String message) {
        log.log(Level.WARNING, message)
    }
}
