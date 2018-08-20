package net.transitionmanager.command.event

import grails.validation.Validateable

/**
 * A command object used in creating a AssetTag.
 */
@Validateable
class CreateEventCommand {
	List<Long> tagIds = []

	List<Long> moveBundle = []

	String  name
	String  description
	String  runbookStatus
	String  runbookBridge1
	String  runbookBridge2
	String  videolink
	String  newsBarMode  = 'off'
	Date    estStartTime = null
	Boolean apiActionBypass  = true

	static constraints = {
		description nullable: true
		estStartTime nullable: true
		name blank: false
		newsBarMode blank: false, inList: ['auto', 'on', 'off']
		runbookBridge1 nullable: true
		runbookBridge2 nullable: true
		runbookStatus nullable: true, inList: ['Pending', 'Draft', 'Final', 'Done']
		videolink nullable: true
	}
}
