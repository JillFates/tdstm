package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

/**
 * Represents the various types of dependency types. These consist of:
 *
 * 	FR - The most common Finish-to-Ready that TM will use to update the successor to READY once the predecessor has completed
 * 	FS - Finish-to-Start is typically used with Resource type tasks that automatically START when the Predecessor completes
 * 	SS - Start-to-Start which in TM will automatically start a successor when the predecessor starts
 * 			(e.g. TBD)
 * 	FF - Finish-to-Finish which in TM will automatically complete a successor task when its predecessor completes
 * 			(e.g. Predecessor:Validation of VM copy successful (SYS_ADMIN), Successor:VM Copy time (SYS_RESOURCE))
 * 	SF - Start-to-Finish the least common method that is not going to be implemented in TM at this time.
 * 	MM - Matched is a special case where the predecessor and successor tasks statuses are matched. When one is updated so is the other.
 * For more information visit http://office.microsoft.com/en-us/project-help/choose-the-right-type-of-task-dependency-HA001220848.aspx
 */
@CompileStatic
enum TaskDependencyType {

	FR('Finish-Ready'),
	FS('Finish-Start'),
	FF('Finish-Finish'),
	SS('Start-Start'),
	MM('Matched-Matched')

	static TaskDependencyType getDefault() { FR }

	final String value

	private TaskDependencyType(String label) {
		value = label
	}

	String value() { value }

	static TaskDependencyType asEnum(String key) {
		values().find { it.name() == key }
	}

	static final List<TaskDependencyType> keys = (values() as List).asImmutable()

	static final List<String> labels = keys.collect { it.value }.asImmutable()

	static List<String> getLabels(String locale = 'en') { labels }
}
