package net.transitionmanager.domain

import com.tdssrc.grails.WebUtil
import org.springframework.jdbc.core.JdbcTemplate

/**
 * Represents the concept of an event where one or move bundles that will occur at one logical period of time.
 */
class MoveEvent {

	public static final String METHOD_LINEAR = 'L'
	public static final String METHOD_MANUAL = 'M'
	public static final List<String> BASIC_EVENT_FIELDS = ['id', 'name']
	public static final List<String> TMR_EVENT_FIELDS = BASIC_EVENT_FIELDS + ['description', 'dateCreated', 'lastUpdated', 'estStartTime', 'estCompletionTime']

	JdbcTemplate jdbcTemplate

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
		description nullable: true
		estCompletionTime nullable: true
		estStartTime nullable: true
		name blank: false
		newsBarMode blank: false, inList: ['auto', 'on', 'off']
		revisedCompletionTime nullable: true
		runbookBridge1 nullable: true
		runbookBridge2 nullable: true
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

	static transients = ['jdbcTemplate']

	/**
	 * Retrieves the PLAN MIN/MAX Start and Completion times of the MoveBundleSteps associate with the MoveBundle.MoveEvent
	 * TODO : JPM 3/2014 - getPlanTimes () doesn't look like it can actually work as the columns don't exist in MoveBundle
	 * @return Map[planStart , planCompletion] times for the MoveEvent
	 */
	Map<String, Object> getPlanTimes() {
		jdbcTemplate.queryForMap('''
			SELECT MIN(mbs.plan_start_time) AS start, MAX(mbs.plan_completion_time) AS completion
			FROM move_bundle mb
			LEFT JOIN move_bundle_step mbs ON mb.move_bundle_id = mbs.move_bundle_id
			WHERE move_event_id = ?''', id)
	}

	/**
	 * Retrieves the MIN/MAX Start and Completion times of the MoveBundles associate with the MoveEvent
	 * @return Map[start , completion] times for the MoveEvent
	 */
	Map<String, Date> getEventTimes() {
		jdbcTemplate.queryForMap('''
			SELECT MIN(start_time) AS start, MAX(completion_time) AS completion
			FROM move_bundle
			WHERE move_event_id = ?''', id)
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
