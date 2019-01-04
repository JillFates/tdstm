package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

/**
 * Defines all the possible Start Pages that the user can be redirected to.
 *
 * Created by Esteban Cantu on 7/17/2017.
 */
@CompileStatic
enum StartPageEnum {

    ADMIN_PORTAL('Admin Portal'),
    PROJECT_SETTINGS('Project Settings'),
    PLANNING_DASHBOARD('Planning Dashboard'),
    CURRENT_DASHBOARD('Current Dashboard'),
    USER_DASHBOARD('User Dashboard')


    final String value

    private StartPageEnum(String value) {
        this.value = value
    }

    String toString() { value }

    static StartPageEnum valueOfParam(String param) {
        values().find { it.value == param }
    }

}
