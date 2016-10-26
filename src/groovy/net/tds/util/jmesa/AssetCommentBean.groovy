package net.tds.util.jmesa

import groovy.transform.CompileStatic

@CompileStatic
class AssetCommentBean implements Serializable {

	private static final long serialVersionUID = 1L

	Long id
	String assetType
	String assetName
	String commentType
	String status
	String description
	Integer taskNumber
	String comment
	Date lastUpdated
	Date dueDate
	String assignedTo
	String role
	String category
	Integer succCount
	Long assetEntityId

	// CSS class names used for the Updated, Due and Status columns
	String updatedClass=''
	String dueClass=''
	String statusClass=''
	String elapsedAgo=''
	Integer score
	Boolean isRunbookTask=false
	Integer hardAssigned=0
}
