package net.transitionmanager.controller.api

import grails.plugins.rest.client.RestBuilder
import groovy.json.JsonOutput

trait LoginAs {

    abstract String grailServerUrl()

    /**
     *
     * @return access_token
     */
    Map loginAs(String username, String password) {
        String jsonString = JsonOutput.toJson([username: username, password: password])
        RestBuilder rest = new RestBuilder()
        def resp = rest.post("${grailServerUrl()}/api/login") {
            header("Accept", "application/json")
            header("Content-Type", "application/json")
            json jsonString
        }
        [
                accessToken: resp?.json?.access_token,
                refreshToken: resp?.json?.refresh_token,
                username: resp?.json?.username,
                tokenType: resp?.json?.token_type,
                expiresIn: resp?.json?.expires_in,
                roles: resp?.json?.roles,
        ]
    }
}
