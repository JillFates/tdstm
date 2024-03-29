package net.transitionmanager.imports

import com.tdssrc.grails.JsonUtil
import com.tdssrc.grails.TimeUtil
import net.transitionmanager.command.task.batch.StoredContextCommand
import net.transitionmanager.command.task.batch.TagCommand
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.person.Person
import net.transitionmanager.project.Project
import net.transitionmanager.task.Recipe
import net.transitionmanager.task.RecipeVersion

/**
 * Represents a batch that is created when a recipe is executed and tasks are generated. This will provide
 * a way of tracking, updating and possibly deleting tasks that were generate in the cookbook.
 *
 * @author John Martin
 */
class TaskBatch {
	Long   eventId
	String context
	String status

	RecipeVersion recipeVersionUsed       // the recipeVersion used to generate the batch of tasks
	Recipe        recipe
	Integer       taskCount = 0                 // number of tasks generated by the process
	Integer       exceptionCount = 0            // number of exceptions that occurred during the recipe generation
	Boolean       isPublished = false           // whether the tasks generated where published to users
	String        exceptionLog = ''              // the exceptions that were created by the generation of the batch
	String        infoLog = ''                   // the debug log/info that were created by the generation of the batch
	Person        createdBy                      // Whom created this version of the recipe
	Project       project

	Date dateCreated
	Date lastUpdated

	static constraints = {
		eventId nullable: true
		dateCreated nullable: true
		lastUpdated nullable: true
		recipe nullable: true
		recipeVersionUsed nullable: true   // Note that recipes can be deleted which will null out any references
		status blank: false, inList: ['Pending', 'Generating', 'Completed', 'Failed', 'Cancelled']
	}

	static mapping = {
		version false
		autoTimestamp false
		id column: 'task_batch_id'
		columns {
			exceptionLog sqltype: 'text'
			infoLog sqltype: 'text'
			taskCount sqltype: 'smallint'
		}
	}

	def beforeInsert = {
		lastUpdated = dateCreated = TimeUtil.nowGMT()
	}
	def beforeUpdate = {
		lastUpdated = TimeUtil.nowGMT()
	}

	def beforeDelete = {
		// Remove any tasks that were created by the batch
		// 160315 OLB: the dependencies of AssetComment are deleted using fk cascade strategy
		// TODO: We should test and check all the dependencies created to be sure that everything is removed
		executeUpdate('delete AssetComment where taskBatch=?0', [this])
	}

	/**
	 * Gets the event name and or the tags, for the taskBatch, and returns them as a String
	 *
	 * @return The name and, or the tag names for the taskBatch
	 */
	String eventName() {
		return MoveEvent.get(eventId)?.name ?: ''
	}

	/**
	 * Gets the list of tag names as a string for display purposes.
	 *
	 * @return the list of tags names as a string.
	 */
	String tagNames() {
		StoredContextCommand storedContext = context()

		if (storedContext?.tag) {
			return storedContext.tag.collect { TagCommand tag -> tag.label }.join(', ')
		}

		return ''
	}

	/**
	 * Gets the context as a StoredContextCommand or null if not set
	 *
	 * @return the context json as a StoredContextCommand or null if not set
	 */
	StoredContextCommand context() {
		return context ? JsonUtil.populateCommandObject(StoredContextCommand, context) : null
	}

	/**
	 * Returns informational representation of the task formated as Context + Context Object + batch (# tasks)
	 * (e.g. Application VSphere 5.0 Cluster - batch (30 tasks) )
	 */
	String toString() {
		(recipe ? recipe.context + ' ' : '') + ' - batch of ' + taskCount + ' tasks'
	}
}
