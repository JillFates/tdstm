package com.tdsops.tm.asset

/**
 * Mapping of Asset Export/Import workbook sheetNames
 */
enum WorkbookSheetName {
    DEVICES("Devices"),
    APPLICATIONS("Applications"),
    DATABASES("Databases"),
    STORAGE("Storage"),
    RACK("Rack"),
    TITLE("Title"),
    DEPENDENCIES("Dependencies"),
    ROOM("Room"),
    CABLING("Cabling"),
    COMMENTS("Comments"),
    VALIDATION("Validation")

    private String name

    WorkbookSheetName(String name) {
        this.name = name
    }


    @Override
    String toString() {
        return name;
    }
}
