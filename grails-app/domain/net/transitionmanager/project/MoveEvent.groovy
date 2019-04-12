package net.transitionmanager.project

import com.tdssrc.grails.WebUtil
import net.transitionmanager.tag.TagEvent

/**
 * Represents the concept of an event where one or move bundles that will occur at one logical period of time.
 */
class MoveEvent {

	public static final String METHOD_LINEAR = 'L'
	public static final String METHOD_MANUAL = 'M'
	public static final List<String> BASIC_EVENT_FIELDS = ['id', 'name']
	public static final List<String> DETAILED_LIST_FIELDS = BASIC_EVENT_FIELDS + ['description', 'dateCreated', 'lastUpdated', 'estStartTime', 'estCompletionTime']

	Project project
	String name
	String description
	String newsBarMode = 'off'
	String calcMethod = METHOD_LINEAR
	String runbookStatus
	Integer runbookVersion = 1
	String runbookBridge1
	String runbookBridge2
	String videolink
	String runbookRecipe

	// API Action "By-Pass" mode feature
	Boolean apiActionBypass = true

	Date revisedCompletionTime      // Revised Completion Time of the MoveEvent which is only set as an exception

	// Not sure if this is going to be stored or not....
	Date actualStartTime
	Date actualCompletionTime
	Date estStartTime
	Date estCompletionTime
	Date dateCreated
	Date lastUpdated

	Collection tagEvents

	static hasMany = [
		moveBundles       : MoveBundle,
		moveEventNewsList : MoveEventNews,
		moveEventSnapshots: MoveEventSnapshot,
		tagEvents         : TagEvent
	]

	static constraints = {
		actualCompletionTime nullable: true
		actualStartTime nullable: true
		calcMethod blank: false, inList: [METHOD_LINEAR, METHOD_MANUAL]
		description nullable: true, size: 0..255
		estCompletionTime nullable: true
		estStartTime nullable: true
		name blank: false, size: 0..255
		newsBarMode blank: false, inList: ['auto', 'on', 'off']
		revisedCompletionTime nullable: true
		runbookBridge1 nullable: true, size: 0..255
		runbookBridge2 nullable: true, size: 0..255
		runbookRecipe nullable: true
		runbookStatus nullable: true, inList: ['Pending', 'Draft', 'Final', 'Done']
		runbookVersion nullable: true
		videolink nullable: true
		apiActionBypass nullable: false
		dateCreated nullable: true
		lastUpdated nullable: true
	}

	static mapping = {
		id column: 'move_event_id'
		sort 'name'
		columns {
			estCompletionTime sqlType: 'DateTime'
			estStartTime sqlType: 'DateTime'
			revisedCompletionTime sqlType: 'DateTime'
			runbookRecipe sqlType: 'Text'
		}
		apiActionBypass column: 'api_action_bypass'
	}


	/**
	 *  Render moveBundles list as comma separated value string
	 */
	String getMoveBundlesString() {
		WebUtil.listAsMultiValueString(moveBundles)
	}

	String toString() { name }

	boolean belongsToClient(client) {
		project.clientId == client?.id
	}

}
