package com.tdsops.tm.enums

enum ControlType {
    STRING("String"),
    LIST("List"),
    NUMBER("Number"),
    YES_NO("YesNo"),
    PERSON("Person"),
    TEAM("Tean"),
    RACK_T("Rack.T"),
    RACK_S("Rack.S"),
    LOCATION_T("Location.T"),
    LOCATION_S("Location.S"),
    CHASSIS_T("Chassis.T"),
    CHASSIS_S("Chassis.S"),
    DATE("Date"),
    DATETIME("Datetime")

    private String type;

    ControlType(String type) {
        this.type = type
    }

    @Override
    public String toString() {
        return type
    }
}