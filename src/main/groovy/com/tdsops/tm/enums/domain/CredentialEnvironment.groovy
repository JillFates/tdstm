package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

@CompileStatic
enum CredentialEnvironment {
    PRODUCTION('Production'),
    SANDBOX('Sandbox'),
    DEVELOPMENT('Development'),
    OTHER('Other')

    private final String environment;

    CredentialEnvironment(String environment) {
        this.environment = environment
    }

    static toMap() {
        values().collectEntries { e ->
            [(e.name()): e.toString()]
        }
    }

    @Override
    String toString() {
        return this.environment;
    }
}
