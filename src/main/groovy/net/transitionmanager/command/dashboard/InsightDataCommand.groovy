package net.transitionmanager.command.dashboard

import net.transitionmanager.command.CommandObject


/**
 * A command object used in filtering a list of tags.
 *
 * @param nameFilter a string for additional filtering, which could be a regex.
 * @param isRegex if the nameFilter is a regex.
 * @param personId a person id to filter by
 * @param depGroup the dependency group number or 'onePlus'
 * @param tagIds tags that should be used as a filter.
 * @param tagMatch how the tags ids should be combined in an OR(ANY) or with an AND(All).
 */

class InsightDataCommand implements CommandObject{

	Integer max = 5
	Integer lowRange = 5
	Integer highRange = 10


	static constraints = {
		max min: 5,  max: 100
		lowRange min: 5, max: 100
		highRange min: 10, max:1000
	}
}
