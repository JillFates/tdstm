package com.tdsops.tm.enums.domain

enum CredentialStatus {
    ACTIVE('Active'),
    INACTIVE('Inactive')

    final String status;

    CredentialStatus(String status) {
        this.status = status
    }

    @Override
    String toString() {
        return this.status;
    }
}