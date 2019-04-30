package net.transitionmanager.command.event

import grails.databinding.BindUsing
import net.transitionmanager.command.CommandObject


/**
 * A command object used in creating an Event with tags.
 */

class CreateEventCommand implements CommandObject{

	//Using this annotation because by default if an empty string is returned for the list, that it would be bound as [null].
	// This makes it, so that if the parameter coming is is falsy "", [], null, then [] is returned,otherwise it does the normal binding.
	@BindUsing({ obj, source ->
		return source['tagIds'] ?: []
	})
	List<Long> tagIds = []

	@BindUsing({ obj, source ->
		return source['moveBundle'] ?: []
	})
	List<Long> moveBundle = []

	String  name
	String  description
	String  runbookStatus
	String  runbookBridge1
	String  runbookBridge2
	String  videolink
	String  newsBarMode  = 'off'
	Date estCompletionTime = null
	Date    estStartTime = null
	Boolean apiActionBypass  = true

	static constraints = {
		description nullable: true
		estCompletionTime nullable: true
		estStartTime nullable: true
		name blank: false
		newsBarMode blank: false, inList: ['auto', 'on', 'off']
		runbookBridge1 nullable: true
		runbookBridge2 nullable: true
		runbookStatus nullable: true, inList: ['Pending', 'Draft', 'Final', 'Done']
		videolink nullable: true
	}
}
