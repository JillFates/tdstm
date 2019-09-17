package net.transitionmanager.task

import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdssrc.grails.GormUtil
import net.transitionmanager.common.MessageSourceService
import net.transitionmanager.i18n.Message
import net.transitionmanager.person.Person
import net.transitionmanager.task.AssetComment
import net.transitionmanager.task.TaskService
import org.grails.core.exceptions.InvalidPropertyException
import org.grails.datastore.mapping.model.PersistentProperty
import org.springframework.beans.factory.annotation.Autowired

class TaskFacade {
	TaskService taskService
	MessageSourceService messageSourceService
	private AssetComment task
	private Person whom

	/**
	 * Default constructor
	 */
	TaskFacade() {
	}

	@Autowired
	/**
	 * Constructor
	 * @param task - the actual task that will be wrapped within the fascade
	 * @param whom - the person whom is interacting with the system that will be associated to the done/started/error methods
	 */
	TaskFacade(AssetComment task, Person whom) {
		this.task = task
		this.whom = whom
	}

	/**
	 * Get task property determines if the property is a java type to return its value, if the property
	 * is not a java type it returns its toString representation
	 * @param name - the property name
	 * @return
	 */
	Object getProperty(String name) {
		try {
			PersistentProperty taskProperty = GormUtil.getDomainProperty(task, name)
			Object value = task.getProperty(taskProperty.name)

			if (GormUtil.isReferenceProperty(task, taskProperty.name)) {
				value.toString()
			} else {
				value
			}
		} catch (InvalidPropertyException e) {
			throw new MissingPropertyException("No such property: " + name)
		}
	}

	/**
	 * Used to determine if the task is done or completed
	 * @return
	 */
	boolean isDone() {
		return task.isDone()
	}

	/**
	 * Used to determine if the task has been started
	 * @return
	 */
	boolean isStarted() {
		return task.isStarted()
	}

	/**
	 * Used to determine if the task is in the HOLD status
	 * @return
	 */
	boolean isOnHold() {
		return task.isOnHold()
	}

	/**
	 * Used to determine if the task is automated
	 * @return
	 */
	boolean isAutomatic() {
		return task.isAutomatic()
	}

	/**
	 * Returns the estimated duration of the task in minutes
	 * @return
	 */
	int durationInMinutes() {
		return task.durationInMinutes()
	}

	/**
	 * Updates a task status to STARTED and records the date/time and description of the activity in the Task comment section
	 */
	void start() {
		updateTaskStatus(AssetCommentStatus.STARTED)
	}

	/**
	 * Updates the task status to DONE and records the date/time and description of the activity in the Task comment section
	 */
	void done() {
		updateTaskStatus(AssetCommentStatus.COMPLETED)
	}

	/**
	 * Places task on HOLD and records the date/time and description of the reason the task was placed on hold
	 * @param message
	 */
	void error(String message) {
		addTaskCommentNoteAndUpdateStatus(AssetCommentStatus.HOLD, message)
	}

	/**
	 * Places task on HOLD and records the date/time and description indicating that action didn't
	 * complete in the specified time window. This is applicable for polling tasks used with asynchronous activities.
	 */
	void lapsed() {
		addTaskCommentNoteAndUpdateStatus(AssetCommentStatus.HOLD, messageSourceService.i18nMessage(Message.ApiActionTaskMessageLapsed))
	}

	/**
	 * Places task on HOLD and records the date/time and description indicating that no progress was made with
	 * the action progress with the stalledAfter period of time.
	 * This is applicable for polling tasks used with asynchronous activities.
	 */
	void stalled() {
		addTaskCommentNoteAndUpdateStatus(AssetCommentStatus.HOLD, messageSourceService.i18nMessage(Message.ApiActionTaskMessageStalled))
	}

	/**
	 * Places task on HOLD and records the date/time and description indicating that API action network call timed-out.
	 */
	void timedOut() {
		addTaskCommentNoteAndUpdateStatus(AssetCommentStatus.HOLD, messageSourceService.i18nMessage(Message.ApiActionTaskMessageTimedout))
	}

	/**
	 * Updates the task status to DONE and records the date/time and description of the activity in the Task comment section.
	 * It also adds a comment informing that this API Action has been by-passed.
	 */
	void byPassed() {
		addTaskCommentNoteAndUpdateStatus(AssetCommentStatus.COMPLETED, messageSourceService.i18nMessage(Message.ApiActionTaskMessageByPassed))
	}

	/**
	 * Add a task comment given by user or default error message when null.
	 * Also set task status
	 *
	 * @param status - new task status
	 * @param message - note message
	 */
	private void addTaskCommentNoteAndUpdateStatus(String status, String message) {
		if (message) {
			taskService.addNote(task, whom, message)
		} else {
			taskService.addNote(task,
					whom,
					messageSourceService.i18nMessage(Message.ApiActionTaskMessageDefaultError))
		}
		updateTaskStatus(status)
	}

	/**
	 * Update task with provided status and whom. If whom is null it gets an automatic person from database
	 * @param status
	 * @param whom
	 */
	private void updateTaskStatus(String status) {
		task = taskService.setTaskStatus(task, status, whom)
	}

}
