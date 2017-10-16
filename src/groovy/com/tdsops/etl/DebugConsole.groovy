package com.tdsops.etl

class DebugConsole {
    /**
     *
     * Console Status defines if the debug console is enable or not
     */
    static enum ConsoleStatus {
        on, off
    }

    ConsoleStatus status

    StringBuffer buffer = new StringBuffer()

    void info (def content) {
        append("INFO", content)
    }

    void debug (def content) {
        append("DEBUG", content)
    }

    void warn (def content) {
        append("WARN", content)
    }

    void error (def content) {
        append("ERROR", content)
    }

    private void append (String level, def content) {
        if (status == ConsoleStatus.on) {
            buffer.append(level)
            buffer.append(" - ")
            buffer.append(content)
            buffer.append(System.lineSeparator())
        }
    }

    String content () {
        buffer.toString()
    }

    Boolean isEnabled () {
        status == ConsoleStatus.on
    }
}
