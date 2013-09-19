/**
 * The TaskDependencyType represents the various types of dependency types. The different types consist of:
 * 
 * 		FS - Finish-to-Start which is the most common that TM will mark the successor as READY once the predecessor has completed
 * 		SS - Start-to-Start which in TM will automatically start a successor when the predecessor starts
 * 			(e.g. TBD)
 * 		FF - Finish-to-Finish which in TM will automatically complete a successor task when its predecessor completes 
 * 			( e.g. Predecessor:Validation of VM copy successful (SYS_ADMIN), Successor:VM Copy time (SYS_RESOURCE) )
 * 		SF - Start-to-Finish the least common method that is not going to be implemented in TM at this time.
 * 
 * For more information visit http://office.microsoft.com/en-us/project-help/choose-the-right-type-of-task-dependency-HA001220848.aspx
 */

package com.tdsops.tm.enums.domain

enum TaskDependencyType {
	
	FF ('Finish-Finish'),
	FS ('Finish-Start'), 
	SS ('Start-Start')

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
			keys = TaskDependencyType.values()*.name()
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