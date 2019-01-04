package net.transitionmanager.command.dependency.analyzer

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

class FilteredAssetsCommand implements CommandObject{

	String     nameFilter = ''
	boolean    isRegex = false
	Long       personId
	String     depGroup
	List<Long> tagIds   = []
	String     tagMatch = 'ANY'

	static constraints = {
		nameFilter nullable: true
		depGroup nullable: true
		personId nullable: true
		tagMatch inList: ['ANY', 'ALL']
	}
}
