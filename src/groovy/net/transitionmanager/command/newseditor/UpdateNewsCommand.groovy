package net.transitionmanager.command.newseditor

import grails.validation.Validateable

/**
 * A command object used for saving and updating news.
 */
@Validateable
class UpdateNewsCommand {
	Long    id
	String  message    = ''
	String  resolution = ''
	Integer isArchived = 0
	String  mode       = 'redirect'
	String  moveBundle = ''
	String  viewFilter = ''

	static constraints = {
		id min: 1l
		moveBundle nullable: true
		viewFilter nullable: true
	}
}