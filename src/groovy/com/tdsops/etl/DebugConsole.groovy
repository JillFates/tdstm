package com.tdsops.etl

class DebugConsole {

    StringBuffer buffer = new StringBuffer()
    ConsoleStatus status

    void info(def content) {
        append("INFO", content)
    }

    void debug(def content) {
        append("INFO", content)
    }

    void warn(def content) {
        append("INFO", content)
    }

    private void append(String level, def content) {
        if (status == ConsoleStatus.on) {
            buffer.append(level)
            buffer.append(" - ")
            buffer.append(content)
            buffer.append(System.lineSeparator())
        }
    }

    String content() {
        buffer.toString()
    }

    Boolean isEnabled() {
        status == ConsoleStatus.on
    }
}
