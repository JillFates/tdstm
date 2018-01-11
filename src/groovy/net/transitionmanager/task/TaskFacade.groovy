package net.transitionmanager.task

import com.tds.asset.AssetComment
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdssrc.grails.GormUtil
import net.transitionmanager.domain.Person
import net.transitionmanager.i18n.Message
import net.transitionmanager.service.MessageSourceService
import net.transitionmanager.service.TaskService
import org.codehaus.groovy.grails.exceptions.InvalidPropertyException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.util.ClassUtils

class TaskFacade {
	TaskService taskService
	MessageSourceService messageSourceService
	private AssetComment task

	@Autowired
	TaskFacade(AssetComment task) {
		this.task = task
	}

	/**
	 * Get task property determines if the property is a java type to return its value, if the property
	 * is not a java type it returns its toString representation
	 * @param name - the property name
	 * @return
	 */
	Object getProperty(String name) {
		Object taskProperty = null
		try {
			taskProperty = GormUtil.getDomainProperty(task, name)
		} catch (InvalidPropertyException e) {
			// swallow this one
		}
		if (Objects.nonNull(taskProperty)) {
			Object value = task.getProperty(name)
			if (Objects.isNull(value) || value instanceof String || ClassUtils.isPrimitiveOrWrapper(value.class)) {
				return value
			} else {
				return value.toString()
			}
		} else {
			throw new MissingPropertyException("No such property: " + name)
		}
	}

	boolean isDone() {
		return task.isDone()
	}

	boolean isStarted() {
		return task.isStarted()
	}

	boolean isOnHold() {
		return task.isOnHold()
	}

	boolean isAutomatic() {
		return task.isAutomatic()
	}

	int durationInMinutes() {
		return task.durationInMinutes()
	}

	void start() {
		updateTaskStatus(AssetCommentStatus.STARTED)
	}

	void done() {
		updateTaskStatus(AssetCommentStatus.COMPLETED)
	}

	void error(String message) {
		addTaskCommentNoteAndUpdateStatus(AssetCommentStatus.HOLD, message)
	}

	void lapsed() {
		addTaskCommentNoteAndUpdateStatus(AssetCommentStatus.HOLD, messageSourceService.getI18NMessage(Message.ApiActionTaskMessageLapsed))
	}

	void stalled() {
		addTaskCommentNoteAndUpdateStatus(AssetCommentStatus.HOLD, messageSourceService.getI18NMessage(Message.ApiActionTaskMessageStalled))
	}

	void timedOut() {
		addTaskCommentNoteAndUpdateStatus(AssetCommentStatus.HOLD, messageSourceService.getI18NMessage(Message.ApiActionTaskMessageTimedout))
	}

	private void addTaskCommentNoteAndUpdateStatus(String status, String message) {
		Person whom = getWhom()
		taskService.addNote(task, whom, message)
		updateTaskStatus(status, whom)
	}

	private void updateTaskStatus(String status, Person whom = null) {
		whom = whom ?: getWhom()
		task = taskService.setTaskStatus(task, status, whom)
	}

	/**
	 * TODO: per ticket description this is temporary
	 * TM-8695 - WHOM section
	 */
	private Person getWhom() {
		return taskService.getAutomaticPerson()
	}
}
