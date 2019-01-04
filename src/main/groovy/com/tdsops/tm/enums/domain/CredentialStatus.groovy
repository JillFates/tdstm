package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

@CompileStatic
enum CredentialStatus {
    ACTIVE('Active'),
    INACTIVE('Inactive')

    final String status;

    CredentialStatus(String status) {
        this.status = status
    }

    static toMap() {
        values().collectEntries { e ->
            [(e.name()): e.toString()]
        }
    }

    @Override
    String toString() {
        return this.status;
    }
}
