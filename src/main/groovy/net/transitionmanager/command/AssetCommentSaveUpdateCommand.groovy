package net.transitionmanager.command



/**
 * Command object for creating/updating AssetComments.
 * All fields are required, except for the id and the status, which is null when creating a new comment.
 */

class AssetCommentSaveUpdateCommand implements CommandObject{

	Long id

	String comment

	String category

	Boolean isResolved

	Long assetEntityId

	String status

	static constraints = {
		id nullable: true, blank: false
		comment nullable: false
		category nullable: false
		isResolved nullable: false
		assetEntityId nullable: false
		status nullable: true
	}

}
