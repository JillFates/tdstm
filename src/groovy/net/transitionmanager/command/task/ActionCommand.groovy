package net.transitionmanager.command.task

import grails.validation.Validateable
import net.transitionmanager.command.CommandObject
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.web.multipart.MultipartFile

@Validateable
class ActionCommand implements CommandObject {

	/**
	 * A message to be added to the tasks notes.
	 */
	String              message

	/**
	 * The amount of progress 0 to 100
	 */
	Integer             progress

	/**
	 * The standard out from running the action.
	 */
	String              stdout

	/**
	 * The standard error from running the action.
	 */
	String              stderr

	/**
	 * JSON context data sent back from running the action.
	 */
	JSONObject          data

	/**
	 * Related data files to be sent back, when an action is run.
	 * For now we only support one file, in the future we will support a list.
	 */
	List<MultipartFile> datafile

	static constraints = {
		message nullable: true
		progress range: 0..100, nullable: true
		stdout nullable: true
		stderr nullable: true
		data nullable: true
		datafile nullable: true
	}
}
