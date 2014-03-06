/**
 * The TaskDependencyType represents the various types of dependency types. The different types consist of:
 * 
 *		FR - The most common Finish-to-Ready that TM will use to update the successor to READY once the predecessor has completed
 * 		FS - Finish-to-Start is typically used with Resource type tasks that automatically START when the Predecessor completes
 * 		SS - Start-to-Start which in TM will automatically start a successor when the predecessor starts
 * 			(e.g. TBD)	
 * 		FF - Finish-to-Finish which in TM will automatically complete a successor task when its predecessor completes 
 * 			( e.g. Predecessor:Validation of VM copy successful (SYS_ADMIN), Successor:VM Copy time (SYS_RESOURCE) )
 * 		SF - Start-to-Finish the least common method that is not going to be implemented in TM at this time.
 *      MM - Matched is a special case where the predecessor and successor tasks statuses are matched. When one is updated so is the other.
 * For more information visit http://office.microsoft.com/en-us/project-help/choose-the-right-type-of-task-dependency-HA001220848.aspx
 */

package com.tdsops.tm.enums.domain

enum TaskDependencyType {
	
	FR ('Finish-Ready'),
	FS ('Finish-Start'), 
	FF ('Finish-Finish'),
	SS ('Start-Start'),
	MM ('Matched-Matched')

	// Returns the application default to use for the enum
	static TaskDependencyType getDefault() {
		return FR
	}

	//
	// Boiler Plate from here down - Just swap out the enum class name
	//

	final String value
	private static List keys
	private static List labels
	
	// Constructor
	TaskDependencyType( String value ) { this.value = value }
	
	String toString() { name() }
	String value() { value }

	// Used to convert a string to the enum or null if string doesn't match any of the constants
	static TaskDependencyType asEnum(key) {
		def obj
		try {
			obj = key as TaskDependencyType
		} catch (e) { }
		return obj
	}

	// Returns the keys of the enum keys
	static List getKeys() { 
		if (keys == null) 
			buildKeys()
		return keys
	}

	// Construct the static keys 
	private static synchronized void buildKeys() { 
		if (keys == null) {
			keys = TaskDependencyType.values()
		}
	} 

	// Returns the labels of the enum labels
	static List getLabels(String locale='en') { 
		if (labels == null) 
			buildLabels()
		return labels
	}

	// Construct the static labels 
	private static synchronized void buildLabels() { 
		if (labels == null) {
			labels = TaskDependencyType.values()*.value
		}
	} 

}