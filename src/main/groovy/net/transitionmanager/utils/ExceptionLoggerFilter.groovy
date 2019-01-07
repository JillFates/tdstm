package net.transitionmanager.utils

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

/**
 *
 * Recipe Domain Object
 * Mutes log logging events for a given loggerClass.
 *
 * @author estebancantu
 */
class ExceptionLoggerFilter extends Filter {

	String loggerClass

	int decide(LoggingEvent event) {

		if (event.getLoggerName().equals(loggerClass)) {

			if (event.getThrowableInformation() != null) {
				return Filter.DENY;
			}
		}
		return Filter.NEUTRAL;
	}

	String getLoggerClass() {
		return loggerClass;
	}

	void setLoggerClass(String loggerClass) {
		this.loggerClass = loggerClass;
	}

}

