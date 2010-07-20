package org.grails.plugins.webtest

import org.apache.log4j.Logger
import org.apache.log4j.Level

class DummyPrinter {
    private final Logger fLogger;
    private final Level fLevel;
    private final StringBuffer fBuffer = new StringBuffer();

    DummyPrinter(final step, final Level level) {
        fLogger = Logger.getLogger(step.getClass());
        fLevel = level;
    }

    void println(final Object object) {
        print(object);
        final String message = fBuffer.toString();
        fBuffer.setLength(0);
        fLogger.log(fLevel, message);
    }

    void print(final Object object) {
        fBuffer.append(String.valueOf(object));
    }

    /**
     * prints the remaining message (if any)
     */
    void flush() {
        if (fBuffer.length() > 0)
            println(""); // forces print of the current buffer
    }
}