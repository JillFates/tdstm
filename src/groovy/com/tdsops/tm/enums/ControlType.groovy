package com.tdsops.tm.enums

import groovy.transform.CompileStatic

/**
 * Enumeration that contains the possible Control Types for the Custom Fields
 */
@CompileStatic
enum ControlType {
    STRING("String"),
    LIST("List"),
    NUMBER("Number"),
    YES_NO("YesNo"),
    PERSON("Person"),
    TEAM("Team"),
    RACK_T("Rack.T"),
    RACK_S("Rack.S"),
    LOCATION_T("Location.T"),
    LOCATION_S("Location.S"),
    CHASSIS_T("Chassis.T"),
    CHASSIS_S("Chassis.S"),
    DATE("Date"),
    DATETIME("Datetime")

    private String value

    ControlType(String value) {
        this.value = value
    }

    /**
     * Added this way to be coherent with the name() method
     * @return
     */
    String value() { value }

    static ControlType asEnum(String key) {
        values().find {
            it.value() == key
        }
    }

    @Override
    String toString() {
        return value
    }
}