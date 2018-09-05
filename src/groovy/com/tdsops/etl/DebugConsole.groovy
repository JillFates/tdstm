package com.tdsops.etl

class DebugConsole {

	/**
	 * Debug message Level
	 */
    static enum LevelMessage {
        INFO, DEBUG, WARN, ERROR
    }
    /**
     *
     * Console Status defines if the debug console is enable or not
     */
    static enum ConsoleStatus {
        on, off
    }

    ConsoleStatus status

    StringBuilder buffer = new StringBuilder()

    void info (def content) {
        append(LevelMessage.INFO, content)
    }

    void debug (def content) {
        append(LevelMessage.DEBUG, content)
    }

    void warn (def content) {
        append(LevelMessage.WARN, content)
    }

    void error (def content) {
        append(LevelMessage.ERROR, content)
    }

    void append (LevelMessage level, def content) {
        if (status == ConsoleStatus.on) {
            buffer.append(level)
            buffer.append(" - ")
            buffer.append(content.toString())
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