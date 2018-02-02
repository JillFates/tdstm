package com.tdsops.tm.enums.domain

enum CredentialType {
    PRODUCTION('Production'),
    SANDBOX('Sandbox')

    final String type;

    CredentialType(String type) {
        this.type = type
    }

    @Override
    String toString() {
        return this.type;
    }
}