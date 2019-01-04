package net.transitionmanager.command.newseditor

import grails.validation.Validateable

/**
 * A command object used for saving and updating news.
 */
@Validateable
class SaveNewsCommand {
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
