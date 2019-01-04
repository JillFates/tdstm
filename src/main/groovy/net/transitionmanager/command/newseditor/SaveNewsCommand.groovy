package net.transitionmanager.command.newseditor

import net.transitionmanager.command.CommandObject


/**
 * A command object used for saving and updating news.
 */

class SaveNewsCommand implements CommandObject{
	Long    moveEventId
	String  message    = ''
	String  resolution = ''
	Integer isArchived = 0
	String  mode       = 'redirect'
	String  moveBundle = ''
	String  viewFilter = ''

	static constraints = {
		moveEventId min: 1l
		moveBundle nullable: true
		viewFilter nullable: true
	}
}