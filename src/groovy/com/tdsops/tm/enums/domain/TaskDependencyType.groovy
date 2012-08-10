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
	FS('FS'), SS('SS'), FF('FF')

	final String value
	
	TaskDependencyType(String value) { this.value = value }
	
	static def getList() { return TaskDependencyType.values()*.value }
	
	String toString() { value }

}
